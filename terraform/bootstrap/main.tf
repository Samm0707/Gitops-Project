terraform {
  required_version = ">= 1.7.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
  }
  # No backend block here on purpose — this config manages its OWN state locally,
  # because it's the thing that creates the remote state backend for everything else.
}

provider "aws" {
  region = var.aws_region

  # default_tags applies these tags to EVERY resource this provider creates,
  # automatically — you never have to remember to tag a resource by hand.
  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
      Owner       = "devops"
    }
  }
}

# A naming convention, defined once, reused everywhere below.
# Real teams do this so every resource name is predictable at a glance:
# prod-xrms-<what-it-is>-<unique-bit>
locals {
  name_prefix = "${var.environment}-${var.project_name}"
}

# The bucket that will hold every other Terraform config's state file.
resource "aws_s3_bucket" "terraform_state" {
  bucket = "${local.name_prefix}-terraform-state-${var.unique_suffix}"

  lifecycle {
    prevent_destroy = true # accidental `terraform destroy` here would be very bad
  }
}

# Versioning means if state ever gets corrupted, you can roll back to a previous version.
resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

# State can contain secrets, so it's encrypted at rest.
resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Nobody should be able to make this bucket public, ever.
resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket                  = aws_s3_bucket.terraform_state.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# The lock table: prevents two `terraform apply` runs from writing state at the same time.
resource "aws_dynamodb_table" "terraform_locks" {
  name         = "${local.name_prefix}-terraform-locks"
  billing_mode = "PAY_PER_REQUEST" # you only pay per request, not per hour — cheap for solo use
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }
}
