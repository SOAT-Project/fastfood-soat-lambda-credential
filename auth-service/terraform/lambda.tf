resource "aws_s3_bucket" "lambda-auth-service-bucket" {
  bucket = "lambda-auth-service-bucket"
}

resource "aws_s3_object" "lambda_auth_service_zip" {
  bucket = aws_s3_bucket.lambda-auth-service-bucket.id
  key    = "auth-service.zip"
  source = "${path.module}/../../auth-service.zip"
}

resource "aws_lambda_function" "auth" {
  function_name = "auth-service"
  handler       = "soat.project.lambdacredential.auth.Handler"
  runtime       = "java17"
  role          = aws_iam_role.lambda_role.arn
  memory_size   = 512
  timeout       = 15

  s3_bucket = aws_s3_bucket.lambda-auth-service-bucket.id
  s3_key    = aws_s3_object.lambda_auth_service_zip.key
  source_code_hash = aws_s3_object.lambda_auth_service_zip.etag

  environment {
    variables = {
      JWT_SECRET        = var.jwt_secret
      DB_PROXY_ENDPOINT = var.db_proxy_endpoint
      DB_USER           = var.db_user
      DB_PASSWORD       = var.db_password
    }
  }
}
