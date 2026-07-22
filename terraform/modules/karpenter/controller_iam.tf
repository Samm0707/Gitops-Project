# IRSA role for Karpenter's own controller pod — lets it call EC2 Fleet APIs,
# read pricing data, and pass the node role above to instances it launches.
# Trust is scoped to the specific Kubernetes ServiceAccount Karpenter's Helm
# chart creates (karpenter namespace, karpenter service account) — same
# pattern as the GitHub OIDC role, just trusting the cluster's own OIDC
# provider instead of GitHub's.
data "aws_iam_policy_document" "karpenter_controller_assume" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [var.oidc_provider_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "${replace(var.oidc_provider_url, "https://", "")}:sub"
      values   = ["system:serviceaccount:karpenter:karpenter"]
    }
    condition {
      test     = "StringEquals"
      variable = "${replace(var.oidc_provider_url, "https://", "")}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "karpenter_controller" {
  name               = "${var.name_prefix}-karpenter-controller"
  assume_role_policy = data.aws_iam_policy_document.karpenter_controller_assume.json
}

data "aws_iam_policy_document" "karpenter_controller_permissions" {
  statement {
    sid    = "AllowScopedEC2InstanceActions"
    effect = "Allow"
    actions = [
      "ec2:RunInstances",
      "ec2:CreateTags",
      "ec2:CreateFleet",
      "ec2:CreateLaunchTemplate",
      "ec2:TerminateInstances",
      "ec2:DeleteLaunchTemplate",
    ]
    resources = ["*"]
  }
  statement {
    sid    = "AllowScopedReads"
    effect = "Allow"
    actions = [
      "ec2:DescribeInstances",
      "ec2:DescribeImages",
      "ec2:DescribeInstanceTypes",
      "ec2:DescribeInstanceTypeOfferings",
      "ec2:DescribeAvailabilityZones",
      "ec2:DescribeLaunchTemplates",
      "ec2:DescribeSubnets",
      "ec2:DescribeSecurityGroups",
      "ec2:DescribeSpotPriceHistory",
      "pricing:GetProducts",
      "ssm:GetParameter",
    ]
    resources = ["*"]
  }
  statement {
    # Missing originally — Karpenter calls this to resolve the cluster's VPC/pod
    # CIDR ranges (needed to tell node IPs apart from pod IPs when scheduling).
    # Without it, EC2NodeClass gets stuck on "Failed to detect the cluster CIDR"
    # forever, exactly what you hit. Read-only, describes cluster metadata only.
    sid       = "AllowClusterCIDRDetection"
    effect    = "Allow"
    actions   = ["eks:DescribeCluster"]
    resources = ["*"]
  }
  statement {
    sid       = "AllowPassingNodeRole"
    effect    = "Allow"
    actions   = ["iam:PassRole"]
    resources = [aws_iam_role.karpenter_node.arn]
  }
  statement {
    sid       = "AllowInstanceProfileManagement"
    effect    = "Allow"
    actions   = ["iam:CreateInstanceProfile", "iam:TagInstanceProfile", "iam:AddRoleToInstanceProfile", "iam:RemoveRoleFromInstanceProfile", "iam:DeleteInstanceProfile", "iam:GetInstanceProfile"]
    resources = ["*"]
  }
  statement {
    sid       = "AllowInterruptionQueueActions"
    effect    = "Allow"
    actions   = ["sqs:DeleteMessage", "sqs:GetQueueUrl", "sqs:ReceiveMessage"]
    resources = [aws_sqs_queue.karpenter_interruption.arn]
  }
}

resource "aws_iam_role_policy" "karpenter_controller" {
  name   = "${var.name_prefix}-karpenter-controller-policy"
  role   = aws_iam_role.karpenter_controller.id
  policy = data.aws_iam_policy_document.karpenter_controller_permissions.json
}