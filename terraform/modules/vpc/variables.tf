variable "name_prefix" {
  description = "Naming prefix, e.g. prod-xrms — every resource in this module is named off this"
  type        = string
}

variable "vpc_cidr" {
  description = "IP range for the whole VPC"
  type        = string
  default     = "10.0.0.0/16"
}

# Mumbai has 3 AZs (ap-south-1a/1b/1c) but 2 is enough for a learning cluster
# and halves your NAT/subnet count versus using all 3 — cheaper, still highly available.
variable "azs" {
  description = "Availability zones to spread subnets across"
  type        = list(string)
  default     = ["ap-south-1a", "ap-south-1b"]
}

variable "public_subnet_cidrs" {
  type    = list(string)
  default = ["10.0.0.0/24", "10.0.1.0/24"]
}

variable "private_subnet_cidrs" {
  type    = list(string)
  default = ["10.0.10.0/24", "10.0.11.0/24"]
}
