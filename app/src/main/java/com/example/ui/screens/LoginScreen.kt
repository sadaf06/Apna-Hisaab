package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.brandBrush
import com.example.ui.theme.Palette
import com.example.ui.theme.Dimens
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onEmailPasswordSubmit: (String, String, Boolean, String) -> Unit, // email, password, isSignUp, fullName
    errorMessage: String?,
    isLoading: Boolean,
    initialEmail: String = "",
    initialIsSignUp: Boolean = false,
    onModeChange: ((Boolean) -> Unit)? = null
) {
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var isSignUp by remember(initialIsSignUp) { mutableStateOf(initialIsSignUp) }
    
    var localError by remember { mutableStateOf<String?>(null) }
    
    var isNameFocused by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isConfirmPasswordFocused by remember { mutableStateOf(false) }

    val context = LocalContext.current
    
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Palette.TextPrimary,
        unfocusedTextColor = Palette.TextPrimary,
        cursorColor = Palette.Teal,
        focusedBorderColor = Palette.Teal,
        unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
        focusedLabelColor = Palette.Teal,
        unfocusedLabelColor = Palette.TextSecondary,
        focusedLeadingIconColor = Palette.Teal,
        unfocusedLeadingIconColor = Palette.TextSecondary,
        focusedContainerColor = Color.White.copy(alpha = 0.06f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
    )
    
    val scrollState = rememberScrollState()
    
    val mintVioletBrush = Brush.horizontalGradient(listOf(Palette.Teal, Palette.PurpleDeep))

    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // App Logo: floating glass circle (white 12% fill + blur + 2dp white 22% border + inset top highlight), 88.dp size, ✨ emoji center
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(2.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                                    startY = 0f,
                                    endY = size.height * 0.35f
                                ),
                                radius = size.minDimension / 2f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App name "Apna Hisaab" — 38.sp, FontWeight.Black, Palette.textPrimary, -1.5.sp letterSpacing
                Text(
                    text = "Apna Hisaab",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Palette.TextPrimary,
                        letterSpacing = (-1.5).sp
                    ),
                    textAlign = TextAlign.Center
                )

                // Tagline: "Ek line likho, baaki hum samjhe" — 15.sp, FontWeight.SemiBold, Palette.textSecondary
                Text(
                    text = "Ek line likho, baaki hum samjhe",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.TextSecondary
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                )

                // Input fields card wrapped in GlassCard
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Text(
                        text = if (isSignUp) "Naya Account Banao 👋" else "Hisaab Shuru Karo! 🔑",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Palette.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Optional fields for Sign Up
                    AnimatedVisibility(
                        visible = isSignUp,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            // Name input
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { 
                                    Text(
                                        "Apna Poora Naam likhein",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    ) 
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (isNameFocused) Palette.Teal else Palette.TextSecondary) },
                                colors = textFieldColors,
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isNameFocused = it.isFocused }
                                    .then(
                                        if (isNameFocused) {
                                            Modifier.shadow(
                                                elevation = 8.dp,
                                                shape = RoundedCornerShape(16.dp),
                                                ambientColor = Palette.Teal.copy(alpha = 0.15f),
                                                spotColor = Palette.Teal.copy(alpha = 0.15f)
                                            )
                                        } else Modifier
                                    )
                                    .testTag("name_input")
                            )
                        }
                    }

                    // Email input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { 
                            Text(
                                "Email id Likho",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) 
                        },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (isEmailFocused) Palette.Teal else Palette.TextSecondary) },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isEmailFocused = it.isFocused }
                            .then(
                                if (isEmailFocused) {
                                    Modifier.shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = Palette.Teal.copy(alpha = 0.15f),
                                        spotColor = Palette.Teal.copy(alpha = 0.15f)
                                    )
                                } else Modifier
                            )
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password input
                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(
                                "Password (min 6 characters)",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = if (isPasswordFocused) Palette.Teal else Palette.TextSecondary) },
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(
                                    text = if (passwordVisible) "Chhupao" else "Dikhao",
                                    color = Palette.Teal,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isPasswordFocused = it.isFocused }
                            .then(
                                if (isPasswordFocused) {
                                    Modifier.shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = Palette.Teal.copy(alpha = 0.15f),
                                        spotColor = Palette.Teal.copy(alpha = 0.15f)
                                    )
                                } else Modifier
                            )
                            .testTag("password_input")
                    )

                    // Optional fields for Confirm Password
                    AnimatedVisibility(
                        visible = isSignUp,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            var confirmPasswordVisible by remember { mutableStateOf(false) }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = {
                                    Text(
                                        "Confirm Password",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm password", tint = if (isConfirmPasswordFocused) Palette.Teal else Palette.TextSecondary) },
                                trailingIcon = {
                                    TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Text(
                                            text = if (confirmPasswordVisible) "Chhupao" else "Dikhao",
                                            color = Palette.Teal,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = textFieldColors,
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isConfirmPasswordFocused = it.isFocused }
                                    .then(
                                        if (isConfirmPasswordFocused) {
                                            Modifier.shadow(
                                                elevation = 8.dp,
                                                shape = RoundedCornerShape(16.dp),
                                                ambientColor = Palette.Teal.copy(alpha = 0.15f),
                                                spotColor = Palette.Teal.copy(alpha = 0.15f)
                                            )
                                        } else Modifier
                                    )
                                    .testTag("confirm_password_input")
                            )
                        }
                    }

                    // "Password bhool gaye? 🔄" link
                    if (!isSignUp) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Password bhool gaye? 🔄",
                            color = Palette.Teal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable {
                                    if (email.isBlank()) {
                                        localError = "Pehle apna Email id Likho! 📧"
                                    } else {
                                        FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    com.example.utils.PremiumToast.show(
                                                        context,
                                                        "Password reset link bhej diya! Spam folder bhi check karna 😊📧",
                                                        longDuration = true
                                                    )
                                                } else {
                                                    localError = task.exception?.localizedMessage ?: "Password reset failed!"
                                                }
                                            }
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val activeError = localError ?: errorMessage
                    if (!activeError.isNullOrEmpty() && activeError != "email not registered") {
                        Text(
                            text = activeError,
                            color = Palette.Danger,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Submit button: full width, 18.dp radius, brandBrush background (mint→violet gradient), white text, FontWeight.Bold, 16.sp, shadow (mint 30% alpha, 8.dp blur, 24.dp spread)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = Palette.Teal.copy(alpha = 0.30f),
                                spotColor = Palette.Teal.copy(alpha = 0.30f)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .alpha(if (isLoading) 0.6f else 1f)
                            .background(mintVioletBrush)
                            .clickable(enabled = !isLoading) {
                                localError = null
                                if (email.isBlank() || password.isBlank()) {
                                    localError = "Email aur password likhna zaroori hai!"
                                    return@clickable
                                }
                                if (password.length < 6) {
                                    localError = "Password kam se kam 6 characters ka hona chahiye!"
                                    return@clickable
                                }
                                if (isSignUp) {
                                    if (fullName.isBlank()) {
                                        localError = "Poora naam likhna zaroori hai!"
                                        return@clickable
                                    }
                                    if (password != confirmPassword) {
                                        localError = "Dono passwords aapas me match nahi ho rahe!"
                                        return@clickable
                                    }
                                }
                                onEmailPasswordSubmit(email.trim(), password.trim(), isSignUp, fullName.trim())
                            }
                            .testTag("auth_submit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSignUp) "Account Banao 🚀" else "Haan, Shuru Karo! 🔑",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom toggle: "Naye ho? Account banao"
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.lg)
                        .clickable {
                            isSignUp = !isSignUp
                            localError = null
                            onModeChange?.invoke(isSignUp)
                        }
                        .testTag("toggle_mode_button")
                ) {
                    Text(
                        text = if (isSignUp) "Pehle se account hai? " else "Naye ho? ",
                        color = Palette.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isSignUp) "Login karo" else "Account banao",
                        color = Palette.Teal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            // UNREGISTERED EMAIL DIALOG
            val showUnregisteredDialog = errorMessage == "email not registered"
            if (showUnregisteredDialog) {
                // Dark scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.50f))
                        .clickable(enabled = false) {}
                )
                
                // Dialog structure
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White.copy(alpha = 0.11f))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
                            .drawBehind {
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                                        startY = 0f,
                                        endY = 15.dp.toPx()
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx())
                                )
                            }
                            .padding(start = 26.dp, end = 26.dp, top = 26.dp, bottom = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "📧",
                                fontSize = 52.sp,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "Email Register Nahi Hai",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Palette.TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "Ye email pehle se registered nahi hai. Naya account banana chahte ho?",
                                fontSize = 14.sp,
                                color = Palette.TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 21.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = Palette.Teal.copy(alpha = 0.30f),
                                        spotColor = Palette.Teal.copy(alpha = 0.30f)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(brandBrush)
                                    .clickable {
                                        isSignUp = true
                                        onModeChange?.invoke(true)
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Haan, Account Banao 🚀",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        onModeChange?.invoke(false)
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nahi, Wapas Jao",
                                    color = Palette.TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    email: String,
    resendTimerSeconds: Int,
    isLoading: Boolean,
    errorMessage: String?,
    onCheckVerification: () -> Unit,
    onResendEmail: () -> Unit,
    onBackToLogin: () -> Unit
) {
    AppBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Palette.Purple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📧", fontSize = 36.sp)
                    }

                    Text(
                        text = "Email Verify Karein! 📬",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Palette.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Humne abhi abhi ek verification link bheja hai:\n\n$email\n\nKripya apna inbox check karein aur link pe click karke verify karein. 😊",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.WarningBg)
                            .border(1.dp, Palette.Warning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "📌 Email nahi mili? \nSpam/Junk folder zaroor check karo!\nGmail mein 'Promotions' tab bhi \ndekh lena 😊",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 18.sp
                            ),
                            color = Palette.Warning,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (!errorMessage.isNullOrEmpty()) {
                        Text(
                            text = errorMessage,
                            color = if (errorMessage.contains("firse", ignoreCase = true) || errorMessage.contains("sent", ignoreCase = true)) Palette.Success else Palette.Danger,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = Palette.Success.copy(alpha = 0.3f),
                                spotColor = Palette.Success.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(Palette.Success)
                            .clickable(enabled = !isLoading) { onCheckVerification() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Maine Verify Kar Liya ✅",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (resendTimerSeconds > 0) {
                            Text(
                                text = "Resend link (${resendTimerSeconds}s baad)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.TextTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Link nahi mila? ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.TextSecondary
                            )
                            Text(
                                text = "Dobara bhejein 🔄",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Palette.Purple,
                                modifier = Modifier
                                    .clickable { onResendEmail() }
                                    .padding(2.dp)
                            )
                        }
                    }

                    Divider(color = Palette.BorderSoft)

                    TextButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.testTag("back_to_login_button")
                    ) {
                        Text(
                            text = "Wapas Login Screen Pe Jao 🔙",
                            color = Palette.Purple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
