output "db_endpoint" {
  description = "host:port to connect to"
  value       = aws_db_instance.this.endpoint
}

output "db_address" {
  description = "host only, no port"
  value       = aws_db_instance.this.address
}

output "secret_arn" {
  description = "Where the actual credentials live — reference this from the app, never hardcode the password itself"
  value       = aws_secretsmanager_secret.db_credentials.arn
}
