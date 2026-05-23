package com.app.shoppy.model

data class CartItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val selectedSize: String,
    val quantity: Int,
    val price: Double,
    val productImageUrl: String
)
