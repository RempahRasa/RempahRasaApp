package com.example.rempahrasa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etName = findViewById<EditText>(R.id.etName)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        val tvEmailError = findViewById<TextView>(R.id.tvEmailError)
        val tvNameError = findViewById<TextView>(R.id.tvNameError)
        val tvPasswordError = findViewById<TextView>(R.id.tvPasswordError)
        val tvConfirmPasswordError = findViewById<TextView>(R.id.tvConfirmPasswordError)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val name = etName.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            var valid = true

            if (email.isEmpty()) {
                tvEmailError.text = "Email is required"
                tvEmailError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvEmailError.visibility = TextView.GONE
            }

            if (name.isEmpty()) {
                tvNameError.text = "Name is required"
                tvNameError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvNameError.visibility = TextView.GONE
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
                // Proceed with registration (e.g., API call)
                registerUser(email, name, password)
            }
        }

        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser(email: String, name: String, password: String) {
        val registerRequest = RegisterRequest(email, name, password)

        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<RegisterResponse> = RetrofitInstance.api.register(registerRequest)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val registerResponse = response.body()
                    if (registerResponse?.success == true) {
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_LONG).show()
                        // Optionally save token and navigate to another screen
                    } else {
                        Toast.makeText(this@RegisterActivity, registerResponse?.message ?: "Registration failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}