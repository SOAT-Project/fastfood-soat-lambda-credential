####
# VARIABLES
####

variable "jwt_secret" {
  description = "JWT secret for signing tokens"
  type        = string
}

variable "db_user" {
  description = "Master username for the database"
  type        = string
}

variable "db_password" {
  description = "Master password for the database"
  type        = string
  sensitive   = true
}

variable "db_proxy_endpoint" {
  description = "Database proxy endpoint"
  type        = string
}
