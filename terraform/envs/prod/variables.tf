variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "project_name" {
  type    = string
  default = "xrms"
}

variable "environment" {
  type    = string
  default = "prod"
}
variable "grafana_url" {
  type    = string
  default = "http://localhost:3000"
}

variable "grafana_auth" {
  type      = string
  sensitive = true
}