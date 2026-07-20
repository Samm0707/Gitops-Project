# This is the piece that enables IRSA (IAM Roles for Service Accounts) —
# it's what lets a pod (e.g. Karpenter, External Secrets, later) assume a
# scoped IAM role directly, instead of the node's broad role being shared
# by every pod on it. Referenced constantly from Phase 2 onward.
data "tls_certificate" "eks" {
  url = aws_eks_cluster.this.identity[0].oidc[0].issuer
}

resource "aws_iam_openid_connect_provider" "eks" {
  url             = aws_eks_cluster.this.identity[0].oidc[0].issuer
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.eks.certificates[0].sha1_fingerprint]
}
