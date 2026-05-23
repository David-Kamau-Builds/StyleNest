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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi

@HiltViewModel
class SharedProductViewModel @Inject constructor(
    private val repository: ShoppyRepository,
    private val apiService: ShoppyApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _userEmail = MutableStateFlow(sessionManager.getUserEmail() ?: "")

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun refreshSession() {
        _userEmail.value = sessionManager.getUserEmail() ?: ""
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteProductIds: StateFlow<Set<Long>> = _userEmail
        .flatMapLatest { email ->
            repository.getWishlistItemsFlow(email)
        }
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

    private val _pagedProducts = MutableStateFlow<List<ProductEntity>>(emptyList())
    val pagedProducts: StateFlow<List<ProductEntity>> = _pagedProducts.asStateFlow()

    private var currentPage = 0
    private val pageSize = 6
    private var isLastPage = false
    private var isLoadingPage = false

    fun loadNextPage() {
        if (isLoadingPage || isLastPage) return
        isLoadingPage = true
        viewModelScope.launch {
            try {
                val offset = currentPage * pageSize
                val newItems = repository.getPagedProducts(pageSize, offset)
                if (newItems.isEmpty()) {
                    isLastPage = true
                } else {
                    _pagedProducts.value = _pagedProducts.value + newItems
                    currentPage++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingPage = false
            }
        }
    }

    fun resetPagination() {
        currentPage = 0
        isLastPage = false
        _pagedProducts.value = emptyList()
        loadNextPage()
    }

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        // Load initial offline cached data instantly
        resetPagination()
        viewModelScope.launch {
            repository.refreshProducts()
            // Reload after refresh to show any new changes
            resetPagination()
        }
    }

    fun toggleWishlist(productId: Long) {
        val email = sessionManager.getUserEmail() ?: ""
        _userEmail.value = email // Update state flow dynamically
        if (email.isNotEmpty()) {
            viewModelScope.launch {
                repository.toggleWishlist(productId, email)
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
