package com.app.shoppy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.shoppy.databinding.ActivityRegisterBinding
import com.app.shoppy.ui.viewmodel.AuthState
import com.app.shoppy.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val authViewModel: AuthViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken("819759054088-h1tdj0ua79fnk4mljv4nm34gk1n4ea2b.apps.googleusercontent.com")
            .build()
            
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogleSignUp.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }

        binding.tvSignInLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
            if (!passwordPattern.matches(password)) {
                Toast.makeText(this, "Password must be at least 8 chars, with upper, lower, number, and special character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.register(name, email, password)
        }

        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        // show loading
                    }
                    is AuthState.Success -> {
                        Toast.makeText(this@RegisterActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Returns to the previous screen (e.g. Profile or wherever launched)
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            saveGoogleUserToPreferences(account)
            Toast.makeText(this, "Welcome, ${account.displayName}", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: ApiException) {
            e.printStackTrace()
            Toast.makeText(this, "Google sign in failed (code: ${e.statusCode})", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGoogleUserToPreferences(account: GoogleSignInAccount) {
        val sharedPrefs = getSharedPreferences("shoppy_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("user_name", account.displayName ?: "")
            putString("user_email", account.email ?: "")
            putString("user_avatar", account.photoUrl?.toString() ?: "")
            putBoolean("is_logged_in", true)
            apply()
        }
    }
}
