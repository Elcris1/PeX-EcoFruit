package com.example.ecofruit.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.repository.ProductRepository
import com.example.ecofruit.ui.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductWithUser(
    val product: Product,
    val user: User
)

class ProductViewModel(
    private val userRepo: UserRepository,
    private val productRepository: ProductRepository
): ViewModel() {

    private val _recommendedProducts = MutableStateFlow<RequestUiState<List<Product>>>(RequestUiState.Idle())
    val recommendedProducts: StateFlow<RequestUiState<List<Product>>> = _recommendedProducts.asStateFlow()

    private val _followingProducts = MutableStateFlow<RequestUiState<List<Product>>>(RequestUiState.Idle())
    val followingProducts: StateFlow<RequestUiState<List<Product>>> = _followingProducts.asStateFlow()

    private val _favouriteProducts = MutableStateFlow<RequestUiState<List<Product>>>(RequestUiState.Idle())
    val favouriteProducts: StateFlow<RequestUiState<List<Product>>> = _favouriteProducts.asStateFlow()

    private val _addProductState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val addProductState: StateFlow<RequestUiState<Unit>> = _addProductState.asStateFlow()

    fun loadHomePage(user: User) {
        viewModelScope.launch {
            // Ponemos todos los estados en Loading primero
            _recommendedProducts.value = RequestUiState.Loading()
            _followingProducts.value = RequestUiState.Loading()
            _favouriteProducts.value = RequestUiState.Loading()

            // Lanzamos las peticiones de forma concurrente
            launch {
                productRepository.getRecommendedProducts().onSuccess {
                    _recommendedProducts.value = RequestUiState.Success(it)
                }.onFailure {
                    _recommendedProducts.value = RequestUiState.Error(it.message ?: "Error loading recommended products")
                }
            }

            launch {
                productRepository.getProductsFromFollowingUsers(user).onSuccess {
                    _followingProducts.value = RequestUiState.Success(it)
                }.onFailure {
                    _followingProducts.value = RequestUiState.Error(it.message ?: "Error loading following products")
                }
            }

            launch {
                productRepository.getFavouriteProducts(user.id).onSuccess {
                    _favouriteProducts.value = RequestUiState.Success(it)
                }.onFailure {
                    _favouriteProducts.value = RequestUiState.Error(it.message ?: "Error loading favourite products")
                }
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _addProductState.value = RequestUiState.Loading()
            productRepository.addProduct(product).onSuccess {
                _addProductState.value = RequestUiState.Success(Unit)
            }.onFailure {
                _addProductState.value = RequestUiState.Error(it.message ?: "Error adding product")
            }
        }
    }

    fun publishProduct(product: Product, imageUris: List<Uri>) {
        viewModelScope.launch {
            _addProductState.value = RequestUiState.Loading()
            
            val imageUrls = mutableListOf<String>()
            var uploadErrorOccurred = false
            
            imageUris.forEach { uri ->
                productRepository.uploadProductImage(uri).onSuccess { url ->
                    imageUrls.add(url)
                }.onFailure {
                    uploadErrorOccurred = true
                }
            }
            
            if (uploadErrorOccurred) {
                _addProductState.value = RequestUiState.Error("Error uploading some images")
                return@launch
            }
            
            val finalProduct = product.copy(imagesUrl = imageUrls)
            productRepository.addProduct(finalProduct).onSuccess {
                _addProductState.value = RequestUiState.Success(Unit)
            }.onFailure {
                _addProductState.value = RequestUiState.Error(it.message ?: "Error adding product to database")
            }
        }
    }
}
