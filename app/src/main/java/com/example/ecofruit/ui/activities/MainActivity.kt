package com.example.ecofruit.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ecofruit.ui.navigation.Screen
import com.example.ecofruit.ui.navigation.bottomNavItems
import com.example.ecofruit.ui.screens.HomeScreen
import com.example.ecofruit.ui.screens.InboxScreen
import com.example.ecofruit.ui.screens.ProfileScreen
import com.example.ecofruit.ui.screens.SearchScreen
import com.example.ecofruit.ui.screens.SellScreen
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.ChatViewModel
import com.example.ecofruit.ui.viewmodels.ProductViewModel
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.example.ecofruit.ui.viewmodels.UserViewModel
import com.example.ecofruit.ui.viewmodels.ViewModelFactory
import com.example.ecofruit.R
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.screens.EditProfileScreen
import com.example.ecofruit.ui.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels { ViewModelFactory() }
    private val productsViewModel: ProductViewModel by viewModels { ViewModelFactory() }
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViweModel: AuthViewModel by viewModels()

    private val chatViewModel: ChatViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            EcoFruitTheme (darkTheme = settings.darkTheme) {
                MainScreen(
                    productsViewModel = productsViewModel,
                    authViewModel = authViweModel,
                    userViewModel = userViewModel,
                    chatViewModel = chatViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatViewModel.getConversationsFromUser(authViweModel.currentAppUserModel?.id ?: "")
    }
}
private val permissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun MainScreen(
    productsViewModel: ProductViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current

    val launchMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionMaps ->
        val fineGranted = permissionMaps[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissionMaps[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            val mode = if (fineGranted) context.getString(R.string.precise) else context.getString(R.string.aproximated)
            Toast.makeText(context, context.getString(R.string.location_right_granted, mode), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, context.getString(R.string.right_denied), Toast.LENGTH_SHORT).show()
        }
    }

    val user = authViewModel.currentAppUserModel
    val conversations by chatViewModel.conversations.collectAsState()

    val visibleTabs = bottomNavItems.filter { screen ->
        !screen.isSeller || user?.isProducer == true
    }

    val recommendedProductsState by productsViewModel.recommendedProducts.collectAsStateWithLifecycle()
    val followedProducerProductsState by productsViewModel.followingProducts.collectAsStateWithLifecycle()
    val favouriteProductsState by productsViewModel.favouriteProducts.collectAsStateWithLifecycle()

    val editProfileState by userViewModel.updateState.collectAsState()

    LaunchedEffect(user) {
        user?.let { it ->
            chatViewModel.getConversationsFromUser(it.id)
            productsViewModel.loadHomePageRealtime(it)
        }

        if (permissions.any {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }) {
            launchMultiplePermissions.launch(permissions)
        }
    }

    Scaffold(
        bottomBar = {
            val showBar = currentDestination?.route != "edit_profile"
            if (showBar) {
                NavigationBar {
                    visibleTabs.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = stringResource(screen.labelRes)
                                )
                            },
                            label = { Text(stringResource(screen.labelRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    currentUser = user,
                    recommendedProductsState = recommendedProductsState,
                    followedProducerProductsState = followedProducerProductsState,
                    favouriteProductsState = favouriteProductsState,
                    onProductClick = { productId ->
                        // TODO: Implement navigation to product detail
                    },
                    onSearchClick = {
                        // TODO: Implement search
                    },
                    onFavouriteClick = { product, userId, isFavourite ->
                        productsViewModel.toggleFavourite(product.id, userId, isFavourite)
                    }
                )
            }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Sell.route) {
                SellScreen(
                    productsViewModel,
                    onPublish = { product, images ->
                        user?.let {
                            product.userId = it.id
                            product.userName = it.name
                            product.userAvatar = it.profileImageUrl
                            productsViewModel.publishProduct(product, images)
                        }
                    }
                )
            }
            composable(Screen.Inbox.route) {
                InboxScreen(
                    currentUser = user,
                    chatViewModel = chatViewModel,
                    conversations = conversations,
                    onConversationClick = { conversation ->
                        Intent(context, ChatActivity::class.java).also {
                            it.putExtra("conversation_id", conversation.id)
                            context.startActivity(it)
                        }
                    }
                )
            }
            composable(Screen.Profile.route) {
                val producerState by userViewModel.producerState.collectAsState()
                ProfileScreen(
                    user = user,
                    uiState = producerState,
                    onEditProfile = {navController.navigate("edit_profile") },
                    onConvertToProducer = { userViewModel.changeProducerState() },
                    onSettings = {
                        Intent(context, SettingsActivity::class.java).also {
                            context.startActivity(it)
                        }
                    },
                    onLogout = {
                        authViewModel.logout()
                        Intent(context, LoginActvity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(it)
                            (context as Activity).finish()
                        }
                    }
                )
            }
            composable("edit_profile") {
                if (user == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }
                EditProfileScreen(
                    user     = user,
                    editProfileState = editProfileState,
                    onSave   = { name, bio, avatarUri, location ->
                        userViewModel.updateUser(
                            updatedUser = user.copy(
                                name = name,
                                bio = bio,
                                location = location
                               ),
                            imageUri = avatarUri
                        )
                    },
                    onCancel = {
                        navController.popBackStack()
                        userViewModel.resetUpdateState()
                    },
                )

            }
        }
    }
}
