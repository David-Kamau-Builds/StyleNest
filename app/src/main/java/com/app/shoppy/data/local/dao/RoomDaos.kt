package com.app.shoppy.data.local.dao

import androidx.room.*
import com.app.shoppy.data.local.entity.CartEntity
import com.app.shoppy.data.local.entity.ProductEntity
import com.app.shoppy.data.local.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)
    
    @Query("DELETE FROM products")
    suspend fun clearAll()
}

@Dao
interface CartDao {

    @Query("SELECT cart.id, cart.productId, p.name as productName, cart.selectedSize, cart.quantity, cart.price, p.imageUrl as productImageUrl FROM cart INNER JOIN products p ON cart.productId = p.id WHERE cart.userEmail = :email")
    fun getCartItemsWithProduct(email: String): Flow<List<com.app.shoppy.model.CartItem>>
    @Query("SELECT * FROM cart WHERE userEmail = :email")
    fun getCartItems(email: String): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartEntity)

        @Query("UPDATE cart SET quantity = :quantity WHERE id = :cartId")
    suspend fun updateQuantity(cartId: Long, quantity: Int)

    @Query("DELETE FROM cart WHERE id = :cartId")
    suspend fun deleteCartItem(cartId: Long)
    
    @Query("DELETE FROM cart WHERE userEmail = :email")
    suspend fun clearCart(email: String)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist WHERE userEmail = :email")
    fun getWishlistItems(email: String): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist WHERE productId = :productId AND userEmail = :email LIMIT 1")
    suspend fun getWishlistItem(productId: Long, email: String): WishlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistEntity)

    @Delete
    suspend fun deleteWishlistItem(item: WishlistEntity)
}


