package com.app.stylenest.data

import android.content.ContentValues
import android.content.Context
import com.app.stylenest.model.CartItem
import com.app.stylenest.model.Product

class StyleNestRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val sharedPrefs = context.getSharedPreferences("stylenest_prefs", Context.MODE_PRIVATE)
    
    private fun getUserEmail(): String {
        val email = sharedPrefs.getString("user_email", "") ?: ""
        return if (email.isBlank()) "guest" else email
    }

    fun isLoggedIn(): Boolean {
        val email = sharedPrefs.getString("user_email", "") ?: ""
        return email.isNotBlank()
    }

    /**
     * Called right after login. Merges any items the guest added to cart/wishlist
     * into the newly-authenticated user's account, then clears the guest rows.
     */
    fun mergeGuestDataToUser(userEmail: String) {
        val db = dbHelper.writableDatabase

        // --- Merge guest CART rows ---
        val guestCartCursor = db.rawQuery(
            "SELECT id, product_id, selected_size, quantity, price FROM cart WHERE user_email = 'guest'",
            null
        )
        if (guestCartCursor.moveToFirst()) {
            do {
                val guestRowId   = guestCartCursor.getInt(0)
                val productId    = guestCartCursor.getInt(1)
                val selectedSize = guestCartCursor.getString(2)
                val guestQty     = guestCartCursor.getInt(3)
                val price        = guestCartCursor.getDouble(4)

                // Check if the user already has this product+size in their cart
                val existingCursor = db.rawQuery(
                    "SELECT id, quantity FROM cart WHERE user_email = ? AND product_id = ? AND selected_size = ?",
                    arrayOf(userEmail, productId.toString(), selectedSize)
                )
                if (existingCursor.moveToFirst()) {
                    // Merge: add guest quantity to existing row
                    val existingId  = existingCursor.getInt(0)
                    val existingQty = existingCursor.getInt(1)
                    val merged = ContentValues().apply { put("quantity", existingQty + guestQty) }
                    db.update("cart", merged, "id = ?", arrayOf(existingId.toString()))
                    // Delete the guest row since it was folded in
                    db.delete("cart", "id = ?", arrayOf(guestRowId.toString()))
                } else {
                    // Reassign the guest row to the real user
                    val reassign = ContentValues().apply { put("user_email", userEmail) }
                    db.update("cart", reassign, "id = ?", arrayOf(guestRowId.toString()))
                }
                existingCursor.close()
            } while (guestCartCursor.moveToNext())
        }
        guestCartCursor.close()

        // --- Merge guest WISHLIST rows ---
        val guestWishCursor = db.rawQuery(
            "SELECT id, product_id FROM wishlist WHERE user_email = 'guest'",
            null
        )
        if (guestWishCursor.moveToFirst()) {
            do {
                val guestRowId = guestWishCursor.getInt(0)
                val productId  = guestWishCursor.getInt(1)

                // Check for duplicates in the user's wishlist
                val dupCursor = db.rawQuery(
                    "SELECT id FROM wishlist WHERE user_email = ? AND product_id = ?",
                    arrayOf(userEmail, productId.toString())
                )
                if (dupCursor.moveToFirst()) {
                    // Already in wishlist — just delete the guest duplicate
                    db.delete("wishlist", "id = ?", arrayOf(guestRowId.toString()))
                } else {
                    // Reassign to the real user
                    val reassign = ContentValues().apply { put("user_email", userEmail) }
                    db.update("wishlist", reassign, "id = ?", arrayOf(guestRowId.toString()))
                }
                dupCursor.close()
            } while (guestWishCursor.moveToNext())
        }
        guestWishCursor.close()
    }

    /**
     * Clears a logged-in user's cart and wishlist from the DB on logout.
     * Guest rows are intentionally left untouched.
     */
    fun clearUserData(userEmail: String) {
        val db = dbHelper.writableDatabase
        db.delete("cart", "user_email = ?", arrayOf(userEmail))
        db.delete("wishlist", "user_email = ?", arrayOf(userEmail))
    }

    fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, name, description, rich_description, category, sub_category, sizes, price, image_url, images FROM products", null)
        
        if (cursor.moveToFirst()) {
            do {
                products.add(
                    Product(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        description = cursor.getString(2),
                        richDescription = cursor.getString(3),
                        category = cursor.getString(4),
                        subCategory = cursor.getString(5),
                        sizes = cursor.getString(6),
                        price = cursor.getDouble(7),
                        imageUrl = cursor.getString(8),
                        images = cursor.getString(9)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }

    fun getFeaturedProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        // Get 6 random items for the featured grid
        val cursor = db.rawQuery("SELECT id, name, description, rich_description, category, sub_category, sizes, price, image_url, images FROM products ORDER BY RANDOM() LIMIT 6", null)
        
        if (cursor.moveToFirst()) {
            do {
                products.add(
                    Product(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        description = cursor.getString(2),
                        richDescription = cursor.getString(3),
                        category = cursor.getString(4),
                        subCategory = cursor.getString(5),
                        sizes = cursor.getString(6),
                        price = cursor.getDouble(7),
                        imageUrl = cursor.getString(8),
                        images = cursor.getString(9)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }

    fun getProductsByCategory(category: String): List<Product> {
        val products = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, name, description, rich_description, category, sub_category, sizes, price, image_url, images FROM products WHERE category = ?", arrayOf(category))
        
        if (cursor.moveToFirst()) {
            do {
                products.add(
                    Product(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        description = cursor.getString(2),
                        richDescription = cursor.getString(3),
                        category = cursor.getString(4),
                        subCategory = cursor.getString(5),
                        sizes = cursor.getString(6),
                        price = cursor.getDouble(7),
                        imageUrl = cursor.getString(8),
                        images = cursor.getString(9)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }

    fun getFilteredProducts(category: String, minPrice: Double, maxPrice: Double, limit: Int, offset: Int): List<Product> {
        val products = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        
        var query = "SELECT id, name, description, rich_description, category, sub_category, sizes, price, image_url, images FROM products WHERE price BETWEEN ? AND ?"
        val args = mutableListOf(minPrice.toString(), maxPrice.toString())
        
        if (category != "All") {
            query += " AND (category = ? OR sub_category = ?)"
            args.add(category)
            args.add(category)
        }
        
        query += " LIMIT ? OFFSET ?"
        args.add(limit.toString())
        args.add(offset.toString())
        
        val cursor = db.rawQuery(query, args.toTypedArray())
        if (cursor.moveToFirst()) {
            do {
                products.add(
                    Product(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        description = cursor.getString(2),
                        richDescription = cursor.getString(3),
                        category = cursor.getString(4),
                        subCategory = cursor.getString(5),
                        sizes = cursor.getString(6),
                        price = cursor.getDouble(7),
                        imageUrl = cursor.getString(8),
                        images = cursor.getString(9)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }
    
    fun getProductById(id: Int): Product? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, name, description, rich_description, category, sub_category, sizes, price, image_url, images FROM products WHERE id = ?", arrayOf(id.toString()))
        var product: Product? = null
        if (cursor.moveToFirst()) {
            product = Product(
                id = cursor.getInt(0),
                name = cursor.getString(1),
                description = cursor.getString(2),
                richDescription = cursor.getString(3),
                category = cursor.getString(4),
                subCategory = cursor.getString(5),
                sizes = cursor.getString(6),
                price = cursor.getDouble(7),
                imageUrl = cursor.getString(8),
                images = cursor.getString(9)
            )
        }
        cursor.close()
        return product
    }

    fun addToCart(productId: Int, size: String, quantity: Int) {
        val product = getProductById(productId) ?: return
        addToCartWithPrice(productId, size, quantity, product.price)
    }

    fun addToCartWithPrice(productId: Int, size: String, quantity: Int, customPrice: Double) {
        val email = getUserEmail() // Returns "guest" when not logged in

        val db = dbHelper.writableDatabase
        
        val cursor = db.rawQuery("SELECT id, quantity FROM cart WHERE product_id = ? AND selected_size = ? AND user_email = ?", arrayOf(productId.toString(), size, email))
        if(cursor.moveToFirst()) {
            val cartId = cursor.getInt(0)
            val currentQty = cursor.getInt(1)
            val values = ContentValues().apply { put("quantity", currentQty + quantity) }
            db.update("cart", values, "id = ?", arrayOf(cartId.toString()))
        } else {
            val values = ContentValues().apply {
                put("user_email", email)
                put("product_id", productId)
                put("selected_size", size)
                put("quantity", quantity)
                put("price", customPrice)
            }
            db.insert("cart", null, values)
        }
        cursor.close()
    }

    fun getCartItems(): List<CartItem> {
        val email = getUserEmail()
        val cartItems = mutableListOf<CartItem>()
        if (email.isEmpty()) return cartItems

        val db = dbHelper.readableDatabase
        
        val query = """
            SELECT c.id, c.product_id, p.name, c.selected_size, c.quantity, c.price, p.image_url 
            FROM cart c 
            JOIN products p ON c.product_id = p.id
            WHERE c.user_email = ?
        """
        val cursor = db.rawQuery(query, arrayOf(email))
        
        if (cursor.moveToFirst()) {
            do {
                cartItems.add(
                    CartItem(
                        id = cursor.getInt(0),
                        productId = cursor.getInt(1),
                        productName = cursor.getString(2),
                        selectedSize = cursor.getString(3),
                        quantity = cursor.getInt(4),
                        price = cursor.getDouble(5),
                        productImageUrl = cursor.getString(6)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return cartItems
    }
    
    fun updateCartQuantity(cartId: Int, newQuantity: Int) {
        val db = dbHelper.writableDatabase
        if (newQuantity <= 0) {
            db.delete("cart", "id = ?", arrayOf(cartId.toString()))
        } else {
            val values = ContentValues().apply { put("quantity", newQuantity) }
            db.update("cart", values, "id = ?", arrayOf(cartId.toString()))
        }
    }

    // Wishlist Methods
    fun toggleWishlist(productId: Int): Boolean {
        val email = getUserEmail()
        if (email.isEmpty()) return false
        
        val db = dbHelper.writableDatabase
        val isFav = isFavorite(productId)
        if (isFav) {
            db.delete("wishlist", "product_id = ? AND user_email = ?", arrayOf(productId.toString(), email))
            return false
        } else {
            val values = ContentValues().apply { 
                put("product_id", productId)
                put("user_email", email) 
            }
            db.insert("wishlist", null, values)
            return true
        }
    }

    fun isFavorite(productId: Int): Boolean {
        val email = getUserEmail()
        if (email.isEmpty()) return false

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id FROM wishlist WHERE product_id = ? AND user_email = ?", arrayOf(productId.toString(), email))
        val isFav = cursor.moveToFirst()
        cursor.close()
        return isFav
    }

    fun getWishlistItems(): List<Product> {
        val email = getUserEmail()
        val products = mutableListOf<Product>()
        if (email.isEmpty()) return products

        val db = dbHelper.readableDatabase
        val query = """
            SELECT p.id, p.name, p.description, p.rich_description, p.category, p.sub_category, p.sizes, p.price, p.image_url, p.images 
            FROM products p 
            JOIN wishlist w ON p.id = w.product_id
            WHERE w.user_email = ?
        """
        val cursor = db.rawQuery(query, arrayOf(email))
        
        if (cursor.moveToFirst()) {
            do {
                products.add(
                    Product(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        description = cursor.getString(2),
                        richDescription = cursor.getString(3),
                        category = cursor.getString(4),
                        subCategory = cursor.getString(5),
                        sizes = cursor.getString(6),
                        price = cursor.getDouble(7),
                        imageUrl = cursor.getString(8),
                        images = cursor.getString(9)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }

    fun getCartCount(): Int {
        val email = getUserEmail()
        if (email.isEmpty()) return 0
        
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(quantity) FROM cart WHERE user_email = ?", arrayOf(email))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getWishlistCount(): Int {
        val email = getUserEmail()
        if (email.isEmpty()) return 0

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM wishlist WHERE user_email = ?", arrayOf(email))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
}
