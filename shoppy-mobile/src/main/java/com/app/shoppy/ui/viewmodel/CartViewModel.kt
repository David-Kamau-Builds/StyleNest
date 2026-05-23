package com.app.shoppy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.shoppy.data.local.SessionManager
import com.app.shoppy.data.repository.ShoppyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: ShoppyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _userEmail = MutableStateFlow(sessionManager.getUserEmail() ?: "")

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun refreshSession() {
        _userEmail.value = sessionManager.getUserEmail() ?: ""
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val cartItems: StateFlow<List<com.app.shoppy.model.CartItem>> = _userEmail
        .flatMapLatest { email ->
            repository.getCartItemsFlow(email)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addToCart(productId: Long, size: String, quantity: Int, price: Double) {
        val email = sessionManager.getUserEmail() ?: ""
        _userEmail.value = email // Update state flow dynamically
        if (email.isNotEmpty()) {
            viewModelScope.launch {
                repository.addToCart(productId, email, size, quantity, price)
            }
        }
    }

    fun updateQuantity(cartId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartId, quantity)
        }
    }

    fun removeCartItem(cartId: Long) {
        viewModelScope.launch {
            repository.removeCartItem(cartId)
        }
    }
}
