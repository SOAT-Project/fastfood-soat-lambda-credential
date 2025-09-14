output "lambda_arn" {
  description = "ARN da função Lambda"
  value       = aws_lambda_function.auth.arn
}

output "lambda_name" {
  description = "Nome da função Lambda"
  value       = aws_lambda_function.auth.function_name
}