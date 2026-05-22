package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yourname.aplikasitrackingpendayagunaan.model.RegisterRequest
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import kotlinx.coroutines.launch

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername        = findViewById<TextInputEditText>(R.id.tvUsername)
        val etFullname        = findViewById<TextInputEditText>(R.id.tvFullname)
        val etEmail           = findViewById<TextInputEditText>(R.id.tvEmail)
        val etPassword        = findViewById<TextInputEditText>(R.id.tvPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.tvConfirmPassword)
        val btnRegister       = findViewById<MaterialButton>(R.id.btn_register)
        val tvLogin           = findViewById<android.widget.TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val name            = etFullname.text.toString().trim()
            val email           = etEmail.text.toString().trim()
            val password        = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validasi
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            btnRegister.text      = "Loading..."

            lifecycleScope.launch {
                try {
                    val response = ApiClient.apiService.register(
                        RegisterRequest(name = name, email = email, password = password, phone = "")
                    )
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@Register, "Registrasi berhasil! Silakan login", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Register, Login::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@Register, response.body()?.message ?: "Registrasi gagal", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Register, "Koneksi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnRegister.isEnabled = true
                    btnRegister.text      = "Daftar"
                }
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}