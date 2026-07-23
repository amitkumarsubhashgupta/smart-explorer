package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyEmailScreen(
    viewModel: AuthViewModel,
    onVerificationSuccess: () -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var timerSeconds by remember { mutableStateOf(30) }
    var isResendEnabled by remember { mutableStateOf(true) }

    // Countdown logic for resending emails
    LaunchedEffect(isResendEnabled, timerSeconds) {
        if (!isResendEnabled && timerSeconds > 0) {
            delay(1000)
            timerSeconds -= 1
            if (timerSeconds == 0) {
                isResendEnabled = true
            }
        }
    }

    // Interactive animated pulse for status icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Moving ambient backdrop colors
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF020617)  // Slate 950
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background light source
        Box(
            modifier = Modifier
                .offset(y = (-150).dp)
                .size(320.dp)
                .background(Color(0xFF4F8CFF).copy(alpha = 0.08f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header visual
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .background(Color(0xFF4F8CFF).copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, Color(0xFF4F8CFF).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Pending Verification Icon",
                    tint = Color(0xFF4F8CFF),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Text Titles
            Text(
                text = "Verify Your Email",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Please verify your email before continuing.",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = "We have sent a verification link to your registered email address. Tap the link in your inbox to complete setup.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // Dynamic User Profile Card
            currentUser?.let { user ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Awaiting verification for:",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F8CFF),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Refresh Verification Status Button
            Button(
                onClick = {
                    viewModel.refreshVerificationStatus(
                        onSuccess = { isVerified ->
                            if (isVerified) {
                                Toast.makeText(context, "Verification Successful! Welcome!", Toast.LENGTH_LONG).show()
                                onVerificationSuccess()
                            } else {
                                Toast.makeText(context, "Email is still unverified. Please check your inbox and click the verification link.", Toast.LENGTH_LONG).show()
                            }
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, "Verification Status Error: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F8CFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("refresh_verification_status_button"),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Verification Status", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }

            // Resend Verification Email Button
            OutlinedButton(
                onClick = {
                    if (isResendEnabled) {
                        viewModel.sendEmailVerification(
                            onSuccess = {
                                isResendEnabled = false
                                timerSeconds = 30
                                Toast.makeText(context, "A new verification email has been dispatched!", Toast.LENGTH_LONG).show()
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, "Resend failed: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(if (isResendEnabled) Color(0xFF4F8CFF).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f))
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F8CFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("resend_verification_email_button"),
                enabled = isResendEnabled && !isLoading
            ) {
                Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isResendEnabled) "Resend Verification Email" else "Resend in ${timerSeconds}s",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (isResendEnabled) Color(0xFF4F8CFF) else Color.White.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cancel / Sign Out Link
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        viewModel.signOut {
                            onNavigateBackToLogin()
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cancel & Sign Out",
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}
