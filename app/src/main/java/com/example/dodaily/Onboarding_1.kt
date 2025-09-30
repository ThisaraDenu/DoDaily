package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button

class Onboarding_1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding1)

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Buttons
        val skipBtn = findViewById<Button>(R.id.skip)
        val nextBtn = findViewById<Button>(R.id.next)

        // Skip → go to Login
        skipBtn.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java) // replace with your Login activity class name
            startActivity(intent)
            finish() // optional, closes onboarding
        }

        // Next → go to Onboarding 2
        nextBtn.setOnClickListener {
            val intent = Intent(this, Onboarding_2::class.java) // replace with your Onboarding_2 activity class
            startActivity(intent)
            finish() // optional
        }
    }
}
