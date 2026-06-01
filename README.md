# StyleNest

A modern, minimalist boutique clothing store Android application tailored for young urban adults (ages 18–35) in Nairobi. Built as a fully offline-capable, premium-feel shopping experience with a **Gold, Black & White** design system.

---

## ✨ Features

- 🏠 **Home Feed** — Horizontally scrollable category pills and a 2-column featured product grid
- 🗂️ **Categories** — Side-drawer category browser with dynamic product filtering
- 🛍️ **Product Detail** — Hero image, interactive size chips, and a persistent "Add to Cart" panel
- 🛒 **Cart** — Real-time quantity controls, subtotal and total calculations, and checkout CTA
- 📦 **Checkout** — Minimal single-page form with Nairobi estate dropdown, M-Pesa and Cash on Delivery payment options
- ✅ **Order Success** — Confirmation screen with a generated order number (e.g. `#ST-98742`)
- 📜 **Order History** — Full local order log with status badges (`Pending Delivery`, `Completed`)
- 🌙 **Dark Mode** — Full Material3 dark theme support out of the box

---

## 📱 Screenshots

> Add screenshots here after your first build.

---

## 🏗️ Architecture

```
StyleNest
├── Single-Activity + Multi-Fragment navigation
│   ├── HomeFragment
│   ├── CategoriesFragment
│   ├── CartFragment
│   └── ProfileFragment
└── Dedicated Activities
    ├── SplashActivity
    ├── ProductDetailActivity
    ├── CheckoutActivity
    ├── OrderSuccessActivity
    └── OrderHistoryActivity
```

**Data layer** — Offline-first with SQLite (`stylenest.db`) via `DatabaseHelper` and `StyleNestRepository`. The product catalogue is pre-seeded on first install, so the app is 100% functional with no network connection.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Android Views + View Binding |
| Design system | Material3 (`Theme.Stylenest`) |
| Local storage | SQLite via `SQLiteOpenHelper` |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |
| Build system | Gradle (Kotlin DSL) |

---

## 🎨 Design System

| Token | Value |
|---|---|
| Primary dark (Black) | `#121212` |
| Secondary dark (Matte Grey) | `#1A1A1A` |
| Accent (Gold) | `#D4AF37` |
| Gold light | `#E5C158` |
| Background (Off-white) | `#F8F9FA` |
| Corner radius | `12dp – 16dp` |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11+
- Android SDK 36

### Clone & Open

```bash
git clone https://github.com/David-Kamau-Builds/shoppy.git
cd shoppy
```

Open the project in Android Studio and let Gradle sync.

### Build & Run

```bash
./gradlew assembleDebug
```

Or press **Run ▶** in Android Studio to launch on a device or emulator (API 24+).

---

## 📦 Package Structure

```
com.app.stylenest
├── adapter/
│   ├── CartAdapter.kt
│   ├── CategoryAdapter.kt
│   ├── OrderAdapter.kt
│   └── ProductAdapter.kt
├── data/
│   ├── DatabaseHelper.kt       # SQLite setup & seeding
│   └── StyleNestRepository.kt  # Data access layer
├── model/
│   ├── CartItem.kt
│   ├── Order.kt
│   └── Product.kt
├── CartFragment.kt
├── CategoriesFragment.kt
├── CheckoutActivity.kt
├── HomeFragment.kt
├── MainActivity.kt
├── OrderHistoryActivity.kt
├── OrderSuccessActivity.kt
├── ProductDetailActivity.kt
├── ProfileFragment.kt
└── SplashActivity.kt
```

---

## 🗄️ Database Schema

```sql
products     (id, name, category, price, image_name, description, sizes, is_featured)
cart         (id, product_id, selected_size, quantity)
orders       (id, order_number, customer_name, phone, delivery_address,
              payment_method, total_amount, order_date, status)
order_items  (id, order_id, product_id, size, quantity, price)
```

---

## 🌍 Localisation

The checkout flow is localised for **Nairobi**, with a delivery estate dropdown covering:
Westlands · Kilimani · CBD · Ngong Road · Lang'ata · Kileleshwa · South B · South C

Payment methods: **M-Pesa** and **Cash on Delivery**.

---

## 📄 License

This project was developed for **Practical Assessment III (CP & ICT 2024)**.
