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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.mock.ProductsMockData.allProducts
import com.example.ecofruit.ui.data.model.User
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
import kotlin.getValue
import com.example.ecofruit.R
import com.example.ecofruit.ui.data.model.RequestUiState

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels { ViewModelFactory() }
    private val productsViewModel: ProductViewModel by viewModels { ViewModelFactory() }
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels { ViewModelFactory() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            EcoFruitTheme (darkTheme = settings.darkTheme) {
                MainScreen(
                    productsViewModel = productsViewModel,
                    userViewModel = userViewModel,
                    chatViewModel = chatViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatViewModel.getConversationsFromUser(userViewModel.currentUser.value?.id ?: "")
    }
}
private val TAG = "MainActivity"
private val permissions=arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

//TODO: manage rights for  wifi
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun MainScreen(
    productsViewModel: ProductViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    //SCreen required
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current


    //Location rights
    val launchMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() )
    {
            permissionMaps->
        val fineGranted = permissionMaps[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissionMaps[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted)
        {
            val mode = if (fineGranted) context.getString(R.string.precise) else context.getString(R.string.aproximated)
            Toast.makeText(context, context.getString(R.string.location_right_granted, mode), Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(context,context.getString(R.string.right_denied), Toast.LENGTH_SHORT).show()
        }
    }

    //General info
    val user by userViewModel.currentUser.collectAsState()

    //inbox
    val conversations by chatViewModel.conversations.collectAsState()


    val visibleTabs = bottomNavItems.filter { screen ->
        !screen.isSeller || user?.isProducer == true
    }

    //Home page
    val currentUser = user
    val recommendedProducts by productsViewModel.recommendedProducts.collectAsState(emptyList())
    val followedProducerProducts by productsViewModel.followingProducts.collectAsState(emptyList())
    val favouriteProducts by productsViewModel.favouriteProducts.collectAsState(emptyList())

    LaunchedEffect(Unit) {
        user?.let { it ->
            chatViewModel.getConversationsFromUser(it.id)
            productsViewModel.loadHomePage(it)
        }

        if (permissions.any {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            launchMultiplePermissions.launch(permissions)
        }
    }



    Scaffold(
        bottomBar = {
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
                        selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Evita acumular destinos en el back stack
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            /*
            val currentUser = MockData.users[0]
            val recommendedProducts = listOf(
                allProducts[0], // Manzanas
                allProducts[5], // Pan
                allProducts[6], // Queso
                allProducts[1]  // Tomates
            )
            val followedProducerProducts = allProducts.filter {
                it.userId == "u1" || it.userId == "u3"
            }
            val favouriteProducts = allProducts.filter {
                it.favouritesList.contains(currentUser.id)
            }

             */


            composable(Screen.Home.route)     { HomeScreen(
                currentUser = currentUser,
                recommendedProducts,
                followedProducerProducts,
                favouriteProducts
            ) }
            composable(Screen.Search.route)   { SearchScreen() }
            composable(Screen.Sell.route)     { SellScreen(
                onPublish = {product ->
                    currentUser?.let {
                        product.userId = currentUser.id
                        product.userName = currentUser.name
                        product.userAvatar = currentUser.profileImageUrl
                        productsViewModel.addProduct(product)
                    }
                }
            ) }
            composable(Screen.Inbox.route)    { InboxScreen(
                currentUser = user,
                chatViewModel = chatViewModel,
                conversations = conversations,
                onConversationClick = { conversation ->
                    Intent(context, ChatActivity::class.java).also {
                        it.putExtra("conversation_id", conversation.id)
                        context.startActivity(it)
                    }
                }
            ) }
            composable(Screen.Profile.route)  { 
                val producerState by userViewModel.producerState.collectAsState()
                ProfileScreen(
                user = user,
                uiState = producerState,
                onEditProfile = {},
                onConvertToProducer = {
                    userViewModel.changeProducerState()
                },
                onSettings = {
                    Intent(context, SettingsActivity::class.java).also {
                        context.startActivity(it)
                    }
                },
                onLogout = {
                    userViewModel.logOut()
                    Intent(context, LoginActvity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(it)
                        (context as Activity).finish()
                    }

                },
            )}
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LightMain() {
    EcoFruitTheme(darkTheme = false) {
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DarkMain() {
    EcoFruitTheme(darkTheme = true) {
        MainScreen()
    }
}
