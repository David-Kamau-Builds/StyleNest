package com.app.shoppy.model

data class Product(
    val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val imageName: String,
    val description: String,
    val sizes: String,
    val isFeatured: Boolean = false
)
