####
# AWS PROVIDER
####

terraform {
  required_version = ">= 1.6.0"

  backend "s3" {
    bucket = "meu-terraform-state"
    key    = "auth-service/terraform.tfstate"
    region = "sa-east-1"
  }
}

provider "aws" {
  region = "sa-east-1"
}
