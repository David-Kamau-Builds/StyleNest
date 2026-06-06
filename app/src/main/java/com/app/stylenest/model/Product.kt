package com.app.stylenest.model

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val richDescription: String,
    val category: String,
    val subCategory: String,
    val sizes: String,
    val price: Double,
    val imageUrl: String,
    val images: String // Comma-separated list for slideshow
)
