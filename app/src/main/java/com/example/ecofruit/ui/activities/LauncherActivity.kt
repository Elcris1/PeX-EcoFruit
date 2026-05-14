package com.example.ecofruit.ui.activities
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecofruit.R
import com.example.ecofruit.ui.components.AnimatedBubbleBackground
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.repository.SettingsKeys
import com.example.ecofruit.ui.data.repository.settingsDataStore
import com.example.ecofruit.ui.managers.LocaleManager
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.AuthViewModel
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LauncherActivity : BaseActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            EcoFruitTheme(darkTheme = settings.darkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LauncherScreen(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
    override fun attachBaseContext(newBase: Context) {
        // Read the saved language synchronously at startup
        val language = runBlocking {
            newBase.settingsDataStore.data
                .map { it[SettingsKeys.LANGUAGE] ?: "es" }
                .first()
        }
        super.attachBaseContext(LocaleManager.applyLocale(newBase, language))
    }
}
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LauncherScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState) {
        when (uiState) {
            is RequestUiState.Success -> {
                Intent(context, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(it)
                }
            }
            is RequestUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as RequestUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> Unit
        }
    }
    AnimatedBubbleBackground {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
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
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.change_starts_now),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 80.dp, bottom = 40.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (uiState is RequestUiState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdTokenOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .setAutoSelectEnabled(true)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdTokenOption)
                                .build()
                            scope.launch {
                                try {
                                    val result = credentialManager.getCredential(
                                        context = context,
                                        request = request
                                    )
                                    val googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(result.credential.data)
                                    authViewModel.signInWithGoogle(googleIdTokenCredential.idToken)
                                } catch (e: GetCredentialException) {
                                    Log.e("LauncherActivity", "Error al obtener credenciales", e)
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.google_icon),
                            modifier = Modifier.height(25.dp),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.google_sign_in))
                    }
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = {
                        Intent(context, RegisterActivity::class.java).also { context.startActivity(it) }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.email_sign_up))
                }
                Text(
                    text = stringResource(R.string.already_have_account),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.log_in),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        Intent(context, LoginActvity::class.java).also { context.startActivity(it) }
                    }
                )
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
