package com.example.ecofruit.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ecofruit.R

sealed class Screen(val route: String, @StringRes val labelRes: Int, val icon: ImageVector, val isSeller: Boolean = false) {
    object Home     : Screen("home",     R.string.tab_home_title,   Icons.Default.Home)
    object Search   : Screen("search",   R.string.tab_search_title,   Icons.Default.Search)
    object Sell     : Screen("sell", R.string.tab_sell_title, Icons.Default.Add, isSeller = true)
    object Inbox    : Screen("inbox", R.string.tab_inbox_title,  Icons.Default.Inbox)
    object Profile  : Screen("profile",  R.string.tab_profile_title,   Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Sell,
    Screen.Inbox,
    Screen.Profile,
)