# Tells AWS that tokens signed by GitHub's OIDC issuer can be trusted at all.
# This is an account-wide, one-time trust anchor — you'll reuse it for every
# repo that ever needs to assume a role via GitHub Actions, not just this one.
data "tls_certificate" "github_actions" {
  url = "https://token.actions.githubusercontent.com"
}

resource "aws_iam_openid_connect_provider" "github_actions" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.github_actions.certificates[0].sha1_fingerprint]
}
