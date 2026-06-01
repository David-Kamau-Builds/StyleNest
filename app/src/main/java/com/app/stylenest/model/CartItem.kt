package com.app.stylenest.model

data class CartItem(
    val id: Int = 0,
    val productId: Int,
    val selectedSize: String,
    var quantity: Int,
    val productName: String = "",
    val price: Double = 0.0,
    val imageName: String = ""
)
