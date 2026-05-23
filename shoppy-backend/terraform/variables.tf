# ============================================================
# Input Variables
# ============================================================

variable "project_id" {
  description = "Your GCP Project ID (found in GCP Console dashboard)"
  type        = string
}

variable "region" {
  description = "GCP region to deploy resources in"
  type        = string
  default     = "us-central1"
}

variable "zone" {
  description = "GCP zone to deploy the VM in"
  type        = string
  default     = "us-central1-a"
}

variable "machine_type" {
  description = "Compute Engine machine type. e2-small = 1 vCPU, 2GB RAM (~$13/month)"
  type        = string
  default     = "e2-small"
}

variable "repo_url" {
  description = "GitHub URL of your shoppy-backend repository (HTTPS)"
  type        = string
  # Example: "https://github.com/your-username/shoppy.git"
}

variable "db_user" {
  description = "PostgreSQL database username"
  type        = string
  default     = "shoppy_user"
}

variable "db_password" {
  description = "PostgreSQL database password"
  type        = string
  sensitive   = true
  default     = "shoppy_password"
}

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "shoppy_db"
}
