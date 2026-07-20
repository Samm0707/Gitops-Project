# This is the actual access control: only things attached to
# var.allowed_security_group_id (your EKS nodes) can reach port 3306.
# Not "anything in the VPC," not "anything with the right password" — a
# specific, named set of resources. This is the real-world pattern for
# database network isolation, not just a password check.
resource "aws_security_group" "rds" {
  name_prefix = "${var.name_prefix}-rds-"
  vpc_id      = var.vpc_id

  ingress {
    description     = "MySQL from EKS nodes only"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [var.allowed_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.name_prefix}-rds-sg"
  }
}
