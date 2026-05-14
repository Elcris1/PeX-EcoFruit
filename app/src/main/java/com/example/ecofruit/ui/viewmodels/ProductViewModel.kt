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
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.model.LocationData

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

    private val _updateProductState = MutableStateFlow<RequestUiState<Unit>>(RequestUiState.Idle())
    val updateProductState: StateFlow<RequestUiState<Unit>> = _updateProductState.asStateFlow()

    private val _searchProductsState = MutableStateFlow<RequestUiState<List<Product>>>(RequestUiState.Idle())
    val searchProductsState: StateFlow<RequestUiState<List<Product>>> = _searchProductsState.asStateFlow()

    private val _searchPagingState = MutableStateFlow(ProductSearchPagingState())
    val searchPagingState: StateFlow<ProductSearchPagingState> = _searchPagingState.asStateFlow()

    private var searchCursor: DocumentSnapshot? = null
    private var searchParams: ProductSearchParams? = null

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

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _updateProductState.value = RequestUiState.Loading()
            productRepository.updateProduct(product).onSuccess {
                _updateProductState.value = RequestUiState.Success(Unit)
            }.onFailure {
                _updateProductState.value = RequestUiState.Error(it.message ?: "Error updating product")
            }
        }
    }

    fun searchProducts(
        query: String,
        category: ProductType? = null,
        location: LocationData? = null,
        radiusKm: Double? = null
    ) {
        viewModelScope.launch {
            _searchProductsState.value = RequestUiState.Loading()
            productRepository.searchProducts(query, category, location, radiusKm)
                .onSuccess { products ->
                    _searchProductsState.value = RequestUiState.Success(products)
                }
                .onFailure { error ->
                    _searchProductsState.value = RequestUiState.Error(error.message ?: "Error searching products")
                }
        }
    }

    fun startSearchPaging(
        query: String,
        category: ProductType? = null,
        location: LocationData? = null,
        radiusKm: Double? = null
    ) {
        val newParams = ProductSearchParams(query, category, location, radiusKm)
        if (newParams == searchParams && _searchPagingState.value.items.isNotEmpty()) return

        searchParams = newParams
        searchCursor = null
        _searchPagingState.value = ProductSearchPagingState()
        loadNextSearchPage()
    }

    fun loadNextSearchPage() {
        val params = searchParams ?: return
        val currentState = _searchPagingState.value
        if (currentState.isLoading || !currentState.hasMore) return

        viewModelScope.launch {
            _searchPagingState.value = currentState.copy(isLoading = true, errorMessage = null)
            productRepository.searchProductsPage(
                query = params.query,
                category = params.category,
                location = params.location,
                radiusKm = params.radiusKm,
                pageSize = 20,
                startAfter = searchCursor
            ).onSuccess { page ->
                searchCursor = page.lastSnapshot
                val merged = currentState.items + page.items
                _searchPagingState.value = currentState.copy(
                    items = merged,
                    isLoading = false,
                    hasMore = page.hasMore,
                    errorMessage = null
                )
            }.onFailure { error ->
                _searchPagingState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error searching products"
                )
            }
        }
    }
}

data class ProductSearchPagingState(
    val items: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasMore: Boolean = true
)

data class ProductSearchParams(
    val query: String,
    val category: ProductType?,
    val location: LocationData?,
    val radiusKm: Double?
)
