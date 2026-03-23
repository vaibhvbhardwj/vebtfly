variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "ec2_instance_id" {
  description = "Existing EC2 instance ID"
  type        = string
  # Set in terraform.tfvars
}

variable "ssh_allowed_cidr" {
  description = "CIDR block allowed to SSH (your IP). Use 0.0.0.0/0 to allow all (not recommended)."
  type        = string
  default     = "0.0.0.0/0"
}
