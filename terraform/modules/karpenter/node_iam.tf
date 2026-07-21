# The role EC2 instances THAT KARPENTER LAUNCHES will assume — separate from
# your existing managed node group's role, because Karpenter-provisioned nodes
# are a distinct fleet, provisioned outside the EKS managed-node-group API.
resource "aws_iam_role" "karpenter_node" {
  name = "${var.name_prefix}-karpenter-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "karpenter_node_worker" {
  role       = aws_iam_role.karpenter_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}
resource "aws_iam_role_policy_attachment" "karpenter_node_cni" {
  role       = aws_iam_role.karpenter_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}
resource "aws_iam_role_policy_attachment" "karpenter_node_ecr" {
  role       = aws_iam_role.karpenter_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}
resource "aws_iam_role_policy_attachment" "karpenter_node_ssm" {
  role       = aws_iam_role.karpenter_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Karpenter launches raw EC2 instances, not through the EKS node-group API —
# EC2 instances need an instance PROFILE (a wrapper around the role) to use it.
resource "aws_iam_instance_profile" "karpenter_node" {
  name = "${var.name_prefix}-karpenter-node-profile"
  role = aws_iam_role.karpenter_node.name
}
