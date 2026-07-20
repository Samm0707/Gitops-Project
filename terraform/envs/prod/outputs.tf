output "vpc_id" {
  value = module.vpc.vpc_id
}

output "cluster_name" {
  value = module.eks.cluster_name
}

output "cluster_endpoint" {
  value = module.eks.cluster_endpoint
}

output "ecr_repository_url" {
  value = module.ecr.repository_url
}

output "github_actions_role_arn" {
  description = "Copy this into your GitHub Actions workflow YAML — used to authenticate CI to AWS with no stored keys"
  value       = module.github_oidc.role_arn
}

output "rds_endpoint" {
  value = module.rds.db_endpoint
}

output "rds_secret_arn" {
  description = "AWS Secrets Manager ARN holding the DB credentials — used in Phase 3 to wire this into the app pod"
  value       = module.rds.secret_arn
}