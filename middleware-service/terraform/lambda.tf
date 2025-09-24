resource "aws_s3_bucket" "lambda-middleware-service-bucket" {
  bucket = "lambda-middleware-service-bucket"
}

resource "aws_s3_object" "lambda_middleware-service_zip" {
  bucket = aws_s3_bucket.lambda-middleware-service-bucket.id
  key    = "auth-service.zip"
  source = "${path.module}/../../middleware-service.zip"
}

resource "aws_lambda_function" "middleware" {
  function_name = "middleware-service"
  handler       = "soat.project.lambdacredential.middleware.Handler"
  runtime       = "java17"
  role          = aws_iam_role.lambda_role.arn
  memory_size   = 512
  timeout       = 15

  s3_bucket        = aws_s3_bucket.lambda-middleware-service-bucket.id
  s3_key           = aws_s3_object.lambda_middleware-service_zip.key
  source_code_hash = aws_s3_object.lambda_middleware-service_zip.etag

  environment {
    variables = {
      JWT_SECRET = var.jwt_secret
    }
  }
}
