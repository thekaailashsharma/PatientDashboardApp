package pheonix.app.patient.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import pheonix.app.patient.R
import pheonix.app.patient.presentation.components.SocialSignInButton
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    state: AuthViewModel.AuthState,
    onGoogleSignInClick: (credential: com.google.firebase.auth.AuthCredential) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val googleSignInClient = state.googleSignInClient

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                onGoogleSignInClick(credential)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(state.currentUser) {
        if (state.currentUser != null) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section with Marquee and Animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Marquee Text Animation
                    MarqueeText(
                        texts = listOf(
                            "Welcome to Patient Dashboard",
                            "Your Health Companion",
                            "Track Your Health Journey",
                            "Connect with Doctors"
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Doctor Animation
                    val composition by rememberLottieComposition(
                        spec = LottieCompositionSpec.RawRes(R.raw.doctor_animation)
                    )
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier
                            .size(240.dp)
                            .padding(16.dp)
                    )
                }
            }

            // Bottom Section with Sign In
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Sign in to access your personalized healthcare dashboard",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SocialSignInButton(
                        text = "Continue with Google",
                        icon = painterResource(R.drawable.google),
                        isLoading = state.isLoading,
                        onClick = {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MarqueeText(
    texts: List<String>,
    modifier: Modifier = Modifier
) {
    var currentTextIndex by remember { mutableStateOf(0) }
    var shouldAnimate by remember { mutableStateOf(true) }

    LaunchedEffect(shouldAnimate) {
        while (shouldAnimate) {
            delay(3000)
            currentTextIndex = (currentTextIndex + 1) % texts.size
        }
    }

    AnimatedContent(
        targetState = currentTextIndex,
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) with
                fadeOut(animationSpec = tween(600))
        },
        modifier = modifier
    ) { index ->
        Text(
            text = texts[index],
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}