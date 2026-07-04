package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.VerificationScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent? = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                errorMessageState = "Google Sign In failed: " + (e.localizedMessage ?: "Unknown Error")
                isLoadingState = false
            }
        } else {
            isLoadingState = false
        }
    }

    private var isLoadingState by mutableStateOf(false)
    private var errorMessageState by mutableStateOf<String?>(null)

    private var loginScreenEmail by mutableStateOf("")
    private var loginScreenIsSignUp by mutableStateOf(false)
    private var showNoAccountDialog by mutableStateOf(false)
    private var noAccountEmail by mutableStateOf("")

    // Verification screen state details
    private var isVerificationScreen by mutableStateOf(false)
    private var emailState by mutableStateOf("")
    private var resendTimerSeconds by mutableStateOf(0)
    private var countdownJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in & email is verified (if they signed up with email)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // If they are an email provider user, verify email is verified
            val isEmailUser = currentUser.providerData.any { it.providerId == "password" }
            if (isEmailUser && !currentUser.isEmailVerified) {
                emailState = currentUser.email ?: ""
                isVerificationScreen = true
                startResendTimer()
            } else {
                navigateToMain()
                return
            }
        }

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(mood = "khush", darkTheme = false) {
                if (isVerificationScreen) {
                    VerificationScreen(
                        email = emailState,
                        resendTimerSeconds = resendTimerSeconds,
                        isLoading = isLoadingState,
                        errorMessage = errorMessageState,
                        onCheckVerification = { checkEmailVerificationStatus() },
                        onResendEmail = { resendVerificationEmail() },
                        onBackToLogin = {
                            auth.signOut()
                            isVerificationScreen = false
                            errorMessageState = null
                        }
                    )
                } else {
                    LoginScreen(
                        onEmailPasswordSubmit = { email, password, isSignUp, fullName ->
                            isLoadingState = true
                            errorMessageState = null
                            if (isSignUp) {
                                handleEmailSignUp(fullName, email, password)
                            } else {
                                handleEmailSignIn(email, password)
                            }
                        },
                        errorMessage = errorMessageState,
                        isLoading = isLoadingState,
                        initialEmail = loginScreenEmail,
                        initialIsSignUp = loginScreenIsSignUp,
                        onModeChange = { isSignUp ->
                            loginScreenIsSignUp = isSignUp
                            errorMessageState = null
                        }
                    )
                }
            }
        }
    }

    private fun startResendTimer() {
        resendTimerSeconds = 60
        countdownJob?.cancel()
        countdownJob = lifecycleScope.launch {
            while (resendTimerSeconds > 0) {
                delay(1000)
                resendTimerSeconds--
            }
        }
    }

    private fun handleEmailSignIn(email: String, javaPassword: String) {
        auth.signInWithEmailAndPassword(email, javaPassword)
            .addOnCompleteListener(this) { task ->
                isLoadingState = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        if (user.isEmailVerified) {
                            navigateToMain()
                        } else {
                            errorMessageState = "Pehle email verify karo! 📧"
                            emailState = email
                            isVerificationScreen = true
                            startResendTimer()
                        }
                    }
                } else {
                    val exception = task.exception
                    val errorMsg = exception?.message ?: exception?.localizedMessage ?: ""
                    val isUserNotFound = exception is FirebaseAuthInvalidUserException ||
                            errorMsg.contains("no user record", ignoreCase = true) ||
                            errorMsg.contains("user-not-found", ignoreCase = true)
                    
                    if (isUserNotFound) {
                        errorMessageState = "email not registered"
                    } else {
                        errorMessageState = exception?.localizedMessage ?: "Login failed. Check email/password."
                    }
                }
            }
    }

    private fun handleEmailSignUp(name: String, email: String, javaPassword: String) {
        auth.createUserWithEmailAndPassword(email, javaPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Send Firebase verification email
                        user.sendEmailVerification()

                        // Save name to Firestore: users/{userId}/profile map AND users/{userId}/profile sub-collection
                        val uid = user.uid
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val profile = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "loginMethod" to "Email"
                        )

                        db.collection("users").document(uid).set(
                            hashMapOf("profile" to profile),
                            com.google.firebase.firestore.SetOptions.merge()
                        )

                        db.collection("users").document(uid).collection("profile").document("info").set(
                            profile,
                            com.google.firebase.firestore.SetOptions.merge()
                        )

                        // Navigate user to Verification screen
                        emailState = email
                        isVerificationScreen = true
                        startResendTimer()
                    }
                    isLoadingState = false
                } else {
                    isLoadingState = false
                    errorMessageState = "Registration failed: " + (task.exception?.localizedMessage ?: "Password must be >= 6 characters")
                }
            }
    }

    private fun checkEmailVerificationStatus() {
        val user = auth.currentUser
        if (user == null) {
            errorMessageState = "Auth session expired. Dobara koshish karein."
            isVerificationScreen = false
            return
        }

        isLoadingState = true
        user.reload().addOnCompleteListener { task ->
            isLoadingState = false
            if (user.isEmailVerified) {
                navigateToMain()
            } else {
                errorMessageState = "Abhi tak verify nahi hua!"
            }
        }
    }

    private fun resendVerificationEmail() {
        val user = auth.currentUser
        if (user == null) {
            errorMessageState = "Auth session expired."
            isVerificationScreen = false
            return
        }

        isLoadingState = true
        user.sendEmailVerification().addOnCompleteListener { task ->
            isLoadingState = false
            if (task.isSuccessful) {
                errorMessageState = "Verification link firse bheja gaya! check karein 📧"
                startResendTimer()
            } else {
                errorMessageState = "Error: " + (task.exception?.localizedMessage ?: "Nahi bhej paye.")
            }
        }
    }

    private fun triggerGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1096486727523-vlttnla2qrkv2o4e9ljv6t2l7g7m6gp8.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                isLoadingState = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Check if name is already in Profile, otherwise default to Google Display Name
                        val uid = user.uid
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                            val exists = doc.get("profile.name") != null
                            if (!exists) {
                                val name = user.displayName ?: user.email?.substringBefore("@") ?: "User"
                                val profileMap = hashMapOf(
                                    "name" to name,
                                    "email" to (user.email ?: ""),
                                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                    "loginMethod" to "Google"
                                )
                                db.collection("users").document(uid).set(
                                    hashMapOf("profile" to profileMap),
                                    com.google.firebase.firestore.SetOptions.merge()
                                )
                            }
                        }
                    }
                    navigateToMain()
                } else {
                    errorMessageState = task.exception?.localizedMessage ?: "Firebase Sign In with Google failed"
                }
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
