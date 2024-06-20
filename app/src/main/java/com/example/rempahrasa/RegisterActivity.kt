package com.example.rempahrasa

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class RegisterActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        val tvFirstNameError = findViewById<TextView>(R.id.tvFirstNameError)
        val tvLastNameError = findViewById<TextView>(R.id.tvLastNameError)
        val tvEmailError = findViewById<TextView>(R.id.tvEmailError)
        val tvPasswordError = findViewById<TextView>(R.id.tvPasswordError)
        val tvConfirmPasswordError = findViewById<TextView>(R.id.tvConfirmPasswordError)

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            var valid = true

            if (firstName.isEmpty()) {
                tvFirstNameError.text = "First name is required"
                tvFirstNameError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvFirstNameError.visibility = TextView.GONE
            }

            if (lastName.isEmpty()) {
                tvLastNameError.text = "Last name is required"
                tvLastNameError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvLastNameError.visibility = TextView.GONE
            }

            if (email.isEmpty()) {
                tvEmailError.text = "Email is required"
                tvEmailError.visibility = TextView.VISIBLE
                valid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tvEmailError.text = "Invalid email format"
                tvEmailError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvEmailError.visibility = TextView.GONE
            }

            if (password.isEmpty()) {
                tvPasswordError.text = "Password is required"
                tvPasswordError.visibility = TextView.VISIBLE
                valid = false
            } else if (password.length < 8 || !password.matches(".*\\d.*".toRegex()) && !password.matches(".*[!@#\$%^&*()].*".toRegex())) {
                tvPasswordError.text = "Password must be at least 8 characters long and contain a number or special symbol"
                tvPasswordError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvPasswordError.visibility = TextView.GONE
            }

            if (confirmPassword.isEmpty()) {
                tvConfirmPasswordError.text = "Confirm Password is required"
                tvConfirmPasswordError.visibility = TextView.VISIBLE
                valid = false
            } else if (password != confirmPassword) {
                tvConfirmPasswordError.text = "Passwords do not match"
                tvConfirmPasswordError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvConfirmPasswordError.visibility = TextView.GONE
            }

            if (valid) {
                Log.d("RegisterActivity", "Starting registration")
                hideKeyboard()
                registerUser(firstName, lastName, email, password, selectedImageUri)
            }
        }

        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
        }
    }

    private fun uriToFile(uri: Uri, contentResolver: ContentResolver): File? {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val mimeType = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))

        if (mimeType !in listOf("jpeg", "jpg", "png")) {
            runOnUiThread {
                Toast.makeText(this, "Only JPEG, JPG, PNG files are allowed", Toast.LENGTH_LONG).show()
            }
            return null
        }

        val file = File(cacheDir, "upload_image.$mimeType")
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var length: Int
        var totalBytes: Long = 0

        while (inputStream?.read(buffer).also { length = it ?: -1 } != -1) {
            outputStream.write(buffer, 0, length)
            totalBytes += length
            if (totalBytes > 5 * 1024 * 1024) {
                runOnUiThread {
                    Toast.makeText(this, "File size must be less than 5MB", Toast.LENGTH_LONG).show()
                }
                inputStream?.close()
                outputStream.close()
                return null
            }
        }

        inputStream?.close()
        outputStream.close()
        return file
    }

    private fun registerUser(firstName: String, lastName: String, email: String, password: String, imageUri: Uri?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imagePart = imageUri?.let {
                    val file = uriToFile(it, contentResolver) ?: return@launch
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                }

                val firstNameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), firstName)
                val lastNameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), lastName)
                val emailBody = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
                val passwordBody = RequestBody.create("text/plain".toMediaTypeOrNull(), password)

                val response: Response<RegisterResponse> = RetrofitInstance.api.register(
                    firstNameBody, lastNameBody, emailBody, passwordBody, imagePart
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val registerResponse = response.body()
                        Log.d("RegisterActivity", "Response: $registerResponse")
                        if (registerResponse?.success == true) {
                            Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = when (registerResponse?.message) {
                                is String -> registerResponse.message
                                is Map<*, *> -> registerResponse.message.toString()
                                else -> "Registration failed"
                            }
                            Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("RegisterActivity", "Response error: ${response.errorBody()?.string()}")
                        Toast.makeText(this@RegisterActivity, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Registration failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
