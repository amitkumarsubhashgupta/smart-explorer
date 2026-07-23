package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.R
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodel.AuthViewModel
import kotlin.math.sin

enum class AuthFormState {
    CHOICES,
    LOGIN_EMAIL,
    REGISTER_EMAIL,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsConditions: () -> Unit = {},
    onNavigateToAppVersion: () -> Unit = {}
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    var formState by remember { mutableStateOf(AuthFormState.CHOICES) }
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }
    var showRegisterPassword by remember { mutableStateOf(false) }

    // Animation values for moving neon background light blobs
    val infiniteTransition = rememberInfiniteTransition(label = "login_bg_anim")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val driftX by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_x"
    )

    val driftY by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_y"
    )

    // Twinkling stars seed list
    val stars = remember {
        List(40) {
            Triple(
                Math.random().toFloat(), // x ratio
                Math.random().toFloat(), // y ratio
                (2..5).random().toFloat() // size
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050816)) // Base deep background
    ) {
        // Futuristic Animated Canvas Background (Drifting neon ambient light & space stars)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val time = System.currentTimeMillis()
            stars.forEachIndexed { index, star ->
                val x = star.first * size.width
                val y = star.second * size.height
                val twinkle = (sin((time / 1000.0) + index * 123.45) * 0.5 + 0.5).toFloat()
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f + twinkle * 0.6f),
                    radius = star.third,
                    center = Offset(x, y)
                )
            }
        }

        // Drifting Neon Ambient Glow (Blurred circles in background)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-50).dp + driftX.dp, y = 100.dp + driftY.dp)
                .blur(90.dp)
                .background(Color(0xFF4F8CFF).copy(alpha = pulseAlpha), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp + driftX.dp, y = 50.dp - driftY.dp)
                .blur(110.dp)
                .background(Color(0xFF7C4DFF).copy(alpha = pulseAlpha), CircleShape)
        )



        // Main Login Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            Spacer(modifier = Modifier.height(10.dp))

            // MIDDLE PART: Glass Card containing Logo, Title, and Action Buttons
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                containerColor = Color(0xFF101827).copy(alpha = 0.65f),
                borderColor = Color(0xFF4F8CFF).copy(alpha = 0.18f),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // FUTURISTIC GLOWING LOGO ICON
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(22.dp),
                                clip = false,
                                ambientColor = Color(0xFF7C4DFF).copy(alpha = 0.4f),
                                spotColor = Color(0xFF4F8CFF).copy(alpha = 0.6f)
                            )
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF4F8CFF), Color(0xFF7C4DFF))),
                                RoundedCornerShape(22.dp)
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        var isError by remember { mutableStateOf(false) }

                        Image(
                            painter = if (!isError) {
                                rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(R.drawable.img_app_logo_1784297350848)
                                        .crossfade(true)
                                        .build(),
                                    onError = { isError = true }
                                )
                            } else {
                                rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(R.mipmap.ic_launcher)
                                        .crossfade(true)
                                        .build()
                                    )
                            },
                            contentDescription = "Official Application Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Smart Explorer",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your AI-powered all-in-one utility platform.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF4F8CFF),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Authorizing Explorer Vault...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else {
                        when (formState) {
                            AuthFormState.CHOICES -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // 2. Continue as Guest Button
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                            .clickable {
                                                viewModel.signInAsGuest(
                                                    onSuccess = {
                                                        Toast.makeText(context, "Welcome back, Guest!", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess()
                                                    },
                                                    onError = { err ->
                                                        Toast.makeText(context, "Guest Login Failed: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            }
                                            .testTag("guest_login_button"),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountCircle,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Continue as Guest",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }

                                    // 3. Email Authentication Button Toggle
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                            .clickable {
                                                formState = AuthFormState.LOGIN_EMAIL
                                            }
                                            .testTag("email_login_toggle_button"),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Email,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Continue with Email",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            AuthFormState.LOGIN_EMAIL -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Login to Your Account",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        label = { Text("Your Email Address") },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                            focusedBorderColor = Color(0xFF4F8CFF),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedLabelColor = Color(0xFF4F8CFF),
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("email_login_email_input")
                                    )

                                     OutlinedTextField(
                                        value = passwordInput,
                                        onValueChange = { passwordInput = it },
                                        label = { Text("Password") },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                                        trailingIcon = {
                                            IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                                Icon(
                                                    imageVector = if (showLoginPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = if (showLoginPassword) "Hide password" else "Show password",
                                                    tint = Color.White.copy(alpha = 0.5f)
                                                )
                                            }
                                        },
                                        visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                            focusedBorderColor = Color(0xFF4F8CFF),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedLabelColor = Color(0xFF4F8CFF),
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("email_login_password_input")
                                    )

                                    // Forgot Password Trigger Link
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = "Forgot Password?",
                                            color = Color(0xFF4F8CFF),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .clickable { formState = AuthFormState.FORGOT_PASSWORD }
                                                .padding(vertical = 2.dp)
                                                .testTag("forgot_password_button")
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                                Toast.makeText(context, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
                                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches()) {
                                                Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.signInWithEmail(
                                                    email = emailInput.trim(),
                                                    password = passwordInput,
                                                    onSuccess = { user ->
                                                        if (user.emailVerified) {
                                                            Toast.makeText(context, "Successfully Logged In! Welcome, ${user.name}!", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(context, "Note: Please verify your email to unlock all features.", Toast.LENGTH_LONG).show()
                                                        }
                                                        onLoginSuccess()
                                                    },
                                                    onError = { err ->
                                                        Toast.makeText(context, "Login Failed: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F8CFF)),
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("email_login_submit_button")
                                    ) {
                                        Text("Sign In", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Don't have an account? ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                                        Text(
                                            text = "Register",
                                            color = Color(0xFF4F8CFF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.clickable { formState = AuthFormState.REGISTER_EMAIL }
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { formState = AuthFormState.CHOICES }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Back to other choices", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                                    }
                                }
                            }

                            AuthFormState.REGISTER_EMAIL -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Register a New Account",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { nameInput = it },
                                        label = { Text("Your Full Name") },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                            focusedBorderColor = Color(0xFF4F8CFF),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedLabelColor = Color(0xFF4F8CFF),
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("email_register_name_input")
                                    )

                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        label = { Text("Email Address") },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                            focusedBorderColor = Color(0xFF4F8CFF),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedLabelColor = Color(0xFF4F8CFF),
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("email_register_email_input")
                                    )

                                    val isNameValid = nameInput.isEmpty() || nameInput.trim().length >= 2
                                    val isEmailValid = emailInput.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches()
                                    
                                    val hasMinLength = passwordInput.length >= 8
                                    val hasUppercase = passwordInput.any { it.isUpperCase() }
                                    val hasLowercase = passwordInput.any { it.isLowerCase() }
                                    val hasDigit = passwordInput.any { it.isDigit() }
                                    val hasSpecialChar = passwordInput.any { !it.isLetterOrDigit() }
                                    
                                    val passwordStrengthScore = listOf(hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecialChar).count { it }
                                    val isPasswordValid = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
                                    val isFormValid = nameInput.trim().length >= 2 && android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches() && isPasswordValid

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = passwordInput,
                                            onValueChange = { passwordInput = it },
                                            label = { Text("Password") },
                                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                                            trailingIcon = {
                                                IconButton(onClick = { showRegisterPassword = !showRegisterPassword }) {
                                                    Icon(
                                                        imageVector = if (showRegisterPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                        contentDescription = if (showRegisterPassword) "Hide password" else "Show password",
                                                        tint = Color.White.copy(alpha = 0.5f)
                                                    )
                                                }
                                            },
                                            visualTransformation = if (showRegisterPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                                focusedBorderColor = Color(0xFF4F8CFF),
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                                focusedLabelColor = Color(0xFF4F8CFF),
                                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("email_register_password_input")
                                        )

                                        if (passwordInput.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Strength: " + when (passwordStrengthScore) {
                                                            0, 1 -> "Weak"
                                                            2, 3 -> "Medium"
                                                            4 -> "Strong"
                                                            else -> "Very Strong"
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (passwordStrengthScore) {
                                                            0, 1 -> Color(0xFFFF5252)
                                                            2, 3 -> Color(0xFFFFB74D)
                                                            else -> Color(0xFF81C784)
                                                        }
                                                    )
                                                }
                                                
                                                LinearProgressIndicator(
                                                    progress = { passwordStrengthScore / 5f },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    color = when (passwordStrengthScore) {
                                                        0, 1 -> Color(0xFFFF5252)
                                                        2, 3 -> Color(0xFFFFB74D)
                                                        else -> Color(0xFF4CAF50)
                                                    },
                                                    trackColor = Color.White.copy(alpha = 0.1f)
                                                )
                                                
                                                Spacer(modifier = Modifier.height(2.dp))
                                                
                                                val reqs = listOf(
                                                    "At least 8 characters" to hasMinLength,
                                                    "At least one uppercase (A-Z)" to hasUppercase,
                                                    "At least one lowercase (a-z)" to hasLowercase,
                                                    "At least one number (0-9)" to hasDigit,
                                                    "At least one special character" to hasSpecialChar
                                                )
                                                
                                                reqs.forEach { (text, isMet) ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = if (isMet) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.25f),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = text,
                                                            fontSize = 10.sp,
                                                            color = if (isMet) Color(0xFF81C784) else Color.White.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        
                                        if (!isNameValid) {
                                            Text(
                                                text = "Name must be at least 2 characters long.",
                                                color = Color(0xFFFF5252),
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                            )
                                        }
                                        if (!isEmailValid) {
                                            Text(
                                                text = "Please enter a valid email address.",
                                                color = Color(0xFFFF5252),
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (isFormValid) {
                                                viewModel.registerWithEmail(
                                                    name = nameInput.trim(),
                                                    email = emailInput.trim(),
                                                    password = passwordInput,
                                                    onSuccess = {
                                                        Toast.makeText(context, "Registration Successful! Verification email sent.", Toast.LENGTH_LONG).show()
                                                        onLoginSuccess()
                                                    },
                                                    onError = { err ->
                                                        Toast.makeText(context, "Registration Failed: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            }
                                        },
                                        enabled = isFormValid,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4F8CFF),
                                            disabledContainerColor = Color(0xFF4F8CFF).copy(alpha = 0.2f),
                                            contentColor = Color.White,
                                            disabledContentColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("email_register_submit_button")
                                    ) {
                                        Text("Register & Verify Email", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Already have an account? ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                                        Text(
                                            text = "Login",
                                            color = Color(0xFF4F8CFF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.clickable { formState = AuthFormState.LOGIN_EMAIL }
                                        )
                                    }
                                }
                            }

                            AuthFormState.FORGOT_PASSWORD -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Reset Password",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "Enter your email address below, and we will dispatch a secure link to reset your password.",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )

                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        label = { Text("Your Email Address") },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                            focusedBorderColor = Color(0xFF4F8CFF),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedLabelColor = Color(0xFF4F8CFF),
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("email_reset_email_input")
                                    )

                                    Button(
                                        onClick = {
                                            if (emailInput.isBlank()) {
                                                Toast.makeText(context, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches()) {
                                                Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.sendPasswordResetEmail(
                                                    email = emailInput.trim(),
                                                    onSuccess = {
                                                        Toast.makeText(context, "Password reset email dispatched successfully!", Toast.LENGTH_LONG).show()
                                                        formState = AuthFormState.LOGIN_EMAIL
                                                    },
                                                    onError = { err ->
                                                        Toast.makeText(context, "Password Reset Failed: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F8CFF)),
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("email_reset_submit_button")
                                    ) {
                                        Text("Send Password Reset Link", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { formState = AuthFormState.LOGIN_EMAIL }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Back to Sign In", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM PART
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.clickable {
                            onNavigateToPrivacyPolicy()
                        }
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.4f))
                    )
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.clickable {
                            onNavigateToTermsConditions()
                        }
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.4f))
                    )
                    Text(
                        text = "App Version",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.clickable {
                            onNavigateToAppVersion()
                        }
                    )
                }

                Text(
                    text = "© 2026 Smart Explorer. All Rights Reserved.\nv1.4.0 • AI Explorer Core",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
