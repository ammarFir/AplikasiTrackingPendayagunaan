package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yourname.aplikasitrackingpendayagunaan.model.LoginRequest
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // Kalau sudah login, langsung ke MainActivity
        if (sessionManager.isLoggedIn()) {
            goToMainActivity()
            return
        }

        val etEmail    = findViewById<TextInputEditText>(R.id.tvFullname)
        val etPassword = findViewById<TextInputEditText>(R.id.emailEditPassword)
        val btnLogin   = findViewById<MaterialButton>(R.id.btn_register)
        val tvSignUp   = findViewById<android.widget.TextView>(R.id.tvSignUp)

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text      = "Loading..."

            lifecycleScope.launch {
                try {
                    val response = ApiClient.apiService.login(LoginRequest(email, password))
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!.data!!
                        sessionManager.saveSession(
                            token  = data.token,
                            userId = data.user_id,
                            name   = data.name,
                            email  = data.email,
                            role   = data.role
                        )
                        goToMainActivity()
                    } else {
                        Toast.makeText(this@Login, response.body()?.message ?: "Login gagal", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Login, "Koneksi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnLogin.isEnabled = true
                    btnLogin.text      = "Masuk"
                }
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}