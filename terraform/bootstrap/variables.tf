variable "aws_region" {
  description = "AWS region everything gets created in"
  type        = string
  default     = "ap-south-1"
}

variable "project_name" {
  description = "Short project identifier, used as part of every resource name and tag"
  type        = string
  default     = "xrms"
}

variable "environment" {
  description = "Environment name — this project is a single 'prod'-labeled environment, but this variable is what would let you reuse this same config for dev/staging later"
  type        = string
  default     = "prod"
}

variable "unique_suffix" {
  description = "A unique string to make the S3 bucket name globally unique (S3 bucket names are unique across ALL of AWS, not just your account). Use something like your name or a random string."
  type        = string
}
