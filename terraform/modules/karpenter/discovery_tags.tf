# Karpenter auto-discovers which subnets/security groups it's allowed to use
# via these exact tags — not by ARN references in a NodePool. This is how
# Karpenter itself works, not a convention we invented.
resource "aws_ec2_tag" "subnet_discovery" {
  for_each    = toset(var.private_subnet_ids)
  resource_id = each.value
  key         = "karpenter.sh/discovery"
  value       = var.cluster_name
}
