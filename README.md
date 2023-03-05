# Demo Work Through

## Purpose

## Initialization

### Setup Working Environments

Create Cloud9 and register CodeCommit repository names in SSM - from CloudShell (e.g. us-east-1 region)

```shell
sh ./workshop-init.sh

```

### Prepare Automations

In Cloud9 environment, pulling data from github

```shell
git clone https://github.com/aws-devax-development/devax-for-gaming-public.git
```

Run the following command

```shell
sh codecommit-init.sh
```

It will create 4 repositories and push the existing code into the repositories.

Now you can work from CodeCommit

cdk deploy -f --require-approval never --all

ACCOUNT_ID=`aws sts get-caller-identity | jq .Account -r`
s3://$ACCOUNT_ID-tf-state

alias tf=terraform

https://docs.aws.amazon.com/cloud9/latest/user-guide/security-iam.html#auth-and-access-control-temporary-managed-credentials-supported

need to copy this to cloud9
export AWS_ACCESS_KEY_ID=***
export AWS_SECRET_ACCESS_KEY=***
export AWS_SESSION_TOKEN=***
export AWS_DEFAULT_REGION=ap-southeast-1

