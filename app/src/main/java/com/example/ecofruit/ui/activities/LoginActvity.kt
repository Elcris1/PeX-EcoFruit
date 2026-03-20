package com.example.ecofruit.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ecofruit.ui.theme.EcoFruitTheme
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ecofruit.R
import com.example.ecofruit.ui.components.AnimatedBubbleBackground
import com.example.ecofruit.ui.components.AnimatedCard
import com.example.ecofruit.ui.components.AnimatedHeader
import com.example.ecofruit.ui.components.AnimatedRedirectionText
import com.example.ecofruit.ui.components.CustomTextField
import com.example.ecofruit.ui.components.LoadingButton
import com.example.ecofruit.ui.components.PasswordTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActvity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoFruitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
// ─── Pantalla principal ──────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (email: String) -> Unit = {},
    onForgotPassword: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography  = MaterialTheme.typography

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var emailError      by remember { mutableStateOf<String?>(null) }
    var passwordError   by remember { mutableStateOf<String?>(null) }

    val scope        = rememberCoroutineScope()

    // Animación de entrada
    val enterAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterAnim.animateTo(1f, tween(800, easing = EaseOutCubic))
    }


    AnimatedBubbleBackground (
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            Spacer(Modifier.height(24.dp))

            // Título
            AnimatedHeader(
                enterAnim = enterAnim,
                description = stringResource(R.string.log_in_to_continue)
            )

            Spacer(Modifier.height(36.dp))

            // Tarjeta del formulario
            AnimatedCard(
                enterAnim = enterAnim
            ){
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        CustomTextField(
                            value = email,
                            onValueChange = { email = it; emailError = null},
                            label = stringResource(R.string.email),
                            placeholder = stringResource(R.string.email_placeholder),
                            icon = Icons.Default.Person,
                            keyboardType = KeyboardType.Email,
                            errorMessage = emailError
                        )

                        PasswordTextField(
                            label = stringResource(R.string.password),
                            placeholder_text = stringResource(R.string.password_placeholder),
                            value = password,
                            onValueChange = {password = it; passwordError = null},
                            passwordVisible = passwordVisible,
                            onToggleVisibility = {passwordVisible = !passwordVisible},
                            errorMessage = passwordError
                        )

                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = stringResource(R.string.forgot_password),
                                color = colorScheme.primary,
                                style = typography.labelMedium,
                                modifier = Modifier.clickable { onForgotPassword() }
                            )
                        }

                        LoadingButton(
                            text = stringResource(R.string.log_in),
                            isLoading = isLoading
                        ) {
                            var valid = true
                            if (email.isBlank() || !email.contains("@")) {
                                emailError = "Ingresa un correo válido"; valid = false
                            }
                            if (password.length < 6) {
                                passwordError = "Mínimo 6 caracteres"; valid = false
                            }
                            if (valid) {
                                scope.launch {
                                    isLoading = true
                                    delay(1800)
                                    isLoading = false
                                    onLoginSuccess(email)
                                }
                            }
                        }
                    }
                }


            AnimatedRedirectionText(
                enterAnim = enterAnim,
                questionText = stringResource(R.string.dont_have_account),
                actionText = stringResource(R.string.sign_up) + "!",
                context = LocalContext.current,
                dst = RegisterActivity::class.java
            )


        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Light")
@Composable
private fun LoginPreviewLight() {
    EcoFruitTheme(darkTheme = false) { LoginScreen() }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun LoginPreviewDark() {
    EcoFruitTheme(darkTheme = true) { LoginScreen() }
}
