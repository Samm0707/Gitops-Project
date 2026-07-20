# Tells RDS which subnets it's allowed to place the database (and its
# standby, if Multi-AZ is ever enabled) into — private subnets only.
resource "aws_db_subnet_group" "this" {
  name       = "${var.name_prefix}-rds-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "${var.name_prefix}-rds-subnet-group"
  }
}
