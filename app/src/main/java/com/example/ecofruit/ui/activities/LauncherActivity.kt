package com.example.ecofruit.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecofruit.R
import com.example.ecofruit.ui.components.AnimatedBubbleBackground
import com.example.ecofruit.ui.theme.EcoFruitTheme

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoFruitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LauncherScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun navigateToPage(context: Context, dst: Class<*>) {
    Intent(context, dst).also {
        context.startActivity(it)
    }
}

@Composable
fun LauncherScreen( modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AnimatedBubbleBackground {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
                .background(Color(0xff033624)),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ecofruit_logo),
                    contentDescription = stringResource(R.string.ecofruit_logo),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {


                Column(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.change_starts_now),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 100.dp, bottom = 50.dp),
                        color= MaterialTheme.colorScheme.onBackground
                    )
                    Button (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            //TODO: firebase google identity provider
                        }
                    ) {

                        Icon(
                            painter = painterResource(R.drawable.google_icon),
                            modifier = Modifier.height(25.dp),
                            tint = Color.Unspecified,
                            contentDescription = stringResource(R.string.google_logo)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.google_sign_in))
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = { navigateToPage(context, RegisterActivity::class.java)}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            modifier = Modifier.height(25.dp),
                            contentDescription = stringResource(R.string.email_icon)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.email_sign_up))
                    }

                    Text(
                        text = stringResource(R.string.already_have_account),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.log_in),
                        color = MaterialTheme.colorScheme.onBackground,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navigateToPage(context, LoginActvity::class.java)
                        }
                    )
                }
            }
        }
    }


}


@Preview(showBackground = true, name = "LightTheme")
@Composable
fun LightThemePreview() {
    EcoFruitTheme(darkTheme = false) { LauncherScreen() }
}

@Preview(showBackground = true, name = "DarkTheme")
@Composable
fun DarkThemePreview() {
    EcoFruitTheme(darkTheme = true) { LauncherScreen() }
}