package com.app.shoppy.data.repository

import com.app.shoppy.data.local.dao.CartDao
import com.app.shoppy.data.local.dao.ProductDao
import com.app.shoppy.data.local.dao.WishlistDao
import com.app.shoppy.data.local.entity.CartEntity
import com.app.shoppy.data.local.entity.ProductEntity
import com.app.shoppy.data.remote.ShoppyApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppyRepository @Inject constructor(
    private val apiService: ShoppyApiService,
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val wishlistDao: WishlistDao
) {
    // Products
    suspend fun refreshProducts() {
        try {
            val response = apiService.getProducts()
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.map { dto ->
                    ProductEntity(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        richDescription = dto.richDescription,
                        category = dto.category,
                        subCategory = dto.subCategory,
                        sizes = dto.sizes,
                        price = dto.price,
                        imageUrl = dto.imageUrl,
                        images = dto.images
                    )
                }
                productDao.insertAll(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Handle network error gracefully (offline mode)
        }
    }

    fun getAllProductsFlow(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }

    suspend fun getPagedProducts(limit: Int, offset: Int): List<ProductEntity> {
        return productDao.getPagedProducts(limit, offset)
    }
    
    // Wishlist
    fun getWishlistItemsFlow(email: String): Flow<List<com.app.shoppy.data.local.entity.WishlistEntity>> {
        return wishlistDao.getWishlistItems(email)
    }

    suspend fun toggleWishlist(productId: Long, email: String) {
        val existing = wishlistDao.getWishlistItem(productId, email)
        if (existing != null) {
            wishlistDao.deleteWishlistItem(existing)
        } else {
            wishlistDao.insertWishlistItem(com.app.shoppy.data.local.entity.WishlistEntity(productId = productId, userEmail = email))
        }
    }

    // Cart
    fun getCartItemsFlow(email: String): Flow<List<com.app.shoppy.model.CartItem>> {
        return cartDao.getCartItemsWithProduct(email)
    }
    
    suspend fun addToCart(productId: Long, email: String, size: String, quantity: Int, price: Double) {
        cartDao.insertCartItem(
            CartEntity(
                productId = productId,
                userEmail = email,
                selectedSize = size,
                quantity = quantity,
                price = price
            )
        )
    }

    suspend fun updateCartQuantity(cartId: Long, newQuantity: Int) {
        cartDao.updateQuantity(cartId, newQuantity)
    }

    suspend fun removeCartItem(cartId: Long) {
        cartDao.deleteCartItem(cartId)
    }
}
