# ============================================================
# Outputs — printed after `terraform apply` completes
# ============================================================

output "backend_ip" {
  description = "Static external IP of the Shoppy backend VM"
  value       = google_compute_address.shoppy_ip.address
}

output "backend_url" {
  description = "Full base URL to use in the Android NetworkModule"
  value       = "http://${google_compute_address.shoppy_ip.address}:8080/api/v1/"
}

output "ssh_command" {
  description = "Command to SSH into the VM from your terminal"
  value       = "gcloud compute ssh shoppy-backend-vm --zone=${var.zone}"
}

output "vm_name" {
  description = "Name of the Compute Engine instance"
  value       = google_compute_instance.shoppy_vm.name
}

output "local_properties_updated" {
  description = "Confirms that BASE_URL was written to Android local.properties"
  value       = "local.properties updated → BASE_URL=http://${google_compute_address.shoppy_ip.address}:8080/api/v1/"
}
