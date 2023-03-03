
# data "aws_region" "current_region" {}

# resource "aws_vpc_endpoint" "endpoint" {

#   private_dns_enabled = false
#   security_group_ids  = [aws_security_group.web_sg.id]
#   service_name        = "com.amazonaws.${data.aws_region.current_region.name}.execute-api"
#   subnet_ids          = split(",", data.aws_ssm_parameter.private_subnets.value)
#   vpc_endpoint_type   = "Interface"
#   vpc_id              = data.aws_ssm_parameter.vpc_id.value
# }