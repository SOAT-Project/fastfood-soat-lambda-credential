####
# VARIABLES
####

variable "jwt_secret" {
  description = "JWT secret for signing tokens"
  type        = string
  sensitive   = true
}

variable "prefix" {
  type = string
}

variable "s3_bucket_name" {
  description = "S3 bucket name where Lambda zip files are stored"
  type        = string
}

variable "lambda_role" {
  description = "IAM role ARN for Lambda functions"
  type        = string
}