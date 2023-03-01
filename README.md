# Demo Work Through

## Purpose

## Initialization

### Setup Working Environments

Create Cloud9 and register CodeCommit repository names in SSM

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
