package com.example.ecofruit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ecofruit.ui.data.repository.ChatRepository
import com.example.ecofruit.ui.data.repository.UserRepository

class ViewModelFactory : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress
            return UserViewModel(UserRepository.getInstance()) as T
        }
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress
            return ProductViewModel(UserRepository.getInstance()) as T
        }
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress
            return ChatViewModel(ChatRepository.getInstance(), UserRepository.getInstance()) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}