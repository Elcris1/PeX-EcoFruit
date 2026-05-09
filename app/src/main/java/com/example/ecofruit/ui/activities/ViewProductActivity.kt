package com.example.ecofruit.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.res.stringResource
import com.example.ecofruit.R
import com.example.ecofruit.ui.data.constants.ReviewType
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.screens.ProductDetailScreen
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.AuthViewModel
import com.example.ecofruit.ui.viewmodels.ChatViewModel
import com.example.ecofruit.ui.viewmodels.ProductViewModel
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.example.ecofruit.ui.viewmodels.ViewModelFactory

class ViewProductActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels { ViewModelFactory() }
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val productId = intent.getStringExtra("product_id") ?: ""

        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            val user = authViewModel.currentAppUserModel
            val productState by productViewModel.product.collectAsState()
            val reviewsState by productViewModel.reviews.collectAsState()
            val addReviewState by productViewModel.addReviewState.collectAsState()
            val deleteReviewState by productViewModel.deleteReviewState.collectAsState()
            val contactState by chatViewModel.contactState.collectAsState()
            
            val context = LocalContext.current

            LaunchedEffect(productId) {
                if (productId.isNotEmpty()) {
                    productViewModel.getProductByIdRealtime(productId)
                    productViewModel.getReviewsByProductIdRealtime(productId)
                }
            }

            LaunchedEffect(contactState) {
                if (contactState is RequestUiState.Success) {
                    val conversationId = (contactState as RequestUiState.Success<String>).data
                    Log.d("VIEWPRODUCTACTIVITY", conversationId)
                    Intent(context, ChatActivity::class.java).also {
                        it.putExtra("conversation_id", conversationId)
                        context.startActivity(it)
                    }
                    chatViewModel.resetContactState()
                } else if (contactState is RequestUiState.Error) {
                    val error = (contactState as RequestUiState.Error).message
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    chatViewModel.resetContactState()
                }
            }

            LaunchedEffect(addReviewState) {
                if (addReviewState is RequestUiState.Success) {
                    Toast.makeText(context, context.getString(R.string.product_detail_added_success), Toast.LENGTH_SHORT).show()
                } else if (addReviewState is RequestUiState.Error) {
                    val message = (addReviewState as RequestUiState.Error).message
                    Toast.makeText(context, context.getString(R.string.product_detail_added_error, message), Toast.LENGTH_SHORT).show()
                }
            }

            LaunchedEffect(deleteReviewState) {
                if (deleteReviewState is RequestUiState.Success) {
                    Toast.makeText(context, context.getString(R.string.product_detail_deleted_success), Toast.LENGTH_SHORT).show()
                } else if (deleteReviewState is RequestUiState.Error) {
                    val message = (deleteReviewState as RequestUiState.Error).message
                    Toast.makeText(context, context.getString(R.string.product_detail_deleted_error, message), Toast.LENGTH_SHORT).show()
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
                                        user?.let { currentUser ->
                                            if (currentUser.id == product.userId) {
                                                Toast.makeText(context, context.getString(R.string.product_detail_contact_self_error), Toast.LENGTH_SHORT).show()
                                            } else {
                                                chatViewModel.contactProducer(currentUser.id, product.userId, product)
                                            }
                                        }
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
                                    },
                                    onDeleteReview = { review ->
                                        productViewModel.deleteReview(review)
                                    }
                                )
                            } else {
                                Text(stringResource(R.string.product_detail_not_found), modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        is RequestUiState.Error -> {
                            Text("${stringResource(R.string.retry)}: ${state.message}", modifier = Modifier.align(Alignment.Center))
                        }
                        else -> {
                            if (productId.isEmpty()) {
                                Text(stringResource(R.string.product_detail_id_missing), modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                    
                    if (contactState is RequestUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}
