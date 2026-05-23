package com.app.shoppy.model

data class Order(
    val id: Int,
    val orderNumber: String,
    val totalAmount: Double,
    val orderDate: String,
    val status: String
)
