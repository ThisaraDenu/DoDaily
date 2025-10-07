package com.example.dodaily

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences for authentication
        sharedPreferences = getSharedPreferences("user_auth", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in
            if (isUserLoggedIn()) {
                // User is logged in, go directly to HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // User is not logged in, show onboarding
                val intent = Intent(this, Onboarding_1::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000) // 2000 ms = 2 sec
    }
    
    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("user_logged_in", false)
    }
}