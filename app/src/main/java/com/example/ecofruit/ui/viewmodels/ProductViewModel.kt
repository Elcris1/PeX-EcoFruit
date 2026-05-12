package com.example.ecofruit.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class ProductWithUser(
    val product: Product,
    val user: User
)

class ProductViewModel(
    private val userRepo: UserRepository,
    private val productRepository: ProductRepository,
    private val reviewRepository: ReviewRepository = ReviewRepository.getInstance()
): ViewModel() {

    private val _recommendedProducts = MutableStateFlow<RequestUiState<List<Product>>>(RequestUiState.Idle())
    val recommendedProducts: StateFlow<RequestUiState<List<Product>>> = _recommendedProducts.asStateFlow()

    private val _followingProducts = MutableStateFlow<RequestUiState<List<Product>>>(RequestUiState.Idle())
    val followingProducts: StateFlow<RequestUiState<List<Product>>> = _followingProducts.asStateFlow()

    private val _favouriteProducts = MutableStateFlow<RequestUiState<List<Product>>>(RequestUiState.Idle())
    val favouriteProducts: StateFlow<RequestUiState<List<Product>>> = _favouriteProducts.asStateFlow()

    private val _addProductState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val addProductState: StateFlow<RequestUiState<Unit>> = _addProductState.asStateFlow()

    private val _product = MutableStateFlow<RequestUiState<Product?>>(RequestUiState.Idle())
    val product: StateFlow<RequestUiState<Product?>> = _product.asStateFlow()

    private val _reviews = MutableStateFlow<RequestUiState<List<Review>>>(RequestUiState.Idle())
    val reviews: StateFlow<RequestUiState<List<Review>>> = _reviews.asStateFlow()

    private val _addReviewState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val addReviewState: StateFlow<RequestUiState<Unit>> = _addReviewState.asStateFlow()

    private val _deleteReviewState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val deleteReviewState: StateFlow<RequestUiState<Unit>> = _deleteReviewState.asStateFlow()

    private val _deleteProductState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val deleteProductState: StateFlow<RequestUiState<Unit>> = _deleteProductState.asStateFlow()

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

    fun loadHomePageRealtime(user: User) {
        viewModelScope.launch {
            _recommendedProducts.value = RequestUiState.Loading()
            _followingProducts.value = RequestUiState.Loading()
            _favouriteProducts.value = RequestUiState.Loading()

            launch {
                productRepository.getRecommendedProductsRealtime().collect { result ->
                    result.onSuccess {
                        _recommendedProducts.value = RequestUiState.Success(it)
                    }.onFailure {
                        _recommendedProducts.value = RequestUiState.Error(it.message ?: "Error loading recommended products")
                    }
                }
            }

            launch {
                productRepository.getProductsFromFollowingUsersRealtime(user).collect { result ->
                    result.onSuccess {
                        _followingProducts.value = RequestUiState.Success(it)
                    }.onFailure {
                        _followingProducts.value = RequestUiState.Error(it.message ?: "Error loading following products")
                    }
                }
            }

            launch {
                productRepository.getFavouriteProductsRealtime(user.id).collect { result ->
                    result.onSuccess {
                        _favouriteProducts.value = RequestUiState.Success(it)
                    }.onFailure {
                        _favouriteProducts.value = RequestUiState.Error(it.message ?: "Error loading favourite products")
                    }
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
            
            try {
                // Upload all images concurrently using async/awaitAll
                val uploadResults = imageUris.map { uri ->
                    async { productRepository.uploadProductImage(uri) }
                }.awaitAll()

                val imageUrls = mutableListOf<String>()
                var uploadErrorOccurred = false
                
                uploadResults.forEach { result ->
                    result.onSuccess { url ->
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
            } catch (e: Exception) {
                _addProductState.value = RequestUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun toggleFavourite(productId: String, userId: String, isFavourite: Boolean) {
        viewModelScope.launch {
            productRepository.setFavourite(productId, userId, isFavourite).onFailure {
                // Opcional: manejar error, por ejemplo con un snackbar o revirtiendo el estado en la UI
            }
        }
    }

    fun getProductById(productId: String) {
        viewModelScope.launch {
            _product.value = RequestUiState.Loading()
            productRepository.getProduct(productId).onSuccess {
                _product.value = RequestUiState.Success(it)
            }.onFailure {
                _product.value = RequestUiState.Error(it.message ?: "Error loading product")
            }
        }
    }

    fun getProductByIdRealtime(productId: String) {
        viewModelScope.launch {
            _product.value = RequestUiState.Loading()
            productRepository.getProductRealtime(productId).collect { result ->
                result.onSuccess {
                    _product.value = RequestUiState.Success(it)
                }.onFailure {
                    _product.value = RequestUiState.Error(it.message ?: "Error loading product")
                }
            }
        }
    }

    fun getReviewsByProductId(productId: String) {
        viewModelScope.launch {
            _reviews.value = RequestUiState.Loading()
            reviewRepository.getReviewsToProduct(productId).onSuccess {
                _reviews.value = RequestUiState.Success(it)
            }.onFailure {
                _reviews.value = RequestUiState.Error(it.message ?: "Error loading reviews")
            }
        }
    }

    fun getReviewsByProductIdRealtime(productId: String) {
        viewModelScope.launch {
            _reviews.value = RequestUiState.Loading()
            reviewRepository.getReviewsToProductRealtime(productId).collect { result ->
                result.onSuccess {
                    _reviews.value = RequestUiState.Success(it)
                }.onFailure {
                    _reviews.value = RequestUiState.Error(it.message ?: "Error loading reviews")
                }
            }
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            _addReviewState.value = RequestUiState.Loading()
            reviewRepository.addReview(review).onSuccess {
                _addReviewState.value = RequestUiState.Success(Unit)
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
            }.onFailure {
                _deleteReviewState.value = RequestUiState.Error(it.message ?: "Error deleting review")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _deleteProductState.value = RequestUiState.Loading()
            productRepository.deleteProduct(productId).onSuccess {
                _deleteProductState.value = RequestUiState.Success(Unit)
            }.onFailure {
                _deleteProductState.value = RequestUiState.Error(it.message ?: "Error deleting product")
            }
        }
    }
}
