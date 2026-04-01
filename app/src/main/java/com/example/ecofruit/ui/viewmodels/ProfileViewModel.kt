package com.example.ecofruit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.FullUserInfo
import com.example.ecofruit.ui.data.repository.ProductRepository
import com.example.ecofruit.ui.data.repository.ReviewRepository
import com.example.ecofruit.ui.data.repository.UserRepository
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

    private val _profile = MutableStateFlow<FullUserInfo?>(null)
    val profile: StateFlow<FullUserInfo?> = _profile.asStateFlow()

    fun getUserProfile(userId: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            val products = productRepository.getProductsFromUserId(userId)
            val reviews = reviewRepository.getReviewsToUser(userId)
            if (user != null && products != null && reviews != null)  {
                _profile.value = FullUserInfo(
                    user = user,
                    products = products,
                    reviews = reviews
                )
            }
        }
    }
}