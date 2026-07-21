variable "name_prefix" {
  type = string
}

variable "cluster_name" {
  type = string
}

variable "oidc_provider_arn" {
  description = "The cluster's OIDC provider — enables IRSA for Karpenter's own controller pod"
  type        = string
}

variable "oidc_provider_url" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}
variable "cluster_security_group_id" {
  type = string
}