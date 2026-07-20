terraform {
  backend "s3" {
    bucket         = "prod-xrms-terraform-state-shivam07"
    key            = "envs/prod/terraform.tfstate"   # a different key from bootstrap's own state — same bucket, separate "file" inside it
    region         = "ap-south-1"
    dynamodb_table = "prod-xrms-terraform-locks"
    encrypt        = true
  }
}
