terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.19.0"
    }
  }
  backend "s3" {
    bucket = "yagr-tf-state-log"
    key    = "demo/ab3/app-web"
    region = "us-east-1"
  }
  
}

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

data "aws_ssm_parameter" "reviews_url" {
  name = "/ab3-demo/ab3-app-review-ext-service"
}

data "aws_ssm_parameter" "details_main_url" {
  name = "/ab3-demo/ab3-app-details-main-ext-service"
}

data "aws_ssm_parameter" "vpc_id" {
  name = "/ab3-demo/vpc_id"
}

data "aws_ssm_parameter" "private_subnets" {
  name = "/ab3-demo/private_subnets"
}

locals {
    account_id = data.aws_caller_identity.current.account_id
    region = data.aws_region.current.name
}

resource "aws_security_group" "web_sg" {
  name        = "web_sg"
  description = "web_sg"
  vpc_id      = data.aws_ssm_parameter.vpc_id.value

  ingress {
    description      = "80"
    from_port        = 80
    to_port          = 80
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8
    to_port     = 0
    protocol    = "icmp"
    description = "Allow ping"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  tags = {
    Name = "allow_tls"
  }
}

resource "aws_api_gateway_rest_api" "rest_apigateway" {
  name        = "demo-rest-gateway"
  description = "rest api gateway"
  endpoint_configuration {
    types            = ["REGIONAL"]
    # vpc_endpoint_ids = [aws_vpc_endpoint.endpoint.id]
  }
}

resource "aws_api_gateway_resource" "resource" {
  rest_api_id = "${aws_api_gateway_rest_api.rest_apigateway.id}"
  parent_id   = "${aws_api_gateway_rest_api.rest_apigateway.root_resource_id}"
  path_part   = "reviews"
}

resource "aws_api_gateway_resource" "resource_details" {
  rest_api_id = "${aws_api_gateway_rest_api.rest_apigateway.id}"
  parent_id   = "${aws_api_gateway_rest_api.rest_apigateway.root_resource_id}"
  path_part   = "details"
}

resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = "${aws_api_gateway_rest_api.rest_apigateway.id}"
  parent_id   = "${aws_api_gateway_resource.resource.id}"
  path_part   = "{ProductID}"
}

resource "aws_api_gateway_resource" "proxy_details" {
  rest_api_id = "${aws_api_gateway_rest_api.rest_apigateway.id}"
  parent_id   = "${aws_api_gateway_resource.resource_details.id}"
  path_part   = "{ProductID}"
}

resource "aws_api_gateway_method" "proxy" {
  rest_api_id   = "${aws_api_gateway_rest_api.rest_apigateway.id}"
  resource_id   = "${aws_api_gateway_resource.proxy.id}"
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_method" "proxy_details" {
  rest_api_id   = "${aws_api_gateway_rest_api.rest_apigateway.id}"
  resource_id   = "${aws_api_gateway_resource.proxy_details.id}"
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "lambda" {
  rest_api_id = "${aws_api_gateway_rest_api.rest_apigateway.id}"
  resource_id = "${aws_api_gateway_method.proxy.resource_id}"
  http_method = "${aws_api_gateway_method.proxy.http_method}"

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = "${aws_lambda_function.reviews.invoke_arn}"
}

resource "aws_api_gateway_integration" "lambda_details" {
  rest_api_id = "${aws_api_gateway_rest_api.rest_apigateway.id}"
  resource_id = "${aws_api_gateway_method.proxy_details.resource_id}"
  http_method = "${aws_api_gateway_method.proxy_details.http_method}"

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = "${aws_lambda_function.details.invoke_arn}"
}

resource "aws_api_gateway_deployment" "api_deployment" {
  depends_on = [
    aws_api_gateway_integration.lambda,
    aws_api_gateway_integration.lambda_details,
  ]

  rest_api_id = "${aws_api_gateway_rest_api.rest_apigateway.id}"
}

resource "aws_api_gateway_stage" "demo_stage" {
  deployment_id = aws_api_gateway_deployment.api_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.rest_apigateway.id
  stage_name    = "demo"
  xray_tracing_enabled = true
}



resource "aws_lambda_function" "reviews" {
  filename      = "reviews.zip"
  function_name = "ab3-reviews"
  role          = aws_iam_role.iam_for_lambda.arn
  handler       = "index.handler"
  timeout       = 10
  source_code_hash = filebase64sha256("reviews.zip")
  runtime = "python3.9"
  vpc_config {
    subnet_ids = split(",", data.aws_ssm_parameter.private_subnets.value)
    security_group_ids = [aws_security_group.web_sg.id]
  }
  tracing_config {
    mode = "Active"
  }
  environment {
    variables = {
      REVIEWS_URL     = data.aws_ssm_parameter.reviews_url.value
    }
  }
}

resource "aws_lambda_function" "details" {
  filename      = "details.zip"
  function_name = "ab3-details"
  role          = aws_iam_role.iam_for_lambda.arn
  handler       = "index.handler"
  timeout       = 10
  source_code_hash = filebase64sha256("details.zip")
  runtime = "python3.9"
  vpc_config {
    subnet_ids = split(",", data.aws_ssm_parameter.private_subnets.value)
    security_group_ids = [aws_security_group.web_sg.id]
  }
  tracing_config {
    mode = "Active"
  }
  environment {
    variables = {
      DETAILS_MAIN_URL = data.aws_ssm_parameter.details_main_url.value
    }
  }
}


resource "aws_lambda_permission" "allow_rest_gateway_reviews" {
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.reviews.arn}"
  principal = "apigateway.amazonaws.com"
  source_arn = "${aws_api_gateway_rest_api.rest_apigateway.execution_arn}/*/*"
  #/reviews/{ProductID}
}

resource "aws_lambda_permission" "allow_rest_gateway_details" {
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.details.arn}"
  principal = "apigateway.amazonaws.com"
  source_arn = "${aws_api_gateway_rest_api.rest_apigateway.execution_arn}/*/*"
  #/details/{ProductID}
}


resource "aws_ssm_parameter" "apigw_url" {
  name = "/ab3-demo/ab3-app-web/api-id"
  type  = "String"
  value = aws_api_gateway_stage.demo_stage.invoke_url
}

resource "aws_iam_role" "iam_for_lambda" {
  name = "iam_for_lambda"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_policy" "lambda_policy" {
  name        = "lambda_policy"
  path        = "/"
  description = "demo"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "WriteLogStreamsAndGroups",
            "Effect": "Allow",
            "Action": [
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Resource": "*"
        },
        {
            "Sid": "CreateLogGroup",
            "Effect": "Allow",
            "Action": "logs:CreateLogGroup",
            "Resource": "*"
        },
        {
            "Sid": "VPC",
            "Effect": "Allow",
            "Action": ["ec2:CreateNetworkInterface", "ec2:DescribeNetworkInterfaces", "ec2:DeleteNetworkInterface"],
            "Resource": "*"
        },
        {
            "Sid": "XRAY",
            "Effect": "Allow",
            "Action": "xray:*",
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lambda_logs" {
  role       = aws_iam_role.iam_for_lambda.name
  policy_arn = aws_iam_policy.lambda_policy.arn
}