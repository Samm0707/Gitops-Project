resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true # required — EKS nodes need DNS hostnames to join the cluster

  tags = {
    Name = "${var.name_prefix}-vpc"
  }
}
