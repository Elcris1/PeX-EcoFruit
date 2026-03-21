package com.example.ecofruit.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ecofruit.ui.theme.EcoFruitTheme
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ecofruit.R
import com.example.ecofruit.ui.components.AnimatedBubbleBackground
import com.example.ecofruit.ui.components.AnimatedCard
import com.example.ecofruit.ui.components.AnimatedCheck
import com.example.ecofruit.ui.components.AnimatedHeader
import com.example.ecofruit.ui.components.AnimatedRedirectionText
import com.example.ecofruit.ui.components.CustomTextField
import com.example.ecofruit.ui.components.GeneralButton
import com.example.ecofruit.ui.components.LoadingButton
import com.example.ecofruit.ui.components.OutlinedGeneralButton
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
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (email: String) -> Unit = {},
    onForgotPassword: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography  = MaterialTheme.typography
    val context = LocalContext.current

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var emailError      by remember { mutableStateOf<String?>(null) }
    var passwordError   by remember { mutableStateOf<String?>(null) }
    var resetPasswordDialog by remember { mutableStateOf(false) }

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
        if(resetPasswordDialog) {
            ForgotPasswordModal(onDismiss = { resetPasswordDialog = false })
        }

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
                                modifier = Modifier.clickable { resetPasswordDialog = true}
                            )
                        }

                        LoadingButton(
                            text = stringResource(R.string.log_in),
                            isLoading = isLoading,
                            onClick = {
                                var valid = true
                                if (email.isBlank() || !email.contains("@")) {
                                    emailError = context.getString(R.string.email_not_valid); valid = false
                                }
                                if (password.length < 8) {
                                    passwordError = context.getString(R.string.password_length); valid = false
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
                        )
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


// ─── Modal: Recuperar contraseña ──────────────────────────────────────────────

private enum class ForgotStep { INPUT, LOADING, SUCCESS }

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordModal(onDismiss: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val typography  = MaterialTheme.typography
    val scope       = rememberCoroutineScope()
    val context = LocalContext.current

    var resetEmail  by remember { mutableStateOf("") }
    var emailError  by remember { mutableStateOf<String?>(null) }
    var step        by remember { mutableStateOf(ForgotStep.INPUT) }

    // Animación de escala/fade al aparecer
    val scaleAnim = remember { Animatable(0.85f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        alphaAnim.animateTo(1f, tween(200))
    }

    BasicAlertDialog(
        onDismissRequest = { if (step != ForgotStep.LOADING) onDismiss() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha  = alphaAnim.value
                }
                .clip(RoundedCornerShape(28.dp))
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                .padding(28.dp)
        ) {
            when (step) {

                // ── Estado 1: formulario ──────────────────────────────────────
                ForgotStep.INPUT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                        // Icono + título
                        ForgotHeader(
                            title = stringResource(R.string.password_recover),
                            subtitle = stringResource(R.string.email_recover_link)
                        )

                        CustomTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it; emailError = null},
                            label = stringResource(R.string.email),
                            placeholder = stringResource(R.string.email_placeholder),
                            icon = Icons.Default.Person,
                            keyboardType = KeyboardType.Email,
                            errorMessage = emailError
                        )

                        // Botones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancelar
                            OutlinedGeneralButton(
                                text=  stringResource(R.string.cancel),
                                modifier = Modifier.weight(1f),
                                onClick = onDismiss
                            )

                            // Enviar
                            GeneralButton(
                                text = stringResource(R.string.send),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (resetEmail.isBlank() || !resetEmail.contains("@")) {
                                    emailError = context.getString(R.string.email_not_valid)
                                } else {
                                    scope.launch {
                                        step = ForgotStep.LOADING
                                        delay(2000)
                                        step = ForgotStep.SUCCESS
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Estado 2: cargando ────────────────────────────────────────
                ForgotStep.LOADING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            stringResource(R.string.sending_link),
                            style = typography.bodyMedium.copy(color = colorScheme.onSurfaceVariant)
                        )
                    }
                }

                // ── Estado 3: éxito ───────────────────────────────────────────
                ForgotStep.SUCCESS -> {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnimatedCheck()

                        ForgotHeader(
                            title = stringResource(R.string.email_sent),
                            subtitle = stringResource(R.string.check_inbox, resetEmail)
                        )

                        // Botón cerrar
                        GeneralButton(
                            text= stringResource(R.string.understood),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}

// ─── Cabecera del modal ───────────────────────────────────────────────────────

@Composable
private fun ForgotHeader(title: String, subtitle: String) {
    val colorScheme = MaterialTheme.colorScheme
    val typography  = MaterialTheme.typography
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            title,
            style = typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )
        Text(
            subtitle,
            style = typography.bodySmall.copy(color = colorScheme.onSurfaceVariant),
            textAlign = TextAlign.Center
        )
    }
}