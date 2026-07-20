output "role_arn" {
  description = "Put this ARN into your GitHub Actions workflow YAML"
  value       = aws_iam_role.github_actions_ci.arn
}
