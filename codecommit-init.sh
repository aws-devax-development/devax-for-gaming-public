#!/bin/bash

REPO_PREFIX="devax-day"
CURRENT_REGION=`aws configure get region`
REPO_WEB=`aws ssm get-parameter --name "/devax/repo/app-web" | jq .Parameter.Value -r`
REPO_RANKING=`aws ssm get-parameter --name "/devax/repo/app-ranking" | jq .Parameter.Value -r`
REPO_CI=`aws ssm get-parameter --name "/devax/repo/ci" | jq .Parameter.Value -r`
REPO_INFRA=`aws ssm get-parameter --name "/devax/repo/infra" | jq .Parameter.Value -r`
REPO_NAMES="$REPO_WEB $REPO_RANKING $REPO_CI $REPO_INFRA"

if [ ! -z "$CURRENT_REGION"]
then
  echo "use default region $CURRENT_REGION"
else
  echo "use default region ap-southeast-1"
  CURRENT_REGION="ap-southeast-1"
fi

for REPO_NAME in $REPO_NAMES
do
  FINAL_REPO_NAME="$REPO_PREFIX"-"$REPO_NAME"
  echo "clean up repo $FINAL_REPO_NAME"
  aws codecommit delete-repository --repository-name $FINAL_REPO_NAME --no-cli-pager
  echo "repo $FINAL_REPO_NAME deleted"
  echo "create new repo $FINAL_REPO_NAME"
  aws codecommit create-repository --repository-name $FINAL_REPO_NAME --no-cli-pager
  echo "repo $FINAL_REPO_NAME created"
done

pip install git-remote-codecommit

mkdir ".codecommit"
cd ".codecommit"
for REPO_NAME in $REPO_NAMES
do
  FINAL_REPO_NAME="$REPO_PREFIX"-"$REPO_NAME"
  git clone "codecommit::$CURRENT_REGION://$FINAL_REPO_NAME"
done

# CredentialProviderChain 
# aws.region system property
# AWS_REGION environment variable
# {user.home}/.aws/credentials and {user.home}/.aws/config files
# in EC2, check the EC2 metadata service

cp ./apps/