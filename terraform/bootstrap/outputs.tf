# ============================================================================
# terraform/bootstrap/outputs.tf
#
# PURPOSE: After `terraform apply`, these values print to your terminal.
# You'll copy the exact bucket name and table name into the `backend.tf`
# of every future Terraform config (envs/dev, modules, etc.) so they know
# where to store THEIR state.
# ============================================================================

output "state_bucket_name" {
  description = "Copy this exact value into every future backend.tf"
  value       = aws_s3_bucket.terraform_state.bucket
}

output "lock_table_name" {
  description = "Copy this exact value into every future backend.tf"
  value       = aws_dynamodb_table.terraform_locks.name
}
