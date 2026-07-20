resource "aws_db_instance" "this" {
  identifier     = "${var.name_prefix}-mysql"
  engine         = "mysql"
  engine_version = "8.0" # AWS resolves this to the latest supported 8.0.x patch automatically

  instance_class    = var.instance_class
  allocated_storage = var.allocated_storage
  storage_type      = "gp3"

  db_name  = var.db_name
  username = var.master_username
  password = random_password.db.result

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az            = false # single-AZ — Multi-AZ roughly doubles cost, not needed for a learning project
  publicly_accessible = false # never reachable from outside the VPC, matches the whole point of this security group

  backup_retention_period = 1
  skip_final_snapshot     = true # so `terraform destroy` between sessions doesn't leave an orphaned snapshot costing money
  deletion_protection     = false # intentionally off — this project is meant to be destroyed and rebuilt repeatedly

  tags = {
    Name = "${var.name_prefix}-mysql"
  }
}
