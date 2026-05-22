package com.app.shoppy.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "stylenest.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createProductsTable = """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                price REAL NOT NULL,
                image_name TEXT NOT NULL,
                description TEXT NOT NULL,
                sizes TEXT NOT NULL,
                is_featured INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createCartTable = """
            CREATE TABLE cart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id INTEGER NOT NULL,
                selected_size TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """.trimIndent()

        val createOrdersTable = """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_number TEXT NOT NULL,
                customer_name TEXT NOT NULL,
                phone TEXT NOT NULL,
                delivery_address TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                total_amount REAL NOT NULL,
                order_date TEXT NOT NULL,
                status TEXT NOT NULL
            )
        """.trimIndent()

        val createOrderItemsTable = """
            CREATE TABLE order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                size TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                FOREIGN KEY(order_id) REFERENCES orders(id),
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """.trimIndent()

        db.execSQL(createProductsTable)
        db.execSQL(createCartTable)
        db.execSQL(createOrdersTable)
        db.execSQL(createOrderItemsTable)

        seedProducts(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS order_items")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS cart")
        db.execSQL("DROP TABLE IF EXISTS products")
        onCreate(db)
    }

    private fun seedProducts(db: SQLiteDatabase) {
        val insertQuery = "INSERT INTO products (name, category, price, image_name, description, sizes, is_featured) VALUES (?, ?, ?, ?, ?, ?, ?)"
        
        val products = listOf(
            arrayOf<Any>("Nairobi Nights Bomber Jacket", "Shirts", 4500.0, "ic_launcher_foreground", "Premium urban bomber jacket perfect for evening outings. Water-resistant with deep pockets.", "S,M,L,XL", 1),
            arrayOf<Any>("Classic White Linen Shirt", "Shirts", 2500.0, "ic_launcher_foreground", "Breathable linen shirt for hot days. Minimalist design, slim fit.", "S,M,L", 1),
            arrayOf<Any>("Urban Cargo Trousers", "Trousers", 3200.0, "ic_launcher_foreground", "Durable cargo pants with multiple utility pockets. Black finish.", "M,L,XL", 1),
            arrayOf<Any>("Gold-Trimmed Evening Dress", "Dresses", 6500.0, "ic_launcher_foreground", "Elegant black dress with subtle gold trim. Perfect for formal events.", "S,M", 1),
            arrayOf<Any>("Street Smart Sneakers", "Shoes", 4800.0, "ic_launcher_foreground", "Comfortable, high-grip sneakers with gold accents.", "40,41,42,43", 1),
            arrayOf<Any>("Minimalist Gold Watch", "Accessories", 3500.0, "ic_launcher_foreground", "Sleek analog watch with a black leather strap and gold dial.", "One Size", 0),
            arrayOf<Any>("Denim Jacket - Vintage Blue", "Shirts", 3800.0, "ic_launcher_foreground", "Classic denim jacket with a modern cut.", "M,L", 0)
        )

        for (product in products) {
            db.execSQL(insertQuery, product)
        }
    }
}
