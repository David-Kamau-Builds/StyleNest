package com.app.shoppy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String?,
    val richDescription: String?,
    val category: String,
    val subCategory: String?,
    val sizes: String,
    val price: Double,
    val imageUrl: String,
    val images: String?
) {
    fun toProduct(): com.app.shoppy.model.Product {
        return com.app.shoppy.model.Product(
            id = id.toInt(),
            name = name,
            description = description ?: "",
            richDescription = richDescription ?: "",
            category = category,
            subCategory = subCategory ?: "",
            sizes = sizes,
            price = price,
            imageUrl = imageUrl,
            images = images ?: ""
        )
    }
}

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val userEmail: String,
    val selectedSize: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val userEmail: String
)
