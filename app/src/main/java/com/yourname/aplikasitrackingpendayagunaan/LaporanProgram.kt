package com.yourname.aplikasitrackingpendayagunaan

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
import com.yourname.aplikasitrackingpendayagunaan.model.TrackingProgram

class LaporanProgram : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: LaporanAdapter
    private lateinit var rvLaporan: RecyclerView

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_program)

        // Setup back handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@LaporanProgram, MainActivity::class.java))
                finish()
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Request permission untuk Android 10 ke bawah
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            }
        }

        sessionManager = SessionManager(this)

        // IMG PROFILE - klik ke halaman Profile
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_laporan
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
                    true
                }
                else -> false
            }
        }

        rvLaporan = findViewById(R.id.rvLaporanProgram)
        rvLaporan.layoutManager = LinearLayoutManager(this)

        adapter = LaporanAdapter(emptyList()) { program ->
            generatePdf(program)
        }
        rvLaporan.adapter = adapter

        loadData()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val token = sessionManager.getToken() ?: return

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

    override fun onResume() {
        super.onResume()
        loadUserProfile()
        loadData()
    }

    private fun generatePdf(program: TrackingProgram) {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                // Ambil detail program (termasuk 8 tahapan)
                val detailResponse = ApiClient.apiService.getTrackingDetail(token, program.id)
                if (!detailResponse.isSuccessful || detailResponse.body()?.success != true) {
                    Toast.makeText(this@LaporanProgram, "Gagal mengambil detail program", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val data = detailResponse.body()!!.data!!
                val timestamp = System.currentTimeMillis()
                val fileName = "laporan_${program.nama_program}_$timestamp.pdf"
                val pdfFile = File(cacheDir, fileName)
                val document = Document()
                PdfWriter.getInstance(document, FileOutputStream(pdfFile))
                document.open()

                // Title
                val titleFont = Font(Font.FontFamily.TIMES_ROMAN, 18f, Font.BOLD)
                document.add(Paragraph("Laporan Program Pendayagunaan Mustahiq", titleFont))
                document.add(Paragraph(" "))
                document.add(Paragraph("=".repeat(50)))
                document.add(Paragraph(" "))

                // Data Program
                val normalFont = Font(Font.FontFamily.TIMES_ROMAN, 12f)
                document.add(Paragraph("Nama Program: ${program.nama_program}", normalFont))
                document.add(Paragraph("Nama Mustahiq: ${program.nama_mustahiq ?: "-"}", normalFont))
                document.add(Paragraph("Jenis Usaha: ${program.jenis_usaha ?: "-"}", normalFont))
                document.add(Paragraph("Total Dana: Rp ${String.format("%,.0f", program.total_dana)}", normalFont))
                document.add(Paragraph("Alamat: ${program.alamat_mustahiq ?: "-"}", normalFont))
                document.add(Paragraph("Progress: ${program.progress_persen}% (${program.total_tahapan_selesai}/8)", normalFont))
                document.add(Paragraph("Status: ${program.status}", normalFont))
                document.add(Paragraph(" "))
                document.add(Paragraph("=".repeat(50)))
                document.add(Paragraph(" "))

                // Detail Tahapan
                val boldFont = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.BOLD)
                document.add(Paragraph("Detail Tahapan:", boldFont))
                document.add(Paragraph(" "))

                data.tahapan.forEachIndexed { index, tahapan ->
                    val statusText = tahapan.status ?: "BELUM"
                    val deskripsiText = tahapan.deskripsi ?: "-"
                    val nomor = index + 1

                    document.add(Paragraph("$nomor. ${tahapan.nama_tahapan} - $statusText", normalFont))
                    document.add(Paragraph("   Deskripsi: $deskripsiText", normalFont))
                    document.add(Paragraph(" "))
                }

                document.add(Paragraph("=".repeat(50)))
                document.add(Paragraph(" "))

                // Footer
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                document.add(Paragraph("Dicetak pada: ${dateFormat.format(Date())}", normalFont))

                document.close()

                // Simpan ke Downloads dan notifikasi
                saveAndNotifyPdf(pdfFile, program.nama_program, timestamp)

            } catch (e: Exception) {
                Toast.makeText(this@LaporanProgram, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    private fun saveAndNotifyPdf(pdfFile: File, programName: String, timestamp: Long) {
        try {
            val fileName = "laporan_${programName}_$timestamp.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        pdfFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    showNotification("PDF selesai diunduh", "Laporan $programName telah tersimpan di folder Downloads")
                    Toast.makeText(this, "PDF tersimpan di Downloads sebagai: $fileName", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val destFile = File(downloadsDir, fileName)
                pdfFile.copyTo(destFile, true)
                showNotification("PDF selesai diunduh", "Laporan $programName telah tersimpan di folder Downloads")
                Toast.makeText(this, "PDF tersimpan di Downloads sebagai: $fileName", Toast.LENGTH_LONG).show()
            }
            pdfFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "pdf_download_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download PDF",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }

    private fun loadData() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTrackingList(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    adapter.updateData(data.programs)
                } else {
                    Toast.makeText(this@LaporanProgram, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LaporanProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin storage diberikan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin storage diperlukan untuk menyimpan PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }
}