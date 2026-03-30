package com.example.ecofruit.ui.viewmodels


import androidx.lifecycle.ViewModel
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.repository.ProductRepository
import com.example.ecofruit.ui.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ProductWithUser(
    val product: Product,
    val user: User
)

class ProductViewModel(
    private val userRepo: UserRepository,
    private val productRepository: ProductRepository
): ViewModel() {

    private val _products = MutableStateFlow<List<ProductWithUser>>(emptyList())
    val products: StateFlow<List<ProductWithUser>> = _products


    init {
        loadProducts()
    }

    private fun loadProducts() {
        val products = productRepository.getProducts()

        val result = products.mapNotNull { product ->
            val user = userRepo.getUserById(product.userId)

            user?.let {
                ProductWithUser(product, it)
            }
        }

        _products.value = result
    }


}