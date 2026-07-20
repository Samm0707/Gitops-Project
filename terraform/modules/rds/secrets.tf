# Terraform generates this — it is never typed by a human, never committed
# to Git, and never appears in application.properties. This is the actual
# implementation of the "secrets management" principle from earlier: the
# app reads this from Secrets Manager at runtime, not from a config file.
resource "random_password" "db" {
  length  = 20
  special = true
  # Exclude characters RDS disallows in MySQL master passwords: / " @ and space
  override_special = "!#$%&*()-_=+[]{}<>?"
}

resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "${var.name_prefix}-rds-credentials"
  description = "RDS MySQL credentials for the HRMS app — read by the app at runtime, never stored in Git"
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.master_username
    password = random_password.db.result
    host     = aws_db_instance.this.address
    port     = aws_db_instance.this.port
    dbname   = var.db_name
  })
}
