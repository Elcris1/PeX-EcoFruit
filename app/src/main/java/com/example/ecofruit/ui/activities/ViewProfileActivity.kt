package com.example.ecofruit.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecofruit.ui.screens.UserProfileScreen
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.ChatViewModel
import com.example.ecofruit.ui.viewmodels.ProfileViewModel
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.example.ecofruit.ui.viewmodels.UserViewModel
import com.example.ecofruit.ui.viewmodels.ViewModelFactory
import kotlin.getValue

class ViewProfileActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels { ViewModelFactory() }
    private val profileViewModel: ProfileViewModel by viewModels { ViewModelFactory() }
    private val chatViewModel: ChatViewModel by viewModels { ViewModelFactory() }
    val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val user by userViewModel.currentUser.collectAsState()
            val profile by profileViewModel.profile.collectAsState()
            val userId = intent.getStringExtra("user_id")?: ""
            val settings by settingsViewModel.settings.collectAsState()

            LaunchedEffect(Unit) {
                profileViewModel.getUserProfile(userId)
            }
            EcoFruitTheme (darkTheme = settings.darkTheme) {
                UserProfileScreen(
                    currentUser = user!!,
                    profile = profile,
                    isOwnProfile = user!!.id == profile?.user?.id,
                    onBack = {
                        finish()
                    },
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
                    onListingClick = {
                        //TODO: OPEN VIEW PRODUCT
                    }

                )
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EcoFruitTheme {
        Greeting("Android")
    }
}