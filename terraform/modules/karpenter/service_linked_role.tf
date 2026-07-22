# AWS requires this account-level role to exist before ANY principal can launch
# EC2 Spot Instances — it's not specific to Karpenter's IAM role, it's a
# one-time account prerequisite. Creating it here, once, via Terraform means
# Karpenter's controller never needs iam:CreateServiceLinkedRole as a standing
# permission — a narrower, more auditable fix than granting that runtime.
resource "aws_iam_service_linked_role" "spot" {
  aws_service_name = "spot.amazonaws.com"
}