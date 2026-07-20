locals {
  name_prefix = "${var.environment}-${var.project_name}"
}

module "vpc" {
  source      = "../../modules/vpc"
  name_prefix = local.name_prefix
}

module "ecr" {
  source      = "../../modules/ecr"
  name_prefix = local.name_prefix
  repo_name   = "hrms-app"
}

module "github_oidc" {
  source              = "../../modules/github-oidc"
  name_prefix         = local.name_prefix
  github_repo         = "Samm0707/Gitops-Project" # owner/repo — must match your actual GitHub repo exactly
  ecr_repository_arn  = module.ecr.repository_arn
}

module "eks" {
  source      = "../../modules/eks"
  name_prefix = local.name_prefix
  vpc_id      = module.vpc.vpc_id

  # Both public and private subnets — a common, simple pattern for a learning cluster.
  # (A stricter production setup would put the control plane ENIs and nodes only in
  # private subnets — worth revisiting once this is working end to end.)
  subnet_ids = concat(module.vpc.public_subnet_ids, module.vpc.private_subnet_ids)

  node_instance_types = ["t3.small"]
  node_desired_size   = 1
  node_min_size       = 1
  node_max_size       = 2
}

module "rds" {
  source                     = "../../modules/rds"
  name_prefix                = local.name_prefix
  vpc_id                     = module.vpc.vpc_id
  private_subnet_ids         = module.vpc.private_subnet_ids
  allowed_security_group_id  = module.eks.cluster_security_group_id
  db_name                    = "HRMS"
}