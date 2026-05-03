package com.example.ecofruit.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.ecofruit.R
import com.example.ecofruit.ui.data.constants.ReviewType
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.screens.UserProfileScreen
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.AuthViewModel
import com.example.ecofruit.ui.viewmodels.ProductViewModel
import com.example.ecofruit.ui.viewmodels.ProfileViewModel
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.example.ecofruit.ui.viewmodels.UserViewModel
import com.example.ecofruit.ui.viewmodels.ViewModelFactory

class ViewProfileActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels { ViewModelFactory() }
    private val profileViewModel: ProfileViewModel by viewModels { ViewModelFactory() }
    val settingsViewModel: SettingsViewModel by viewModels()
    val productViewModel: ProductViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val user = authViewModel.currentAppUserModel
            val currentUserFull by userViewModel.currentUser.collectAsState()
            val profileState by profileViewModel.profileState.collectAsState()
            val addReviewState by profileViewModel.addReviewState.collectAsState()
            val deleteReviewState by profileViewModel.deleteReviewState.collectAsState()
            
            val userId = intent.getStringExtra("user_id") ?: ""
            val settings by settingsViewModel.settings.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    profileViewModel.getUserProfile(userId)
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
                if (currentUserFull != null) {
                    UserProfileScreen(
                        currentUser = currentUserFull!!,
                        profileState = profileState,
                        onBack = { finish() },
                        onMessage = {
                            //TODO: CREATE conversation and redirect to chat
                        },
                        onEditProfile = {
                            //TODO: Create edit profile
                        },
                        onFollow = { following ->
                            if (!following) {
                                userViewModel.followUser(userId)
                            } else {
                                userViewModel.unfollowUser(userId)
                            }
                        },
                        onFavouriteClick = { product, uid, favourite ->
                            productViewModel.toggleFavourite(product.id, uid, favourite)
                        },
                        onListingClick = { pid ->
                            val intent = Intent(context, ViewProductActivity::class.java).apply {
                                putExtra("product_id", pid)
                            }
                            context.startActivity(intent)
                        },
                        onAddReview = { rating, comment ->
                            user?.let { u ->
                                val newReview = Review(
                                    userId = u.id,
                                    authorName = u.name,
                                    authorAvatar = u.profileImageUrl,
                                    dstId = userId,
                                    rating = rating.toDouble(),
                                    description = comment,
                                    reviewType = ReviewType.USER,
                                    createdAt = System.currentTimeMillis() / 1000
                                )
                                profileViewModel.addReview(newReview)
                            }
                        },
                        onDeleteReview = { review ->
                            profileViewModel.deleteReview(review)
                        }
                    )
                }
            }
        }
    }
}
