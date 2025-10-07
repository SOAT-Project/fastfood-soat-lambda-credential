resource "aws_s3_object" "lambda_auth_service_zip" {
  bucket = var.s3_bucket_name
  key    = "auth-service-${var.prefix}.zip"
  source = "${path.module}/auth-service-${var.prefix}.zip"
}

resource "aws_lambda_function" "auth" {
  function_name = "auth-service-${var.prefix}"
  handler       = "soat.project.lambdacredential.auth.Handler::handleRequest"
  runtime       = "java17"
  role          = var.lambda_role
  memory_size   = 512
  timeout       = 15

  s3_bucket        = var.s3_bucket_name
  s3_key           = aws_s3_object.lambda_auth_service_zip.key
  source_code_hash = aws_s3_object.lambda_auth_service_zip.etag

  environment {
    variables = {
      JWT_SECRET  = var.jwt_secret
      DB_HOST     = var.db_host
      DB_NAME     = var.db_name
      DB_USERNAME = var.db_username
      DB_PASSWORD = var.db_password
    }
  }
}
