package com.example.ecofruit.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.model.Product
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

    private val _productsWithUser = MutableStateFlow<List<ProductWithUser>>(emptyList())
    val productsWithUser: StateFlow<List<ProductWithUser>> = _productsWithUser.asStateFlow()

    private val _recommendedProducts = MutableStateFlow<List<Product>>(emptyList())
    val recommendedProducts: StateFlow<List<Product>> = _recommendedProducts.asStateFlow()

    private val _followingProducts = MutableStateFlow<List<Product>>(emptyList())
    val followingProducts: StateFlow<List<Product>> = _followingProducts.asStateFlow()

    private val _favouriteProducts = MutableStateFlow<List<Product>>(emptyList())
    val favouriteProducts: StateFlow<List<Product>> = _favouriteProducts.asStateFlow()




    init {
        //loadProducts()
    }


    private fun loadProductsWithUser() {
        val products = productRepository.getProducts()

        val result = products.mapNotNull { product ->
            val user = userRepo.getUserById(product.userId)

            user?.let {
                ProductWithUser(product, it)
            }
        }

        _productsWithUser.value = result
    }

    fun loadHomePage(user: User) {
        viewModelScope.launch {
            _recommendedProducts.value = productRepository.getRecommendedProducts(user.id)
            _followingProducts.value = productRepository.getProductsFromFollowingUsers(user)
            _favouriteProducts.value = productRepository.getFavouriteProducts(user.id)
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            productRepository.addProduct(product)
        }
    }


}