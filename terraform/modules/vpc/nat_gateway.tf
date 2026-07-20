# Cost decision: ONE NAT gateway shared across both AZs, not one per AZ.
# One per AZ is the "textbook" HA answer, but each NAT costs ~$32/month on its own —
# for a learning project that gets destroyed between sessions, one shared NAT
# is the right tradeoff. Worth knowing this is a tradeoff you're making deliberately,
# not an oversight — a real production account usually WOULD do one per AZ.
resource "aws_eip" "nat" {
  domain = "vpc"

  tags = {
    Name = "${var.name_prefix}-nat-eip"
  }
}

resource "aws_nat_gateway" "this" {
  allocation_id = aws_eip.nat.id
  subnet_id     = values(aws_subnet.public)[0].id

  tags = {
    Name = "${var.name_prefix}-nat"
  }

  depends_on = [aws_internet_gateway.this]
}
