package com.example.ecofruit.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ecofruit.ui.data.constants.ReviewType
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.screens.ProductDetailScreen
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.ProductViewModel
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.example.ecofruit.ui.viewmodels.UserViewModel
import com.example.ecofruit.ui.viewmodels.ViewModelFactory

class ViewProductActivity : ComponentActivity() {

    private val userViewModel: UserViewModel by viewModels { ViewModelFactory() }
    private val productViewModel: ProductViewModel by viewModels { ViewModelFactory() }
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val productId = intent.getStringExtra("product_id") ?: ""

        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            val user by userViewModel.currentUser.collectAsState()
            val productState by productViewModel.product.collectAsState()
            val reviewsState by productViewModel.reviews.collectAsState()
            val addReviewState by productViewModel.addReviewState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(productId) {
                if (productId.isNotEmpty()) {
                    productViewModel.getProductByIdRealtime(productId)
                    productViewModel.getReviewsByProductIdRealtime(productId)
                }
            }

            LaunchedEffect(addReviewState) {
                if (addReviewState is RequestUiState.Success) {
                    Toast.makeText(context, "Reseña añadida correctamente", Toast.LENGTH_SHORT).show()
                } else if (addReviewState is RequestUiState.Error) {
                    Toast.makeText(context, "Error al añadir reseña: ${(addReviewState as RequestUiState.Error).message}", Toast.LENGTH_SHORT).show()
                }
            }

            EcoFruitTheme(darkTheme = settings.darkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = productState) {
                        is RequestUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        is RequestUiState.Success -> {
                            val product = state.data
                            if (product != null) {
                                val reviews = (reviewsState as? RequestUiState.Success)?.data ?: emptyList()
                                ProductDetailScreen(
                                    product = product,
                                    reviews = reviews,
                                    currentUserId = user?.id ?: "",
                                    onBackClick = { finish() },
                                    onContactProducer = {
                                        // TODO: Navegar a ChatActivity con el productor
                                    },
                                    onProducerClick = {
                                        Intent(context, ViewProfileActivity::class.java).also {
                                            it.putExtra("user_id", product.userId)
                                            context.startActivity(it)
                                        }
                                    },
                                    onToggleFavourite = { isFav ->
                                        user?.let {
                                            productViewModel.toggleFavourite(product.id, it.id, isFav)
                                        }
                                    },
                                    onAddReview = { rating, comment ->
                                        user?.let { u ->
                                            val newReview = Review(
                                                userId = u.id,
                                                authorName = u.name,
                                                authorAvatar = u.profileImageUrl,
                                                dstId = product.id,
                                                rating = rating.toDouble(),
                                                description = comment,
                                                reviewType = ReviewType.PRODUCT,
                                                createdAt = System.currentTimeMillis() / 1000
                                            )
                                            productViewModel.addReview(newReview)
                                        }
                                    }
                                )
                            } else {
                                Text("Producto no encontrado", modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        is RequestUiState.Error -> {
                            Text("Error: ${state.message}", modifier = Modifier.align(Alignment.Center))
                        }
                        else -> {
                            if (productId.isEmpty()) {
                                Text("ID de producto no proporcionado", modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
        }
    }
}
