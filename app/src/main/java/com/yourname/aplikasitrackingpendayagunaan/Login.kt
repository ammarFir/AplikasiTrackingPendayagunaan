package com.yourname.aplikasitrackingpendayagunaan

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yourname.aplikasitrackingpendayagunaan.model.LoginRequest
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.launch

// ============================================================
// LOGIN ACTIVITY - HALAMAN LOGIN
// ============================================================
// Fungsi:
// 1. Menampilkan form login (email & password)
// 2. Validasi input user
// 3. Mengirim request login ke server via API
// 4. Menyimpan session jika login berhasil
// 5. Pindah ke halaman utama jika sudah login
// 6. Navigasi ke halaman register jika belum punya akun
// ============================================================
class Login : AppCompatActivity() {

    // ============================================================
    // DEKLARASI VARIABEL
    // ============================================================

    // SessionManager: menyimpan data session setelah login berhasil
    // Digunakan untuk mengecek status login dan menyimpan token
    private lateinit var sessionManager: SessionManager

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    // Dipanggil saat activity pertama kali dibuat.
    // Di sini kita inisialisasi semua komponen UI dan cek session.
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghubungkan layout XML activity_login ke activity ini
        setContentView(R.layout.activity_login)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // ============================================================
        // INISIALISASI SESSION MANAGER
        // ============================================================
        // SessionManager digunakan untuk:
        // 1. Menyimpan token setelah login
        // 2. Menyimpan data user (nama, email, role, avatar)
        // 3. Mengecek apakah user sudah login
        // 4. Menghapus session saat logout
        sessionManager = SessionManager(this)

        // ============================================================
        // CEK SESSION (AUTO LOGIN)
        // ============================================================
        // Jika user sudah login sebelumnya (token masih tersimpan),
        // maka langsung pindah ke halaman utama tanpa perlu login lagi.
        // ============================================================
        if (sessionManager.isLoggedIn()) {
            // Pindah ke MainActivity dan tutup halaman login
            goToMainActivity()
            return
        }

        // ============================================================
        // INISIALISASI KOMPONEN UI
        // ============================================================
        // Mencari semua komponen dari layout XML
        // ============================================================

        // TextInputEditText untuk input email
        // ID: tvFullname (dari layout activity_login)
        val etEmail = findViewById<TextInputEditText>(R.id.tvFullname)

        // TextInputEditText untuk input password
        // ID: emailEditPassword (dari layout activity_login)
        val etPassword = findViewById<TextInputEditText>(R.id.emailEditPassword)

        // Tombol login
        // ID: btn_register (dari layout activity_login)
        val btnLogin = findViewById<MaterialButton>(R.id.btn_register)

        // TextView untuk navigasi ke halaman register
        // ID: tvSignUp (dari layout activity_login)
        val tvSignUp = findViewById<android.widget.TextView>(R.id.tvSignUp)

        // ============================================================
        // EVENT CLICK: TOMBOL LOGIN
        // ============================================================
        // Saat tombol login ditekan:
        // 1. Ambil input email dan password
        // 2. Validasi input (tidak boleh kosong)
        // 3. Kirim request ke server via API
        // 4. Jika berhasil, simpan session dan pindah ke halaman utama
        // 5. Jika gagal, tampilkan pesan error
        // ============================================================
        btnLogin.setOnClickListener {
            // Ambil input user dan bersihkan spasi di awal/akhir
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // ============================================================
            // VALIDASI INPUT
            // ============================================================
            // Cek apakah email atau password kosong
            if (email.isEmpty() || password.isEmpty()) {
                // Tampilkan pesan error ke user
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Hentikan proses login
            }

            // ============================================================
            // PROSES LOGIN KE SERVER
            // ============================================================

            // Nonaktifkan tombol login agar user tidak bisa klik berulang kali
            btnLogin.isEnabled = false

            // Ubah teks tombol menjadi "Loading..." sebagai indikator proses
            btnLogin.text = "Loading..."

            // ============================================================
            // PANGGIL API LOGIN MENGGUNAKAN COROUTINE
            // ============================================================
            // lifecycleScope.launch: menjalankan kode di background thread
            // tanpa mengganggu UI thread.
            // ============================================================
            lifecycleScope.launch {
                try {
                    // ============================================================
                    // KIRIM REQUEST LOGIN KE SERVER
                    // ============================================================
                    // Membuat objek LoginRequest yang berisi email dan password
                    // ApiClient.apiService.login() akan mengirim request ke server
                    // ============================================================
                    val response = ApiClient.apiService.login(
                        LoginRequest(email, password)
                    )

                    // ============================================================
                    // PROSES RESPONSE DARI SERVER
                    // ============================================================

                    // Cek apakah response sukses (status code 200) dan data valid
                    if (response.isSuccessful && response.body()?.success == true) {
                        // ============================================================
                        // LOGIN BERHASIL
                        // ============================================================

                        // Ambil data user dari response body
                        val data = response.body()!!.data!!

                        // Simpan data session ke SessionManager
                        // Data yang disimpan: token, user_id, name, email, role, avatar
                        sessionManager.saveSession(
                            token = data.token,
                            userId = data.user_id,
                            name = data.name,
                            email = data.email,
                            role = data.role,
                            avatar = data.avatar
                        )

                        // Pindah ke halaman utama (MainActivity)
                        goToMainActivity()

                    } else {
                        // ============================================================
                        // LOGIN GAGAL
                        // ============================================================
                        // Tampilkan pesan error dari server
                        // Jika pesan null, tampilkan "Login gagal" sebagai default
                        Toast.makeText(
                            this@Login,
                            response.body()?.message ?: "Login gagal",
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
                        this@Login,
                        "Koneksi gagal: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                } finally {
                    // ============================================================
                    // AKHIR PROSES (SELALU DIJALANKAN)
                    // ============================================================
                    // Kembalikan tombol login ke状态 semula
                    // Dijalankan baik login berhasil, gagal, maupun error
                    // ============================================================

                    // Aktifkan kembali tombol login
                    btnLogin.isEnabled = true

                    // Kembalikan teks tombol menjadi "Masuk"
                    btnLogin.text = "Masuk"
                }
            }
        }

        // ============================================================
        // EVENT CLICK: TEXT "LOGIN" DI HALAMAN REGISTER
        // ============================================================
        // Saat user menekan teks "Login" (di halaman register),
        // pindah ke halaman Login (jika user sudah punya akun).
        // ============================================================
        tvSignUp.setOnClickListener {
            // Membuat intent untuk pindah ke Register activity
            val intent = Intent(this, Register::class.java)

            // Menjalankan intent
            startActivity(intent)

            // Menutup activity ini agar tidak menumpuk di back stack
            finish()
        }
    }

    // ============================================================
    // FUNGSI PINDAH KE HALAMAN UTAMA
    // ============================================================
    // Digunakan setelah login berhasil atau session masih aktif.
    // ============================================================
    private fun goToMainActivity() {
        // Membuat intent untuk pindah ke MainActivity
        val intent = Intent(this, MainActivity::class.java)

        // Menjalankan intent
        startActivity(intent)

        // Menutup activity ini (Login) agar user tidak bisa kembali ke halaman login
        // dengan menekan tombol back
        finish()
    }
}