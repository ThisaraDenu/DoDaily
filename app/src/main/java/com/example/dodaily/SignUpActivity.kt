package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Apply edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views
        val signUpBtn = findViewById<MaterialButton>(R.id.btnSignUp2)
        val googleBtn = findViewById<MaterialButton>(R.id.btnGoogle)
        val facebookBtn = findViewById<MaterialButton>(R.id.btnFacebook2)
        val loginLink = findViewById<TextView>(R.id.loginLink2)

        // Sign Up Button → Navigate to Dashboard (replace with your main activity)
        signUpBtn.setOnClickListener {
            // TODO: Add input validation and real sign-up logic
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java) // replace with your real dashboard activity
            startActivity(intent)
            finish()
        }

        // Google Sign Up → Placeholder for Google sign-up
        googleBtn.setOnClickListener {
            Toast.makeText(this, "Google Sign Up clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement Google Sign-In with Firebase/Auth
        }

        // Facebook Sign Up → Placeholder for Facebook sign-up
        facebookBtn.setOnClickListener {
            Toast.makeText(this, "Facebook Sign Up clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement Facebook Login with SDK
        }

        // Already have account → Back to Login screen
        loginLink.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
