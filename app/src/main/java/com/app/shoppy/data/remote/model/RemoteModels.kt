package com.app.shoppy.data.remote.model

import com.google.gson.annotations.SerializedName

data class ProductDto(
    val id: Long,
    val name: String,
    val description: String?,
    val richDescription: String?,
    val category: String,
    val subCategory: String?,
    val sizes: String,
    val price: Double,
    val imageUrl: String,
    val images: String?
)

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val twoFactorEnabled: Boolean,
    val loyaltyPoints: Int,
    val password: String? = null
)

data class OrderDto(
    val id: Long?,
    val userEmail: String,
    val orderNumber: String,
    val customerName: String,
    val phone: String,
    val deliveryAddress: String,
    val paymentMethod: String,
    val totalAmount: Double,
    val orderDate: String,
    val status: String?,
    val orderItems: List<OrderItemDto>
)

data class OrderItemDto(
    val id: Long?,
    val productId: Long,
    val size: String,
    val quantity: Int,
    val price: Double
)

data class ReviewDto(
    val id: Long?,
    val productId: Long,
    val userEmail: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String?
)
