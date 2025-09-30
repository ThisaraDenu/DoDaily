package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class LogInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        // Edge-to-edge handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views
        val loginBtn = findViewById<MaterialButton>(R.id.btnLogin)
        val googleBtn = findViewById<MaterialButton>(R.id.btnGoogle)
        val facebookBtn = findViewById<MaterialButton>(R.id.btnFacebook3)
        val signUpLink = findViewById<TextView>(R.id.signUpLink)

        // Log In Button → Navigate to Dashboard (replace with your dashboard activity)
        loginBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java) // 🔹 replace with your activity
            startActivity(intent)
            finish()
        }

        // Google Button → Placeholder for Google Sign-In
        googleBtn.setOnClickListener {
            // TODO: Implement Google Sign-In
            // For now just navigate to dashboard or show a toast
            val intent = Intent(this,  HomeActivity::class.java)
            startActivity(intent)
        }

        // Facebook Button → Placeholder for Facebook Sign-In
        facebookBtn.setOnClickListener {
            // TODO: Implement Facebook Login
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Sign Up Link → Navigate to Sign Up Activity
        signUpLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
