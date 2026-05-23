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

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: ShoppyRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val userEmail = sessionManager.getUserEmail() ?: ""

    val cartItems: StateFlow<List<com.app.shoppy.model.CartItem>> = repository.getCartItemsFlow(userEmail)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addToCart(productId: Long, size: String, quantity: Int, price: Double) {
        if (userEmail.isNotEmpty()) {
            viewModelScope.launch {
                repository.addToCart(productId, userEmail, size, quantity, price)
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
