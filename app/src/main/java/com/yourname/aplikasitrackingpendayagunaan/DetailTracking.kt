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
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================================
// DETAIL TRACKING - HALAMAN DETAIL PROGRAM
// ============================================================
// Fungsi:
// 1. Menampilkan informasi lengkap program dan mustahiq
// 2. Menampilkan 8 tahapan tracking dengan tanggal dan status
// 3. Dot hijau/abu untuk status selesai/belum
// 4. Tombol "Update Status" hanya untuk admin
// 5. Avatar user di header (klik ke profile)
// ============================================================
class DetailTracking : AppCompatActivity() {

    // ============================================================
    // DEKLARASI VARIABEL
    // ============================================================

    // SessionManager: menyimpan data session user (token, role, avatar)
    private lateinit var sessionManager: SessionManager

    // Tanggal mulai program (digunakan untuk tahapan pertama)
    private var tanggalMulaiProgram: String = ""

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    // Dipanggil saat activity pertama kali dibuat.
    // Inisialisasi komponen UI, cek role, load detail program.
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghubungkan layout XML activity_detail_tracking ke activity ini
        setContentView(R.layout.activity_detail_tracking)

        // ============================================================
        // INISIALISASI SESSION MANAGER
        // ============================================================
        sessionManager = SessionManager(this)

        // ============================================================
        // AMBIL ID PROGRAM DARI INTENT
        // ============================================================
        // program_id dikirim dari MenuTracking saat user klik item
        val programId = intent.getIntExtra("program_id", -1)

        // Jika ID tidak valid, tampilkan error dan tutup halaman
        if (programId == -1) {
            Toast.makeText(this, "Program tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ============================================================
        // SETUP BUTTON UPDATE STATUS (BERDASARKAN ROLE)
        // ============================================================
        // Hanya admin yang bisa melihat dan mengklik tombol "Update Status"
        // Donatur: tombol disembunyikan (GONE)
        // ============================================================

        // Mencari tombol update status
        val btnUpdate = findViewById<Button>(R.id.btnExport)

        // Ambil role user dari session
        val userRole = sessionManager.getRole()

        // Cek apakah user adalah admin
        if (userRole == "admin") {
            // Admin: tampilkan tombol dan set click listener
            btnUpdate.visibility = View.VISIBLE
            btnUpdate.setOnClickListener {
                // Pindah ke halaman MonitoringProgram untuk update progress
                val intent = Intent(this, MonitoringProgram::class.java)
                intent.putExtra("program_id", programId) // Kirim ID program
                startActivity(intent)
            }
        } else {
            // Donatur: sembunyikan tombol (tidak bisa update)
            btnUpdate.visibility = View.GONE
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
        // 1. fetchDetail(): mengambil detail program + 8 tahapan
        // 2. loadUserProfile(): mengambil avatar user
        // ============================================================

        fetchDetail(programId)
        loadUserProfile()
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
                                if (fileName.contains("/")) {
                                    fileName = fileName.substringAfterLast("/")
                                }

                                // Buat URL lengkap
                                val avatarUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"

                                // Tampilkan avatar menggunakan Glide
                                Glide.with(this@DetailTracking)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.logokoceng)
                                    .error(R.drawable.logokoceng)
                                    .circleCrop()
                                    .into(imgProfile)
                            } else {
                                // Jika tidak ada avatar, pakai gambar default
                                imgProfile.setImageResource(R.drawable.logokoceng)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ============================================================
    // FUNGSI FETCH DETAIL PROGRAM
    // ============================================================
    // Mengambil detail program dari server via API.
    // Data yang diambil:
    // 1. Informasi program dan mustahiq
    // 2. 8 tahapan tracking (status, deskripsi, foto, tanggal)
    // ============================================================
    private fun fetchDetail(programId: Int) {
        // Ambil token dari session
        val token = sessionManager.getToken()
        if (token == null) return

        // Jalankan proses di background thread
        lifecycleScope.launch {
            try {
                // ============================================================
                // PANGGIL API UNTUK MENDAPATKAN DETAIL PROGRAM
                // ============================================================
                val response = ApiClient.apiService.getTrackingDetail(token, programId)

                // ============================================================
                // PROSES RESPONSE DARI SERVER
                // ============================================================
                if (response.isSuccessful && response.body()?.success == true) {
                    // Ambil data dari response
                    val data = response.body()!!.data!!

                    // Simpan tanggal mulai program untuk tahapan pertama
                    tanggalMulaiProgram = data.tanggal_mulai ?: ""

                    // ============================================================
                    // TAMPILKAN DATA PROGRAM DAN MUSTAHIQ DI UI
                    // ============================================================

                    // Header card (di bagian atas)
                    findViewById<TextView>(R.id.tvJenisProgram).text = data.nama_program
                    findViewById<TextView>(R.id.tvNamaMustahiq).text = data.nama_mustahiq ?: "-"
                    findViewById<TextView>(R.id.tvUsahaMustahiq).text = data.jenis_usaha ?: "-"
                    findViewById<TextView>(R.id.tvAlamatMustahiq).text = data.alamat ?: "-"

                    // Badge UPZPRENEUR
                    findViewById<TextView>(R.id.tvBanner).text = data.nama_program

                    // Card body (foto + info mustahiq)
                    findViewById<TextView>(R.id.tvNama).text = data.nama_mustahiq ?: "-"
                    findViewById<TextView>(R.id.tvUsaha).text = data.jenis_usaha ?: "-"
                    findViewById<TextView>(R.id.tvDana).text =
                        "Rp ${String.format("%,.0f", data.total_dana)}"
                    findViewById<TextView>(R.id.tvLokasi).text = data.alamat ?: "-"

                    // ============================================================
                    // TAMPILKAN FOTO MUSTAHIQ
                    // ============================================================
                    val imgDetail = findViewById<ShapeableImageView>(R.id.imgDetail)
                    val fotoUrl = data.foto_mustahiq

                    if (!fotoUrl.isNullOrEmpty()) {
                        // Jika ada foto, proses path dan tampilkan
                        var fileName = fotoUrl
                        if (fileName.contains("/")) {
                            fileName = fileName.substringAfterLast("/")
                        }
                        val fullUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"

                        Glide.with(this@DetailTracking)
                            .load(fullUrl)
                            .placeholder(R.drawable.img)  // Default saat loading
                            .error(R.drawable.img)        // Default jika error
                            .into(imgDetail)
                    } else {
                        // Jika tidak ada foto, pakai gambar default
                        imgDetail.setImageResource(R.drawable.img)
                    }

                    // ============================================================
                    // TAMPILKAN 8 TAHAPAN BESERTA TANGGAL
                    // ============================================================
                    // Daftar ID TextView untuk tanggal setiap tahapan
                    val tglViews = listOf(
                        R.id.tvTglProses1, R.id.tvTglProses2, R.id.tvTglProses3,
                        R.id.tvTglProses4, R.id.tvTglProses5, R.id.tvTglProses6,
                        R.id.tvTglProses7, R.id.tvTglProses8
                    )

                    // Loop semua tahapan dari data API
                    data.tahapan.forEachIndexed { index, tahapan ->
                        if (index < tglViews.size) {
                            // Variabel untuk menyimpan tanggal
                            var tgl = "-"

                            // Tahapan pertama (index 0) menggunakan tanggal_mulai program
                            if (index == 0 && tanggalMulaiProgram.isNotEmpty()) {
                                tgl = tanggalMulaiProgram.substring(0, 10)
                            } else {
                                // Tahapan lainnya menggunakan updated_at dari progress
                                if (!tahapan.updated_at.isNullOrEmpty()) {
                                    tgl = tahapan.updated_at.substring(0, 10)
                                }
                            }

                            // Set tanggal ke TextView
                            findViewById<TextView>(tglViews[index]).text = tgl
                        }
                    }

                    // ============================================================
                    // TAMPILKAN DOT (LINGKARAN) UNTUK STATUS TAHAPAN
                    // ============================================================
                    // Daftar ID View untuk dot setiap tahapan
                    val dotViews = listOf(
                        R.id.dot1, R.id.dot2, R.id.dot3, R.id.dot4,
                        R.id.dot5, R.id.dot6, R.id.dot7, R.id.dot8
                    )

                    // Loop semua tahapan
                    data.tahapan.forEachIndexed { index, tahapan ->
                        if (index < dotViews.size) {
                            // Cari View dot
                            val dot = findViewById<View>(dotViews[index])

                            // Jika status = "SELESAI", dot hijau (active)
                            if (tahapan.status == "SELESAI") {
                                dot.setBackgroundResource(R.drawable.dot_active)
                            } else {
                                // Jika belum selesai, dot abu (inactive)
                                dot.setBackgroundResource(R.drawable.dot_inactive)
                            }
                        }
                    }

                } else {
                    // Jika API gagal, tampilkan pesan error
                    Toast.makeText(
                        this@DetailTracking,
                        "Gagal memuat detail",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                // Jika terjadi error koneksi
                Toast.makeText(
                    this@DetailTracking,
                    "Koneksi gagal: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}