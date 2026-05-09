package com.example.ecofruit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.FullUserInfo
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.repository.ProductRepository
import com.example.ecofruit.ui.data.repository.ReviewRepository
import com.example.ecofruit.ui.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel (
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val TAG = "ProfileViewModel"

    private val _profileState = MutableStateFlow<RequestUiState<FullUserInfo>>(RequestUiState.Idle())
    val profileState: StateFlow<RequestUiState<FullUserInfo>> = _profileState.asStateFlow()

    private val _addReviewState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val addReviewState: StateFlow<RequestUiState<Unit>> = _addReviewState.asStateFlow()

    private val _deleteReviewState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val deleteReviewState: StateFlow<RequestUiState<Unit>> = _deleteReviewState.asStateFlow()

    fun getUserProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = RequestUiState.Loading()
            
            try {
                val userDeferred = async { userRepository.getUserFromFirestore(userId) }
                val productsDeferred = async { productRepository.getProductsFromUserId(userId) }
                val reviewsDeferred = async { reviewRepository.getReviewsToUser(userId) }

                // Lanzamos las peticiones a la vez y las esperamos con awaitAll
                val results = awaitAll(userDeferred, productsDeferred, reviewsDeferred)

                @Suppress("UNCHECKED_CAST")
                val userResult = results[0] as Result<User?>
                @Suppress("UNCHECKED_CAST")
                val productsResult = results[1] as Result<List<Product>>
                @Suppress("UNCHECKED_CAST")
                val reviewsResult = results[2] as Result<List<Review>>

                val user = userResult.getOrThrow()
                val products = productsResult.getOrDefault(emptyList())
                val reviews = reviewsResult.getOrDefault(emptyList())

                if (user != null) {
                    _profileState.value = RequestUiState.Success(
                        FullUserInfo(
                            user = user,
                            products = products,
                            reviews = reviews
                        )
                    )
                } else {
                    _profileState.value = RequestUiState.Error("User not found")
                }
            } catch (e: Exception) {
                _profileState.value = RequestUiState.Error(e.message ?: "Error loading profile")
            }
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            _addReviewState.value = RequestUiState.Loading()
            reviewRepository.addReview(review).onSuccess {
                _addReviewState.value = RequestUiState.Success(Unit)
                // Refresh profile to show new review and updated rating
                getUserProfile(review.dstId)
            }.onFailure {
                _addReviewState.value = RequestUiState.Error(it.message ?: "Error adding review")
            }
        }
    }

    fun deleteReview(review: Review) {
        viewModelScope.launch {
            _deleteReviewState.value = RequestUiState.Loading()
            reviewRepository.deleteReview(review).onSuccess {
                _deleteReviewState.value = RequestUiState.Success(Unit)
                // Refresh profile
                getUserProfile(review.dstId)
            }.onFailure {
                _deleteReviewState.value = RequestUiState.Error(it.message ?: "Error deleting review")
            }
        }
    }
}
