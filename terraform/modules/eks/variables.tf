variable "name_prefix" {
  type = string
}

variable "cluster_version" {
  description = "Kubernetes version for the EKS control plane"
  type        = string
  default     = "1.29"
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  description = "Subnets the EKS control plane and nodes will use — needs at least 2 different AZs"
  type        = list(string)
}

variable "node_instance_types" {
  description = "EC2 instance type(s) for worker nodes — t3.small is cost-effective for a learning cluster"
  type        = list(string)
  default     = ["t3.small"]
}

variable "node_desired_size" {
  type    = number
  default = 1
}

variable "node_min_size" {
  type    = number
  default = 1
}

variable "node_max_size" {
  type    = number
  default = 2
}
