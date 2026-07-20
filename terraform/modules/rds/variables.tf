variable "name_prefix" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  description = "RDS lives in private subnets only — never internet-reachable"
  type        = list(string)
}

variable "allowed_security_group_id" {
  description = "Only traffic from this security group (your EKS nodes) can reach the database on port 3306"
  type        = string
}

variable "db_name" {
  type    = string
  default = "HRMS"
}

variable "master_username" {
  description = "'root' and 'admin' are reserved/disallowed as RDS master usernames for MySQL — must be something else"
  type        = string
  default     = "hrmsadmin"
}

variable "instance_class" {
  description = "db.t4g.micro is the cheapest current-generation RDS instance class — ARM-based, cheaper than t3.micro for the same specs"
  type        = string
  default     = "db.t4g.micro"
}

variable "allocated_storage" {
  description = "Storage in GB — 20 is the minimum for MySQL on RDS and plenty for a learning project"
  type        = number
  default     = 20
}
