terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  required_version = ">= 1.3.0"
}

provider "aws" {
  region = var.aws_region
}

# Reference existing EC2 instance by ID
data "aws_instance" "vently_ec2" {
  instance_id = var.ec2_instance_id
}

# Reference existing RDS instance
data "aws_db_instance" "vently_db" {
  db_instance_identifier = "vently-db"
}

# Security group for EC2 — allows HTTP, HTTPS, SSH
resource "aws_security_group" "vently_sg" {
  name        = "vently-ec2-sg"
  description = "Vently EC2 security group"
  vpc_id      = data.aws_instance.vently_ec2.vpc_id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_allowed_cidr]
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "vently-ec2-sg"
    Project = "vently"
  }
}

# Output useful values
output "ec2_public_ip" {
  value = data.aws_instance.vently_ec2.public_ip
}

output "rds_endpoint" {
  value = data.aws_db_instance.vently_db.endpoint
}
