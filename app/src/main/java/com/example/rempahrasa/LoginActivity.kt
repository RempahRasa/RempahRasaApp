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

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        val tvEmailError = findViewById<TextView>(R.id.tvEmailError)
        val tvPasswordError = findViewById<TextView>(R.id.tvPasswordError)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            var valid = true

            if (email.isEmpty()) {
                tvEmailError.text = "Email is required"
                tvEmailError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvEmailError.visibility = TextView.GONE
            }

            if (password.isEmpty()) {
                tvPasswordError.text = "Password is required"
                tvPasswordError.visibility = TextView.VISIBLE
                valid = false
            } else {
                tvPasswordError.visibility = TextView.GONE
            }

            if (valid) {
                // Proceed with login (e.g., API call)

                loginUser(email, password)
            }
        }

        tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<LoginResponse> = RetrofitInstance.api.login(loginRequest)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()
                    if (loginResponse?.success == true) {
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_LONG).show()
                        // Optionally save token and navigate to another screen
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
