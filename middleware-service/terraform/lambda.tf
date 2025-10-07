resource "aws_s3_object" "lambda_middleware-service_zip" {
  bucket = var.s3_bucket_name
  key    = "middleware-service-${var.prefix}.zip"
  source = "${path.module}/middleware-service-${var.prefix}.zip"
}

resource "aws_lambda_function" "middleware" {
  function_name = "middleware-service-${var.prefix}"
  handler       = "soat.project.lambdacredential.middleware.Handler::handleRequest"
  runtime       = "java17"
  role          = var.lambda_role
  memory_size   = 512
  timeout       = 15

  s3_bucket        = var.s3_bucket_name
  s3_key           = aws_s3_object.lambda_middleware-service_zip.key
  source_code_hash = aws_s3_object.lambda_middleware-service_zip.etag

  environment {
    variables = {
      JWT_SECRET = var.jwt_secret
    }
  }
}
