package com.example.ecofruit.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.ecofruit.ui.viewmodels.NavigationIntentViewModel
import com.example.ecofruit.R
import com.example.ecofruit.ui.screens.EditProfileScreen
import com.example.ecofruit.ui.viewmodels.AuthViewModel
import androidx.compose.ui.res.stringResource
import com.example.ecofruit.ui.components.NetworkStatusNotification

class MainActivity : BaseActivity() {
    private val userViewModel: UserViewModel by viewModels { ViewModelFactory() }
    private val productsViewModel: ProductViewModel by viewModels { ViewModelFactory() }
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private val chatViewModel: ChatViewModel by viewModels { ViewModelFactory() }
    private val navigationIntentViewModel: NavigationIntentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // read optional navigation target from the launching intent
        val navigateTo = intent?.getStringExtra("navigate_to")
        // forward initial value to the NavigationIntentViewModel so UI can react
        navigationIntentViewModel.send(navigateTo)

        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            EcoFruitTheme (darkTheme = settings.darkTheme) {
                MainScreen(
                    productsViewModel = productsViewModel,
                    authViewModel = authViewModel,
                    userViewModel = userViewModel,
                    chatViewModel = chatViewModel,
                    navigationIntentViewModel = navigationIntentViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    // update the Activity intent when a new intent is delivered (e.g., Activity already running)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // replace the Activity's intent so composables reading (context as Activity).intent will see the new extras
        setIntent(intent)
        // forward new navigation request to the view model so the UI can react while activity is alive
        navigationIntentViewModel.send(intent.getStringExtra("navigate_to"))
    }

    override fun onResume() {
        super.onResume()
        val userId = authViewModel.currentAppUserModel?.id
        if (userId != null) {
            chatViewModel.getConversationsFromUser(userId)
        }
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
    chatViewModel: ChatViewModel = viewModel(),
    navigationIntentViewModel: NavigationIntentViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    // observe navigation requests coming from the Activity (NavigationIntentViewModel)
    val requestedNavigateTo by navigationIntentViewModel.navigateTo.collectAsStateWithLifecycle()
    val isConnected by settingsViewModel.isConnectionSatisfied.collectAsStateWithLifecycle()

    LaunchedEffect(requestedNavigateTo) {
        requestedNavigateTo?.let { target ->
            val route = when (target) {
                "home" -> Screen.Home.route
                "profile" -> Screen.Profile.route
                "inbox" -> Screen.Inbox.route
                "sell" -> Screen.Sell.route
                "search" -> Screen.Search.route
                "edit_profile" -> "edit_profile"
                else -> null
            }

            route?.let { r ->
                navController.navigate(r) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current

    val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                context,
                context.getString(R.string.notifications_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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

    val visibleTabs = bottomNavItems.filter { screen ->
        !screen.isSeller || user?.isProducer == true
    }

    val recommendedProductsState by productsViewModel.recommendedProducts.collectAsStateWithLifecycle()
    val followedProducerProductsState by productsViewModel.followingProducts.collectAsStateWithLifecycle()
    val favouriteProductsState by productsViewModel.favouriteProducts.collectAsStateWithLifecycle()
    val searchPagingState by productsViewModel.searchPagingState.collectAsStateWithLifecycle()
    val searchedUsers by userViewModel.searchedUsers.collectAsStateWithLifecycle()

    val editProfileState by userViewModel.updateState.collectAsStateWithLifecycle()

    LaunchedEffect(user) {
        user?.let {
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
        Column(modifier = Modifier.padding(innerPadding)) {
            NetworkStatusNotification(isConnected = isConnected)
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.weight(1f)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        currentUser = user,
                        recommendedProductsState = recommendedProductsState,
                        followedProducerProductsState = followedProducerProductsState,
                        favouriteProductsState = favouriteProductsState,
                        onProductClick = { productId ->
                           Intent(context, ViewProductActivity::class.java).also {
                                it.putExtra("product_id", productId)
                                context.startActivity(it)
                            }
                        },
                        onSearchClick = {
                            navigationIntentViewModel.send("search")
                        },
                        onFavouriteClick = { product, userId, isFavourite ->
                            productsViewModel.toggleFavourite(product.id, userId, isFavourite)
                        }
                    )
                }
                composable(Screen.Search.route) { SearchScreen(
                    searchState = searchPagingState,
                    users = searchedUsers,
                    onSearch = { query, category, location, radiusKm ->
                        productsViewModel.startSearchPaging(query, category, location, radiusKm)
                    },
                    onUserSearch = { query ->
                        userViewModel.searchUsersByName(query)
                    },
                    onLoadMore = {
                        productsViewModel.loadNextSearchPage()
                    },
                ) }
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
                        onConversationClick = { conversation ->
                            Intent(context, ChatActivity::class.java).also {
                                it.putExtra("conversation_id", conversation.id)
                                context.startActivity(it)
                            }
                        }
                    )
                }
                composable(Screen.Profile.route) {
                    val producerState by userViewModel.producerState.collectAsStateWithLifecycle()
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
                        },
                        onViewShop = { userId ->
                            Intent(context, ViewProfileActivity::class.java).also {
                                it.putExtra("user_id", userId)
                                context.startActivity(it)
                            }
                        },
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
}
