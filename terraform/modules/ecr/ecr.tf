resource "aws_ecr_repository" "this" {
  name                 = "${var.name_prefix}-${var.repo_name}"
  image_tag_mutability = "IMMUTABLE" # once a tag like git-sha-abc123 is pushed, it can never be overwritten —
                                      # this is what makes "deployed image == exact commit" a guarantee, not a convention

  image_scanning_configuration {
    scan_on_push = true # ECR's own basic scan, in addition to the Trivy scan your CI pipeline will run
  }

  tags = {
    Name = "${var.name_prefix}-${var.repo_name}"
  }
}

# Without this, old untagged image layers (produced by every rebuild) pile up forever
# and quietly cost storage money. This expires them automatically after 7 days.
resource "aws_ecr_lifecycle_policy" "this" {
  repository = aws_ecr_repository.this.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Expire untagged images after 7 days"
      selection = {
        tagStatus   = "untagged"
        countType   = "sinceImagePushed"
        countUnit   = "days"
        countNumber = 7
      }
      action = { type = "expire" }
    }]
  })
}
