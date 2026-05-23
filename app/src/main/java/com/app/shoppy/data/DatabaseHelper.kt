package com.app.shoppy.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Random

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "stylenest.db"
        // Upgraded to Version 6 for scoping cart/wishlist/orders to user
        private const val DATABASE_VERSION = 6
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT,
                avatar_url TEXT,
                two_factor_enabled INTEGER DEFAULT 0
            )
        """
        db.execSQL(createUsersTable)

        val createProductsTable = """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                description TEXT,
                rich_description TEXT,
                category TEXT,
                sub_category TEXT,
                sizes TEXT,
                price REAL,
                image_url TEXT,
                images TEXT
            )
        """
        db.execSQL(createProductsTable)

        val createCartTable = """
            CREATE TABLE cart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_email TEXT,
                product_id INTEGER,
                selected_size TEXT,
                quantity INTEGER,
                price REAL,
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """
        db.execSQL(createCartTable)
        
        val createWishlistTable = """
            CREATE TABLE wishlist (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_email TEXT,
                product_id INTEGER,
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """
        db.execSQL(createWishlistTable)
        
        val createOrdersTable = """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_email TEXT,
                order_number TEXT,
                customer_name TEXT,
                phone TEXT,
                delivery_address TEXT,
                payment_method TEXT,
                total_amount REAL,
                order_date TEXT,
                status TEXT
            )
        """
        db.execSQL(createOrdersTable)

        val createOrderItemsTable = """
            CREATE TABLE order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER,
                product_id INTEGER,
                size TEXT,
                quantity INTEGER,
                price REAL,
                FOREIGN KEY(order_id) REFERENCES orders(id),
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """
        db.execSQL(createOrderItemsTable)

        // Run the massive seeder
        seed100Products(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop and recreate to apply massive schema upgrades easily
        db.execSQL("DROP TABLE IF EXISTS order_items")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS wishlist")
        db.execSQL("DROP TABLE IF EXISTS cart")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    private fun seed100Products(db: SQLiteDatabase) {
        val items = mutableListOf<ContentValues>()
        
        val categoryStructure = mapOf(
            "Shirts" to listOf("Polos", "Tees", "Button-Downs", "Long-Sleeve"),
            "Trousers" to listOf("Jeans", "Chinos", "Sweatpants", "Cargo"),
            "Dresses" to listOf("Maxi", "Midi", "Summer", "Evening"),
            "Shoes" to listOf("Sneakers", "Boots", "Loafers", "Heels"),
            "Accessories" to listOf("Belts", "Socks", "Perfumes", "Watches")
        )
        
        val random = Random(42) // Fixed seed so URLs don't rotate on every reboot
        var idCounter = 1
        
        for ((mainCat, subCats) in categoryStructure) {
            for (subCat in subCats) {
                // Generate 5 items per sub-category = exactly 100 items!
                for (i in 1..5) {
                    val price = (10..150).random(random) * 100.0 // Generates nice prices like 4500.0
                    
                    // We use picsum.photos with a seed to dynamically generate distinct fashion images that are cacheable
                    val img1 = "https://picsum.photos/seed/${idCounter}1/500/700"
                    val img2 = "https://picsum.photos/seed/${idCounter}2/500/700"
                    val img3 = "https://picsum.photos/seed/${idCounter}3/500/700"
                    val imagesCsv = "${img1},${img2},${img3}"
                    
                    val sizes = when (mainCat) {
                        "Shoes" -> "39,40,41,42,43,44"
                        "Accessories" -> "One Size"
                        else -> "S,M,L,XL,XXL"
                    }
                    
                    val descShort = "A sleek and stylish addition to your wardrobe."
                    val descLong = "Premium quality $subCat made for the modern lifestyle. Features top-tier fabric and an excellent fit. Designed with exceptional attention to detail to make you stand out. Hand-stitched finishes ensure durability and luxury."

                    val values = ContentValues().apply {
                        put("name", "Premium $subCat - Edition $i")
                        put("description", descShort)
                        put("rich_description", descLong)
                        put("category", mainCat)
                        put("sub_category", subCat)
                        put("sizes", sizes)
                        put("price", price)
                        put("image_url", img1)
                        put("images", imagesCsv)
                    }
                    items.add(values)
                    idCounter++
                }
            }
        }
        
        for (item in items) {
            db.insert("products", null, item)
        }
    }
    
    // Extension function to simulate a kotlin random range easily
    private fun IntRange.random(random: Random): Int {
        return random.nextInt(endInclusive - start + 1) + start
    }
}
