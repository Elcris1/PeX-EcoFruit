package com.example.ecofruit.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofruit.R
import com.example.ecofruit.ui.components.AnimatedBubbleBackground
import com.example.ecofruit.ui.components.AnimatedCard
import com.example.ecofruit.ui.components.AnimatedHeader
import com.example.ecofruit.ui.components.AnimatedRedirectionText
import com.example.ecofruit.ui.components.CustomTextField
import com.example.ecofruit.ui.components.ErrorToast
import com.example.ecofruit.ui.components.LoadingButton
import com.example.ecofruit.ui.components.PasswordTextField
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.example.ecofruit.ui.viewmodels.UserViewModel
import com.example.ecofruit.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class RegisterActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            EcoFruitTheme (darkTheme = settings.darkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegisterScreen(
                        modifier = Modifier.padding(innerPadding),
                        userViewModel = userViewModel
                    )
                }
            }
        }
    }
}


    @Composable
    fun RegisterScreen(
        modifier: Modifier = Modifier,
        onRegisterClick: (name: String, email: String, password: String) -> Unit = { _, _, _ -> },
        userViewModel: UserViewModel = viewModel()
    ) {
        val context = LocalContext.current
        val uiState by userViewModel.registerUiState.collectAsState()

        var isLoading       by remember { mutableStateOf(false) }
        var name          by remember { mutableStateOf("") }
        var email           by remember { mutableStateOf("") }
        var password        by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var termsAccepted   by remember { mutableStateOf(false) }

        var nameError    by remember { mutableStateOf<String?>(null) }
        var emailError     by remember { mutableStateOf<String?>(null) }
        var passwordError  by remember { mutableStateOf<String?>(null) }
        var termsError     by remember { mutableStateOf(false) }

        var registerError by remember { mutableStateOf(false) }
        when (uiState) {
            is RequestUiState.Loading -> isLoading = true
            is RequestUiState.Success -> {
                isLoading = false
                Intent(context, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(it)
                }
            }
            is RequestUiState.Error -> {
                isLoading = false
                registerError = true

            }
            else -> Unit
        }

        val scope        = rememberCoroutineScope()
        @SuppressLint("LocalContextGetResourceValueCall")
        fun validate(): Boolean {
            nameError = if (name.isBlank()) context.getString(R.string.name_mandatory) else null
            emailError = when {
                email.isBlank()      -> context.getString(R.string.email_mandatory)
                !email.contains("@") -> context.getString(R.string.email_not_valid)
                else                 -> null
            }
            passwordError = when {
                password.isBlank()  -> context.getString(R.string.password_mandatory)
                password.length < 8 -> context.getString(R.string.password_length)
                else                -> null
            }
            termsError = !termsAccepted
            return listOf(nameError, emailError, passwordError).all { it == null }
                    && termsAccepted
        }

        val colors = MaterialTheme.colorScheme
        val enterAnim = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            enterAnim.animateTo(1f, tween(800, easing = EaseOutCubic))
        }

        AnimatedBubbleBackground (
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(50.dp))

                AnimatedHeader(
                    enterAnim = enterAnim,
                    description = stringResource(R.string.join_comunity)
                )

                Spacer(Modifier.height(28.dp))

                AnimatedCard(
                    enterAnim = enterAnim
                ){
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        CustomTextField(
                            value         = name,
                            onValueChange = { name = it; nameError = null },
                            label         = stringResource(R.string.name),
                            placeholder   = stringResource(R.string.name_placeholder),
                            icon          = Icons.Outlined.Person,
                            errorMessage  = nameError
                        )
                        CustomTextField(
                            value         = email,
                            onValueChange = { email = it; emailError = null },
                            label         = stringResource(R.string.email),
                            placeholder   = stringResource(R.string.email_placeholder),
                            icon          = Icons.Outlined.Email,
                            keyboardType  = KeyboardType.Email,
                            errorMessage  = emailError
                        )
                        PasswordTextField(
                            label              = stringResource(R.string.password),
                            placeholder_text   = stringResource(R.string.password_placeholder),
                            value              = password,
                            onValueChange      = { password = it; passwordError = null },
                            passwordVisible    = passwordVisible,
                            onToggleVisibility = { passwordVisible = !passwordVisible },
                            errorMessage       = passwordError
                        )
                        TermsRow(
                            accepted  = termsAccepted,
                            onToggle  = { termsAccepted = !termsAccepted; termsError = false },
                            showError = termsError
                        )
                        ErrorToast(
                            message = stringResource(R.string.register_error),
                            visible = registerError,
                            onDismiss = {registerError = false },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LoadingButton(
                            text = stringResource(R.string.create_account),
                            isLoading = isLoading
                        ) {
                            if (validate()) {
                                scope.launch {
                                    userViewModel.registerUser(name, email, password)
                                }
                            }
                        }

                    }
                }

                AnimatedRedirectionText(
                    enterAnim = enterAnim,
                    questionText = stringResource(R.string.already_have_account),
                    actionText = stringResource(R.string.log_in) + "!",
                    context = context,
                    dst = LoginActvity::class.java
                )

                Spacer(Modifier.height(50.dp))
            }
        }
    }


    @Composable
    private fun TermsRow(
        accepted  : Boolean,
        onToggle  : () -> Unit,
        showError : Boolean
    ) {
        val colors = MaterialTheme.colorScheme
        val containerColor = when {
            showError -> colors.errorContainer
            accepted  -> colors.primaryContainer
            else      -> colors.surfaceVariant
        }
        val borderColor = when {
            showError -> colors.error
            accepted  -> colors.primary
            else      -> colors.outline
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor)
                    .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onToggle() }
                    .padding(start = 4.dp, end = 12.dp, top = 8.dp)
            ) {
                RadioButton(
                    selected = accepted,
                    onClick  = onToggle,
                    colors   = RadioButtonDefaults.colors(
                        selectedColor   = colors.primary,
                        unselectedColor = colors.onSurfaceVariant
                    )
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.onSurfaceVariant)) {

                            append(stringResource(R.string.agree_to_terms_row))
                        }
                        withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)) {
                            append(stringResource(R.string.terms_of_service_terms_row))
                        }
                        withStyle(SpanStyle(color = colors.onSurfaceVariant)) {
                            append(stringResource(R.string.and_terms_row))
                        }
                        withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)) {
                            append(stringResource(R.string.privacy_policy_terms_row))
                        }
                    },
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
            AnimatedVisibility(visible = showError, enter = fadeIn() + slideInVertically()) {
                Text(
                    text  = stringResource(R.string.terms_mandatory),
                    color = colors.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }


    @Preview(showBackground = true, name = "Light")
    @Composable
    fun RegisterScreenLightPreview() {
        EcoFruitTheme(darkTheme = false) { RegisterScreen() }
    }

    @Preview(showBackground = true, name = "Dark")
    @Composable
    fun RegisterScreenDarkPreview() {
        EcoFruitTheme(darkTheme = true) { RegisterScreen() }
    }