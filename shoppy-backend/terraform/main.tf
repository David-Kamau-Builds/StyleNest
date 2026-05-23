# ============================================================
# Shoppy Backend — Terraform Deployment on GCP
# ============================================================
# Resources created:
#   - Static external IP address
#   - Firewall rules (SSH port 22, API port 8080)
#   - Compute Engine VM (e2-small, Ubuntu 22.04)
#   - Startup script that installs Docker, clones code, runs app
# ============================================================

terraform {
  required_version = ">= 1.5"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

# ── Provider ──────────────────────────────────────────────
provider "google" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}

# ── Static External IP ────────────────────────────────────
resource "google_compute_address" "shoppy_ip" {
  name   = "shoppy-backend-ip"
  region = var.region
}

# ── Firewall: Allow SSH (port 22) ─────────────────────────
resource "google_compute_firewall" "allow_ssh" {
  name    = "shoppy-allow-ssh"
  network = "default"

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["shoppy-backend"]
}

# ── Firewall: Allow API traffic (port 8080) ───────────────
resource "google_compute_firewall" "allow_api" {
  name    = "shoppy-allow-api"
  network = "default"

  allow {
    protocol = "tcp"
    ports    = ["8080"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["shoppy-backend"]
}

# ── Compute Engine VM ─────────────────────────────────────
resource "google_compute_instance" "shoppy_vm" {
  name         = "shoppy-backend-vm"
  machine_type = var.machine_type
  zone         = var.zone
  tags         = ["shoppy-backend"]

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-2204-lts"
      size  = 20  # GB
      type  = "pd-standard"
    }
  }

  network_interface {
    network = "default"
    access_config {
      nat_ip = google_compute_address.shoppy_ip.address
    }
  }

  # Pass config as instance metadata (read in startup script)
  metadata = {
    DB_USER     = var.db_user
    DB_PASSWORD = var.db_password
    DB_NAME     = var.db_name
    REPO_URL    = var.repo_url

    # Startup script runs once when the VM first boots
    startup-script = file("${path.module}/scripts/startup.sh")
  }

  # Allow the VM to access GCP services (Cloud Logging etc.)
  service_account {
    scopes = ["cloud-platform"]
  }

  labels = {
    environment = "production"
    app         = "shoppy-backend"
  }
}
