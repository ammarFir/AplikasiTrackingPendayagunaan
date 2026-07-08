package com.yourname.aplikasitrackingpendayagunaan

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourname.aplikasitrackingpendayagunaan.adapter.TrackingAdapter
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================================
// MENU TRACKING - HALAMAN DAFTAR PROGRAM TRACKING
// ============================================================
// Fungsi:
// 1. Menampilkan daftar program pendayagunaan mustahiq dalam RecyclerView
// 2. Menampilkan summary cards (program aktif, selesai, total dana)
// 3. Navigasi ke halaman detail program saat item diklik
// 4. Tombol tambah program baru (untuk admin)
// 5. Bottom navigation (Home, Tracking, Laporan)
// 6. Avatar & profile di header
// ============================================================
class MenuTracking : AppCompatActivity() {

    // ============================================================
    // DEKLARASI VARIABEL
    // ============================================================

    // SessionManager: menyimpan data session user (token, nama, role, avatar)
    private lateinit var sessionManager: SessionManager

    // TrackingAdapter: adapter untuk RecyclerView daftar program
    private lateinit var adapter: TrackingAdapter

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    // Dipanggil saat activity pertama kali dibuat.
    // Inisialisasi komponen UI, setup RecyclerView, bottom nav, dan load data.
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghubungkan layout XML activity_menu_tracking ke activity ini
        setContentView(R.layout.activity_menu_tracking)

        // ============================================================
        // INISIALISASI SESSION MANAGER
        // ============================================================
        sessionManager = SessionManager(this)

        // ============================================================
        // SETUP RECYCLERVIEW (DAFTAR PROGRAM)
        // ============================================================
        // RecyclerView digunakan untuk menampilkan daftar program
        // secara vertikal dengan item layout yang reusable.
        // ============================================================

        // Mencari RecyclerView dari layout
        val rvPenerima = findViewById<RecyclerView>(R.id.rvPenerima)

        // Inisialisasi adapter dengan data kosong dan fungsi klik item
        // Ketika item diklik, akan pindah ke halaman DetailTracking
        adapter = TrackingAdapter(emptyList()) { program ->
            // Membuat intent untuk pindah ke DetailTracking
            val intent = Intent(this, DetailTracking::class.java)

            // Mengirim ID program sebagai extra
            intent.putExtra("program_id", program.id)

            // Menjalankan intent
            startActivity(intent)
        }

        // Mengatur layout manager: vertical (linear) dengan urutan dari atas ke bawah
        rvPenerima.layoutManager = LinearLayoutManager(this)

        // Menyambungkan adapter ke RecyclerView
        rvPenerima.adapter = adapter

        // ============================================================
        // SETUP BOTTOM NAVIGATION
        // ============================================================
        // BottomNavigationView untuk navigasi antar 3 halaman utama
        // ============================================================

        // Mencari komponen BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Menandai menu "Tracking" sebagai aktif (warna icon berubah hijau)
        bottomNav.selectedItemId = R.id.nav_tracking

        // Menambahkan listener untuk menangani klik pada setiap menu
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Menu Home: pindah ke MainActivity
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                // Menu Tracking: tetap di halaman ini (tidak pindah)
                R.id.nav_tracking -> true
                // Menu Laporan: pindah ke LaporanProgram
                R.id.nav_laporan -> {
                    startActivity(Intent(this, LaporanProgram::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // ============================================================
        // SETUP TOMBOL TAMBAH PROGRAM
        // ============================================================
        // Tombol "Tambah Penerima Baru" hanya tampil untuk admin.
        // Saat diklik, pindah ke halaman AddProgram.
        // ============================================================

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            // Pindah ke halaman tambah program
            startActivity(Intent(this, AddProgram::class.java))
        }

        // ============================================================
        // SETUP IMG PROFILE -> KE HALAMAN PROFILE
        // ============================================================
        // Avatar di header dapat diklik untuk pindah ke halaman profile.
        // ============================================================

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // ============================================================
        // LOAD DATA DARI API
        // ============================================================
        // 1. fetchTrackingList(): mengambil daftar program dari server
        // 2. loadUserProfile(): mengambil avatar user dari server
        // ============================================================

        fetchTrackingList()
        loadUserProfile()
    }

    // ============================================================
    // LIFECYCLE: onResume()
    // ============================================================
    // Dipanggil setiap kali activity muncul kembali ke layar.
    // Digunakan untuk refresh data jika ada perubahan.
    // ============================================================
    override fun onResume() {
        super.onResume()

        // Refresh avatar user (jika berubah di ProfileActivity)
        loadUserProfile()

        // Refresh daftar program (jika ada perubahan dari halaman lain)
        fetchTrackingList()
    }

    // ============================================================
    // FUNGSI LOAD AVATAR USER
    // ============================================================
    // Mengambil data profile user dari server dan menampilkan avatar
    // di ImageView header (imgProfile).
    // ============================================================
    private fun loadUserProfile() {
        // Ambil token dari session
        val token = sessionManager.getToken()
        if (token == null) return

        // Jalankan proses di background thread (IO)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Panggil API untuk mendapatkan data profile
                val response = ApiClient.apiService.getProfile(token)

                // Kembali ke Main Thread untuk update UI
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Ambil data user dari response
                        val user = response.body()?.data

                        user?.let {
                            // Cari ImageView avatar di layout
                            val imgProfile = findViewById<ImageView>(R.id.imgProfile)

                            // Cek apakah user memiliki avatar
                            if (!it.avatar.isNullOrEmpty()) {
                                // Proses path avatar
                                var fileName = it.avatar

                                // Jika path mengandung "/", ambil bagian setelah "/" terakhir
                                // Contoh: "uploads/avatar_1.jpg" -> "avatar_1.jpg"
                                if (fileName.contains("/")) {
                                    fileName = fileName.substringAfterLast("/")
                                }

                                // Buat URL lengkap
                                val avatarUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"

                                // Tampilkan avatar menggunakan Glide
                                Glide.with(this@MenuTracking)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.logokoceng)  // Default saat loading
                                    .error(R.drawable.logokoceng)        // Default jika error
                                    .circleCrop()                         // Bentuk lingkaran
                                    .into(imgProfile)
                            } else {
                                // Jika tidak ada avatar, pakai gambar default
                                imgProfile.setImageResource(R.drawable.logokoceng)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Jika terjadi error, print stack trace
                e.printStackTrace()
            }
        }
    }

    // ============================================================
    // FUNGSI FETCH TRACKING LIST (DAFTAR PROGRAM)
    // ============================================================
    // Mengambil daftar program tracking dari server via API.
    // Data yang diambil:
    // 1. Daftar program (untuk RecyclerView)
    // 2. Summary cards (program aktif, selesai, total dana)
    // ============================================================
    private fun fetchTrackingList() {
        // Ambil token dari session
        val token = sessionManager.getToken()
        if (token == null) return

        // Jalankan proses di background thread
        //
        lifecycleScope.launch {
            try {
                // ============================================================
                // PANGGIL API UNTUK MENDAPATKAN DAFTAR PROGRAM
                // ============================================================
                val response = ApiClient.apiService.getTrackingList(token)

                // ============================================================
                // PROSES RESPONSE DARI SERVER
                // ============================================================
                if (response.isSuccessful && response.body()?.success == true) {
                    // Ambil data dari response
                    val data = response.body()!!.data!!

                    // ============================================================
                    // UPDATE RECYCLERVIEW (DAFTAR PROGRAM)
                    // ============================================================
                    // Mengirim data program ke adapter agar ditampilkan
                    adapter.updateData(data.programs)

                    // ============================================================
                    // UPDATE SUMMARY CARDS
                    // ============================================================
                    // Program Aktif: jumlah program dengan status "AKTIF"
                    findViewById<TextView>(R.id.tvTotalPenerima).text =
                        data.summary.program_aktif.toString()

                    // Program Selesai: jumlah program dengan status "SELESAI"
                    findViewById<TextView>(R.id.tvProgramSelesai).text =
                        data.summary.program_selesai.toString()

                    // Total Dana: total dana yang sudah digunakan
                    findViewById<TextView>(R.id.tvTotalDana).text =
                        "Rp ${String.format("%,.0f", data.summary.total_dana_digunakan)}"

                    // ============================================================
                    // LOG UNTUK DEBUG (CEK DATA PROGRESS)
                    // ============================================================
                    // Menampilkan log di Logcat untuk memeriksa data progress
                    data.programs.forEach { program ->
                        android.util.Log.d(
                            "TRACKING_DATA",
                            "Program: ${program.nama_program}, " +
                                    "Progress: ${program.progress_persen}%, " +
                                    "Selesai: ${program.total_tahapan_selesai}/8"
                        )
                    }

                } else {
                    // Jika gagal, tampilkan pesan error
                    Toast.makeText(
                        this@MenuTracking,
                        "Gagal memuat data",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                // Jika terjadi error koneksi
                Toast.makeText(
                    this@MenuTracking,
                    "Koneksi gagal: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}