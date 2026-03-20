package com.example.ecofruit.ui.components

import android.app.Activity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoBackBar(
    title: String
) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSecondary
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                (context as Activity).finish()
            }) {
                Icon(
                    imageVector        = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint               = MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor          = MaterialTheme.colorScheme.secondary,
            scrolledContainerColor  = MaterialTheme.colorScheme.secondary
        )
    )
}