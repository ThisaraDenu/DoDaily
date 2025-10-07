package com.example.dodaily

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileActivity : AppCompatActivity() {
    
    companion object {
        private const val REQUEST_CAMERA = 1001
        private const val REQUEST_GALLERY = 1002
        private const val CAMERA_PERMISSION_REQUEST = 1003
        private const val STORAGE_PERMISSION_REQUEST = 1004
    }
    
    private lateinit var backButton: ImageButton
    private lateinit var profilePicture: ImageView
    private lateinit var editProfilePicture: ImageButton
    private lateinit var userName: TextView
    private lateinit var userRole: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    
    // Form elements
    private lateinit var nameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var genderSelector: LinearLayout
    private lateinit var genderText: TextView
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var saveProfileButton: Button
    
    private var currentPhotoPath: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("profile_data", MODE_PRIVATE)
        
        // Initialize views
        initializeViews()
        
        // Setup click listeners
        setupClickListeners()
        
        // Setup bottom navigation
        setupBottomNavigation()
        
        // Load user data
        loadUserData()
        loadProfileData()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        profilePicture = findViewById(R.id.profile_picture)
        editProfilePicture = findViewById(R.id.edit_profile_picture)
        userName = findViewById(R.id.user_name)
        userRole = findViewById(R.id.user_role)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        
        // Form elements
        nameInput = findViewById(R.id.name_input)
        usernameInput = findViewById(R.id.username_input)
        genderSelector = findViewById(R.id.gender_selector)
        genderText = findViewById(R.id.gender_text)
        phoneInput = findViewById(R.id.phone_input)
        emailInput = findViewById(R.id.email_input)
        saveProfileButton = findViewById(R.id.save_profile_button)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        editProfilePicture.setOnClickListener {
            showEditProfilePictureDialog()
        }
        
        // Gender selector
        genderSelector.setOnClickListener {
            showGenderDialog()
        }
        
        // Save profile button
        saveProfileButton.setOnClickListener {
            saveProfileData()
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_settings
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_mood -> {
                    val intent = Intent(this, MoodPageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_hydration -> {
                    val intent = Intent(this, HydrationSettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    // Already on settings page
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadUserData() {
        // Load user data from preferences or database
        // For now, using default values
        userName.text = "Thisara"
        userRole.text = "User"
        
        // Load saved profile picture
        loadProfilePicture()
    }
    
    private fun showEditProfilePictureDialog() {
        AlertDialog.Builder(this)
            .setTitle("Change Profile Picture")
            .setMessage("Choose an option to update your profile picture")
            .setPositiveButton("Camera") { _, _ ->
                openCamera()
            }
            .setNeutralButton("Gallery") { _, _ ->
                openGallery()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    
    
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        } else {
            startCameraIntent()
        }
    }
    
    private fun openGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), STORAGE_PERMISSION_REQUEST)
        } else {
            startGalleryIntent()
        }
    }
    
    private fun startCameraIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            // Use simple camera intent without file output for better compatibility
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startGalleryIntent() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }
    
    private fun createImageFile(): File? {
        val imageFileName = "profile_photo_${System.currentTimeMillis()}"
        val storageDir = getExternalFilesDir(null)
        return try {
            val image = File.createTempFile(imageFileName, ".jpg", storageDir)
            currentPhotoPath = image.absolutePath
            image
        } catch (ex: IOException) {
            null
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraIntent()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryIntent()
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    handleCameraResult(data)
                }
                REQUEST_GALLERY -> {
                    handleGalleryResult(data)
                }
            }
        }
    }
    
    private fun handleCameraResult(data: Intent?) {
        val bitmap = data?.extras?.get("data") as? Bitmap
        if (bitmap != null) {
            setProfilePicture(bitmap)
            saveProfilePicture(bitmap)
        } else {
            Toast.makeText(this, "Failed to capture image from camera", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleGalleryResult(data: Intent?) {
        data?.data?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    setProfilePicture(bitmap)
                    saveProfilePicture(bitmap)
                } else {
                    Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setProfilePicture(bitmap: Bitmap) {
        // Create a circular bitmap
        val circularBitmap = createCircularBitmap(bitmap)
        profilePicture.setImageBitmap(circularBitmap)
        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
    }
    
    private fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        
        val paint = android.graphics.Paint()
        val rect = android.graphics.Rect(0, 0, size, size)
        val rectF = android.graphics.RectF(rect)
        
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = android.graphics.Color.WHITE
        canvas.drawOval(rectF, paint)
        
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        
        return output
    }
    
    private fun saveProfilePicture(bitmap: Bitmap) {
        try {
            val file = File(getExternalFilesDir(null), "profile_picture.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving profile picture", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadProfilePicture() {
        try {
            val file = File(getExternalFilesDir(null), "profile_picture.jpg")
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    val circularBitmap = createCircularBitmap(bitmap)
                    profilePicture.setImageBitmap(circularBitmap)
                }
            }
        } catch (e: Exception) {
            // Use default profile picture if loading fails
        }
    }
    
    private fun loadProfileData() {
        val name = sharedPreferences.getString("name", "Thisara") ?: "Thisara"
        val username = sharedPreferences.getString("username", "thisara_") ?: "thisara_"
        val gender = sharedPreferences.getString("gender", "Male") ?: "Male"
        val phone = sharedPreferences.getString("phone", "+94 77 123 4567") ?: "+94 77 123 4567"
        val email = sharedPreferences.getString("email", "thisara@email.com") ?: "thisara@email.com"
        
        nameInput.setText(name)
        usernameInput.setText(username)
        genderText.text = gender
        phoneInput.setText(phone)
        emailInput.setText(email)
        
        // Update the displayed name
        userName.text = name
    }
    
    private fun saveProfileData() {
        val name = nameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val gender = genderText.text.toString()
        val phone = phoneInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        
        // Validate inputs
        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            return
        }
        
        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            return
        }
        
        if (phone.isEmpty()) {
            phoneInput.error = "Phone number is required"
            return
        }
        
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            return
        }
        
        // Save to SharedPreferences
        sharedPreferences.edit().apply {
            putString("name", name)
            putString("username", username)
            putString("gender", gender)
            putString("phone", phone)
            putString("email", email)
            apply()
        }
        
        // Update the displayed name
        userName.text = name
        
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showGenderDialog() {
        val genders = arrayOf("Male", "Female", "Other", "Prefer not to say")
        val currentGender = genderText.text.toString()
        val currentIndex = genders.indexOf(currentGender)
        
        AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setSingleChoiceItems(genders, currentIndex) { dialog, which ->
                genderText.text = genders[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
