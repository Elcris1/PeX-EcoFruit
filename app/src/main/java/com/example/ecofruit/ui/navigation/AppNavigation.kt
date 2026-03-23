package com.example.ecofruit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val isSeller: Boolean = false) {
    object Home     : Screen("home",     "Inicio",   Icons.Default.Home)
    object Search   : Screen("search",   "Buscar",   Icons.Default.Search)
    object Sell     : Screen("sell", "Vender", Icons.Default.Add, isSeller = true)
    object Inbox    : Screen("inbox", "Inbox",  Icons.Default.Inbox)
    object Profile  : Screen("profile",  "Perfil",   Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Sell,
    Screen.Inbox,
    Screen.Profile,
)