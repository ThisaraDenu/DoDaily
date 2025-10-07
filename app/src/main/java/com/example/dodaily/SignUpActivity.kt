package com.example.dodaily

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var btnSignUp: MaterialButton
    private lateinit var btnGoogle: MaterialButton
    private lateinit var btnFacebook: MaterialButton
    private lateinit var loginLink: TextView
    
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_auth", MODE_PRIVATE)
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        usernameLayout = findViewById(R.id.usernameLayout2)
        usernameInput = findViewById(R.id.usernameInput2)
        emailLayout = findViewById(R.id.emailLayout)
        emailInput = findViewById(R.id.emailInput)
        passwordLayout = findViewById(R.id.passwordLayout)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout2)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput2)
        btnSignUp = findViewById(R.id.btnSignUp2)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook2)
        loginLink = findViewById(R.id.loginLink2)
    }
    
    private fun setupClickListeners() {
        btnSignUp.setOnClickListener {
            signUp()
        }
        
        btnGoogle.setOnClickListener {
            signUpWithGoogle()
        }
        
        btnFacebook.setOnClickListener {
            signUpWithFacebook()
        }
        
        loginLink.setOnClickListener {
            navigateToLogin()
        }
    }
    
    private fun signUp() {
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()
        
        // Clear previous errors
        clearErrors()
        
        // Validate inputs
        if (!validateInputs(username, email, password, confirmPassword)) {
            return
        }
        
        // Check if user already exists
        if (userExists(email)) {
            emailLayout.error = "Email already registered"
            return
        }
        
        // Create user account
        if (createUser(username, email, password)) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            
            // Auto-login after successful signup
            loginUser(email, password)
        } else {
            Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        
        // Validate username
        if (username.isEmpty()) {
            usernameLayout.error = "Username is required"
            isValid = false
        } else if (username.length < 3) {
            usernameLayout.error = "Username must be at least 3 characters"
            isValid = false
        }
        
        // Validate email
        if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailLayout.error = "Please enter a valid email"
            isValid = false
        }
        
        // Validate password
        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        }
        
        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        }
        
        return isValid
    }
    
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun clearErrors() {
        usernameLayout.error = null
        emailLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null
    }
    
    private fun userExists(email: String): Boolean {
        val savedEmail = sharedPreferences.getString("user_email", "")
        return savedEmail == email
    }
    
    private fun createUser(username: String, email: String, password: String): Boolean {
        return try {
            sharedPreferences.edit().apply {
                putString("user_username", username)
                putString("user_email", email)
                putString("user_password", password) // In real app, hash this
                putBoolean("user_logged_in", false)
                putLong("user_created_at", System.currentTimeMillis())
                apply()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun loginUser(email: String, password: String) {
        sharedPreferences.edit().apply {
            putBoolean("user_logged_in", true)
            putLong("last_login", System.currentTimeMillis())
            apply()
        }
        
        // Navigate to home screen
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    
    private fun signUpWithGoogle() {
        Toast.makeText(this, "Google Sign Up coming soon!", Toast.LENGTH_SHORT).show()
        // TODO: Implement Google Sign-In
    }
    
    private fun signUpWithFacebook() {
        Toast.makeText(this, "Facebook Sign Up coming soon!", Toast.LENGTH_SHORT).show()
        // TODO: Implement Facebook Sign-In
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        finish()
    }
}