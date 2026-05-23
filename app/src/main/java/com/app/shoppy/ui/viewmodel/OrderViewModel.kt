package com.app.shoppy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.shoppy.data.local.SessionManager
import com.app.shoppy.data.remote.ShoppyApiService
import com.app.shoppy.data.remote.model.OrderDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    data class Success(val orders: List<OrderDto>) : OrderState()
    data class Error(val message: String) : OrderState()
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val apiService: ShoppyApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    fun fetchUserOrders() {
        val email = sessionManager.getUserEmail() ?: return
        
        _orderState.value = OrderState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getUserOrders(email)
                if (response.isSuccessful && response.body() != null) {
                    _orderState.value = OrderState.Success(response.body()!!)
                } else {
                    _orderState.value = OrderState.Error("Failed to fetch orders.")
                }
            } catch (e: Exception) {
                _orderState.value = OrderState.Error("Network Error: ${e.message}")
            }
        }
    }

    fun submitOrder(order: OrderDto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.createOrder(order)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Order submission failed.")
                }
            } catch (e: Exception) {
                onError("Network Error: ${e.message}")
            }
        }
    }

    private val _selectedOrder = MutableStateFlow<OrderDto?>(null)
    val selectedOrder: StateFlow<OrderDto?> = _selectedOrder.asStateFlow()

    fun fetchOrder(orderNumber: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getOrder(orderNumber)
                if (response.isSuccessful && response.body() != null) {
                    _selectedOrder.value = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
