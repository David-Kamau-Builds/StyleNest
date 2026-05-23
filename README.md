# 🛍️ Shoppy — Premium Full-Stack E-Commerce Platform

Shoppy is a state-of-the-art, production-grade monorepo containing a modern Android mobile application and a robust Spring Boot backend. It is designed with offline-first local database caching, reactive state management, server-side data seeding, and fully automated GCP infrastructure deployment using Terraform.

---

## 🏗️ Architecture Overview

Shoppy uses a monorepo structure to keep the mobile and backend projects unified side-by-side:

```
shoppy/ (Root)
├── gradle/
├── shoppy-mobile/        ← Premium Android App (MVVM, Room, Retrofit, Hilt)
├── shoppy-backend/       ← Spring Boot REST API & PostgreSQL Database
│   ├── terraform/        ← Infrastructure as Code (GCP VM, Static IP, Firewall)
│   └── src/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🌟 Premium Features

### 📱 Android Application (`shoppy-mobile`)
* **Offline-First Architecture:** Local cache driven by **Room Database** and reactive **StateFlows** for seamless offline usability.
* **Modern MVVM & DI:** High-performing, decoupled layers using Hilt dependency injection and reactive ViewModel-to-UI bindings.
* **Lazy Loading & Infinite Scroll:** Smooth SQLite-based pagination using `LIMIT` and `OFFSET` queries dynamically fetched via a RecyclerView scroll listener.
* **Secure 2FA Biometric Login:** Seamless integration of standard device biometrics (fingerprint/face) for advanced multi-factor security.
* **Advanced Search & Filtering:** Live search and bottom-sheet filters covering category chips and price range sliders.
* **Auto-Adaptive Environments:** Zero manual config! Built-in `BuildConfig` automatically points to **localhost** during `debug` builds and your **live cloud server** during `release` builds.

### ☕ Spring Boot Backend (`shoppy-backend`)
* **Rest API Services:** Structured endpoints covering Authentication, Products, Orders, Carts, Wishlists, and Reviews.
* **Loyalty Points System:** Auto-calculates and awards customer loyalty rewards dynamically at checkout (e.g., 1 point per KSh 100 spent).
* **Automatic Database Seeding:** Populates the PostgreSQL database automatically with 50+ beautiful products and categories on startup.
* **Postman Integration:** Includes a pre-configured [Postman Collection](file:///C:/Users/david/AndroidStudioProjects/shoppy/shoppy-backend/Shoppy_API.postman_collection.json) with test scripts that dynamically capture sessions and variables.

### ☁️ Infrastructure & Cloud (`shoppy-backend/terraform`)
* **Infrastructure as Code:** Fully automated provisioning of a GCP Compute Engine instance (`e2-small` VM), static external IP, and network firewalls.
* **Zero-Touch Provisioning:** VM startup script automatically installs Java 21, Maven, and Docker, launches PostgreSQL, clones the private branch securely with Git tokens, builds the app, and registers it as a self-healing `systemd` service.

---

## 🚀 Getting Started

### 1. Mobile Development (`shoppy-mobile`)

To open and run the mobile application in **Android Studio**:
1. Open Android Studio, select **Open**, and select the **root `shoppy/` folder** (do not open `shoppy-mobile` directly).
2. Android Studio will automatically sync Gradle and resolve dependencies.
3. Select your active Build Variant:
   * **`debug`:** Connects automatically to your local emulator (`http://10.0.2.2:8080`).
   * **`release`:** Connects automatically to your live GCP cloud server (`http://34.121.48.4:8080`).
4. Press **Run** to launch the app on your emulator or physical device.

To compile a debug or release APK from the terminal:
```powershell
# Navigate to the root directory
cd C:\Users\david\AndroidStudioProjects\shoppy

# Clean and compile debug APK
.\gradlew clean assembleDebug

# Clean and compile release APK
.\gradlew clean assembleRelease
```
The compiled APK will be generated at `shoppy-mobile/build/outputs/apk/`.

---

### 2. Backend Local Development (`shoppy-backend`)

The backend runs locally on port `8080` backed by a local PostgreSQL database running in Docker:

1. Ensure **Docker Desktop** is running.
2. Spin up the local database:
   ```powershell
   cd C:\Users\david\AndroidStudioProjects\shoppy\shoppy-backend
   docker compose up -d
   ```
3. Run the Spring Boot application:
   ```powershell
   mvn spring-boot:run
   ```

---

### 3. Cloud Deployment (`shoppy-backend/terraform`)

To deploy the entire production backend and database automatically to Google Cloud Platform (GCP) using Terraform:

1. Navigate to the `terraform/` folder:
   ```powershell
   cd C:\Users\david\AndroidStudioProjects\shoppy\shoppy-backend\terraform
   ```
2. Open `terraform.tfvars` and fill in your GCP Project ID and GitHub classic PAT:
   ```hcl
   project_id   = "your-gcp-project-id"
   github_token = "ghp_yourGitHubClassicPersonalAccessToken"
   ```
3. Initialize, plan, and deploy!
   ```powershell
   terraform init
   terraform plan -out=shoppyplan
   terraform apply "shoppyplan"
   ```
4. Once completed, Terraform will print out your live `backend_ip`, `backend_url`, and the `ssh_command` to watch the server logs in real-time.

---

## 📈 Technology Stack

* **Mobile:** Kotlin, Android SDK, Dagger Hilt, Retrofit, Room Database, Moshi/Gson, Coroutines, ViewBinding, Biometrics API.
* **Backend:** Java 21, Spring Boot, Spring Data JPA, Spring Web, PostgreSQL, Hibernate, Maven.
* **Infrastructure:** Terraform, Docker, Google Cloud Platform (Compute Engine, VPC, Cloud Firewalls, IAM).
