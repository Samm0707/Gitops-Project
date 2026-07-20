# Public subnets: for the NAT gateway and any internet-facing load balancer.
resource "aws_subnet" "public" {
  for_each = { for idx, cidr in var.public_subnet_cidrs : var.azs[idx] => cidr }

  vpc_id                  = aws_vpc.this.id
  cidr_block               = each.value
  availability_zone        = each.key
  map_public_ip_on_launch  = true

  tags = {
    Name                     = "${var.name_prefix}-public-${each.key}"
    # This tag is how the AWS Load Balancer Controller (added later) knows
    # which subnets it's allowed to place public load balancers into.
    "kubernetes.io/role/elb" = "1"
  }
}

# Private subnets: this is where the EKS worker nodes actually live —
# no direct route in from the internet, only out (via NAT).
resource "aws_subnet" "private" {
  for_each = { for idx, cidr in var.private_subnet_cidrs : var.azs[idx] => cidr }

  vpc_id            = aws_vpc.this.id
  cidr_block        = each.value
  availability_zone = each.key

  tags = {
    Name                              = "${var.name_prefix}-private-${each.key}"
    "kubernetes.io/role/internal-elb" = "1"
  }
}
