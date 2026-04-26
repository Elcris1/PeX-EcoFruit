package com.example.ecofruit.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.Calendar
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.ecofruit.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TAG = "ProfileComponents"
fun getYear(timestamp: Long): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp * 1000
    Log.d(TAG, "${timestamp}, ${calendar.get(Calendar.YEAR)}")

    return calendar.get(Calendar.YEAR)
}

fun getReadableData(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}



@Composable
fun UserImage(imageUrl: String, name: String, modifier: Modifier = Modifier.size(120.dp)) {
    if (imageUrl != "") {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .listener(
                    onError = { _, result ->
                        Log.d(TAG, "Error loading image , ${result.throwable}")
                    },
                    onSuccess = { _,_ ->
                        Log.d(TAG, "Image loaded successfully")
                    }
                )
                .crossfade(true)
                .build(),
            contentDescription = "Imagen de usuario",
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_user_placeholder),
            error = painterResource(R.drawable.ic_user_placeholder)
        )
    } else {
        ProfileText(name)
    }

}

@Composable
private fun ProfileText(name: String){
    Text(
        text = name
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString(""),
        //style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onPrimaryContainer,
    )
}

@Composable
fun RatingRow(rating: Float, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Star icons
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..5) {
                val starAlpha = when {
                    i <= rating.toInt() -> 1f
                    i - rating < 1f -> 0.5f
                    else -> 0.2f
                }
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = colorScheme.tertiary.copy(alpha = starAlpha),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.profile_ratings_count, count),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}