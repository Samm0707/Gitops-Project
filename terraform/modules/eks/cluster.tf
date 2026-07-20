resource "aws_eks_cluster" "this" {
  name     = "${var.name_prefix}-eks"
  role_arn = aws_iam_role.cluster.arn
  version  = var.cluster_version

  vpc_config {
    subnet_ids              = var.subnet_ids
    endpoint_public_access  = true # so you can `kubectl` from your laptop
    endpoint_private_access = true # so nodes can reach the API server without leaving the VPC
  }

  depends_on = [aws_iam_role_policy_attachment.cluster_policy]

  tags = {
    Name = "${var.name_prefix}-eks"
  }
}
