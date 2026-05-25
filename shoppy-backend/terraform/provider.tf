# ============================================================
# Providers & Terraform version constraints
# ============================================================

terraform {
  required_version = ">= 1.5"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
    # Used to write BASE_URL into the Android local.properties file
    local = {
      source  = "hashicorp/local"
      version = "~> 2.0"
    }
  }
}

# ── Google Cloud ───────────────────────────────────────────
provider "google" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}
