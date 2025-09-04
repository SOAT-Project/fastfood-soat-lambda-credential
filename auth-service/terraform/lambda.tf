resource "aws_lambda_function" "auth" {
  function_name = "auth-service"
  handler       = "com.example.auth.handler.AuthHandler"
  runtime       = "java17"
  role          = aws_iam_role.lambda_role.arn
  memory_size   = 512
  timeout       = 15

  filename         = "${path.module}/../build/distributions/auth-service.zip"
  source_code_hash = filebase64sha256("${path.module}/../build/distributions/auth-service.zip")

  environment {
    variables = {
      JWT_SECRET        = var.jwt_secret
      DB_PROXY_ENDPOINT = var.db_proxy_endpoint
      DB_USER           = var.db_user
      DB_PASSWORD       = var.db_password
    }
  }
}
