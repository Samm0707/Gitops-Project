# GitHub repos created on/after 15 July 2026 emit an immutable "sub" claim:
#   repo:<owner>@<owner_id>/<repo>@<repo_id>:ref:refs/heads/<branch>
# We don't hardcode the numeric IDs (they're internal GitHub identifiers, not
# something you look up) — wildcards fill that part in.
locals {
  github_owner     = split("/", var.github_repo)[0]
  github_repo_name = split("/", var.github_repo)[1]
}

# The role your GitHub Actions workflow will assume.
#
# AWS REQUIRES any role trusting GitHub's OIDC provider to evaluate either
# "sub" or "job_workflow_ref", scoped to something specific — this isn't
# optional, AWS rejects the policy outright otherwise (confirmed by the
# MalformedPolicyDocument error). "repository" and "ref" are kept as an
# additional, explicit layer on top — defense in depth, per AWS's own
# recommended pattern (docs.github.com/en/actions/reference/security/oidc,
# "Configuring OpenID Connect in Amazon Web Services").
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
          "token.actions.githubusercontent.com:aud"        = "sts.amazonaws.com"
          "token.actions.githubusercontent.com:repository" = var.github_repo
        }
        StringLike = {
          "token.actions.githubusercontent.com:sub" = "repo:${local.github_owner}@*/${local.github_repo_name}@*:ref:refs/heads/main"
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