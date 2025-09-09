####
# AWS PROVIDER
####

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

####
# AWS PROVIDER NO LOCALSTACK
####
provider "aws" {
  region                      = "sa-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  s3_use_path_style = true
  skip_credentials_validation = true
  skip_requesting_account_id  = true

  endpoints {
    apigateway = "http://localhost:4566"
    cloudwatch = "http://localhost:4566"
    dynamodb   = "http://localhost:4566"
    ec2        = "http://localhost:4566"
    iam        = "http://localhost:4566"
    lambda     = "http://localhost:4566"
    s3         = "http://localhost:4566"
    sns        = "http://localhost:4566"
    sqs        = "http://localhost:4566"
    sts        = "http://localhost:4566"
  }
}