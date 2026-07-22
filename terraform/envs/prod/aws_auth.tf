# Reads the aws-auth ConfigMap EKS already manages, so we can ADD to it
# without wiping out the entry the managed node group already relies on.
data "kubernetes_config_map_v1" "aws_auth" {
  metadata {
    name      = "aws-auth"
    namespace = "kube-system"
  }
}

locals {
  existing_map_roles = yamldecode(data.kubernetes_config_map_v1.aws_auth.data["mapRoles"])

  # Without this entry, Karpenter's EC2 instances launch successfully but can
  # never register as real Kubernetes Nodes — this cluster's CONFIG_MAP auth
  # mode requires every node-capable IAM role to be listed here explicitly.
  karpenter_node_role_entry = {
    rolearn  = module.karpenter.node_role_arn
    username = "system:node:{{EC2PrivateDNSName}}"
    groups   = ["system:bootstrappers", "system:nodes"]
  }

  merged_map_roles = concat(local.existing_map_roles, [local.karpenter_node_role_entry])
}

resource "kubernetes_config_map_v1_data" "aws_auth" {
  metadata {
    name      = "aws-auth"
    namespace = "kube-system"
  }

  data = {
    mapRoles = yamlencode(local.merged_map_roles)
  }

  force = true
}