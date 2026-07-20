# The role your GitHub Actions workflow will assume. The "Condition" block below
# is the actual security boundary: it restricts WHICH GitHub Actions runs are
# allowed to use this role — only runs on the main branch of your exact repo.
# A workflow on a fork, or a PR branch, or ANY other repo cannot assume this role.
resource "aws_iam_role" "github_actions_ci" {
  name = "${var.name_prefix}-github-actions-ci"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Federated = aws_iam_openid_connect_provider.github_actions.arn }
      Action    = "sts:AssumeRoleWithWebIdentity"
      Condition = {
        StringEquals = {
          "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
        }
        StringLike = {
          "token.actions.githubusercontent.com:sub" = "repo:${var.github_repo}:ref:refs/heads/main"
        }
      }
    }]
  })
}

# Least privilege: this role can push/pull images to exactly ONE ECR repo — nothing else.
data "aws_iam_policy_document" "ecr_push" {
  statement {
    sid       = "ECRAuth"
    effect    = "Allow"
    actions   = ["ecr:GetAuthorizationToken"] # this specific action doesn't support resource-level scoping in AWS
    resources = ["*"]
  }

  statement {
    sid    = "ECRPushToOneRepo"
    effect = "Allow"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:PutImage",
      "ecr:InitiateLayerUpload",
      "ecr:UploadLayerPart",
      "ecr:CompleteLayerUpload",
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer",
    ]
    resources = [var.ecr_repository_arn]
  }
}

resource "aws_iam_role_policy" "github_actions_ecr_push" {
  name   = "${var.name_prefix}-ecr-push"
  role   = aws_iam_role.github_actions_ci.id
  policy = data.aws_iam_policy_document.ecr_push.json
}
