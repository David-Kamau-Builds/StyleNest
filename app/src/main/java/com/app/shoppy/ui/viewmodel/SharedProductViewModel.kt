package com.app.shoppy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.shoppy.data.local.SessionManager
import com.app.shoppy.data.local.entity.ProductEntity
import com.app.shoppy.data.remote.ShoppyApiService
import com.app.shoppy.data.remote.model.ProductDto
import com.app.shoppy.data.repository.ShoppyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedProductViewModel @Inject constructor(
    private val repository: ShoppyRepository,
    private val apiService: ShoppyApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val userEmail = sessionManager.getUserEmail() ?: ""

    val favoriteProductIds: StateFlow<Set<Long>> = repository.getWishlistItemsFlow(userEmail)
        .map { list -> list.map { it.productId }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val _recommendations = MutableStateFlow<List<ProductDto>>(emptyList())
    val recommendations: StateFlow<List<ProductDto>> = _recommendations.asStateFlow()

    // For Advanced Search & Filtering, we can expose a mutable list of search results
    // Or we could replace the main products list. Let's keep a separate one for searches/filters
    // so we don't overwrite the Room database flow of all products.
    private val _searchResults = MutableStateFlow<List<ProductDto>?>(null)
    val searchResults: StateFlow<List<ProductDto>?> = _searchResults.asStateFlow()

    val products: StateFlow<List<ProductEntity>> = repository.getAllProductsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            repository.refreshProducts()
        }
    }

    fun toggleWishlist(productId: Long) {
        if (userEmail.isNotEmpty()) {
            viewModelScope.launch {
                repository.toggleWishlist(productId, userEmail)
            }
        }
    }

    fun fetchRecommendations(productId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getRecommendations(productId)
                if (response.isSuccessful && response.body() != null) {
                    _recommendations.value = response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                val response = apiService.searchProducts(query)
                if (response.isSuccessful && response.body() != null) {
                    _searchResults.value = response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filterProducts(category: String, minPrice: Double, maxPrice: Double) {
        viewModelScope.launch {
            try {
                val response = apiService.filterProducts(category, minPrice, maxPrice)
                if (response.isSuccessful && response.body() != null) {
                    _searchResults.value = response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSearchAndFilter() {
        _searchResults.value = null
    }
}
