package com.app.shoppy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.shoppy.data.remote.ShoppyApiService
import com.app.shoppy.data.remote.model.ReviewDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReviewState {
    object Idle : ReviewState()
    object Loading : ReviewState()
    data class Success(val reviews: List<ReviewDto>) : ReviewState()
    data class Error(val message: String) : ReviewState()
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val apiService: ShoppyApiService
) : ViewModel() {

    private val _reviewState = MutableStateFlow<ReviewState>(ReviewState.Idle)
    val reviewState: StateFlow<ReviewState> = _reviewState.asStateFlow()

    fun fetchReviews(productId: Long) {
        _reviewState.value = ReviewState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getProductReviews(productId)
                if (response.isSuccessful && response.body() != null) {
                    _reviewState.value = ReviewState.Success(response.body()!!)
                } else {
                    _reviewState.value = ReviewState.Error("Failed to fetch reviews")
                }
            } catch (e: Exception) {
                _reviewState.value = ReviewState.Error("Network Error: ${e.message}")
            }
        }
    }

    fun submitReview(review: ReviewDto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.addReview(review)
                if (response.isSuccessful) {
                    onSuccess()
                    // Re-fetch reviews to update the list
                    fetchReviews(review.productId)
                } else {
                    onError("Failed to submit review")
                }
            } catch (e: Exception) {
                onError("Network Error: ${e.message}")
            }
        }
    }
}
