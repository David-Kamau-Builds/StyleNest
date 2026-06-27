package com.app.stylenest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.stylenest.databinding.ActivityLoginBinding
import com.app.stylenest.data.DatabaseHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    // private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var dbHelper: DatabaseHelper

    // private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    //     val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    //     handleSignInResult(task)
    // }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Configure Google Sign-In
        // val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        //     .requestEmail()
        //     .requestProfile()
        //     .requestIdToken("819759054088-h1tdj0ua79fnk4mljv4nm34gk1n4ea2b.apps.googleusercontent.com")
        //     .build()
        //     
        // googleSignInClient = GoogleSignIn.getClient(this, gso)

        // binding.btnGoogleSignIn.setOnClickListener {
        //     val signInIntent = googleSignInClient.signInIntent
        //     signInLauncher.launch(signInIntent)
        // }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT name, avatar_url, two_factor_enabled FROM users WHERE email=? AND password=?", arrayOf(email, password))
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0)
                val avatar = cursor.getString(1)
                val is2FaEnabled = cursor.getInt(2) == 1
                cursor.close()

                if (is2FaEnabled) {
                    val executor: Executor = ContextCompat.getMainExecutor(this@LoginActivity)
                    val biometricPrompt = BiometricPrompt(this@LoginActivity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                Toast.makeText(this@LoginActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                completeLogin(name, email, avatar)
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                            }
                        })

                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Biometric login for StyleNest")
                        .setSubtitle("Log in using your biometric credential")
                        .setNegativeButtonText("Cancel")
                        .build()

                    biometricPrompt.authenticate(promptInfo)
                } else {
                    completeLogin(name, email, avatar)
                }
            } else {
                cursor.close()
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSignUpLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun completeLogin(name: String, email: String, avatar: String?) {
        // Merge any guest cart/wishlist items into the user's account before saving session
        val repository = com.app.stylenest.data.StyleNestRepository(this)
        repository.mergeGuestDataToUser(email)

        val sharedPrefs = getSharedPreferences("stylenest_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_avatar", avatar ?: "")
            putBoolean("is_logged_in", true)
            apply()
        }
        Toast.makeText(this, "Welcome back, $name", Toast.LENGTH_SHORT).show()
        finish()
    }

    /*
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account.email ?: ""
            val name = account.displayName ?: ""
            val avatarUrl = account.photoUrl?.toString() ?: ""

            if (email.isEmpty()) {
                Toast.makeText(this, "Google Sign In failed: No email provided", Toast.LENGTH_SHORT).show()
                return
            }

            val db = dbHelper.writableDatabase
            val cursor = db.rawQuery("SELECT two_factor_enabled FROM users WHERE email=?", arrayOf(email))
            
            var is2FaEnabled = false
            if (cursor.moveToFirst()) {
                is2FaEnabled = cursor.getInt(0) == 1
            } else {
                // New Google user, insert into DB
                val values = android.content.ContentValues().apply {
                    put("name", name)
                    put("email", email)
                    put("password", "") // No standard password
                    put("avatar_url", avatarUrl)
                    put("two_factor_enabled", 0)
                }
                db.insert("users", null, values)
            }
            cursor.close()

            if (is2FaEnabled) {
                val executor: Executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            Toast.makeText(this@LoginActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                        }
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            completeLogin(name, email, avatarUrl)
                        }
                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for StyleNest")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Cancel")
                    .build()
                biometricPrompt.authenticate(promptInfo)
            } else {
                completeLogin(name, email, avatarUrl)
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            Toast.makeText(this, "Google sign in failed (code: ${e.statusCode})", Toast.LENGTH_SHORT).show()
        }
    }
    */
}
