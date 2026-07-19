package com.yourname.aplikasitrackingpendayagunaan

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.itextpdf.text.Document
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.yourname.aplikasitrackingpendayagunaan.adapter.LaporanAdapter
import com.yourname.aplikasitrackingpendayagunaan.model.TrackingProgram
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
// LAPORAN PROGRAM - HALAMAN LAPORAN & EXPORT PDF
// ============================================================
// Fungsi:
// 1. Menampilkan daftar program dalam RecyclerView
// 2. Setiap item memiliki tombol untuk export PDF
// 3. Generate PDF berisi detail program + 8 tahapan
// 4. Simpan PDF ke folder Downloads
// 5. Tampilkan notifikasi setelah PDF selesai
// 6. Bottom navigation (Home, Tracking, Laporan)
// 7. Avatar user di header (klik ke profile)
// 8. Permission request untuk penyimpanan (Android 10 ke bawah)
// ============================================================
class LaporanProgram : AppCompatActivity() {

    // ============================================================
    // DEKLARASI VARIABEL
    // ============================================================

    // SessionManager: menyimpan data session user (token, avatar)
    private lateinit var sessionManager: SessionManager

    // Adapter untuk RecyclerView daftar program
    private lateinit var adapter: LaporanAdapter

    // RecyclerView untuk menampilkan daftar program
    private lateinit var rvLaporan: RecyclerView

    // Kode request untuk permission storage
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    // Dipanggil saat activity pertama kali dibuat.
    // Inisialisasi komponen UI, setup RecyclerView, bottom nav, dan load data.
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghubungkan layout XML activity_laporan_program ke activity ini
        setContentView(R.layout.activity_laporan_program)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // ============================================================
        // SETUP BACK HANDLING (TOMOL BACK DI HP)
        // ============================================================
        // Ketika user menekan tombol back, pindah ke MainActivity
        // (bukan kembali ke halaman sebelumnya)
        // ============================================================
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@LaporanProgram, MainActivity::class.java))
                finish()
            }
        })

        // ============================================================
        // SETUP WINDOW INSETS (PADDING SYSTEM BARS)
        // ============================================================
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ============================================================
        // REQUEST PERMISSION STORAGE (UNTUK ANDROID 10 KE BAWAH)
        // ============================================================
        // Android 10 (Q) ke atas menggunakan MediaStore, tidak perlu permission.
        // Android 9 ke bawah butuh WRITE_EXTERNAL_STORAGE.
        // ============================================================
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        // ============================================================
        // INISIALISASI SESSION MANAGER
        // ============================================================
        sessionManager = SessionManager(this)

        // ============================================================
        // SETUP IMG PROFILE -> KE HALAMAN PROFILE
        // ============================================================
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // ============================================================
        // SETUP BOTTOM NAVIGATION
        // ============================================================
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set menu "Laporan" sebagai aktif
        bottomNav.selectedItemId = R.id.nav_laporan

        // Navigasi antar halaman
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tracking -> {
                    startActivity(Intent(this, MenuTracking::class.java))
                    finish()
                    true
                }
                R.id.nav_laporan -> {
                    true // Tetap di halaman ini
                }
                else -> false
            }
        }

        // ============================================================
        // SETUP RECYCLERVIEW (DAFTAR PROGRAM)
        // ============================================================
        rvLaporan = findViewById(R.id.rvLaporanProgram)
        rvLaporan.layoutManager = LinearLayoutManager(this)

        // Inisialisasi adapter dengan data kosong dan fungsi generate PDF
        // Ketika item diklik (tombol export), generatePdf() dipanggil
        adapter = LaporanAdapter(emptyList()) { program ->
            generatePdf(program)
        }
        rvLaporan.adapter = adapter

        // ============================================================
        // LOAD DATA
        // ============================================================
        loadData()
        loadUserProfile()
    }

    // ============================================================
    // LIFECYCLE: onResume()
    // ============================================================
    // Refresh data saat kembali ke halaman ini.
    // ============================================================
    override fun onResume() {
        super.onResume()
        loadUserProfile()
        loadData()
    }

    // ============================================================
    // FUNGSI LOAD AVATAR USER
    // ============================================================
    private fun loadUserProfile() {
        val token = sessionManager.getToken()
        if (token == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getProfile(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.data
                        user?.let {
                            val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                            if (!it.avatar.isNullOrEmpty()) {
                                var fileName = it.avatar
                                if (fileName.contains("/")) {
                                    fileName = fileName.substringAfterLast("/")
                                }
                                val avatarUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"
                                Glide.with(this@LaporanProgram)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.logokoceng)
                                    .error(R.drawable.logokoceng)
                                    .circleCrop()
                                    .into(imgProfile)
                            } else {
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
    // FUNGSI LOAD DATA PROGRAM
    // ============================================================
    // Mengambil daftar program dari server via API.
    // ============================================================
    private fun loadData() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Panggil API untuk mendapatkan daftar program
                val response = ApiClient.apiService.getTrackingList(token)

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!

                    // Update adapter dengan data program
                    adapter.updateData(data.programs)

                } else {
                    Toast.makeText(
                        this@LaporanProgram,
                        "Gagal memuat data",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@LaporanProgram,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ============================================================
    // FUNGSI GENERATE PDF
    // ============================================================
    // Membuat file PDF berisi:
    // 1. Informasi program (nama, mustahiq, usaha, dana, alamat, progress, status)
    // 2. Detail 8 tahapan (nama, status, deskripsi)
    // 3. Footer tanggal cetak
    // ============================================================
    private fun generatePdf(program: TrackingProgram) {
        val token = sessionManager.getToken()
        if (token == null) return

        lifecycleScope.launch {
            try {
                // ============================================================
                // AMBIL DETAIL PROGRAM (TERMASUK 8 TAHAPAN)
                // ============================================================
                val detailResponse = ApiClient.apiService.getTrackingDetail(token, program.id)

                // Cek apakah response sukses
                if (!detailResponse.isSuccessful || detailResponse.body()?.success != true) {
                    Toast.makeText(
                        this@LaporanProgram,
                        "Gagal mengambil detail program",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Ambil data detail
                val data = detailResponse.body()!!.data!!

                // ============================================================
                // BUAT FILE PDF
                // ============================================================
                val timestamp = System.currentTimeMillis()
                val fileName = "laporan_${program.nama_program}_$timestamp.pdf"
                val pdfFile = File(cacheDir, fileName)

                // Inisialisasi document PDF
                val document = Document()
                PdfWriter.getInstance(document, FileOutputStream(pdfFile))
                document.open()

                // ============================================================
                // TITLE
                // ============================================================
                val titleFont = Font(Font.FontFamily.TIMES_ROMAN, 18f, Font.BOLD)
                document.add(Paragraph("Laporan Program Pendayagunaan Mustahiq", titleFont))
                document.add(Paragraph(" "))
                document.add(Paragraph("=".repeat(50)))
                document.add(Paragraph(" "))

                // ============================================================
                // DATA PROGRAM
                // ============================================================
                val normalFont = Font(Font.FontFamily.TIMES_ROMAN, 12f)

                document.add(Paragraph("Nama Program: ${program.nama_program}", normalFont))
                document.add(Paragraph("Nama Mustahiq: ${program.nama_mustahiq ?: "-"}", normalFont))
                document.add(Paragraph("Jenis Usaha: ${program.jenis_usaha ?: "-"}", normalFont))
                document.add(Paragraph(
                    "Total Dana: Rp ${String.format("%,.0f", program.total_dana)}",
                    normalFont
                ))
                document.add(Paragraph("Alamat: ${program.alamat_mustahiq ?: "-"}", normalFont))
                document.add(Paragraph(
                    "Progress: ${program.progress_persen}% (${program.total_tahapan_selesai}/8)",
                    normalFont
                ))
                document.add(Paragraph("Status: ${program.status}", normalFont))
                document.add(Paragraph(" "))
                document.add(Paragraph("=".repeat(50)))
                document.add(Paragraph(" "))

                // ============================================================
                // DETAIL TAHAPAN (8 TAHAPAN)
                // ============================================================
                val boldFont = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.BOLD)
                document.add(Paragraph("Detail Tahapan:", boldFont))
                document.add(Paragraph(" "))

                // Loop semua tahapan dari API
                data.tahapan.forEachIndexed { index, tahapan ->
                    val statusText = tahapan.status ?: "BELUM"  // Jika null, berarti belum
                    val deskripsiText = tahapan.deskripsi ?: "-" // Jika null, berarti kosong
                    val nomor = index + 1

                    // Cetak: "1. Penerima Ditentukan - SELESAI"
                    document.add(Paragraph(
                        "$nomor. ${tahapan.nama_tahapan} - $statusText",
                        normalFont
                    ))

                    // Cetak deskripsi dengan indentasi
                    document.add(Paragraph("   Deskripsi: $deskripsiText", normalFont))
                    document.add(Paragraph(" ")) // Spasi antar tahapan
                }

                document.add(Paragraph("=".repeat(50)))
                document.add(Paragraph(" "))

                // ============================================================
                // FOOTER (TANGGAL CETAK)
                // ============================================================
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                document.add(Paragraph(
                    "Dicetak pada: ${dateFormat.format(Date())}",
                    normalFont
                ))

                // Tutup document
                document.close()

                // ============================================================
                // SIMPAN PDF KE DOWNLOADS & TAMPILKAN NOTIFIKASI
                // ============================================================
                saveAndNotifyPdf(pdfFile, program.nama_program, timestamp)

            } catch (e: Exception) {
                Toast.makeText(
                    this@LaporanProgram,
                    "Gagal membuat PDF: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    // ============================================================
    // FUNGSI SIMPAN PDF & NOTIFIKASI
    // ============================================================
    // Menyimpan file PDF ke folder Downloads.
    // Untuk Android 10+ menggunakan MediaStore.
    // Untuk Android 9 ke bawah menggunakan Environment.
    // ============================================================
    private fun saveAndNotifyPdf(pdfFile: File, programName: String, timestamp: Long) {
        try {
            val fileName = "laporan_${programName}_$timestamp.pdf"

            // ============================================================
            // ANDROID 10+ (Q) MENGGUNAKAN MEDIASTORE
            // ============================================================
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                // Insert file ke MediaStore
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    // Tulis file ke output stream
                    resolver.openOutputStream(it)?.use { outputStream ->
                        pdfFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    // Tampilkan notifikasi
                    showNotification(
                        "PDF selesai diunduh",
                        "Laporan $programName telah tersimpan di folder Downloads"
                    )
                    Toast.makeText(
                        this,
                        "PDF tersimpan di Downloads sebagai: $fileName",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // ============================================================
                // ANDROID 9 KE BAWAH MENGGUNAKAN ENVIRONMENT
                // ============================================================
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )

                // Buat folder jika belum ada
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                // Copy file ke Downloads
                val destFile = File(downloadsDir, fileName)
                pdfFile.copyTo(destFile, true)

                // Tampilkan notifikasi
                showNotification(
                    "PDF selesai diunduh",
                    "Laporan $programName telah tersimpan di folder Downloads"
                )
                Toast.makeText(
                    this,
                    "PDF tersimpan di Downloads sebagai: $fileName",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Hapus file sementara di cache
            pdfFile.delete()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Gagal menyimpan PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ============================================================
    // FUNGSI TAMPILKAN NOTIFIKASI
    // ============================================================
    // Menampilkan notifikasi di system tray setelah PDF selesai disimpan.
    // ============================================================
    private fun showNotification(title: String, message: String) {
        val channelId = "pdf_download_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // ============================================================
        // BUAT NOTIFICATION CHANNEL (UNTUK ANDROID 8+)
        // ============================================================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download PDF",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // ============================================================
        // BUAT NOTIFIKASI
        // ============================================================
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Otomatis hilang setelah diklik
            .build()

        // Tampilkan notifikasi
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }

    // ============================================================
    // HANDLE PERMISSION REQUEST RESULT
    // ============================================================
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin storage diberikan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Izin storage diperlukan untuk menyimpan PDF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}