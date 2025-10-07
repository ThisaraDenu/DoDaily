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

class LogInActivity : AppCompatActivity() {
    
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordInput: TextInputEditText
    private lateinit var forgotPassword: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogle: MaterialButton
    private lateinit var btnFacebook: MaterialButton
    private lateinit var signUpLink: TextView
    
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_auth", MODE_PRIVATE)
        
        initializeViews()
        setupClickListeners()
        
        // Check if user is already logged in
        checkAutoLogin()
    }
    
    private fun initializeViews() {
        emailLayout = findViewById(R.id.emailLayout)
        emailInput = findViewById(R.id.emailInput)
        passwordLayout = findViewById(R.id.passwordLayout)
        passwordInput = findViewById(R.id.passwordInput)
        forgotPassword = findViewById(R.id.forgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook3)
        signUpLink = findViewById(R.id.signUpLink)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            login()
        }
        
        forgotPassword.setOnClickListener {
            handleForgotPassword()
        }
        
        btnGoogle.setOnClickListener {
            loginWithGoogle()
        }
        
        btnFacebook.setOnClickListener {
            loginWithFacebook()
        }
        
        signUpLink.setOnClickListener {
            navigateToSignUp()
        }
    }
    
    private fun checkAutoLogin() {
        val isLoggedIn = sharedPreferences.getBoolean("user_logged_in", false)
        if (isLoggedIn) {
            // User is already logged in, go to home screen
            navigateToHome()
        }
    }
    
    private fun login() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        
        // Clear previous errors
        clearErrors()
        
        // Validate inputs
        if (!validateInputs(email, password)) {
            return
        }
        
        // Check credentials
        if (authenticateUser(email, password)) {
            // Login successful
            sharedPreferences.edit().apply {
                putBoolean("user_logged_in", true)
                putLong("last_login", System.currentTimeMillis())
                apply()
            }
            
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            // Login failed
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
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
        }
        
        return isValid
    }
    
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun clearErrors() {
        emailLayout.error = null
        passwordLayout.error = null
    }
    
    private fun authenticateUser(email: String, password: String): Boolean {
        val savedEmail = sharedPreferences.getString("user_email", "")
        val savedPassword = sharedPreferences.getString("user_password", "")
        
        return savedEmail == email && savedPassword == password
    }
    
    private fun handleForgotPassword() {
        val email = emailInput.text.toString().trim()
        
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if email exists
        if (userExists(email)) {
            Toast.makeText(this, "Password reset instructions sent to $email", Toast.LENGTH_LONG).show()
            // TODO: Implement actual password reset functionality
        } else {
            Toast.makeText(this, "No account found with this email", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun userExists(email: String): Boolean {
        val savedEmail = sharedPreferences.getString("user_email", "")
        return savedEmail == email
    }
    
    private fun loginWithGoogle() {
        Toast.makeText(this, "Google Login coming soon!", Toast.LENGTH_SHORT).show()
        // TODO: Implement Google Sign-In
    }
    
    private fun loginWithFacebook() {
        Toast.makeText(this, "Facebook Login coming soon!", Toast.LENGTH_SHORT).show()
        // TODO: Implement Facebook Sign-In
    }
    
    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}