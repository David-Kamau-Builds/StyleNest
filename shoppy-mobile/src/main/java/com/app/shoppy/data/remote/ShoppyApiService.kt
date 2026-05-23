package com.app.shoppy.data.remote

import com.app.shoppy.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface ShoppyApiService {

    @GET("products")
    suspend fun getProducts(): Response<List<ProductDto>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductDto>
    
    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): Response<List<ProductDto>>
    
    @GET("products/filter")
    suspend fun filterProducts(
        @Query("category") category: String,
        @Query("minPrice") minPrice: Double,
        @Query("maxPrice") maxPrice: Double
    ): Response<List<ProductDto>>
    
    @GET("products/{id}/recommendations")
    suspend fun getRecommendations(@Path("id") id: Long): Response<List<ProductDto>>
    
    // Auth
    @POST("users/register")
    suspend fun register(@Body user: UserDto): Response<UserDto>
    
    @POST("users/login")
    suspend fun login(
        @Query("email") email: String, 
        @Query("password") password: String
    ): Response<UserDto>
    
    @GET("users/{email}")
    suspend fun getUserProfile(@Path("email") email: String): Response<UserDto>
    
    @PUT("users/{email}")
    suspend fun updateUserProfile(@Path("email") email: String, @Body user: UserDto): Response<UserDto>
    
    // Orders
    @GET("orders/user/{email}")
    suspend fun getUserOrders(@Path("email") email: String): Response<List<OrderDto>>
    
    @POST("orders")
    suspend fun createOrder(@Body order: OrderDto): Response<OrderDto>

    @GET("orders/{orderNumber}")
    suspend fun getOrder(@Path("orderNumber") orderNumber: String): Response<OrderDto>
    
    // Reviews
    @GET("reviews/product/{productId}")
    suspend fun getProductReviews(@Path("productId") productId: Long): Response<List<ReviewDto>>
    
    @POST("reviews")
    suspend fun addReview(@Body review: ReviewDto): Response<ReviewDto>
}
