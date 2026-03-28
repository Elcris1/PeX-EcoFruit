package com.example.ecofruit.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.navigation.Screen
import com.example.ecofruit.ui.navigation.bottomNavItems
import com.example.ecofruit.ui.screens.HomeScreen
import com.example.ecofruit.ui.screens.InboxScreen
import com.example.ecofruit.ui.screens.ProfileScreen
import com.example.ecofruit.ui.screens.SearchScreen
import com.example.ecofruit.ui.screens.SellScreen
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.ProductViewModel
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.example.ecofruit.ui.viewmodels.UserViewModel
import com.example.ecofruit.ui.viewmodels.ViewModelFactory
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels { ViewModelFactory() }
    private val productsViewModel: ProductViewModel by viewModels { ViewModelFactory() }
    private val settingsViewModel: SettingsViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            EcoFruitTheme (darkTheme = settings.darkTheme) {
                MainScreen(
                    productsViewModel = productsViewModel,
                    userViewModel = userViewModel
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    productsViewModel: ProductViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current

    val user by userViewModel.currentUser.collectAsState()

    val visibleTabs = bottomNavItems.filter { screen ->
        !screen.isSeller || user?.isProducer == true
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
            composable(Screen.Home.route)     { HomeScreen() }
            composable(Screen.Search.route)   { SearchScreen() }
            composable(Screen.Sell.route)     { SellScreen() }
            composable(Screen.Inbox.route)    { InboxScreen() }
            composable(Screen.Profile.route)  { ProfileScreen(
                user = user,
                onEditProfile = {},
                onConvertToProducer = {},
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