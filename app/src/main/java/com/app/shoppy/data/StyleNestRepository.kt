package com.app.shoppy.data

import android.content.ContentValues
import android.content.Context
import com.app.shoppy.model.CartItem
import com.app.shoppy.model.Product

class StyleNestRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun getAllProducts(): List<Product> {
        val productList = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM products", null)
        
        if (cursor.moveToFirst()) {
            do {
                val product = Product(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    imageName = cursor.getString(cursor.getColumnIndexOrThrow("image_name")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    sizes = cursor.getString(cursor.getColumnIndexOrThrow("sizes")),
                    isFeatured = cursor.getInt(cursor.getColumnIndexOrThrow("is_featured")) == 1
                )
                productList.add(product)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return productList
    }

    fun getFeaturedProducts(): List<Product> {
        return getAllProducts().filter { it.isFeatured }
    }

    fun getProductsByCategory(category: String): List<Product> {
        return getAllProducts().filter { it.category.equals(category, ignoreCase = true) }
    }

    fun addToCart(productId: Int, size: String, quantity: Int): Long {
        val db = dbHelper.writableDatabase
        
        // Simple check if it exists in cart to increment quantity
        val cursor = db.rawQuery("SELECT id, quantity FROM cart WHERE product_id = ? AND selected_size = ?", arrayOf(productId.toString(), size))
        if (cursor.moveToFirst()) {
            val cartId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
            val newQuantity = existingQuantity + quantity
            
            val values = ContentValues().apply { put("quantity", newQuantity) }
            db.update("cart", values, "id = ?", arrayOf(cartId.toString()))
            cursor.close()
            return cartId.toLong()
        }
        cursor.close()

        // Otherwise insert new
        val values = ContentValues().apply {
            put("product_id", productId)
            put("selected_size", size)
            put("quantity", quantity)
        }
        return db.insert("cart", null, values)
    }

    fun getCartItems(): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()
        val db = dbHelper.readableDatabase
        
        val query = """
            SELECT c.id, c.product_id, c.selected_size, c.quantity, p.name, p.price, p.image_name 
            FROM cart c 
            INNER JOIN products p ON c.product_id = p.id
        """.trimIndent()
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val item = CartItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                    selectedSize = cursor.getString(cursor.getColumnIndexOrThrow("selected_size")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    productName = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    imageName = cursor.getString(cursor.getColumnIndexOrThrow("image_name"))
                )
                cartItems.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return cartItems
    }
}
