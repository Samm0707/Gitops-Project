variable "name_prefix" {
  type = string
}

variable "github_repo" {
  description = "GitHub repo in 'owner/repo' form — only THIS repo's workflows will be able to assume the CI role"
  type        = string
}

variable "ecr_repository_arn" {
  description = "The ECR repo this CI role is allowed to push images to — scoping to one ARN, not '*', keeps blast radius small"
  type        = string
}
