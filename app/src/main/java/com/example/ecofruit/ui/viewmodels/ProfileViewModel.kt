package com.example.ecofruit.ui.viewmodels

import com.example.ecofruit.ui.data.repository.ProductRepository
import com.example.ecofruit.ui.data.repository.ReviewRepository
import com.example.ecofruit.ui.data.repository.UserRepository

class ProfileViewModel (
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val reviewRepository: ReviewRepository
) {
    private val TAG = "ProfileViewModel"
}