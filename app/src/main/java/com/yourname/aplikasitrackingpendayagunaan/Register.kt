package com.yourname.aplikasitrackingpendayagunaan

// ============================================================
// IMPORT LIBRARY
// ============================================================
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

// ============================================================
// REGISTER ACTIVITY - HALAMAN PENDAFTARAN AKUN
// ============================================================
// Fungsi:
// 1. Menampilkan form pendaftaran (username, fullname, email, password, confirm password)
// 2. Validasi input user (tidak boleh kosong, password harus cocok, minimal 6 karakter)
// 3. Mengirim request registrasi ke server via API
// 4. Jika berhasil, pindah ke halaman login
// 5. Navigasi ke halaman login jika sudah punya akun
// ============================================================
class Register : AppCompatActivity() {

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    // Dipanggil saat activity pertama kali dibuat.
    // Di sini kita inisialisasi semua komponen UI.
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghubungkan layout XML activity_register ke activity ini
        setContentView(R.layout.activity_register)

        // ============================================================
        // INISIALISASI KOMPONEN UI
        // ============================================================
        // Mencari semua komponen dari layout XML
        // ============================================================

        // Input username
        val etUsername = findViewById<TextInputEditText>(R.id.tvUsername)

        // Input nama lengkap
        val etFullname = findViewById<TextInputEditText>(R.id.tvFullname)

        // Input email
        val etEmail = findViewById<TextInputEditText>(R.id.tvEmail)

        // Input password
        val etPassword = findViewById<TextInputEditText>(R.id.tvPassword)

        // Input konfirmasi password
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.tvConfirmPassword)

        // Tombol daftar / register
        val btnRegister = findViewById<MaterialButton>(R.id.btn_register)

        // TextView untuk navigasi ke halaman login (jika sudah punya akun)
        val tvLogin = findViewById<android.widget.TextView>(R.id.tvLogin)

        // ============================================================
        // EVENT CLICK: TOMBOL DAFTAR / REGISTER
        // ============================================================
        // Saat tombol daftar ditekan:
        // 1. Ambil input dari semua field
        // 2. Validasi input (tidak boleh kosong, password cocok, minimal 6 karakter)
        // 3. Kirim request registrasi ke server via API
        // 4. Jika berhasil, tampilkan pesan sukses dan pindah ke halaman login
        // 5. Jika gagal, tampilkan pesan error
        // ============================================================
        btnRegister.setOnClickListener {
            // Ambil input user dan bersihkan spasi di awal/akhir
            val name = etFullname.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // ============================================================
            // VALIDASI INPUT - FIELD TIDAK BOLEH KOSONG
            // ============================================================
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                // Tampilkan pesan error ke user
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Hentikan proses registrasi
            }

            // ============================================================
            // VALIDASI INPUT - PASSWORD HARUS COCOK
            // ============================================================
            if (password != confirmPassword) {
                // Tampilkan pesan error jika password dan konfirmasi tidak sama
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Hentikan proses registrasi
            }

            // ============================================================
            // VALIDASI INPUT - PASSWORD MINIMAL 6 KARAKTER
            // ============================================================
            if (password.length < 6) {
                // Tampilkan pesan error jika password kurang dari 6 karakter
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Hentikan proses registrasi
            }

            // ============================================================
            // PROSES REGISTRASI KE SERVER
            // ============================================================

            // Nonaktifkan tombol daftar agar user tidak bisa klik berulang kali
            btnRegister.isEnabled = false

            // Ubah teks tombol menjadi "Loading..." sebagai indikator proses
            btnRegister.text = "Loading..."

            // ============================================================
            // PANGGIL API REGISTER MENGGUNAKAN COROUTINE
            // ============================================================
            // lifecycleScope.launch: menjalankan kode di background thread
            // tanpa mengganggu UI thread.
            // ============================================================
            lifecycleScope.launch {
                try {
                    // ============================================================
                    // KIRIM REQUEST REGISTER KE SERVER
                    // ============================================================
                    // Membuat objek RegisterRequest yang berisi:
                    // - name: nama lengkap user
                    // - email: alamat email user
                    // - password: password user
                    // - phone: nomor telepon (dikirim string kosong, bisa diisi nanti)
                    // ============================================================
                    val response = ApiClient.apiService.register(
                        RegisterRequest(
                            name = name,
                            email = email,
                            password = password,
                            phone = ""  // Phone dikosongkan, user bisa update nanti di profile
                        )
                    )

                    // ============================================================
                    // PROSES RESPONSE DARI SERVER
                    // ============================================================

                    // Cek apakah response sukses (status code 200) dan data valid
                    if (response.isSuccessful && response.body()?.success == true) {
                        // ============================================================
                        // REGISTRASI BERHASIL
                        // ============================================================
                        // Tampilkan pesan sukses ke user
                        Toast.makeText(
                            this@Register,
                            "Registrasi berhasil! Silakan login",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Pindah ke halaman Login agar user bisa login dengan akun baru
                        val intent = Intent(this@Register, Login::class.java)
                        startActivity(intent)

                        // Tutup activity ini agar user tidak bisa kembali ke halaman register
                        // dengan menekan tombol back
                        finish()

                    } else {
                        // ============================================================
                        // REGISTRASI GAGAL
                        // ============================================================
                        // Tampilkan pesan error dari server
                        // Jika pesan null, tampilkan "Registrasi gagal" sebagai default
                        Toast.makeText(
                            this@Register,
                            response.body()?.message ?: "Registrasi gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    // ============================================================
                    // ERROR KONEKSI / EXCEPTION
                    // ============================================================
                    // Terjadi jika:
                    // 1. Koneksi internet bermasalah
                    // 2. Server tidak merespons
                    // 3. URL API salah
                    // ============================================================
                    Toast.makeText(
                        this@Register,
                        "Koneksi gagal: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                } finally {
                    // ============================================================
                    // AKHIR PROSES (SELALU DIJALANKAN)
                    // ============================================================
                    // Kembalikan tombol daftar ke状态 semula
                    // Dijalankan baik registrasi berhasil, gagal, maupun error
                    // ============================================================

                    // Aktifkan kembali tombol daftar
                    btnRegister.isEnabled = true

                    // Kembalikan teks tombol menjadi "Daftar"
                    btnRegister.text = "Daftar"
                }
            }
        }

        // ============================================================
        // EVENT CLICK: TEXT "LOGIN" DI HALAMAN REGISTER
        // ============================================================
        // Saat user menekan teks "Login" (di halaman register),
        // pindah ke halaman Login (jika user sudah punya akun).
        // ============================================================
        tvLogin.setOnClickListener {
            // Membuat intent untuk pindah ke Login activity
            val intent = Intent(this, Login::class.java)

            // Menjalankan intent
            startActivity(intent)

            // Menutup activity ini agar tidak menumpuk di back stack
            finish()
        }
    }
}