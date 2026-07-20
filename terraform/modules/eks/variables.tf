variable "name_prefix" {
  type = string
}

variable "cluster_version" {
  description = "Kubernetes version for the EKS control plane. IMPORTANT: EKS charges $0.60/hr instead of $0.10/hr for any version outside its 14-month 'standard support' window — check https://docs.aws.amazon.com/eks/latest/userguide/kubernetes-versions-standard.html before changing this, and keep it current."
  type        = string
  default     = "1.34" # in standard support as of mid-2026 — do not use 1.29, 1.30, 1.31, 1.32, or 1.33, all past or nearly past standard support
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
