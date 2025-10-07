####
# VARIABLES
####

variable "jwt_secret" {
  description = "JWT secret for signing tokens"
  type        = string
  sensitive   = true
}

variable "db_username" {
  description = "Master username for the database"
  type        = string
}

variable "db_password" {
  description = "Master password for the database"
  type        = string
  sensitive   = true
}

variable "db_host" {
  description = "Database host"
  type        = string
}

variable "db_name" {
  description = "Database name"
  type        = string
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