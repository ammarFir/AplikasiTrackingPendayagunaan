package com.yourname.aplikasitrackingpendayagunaan

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourname.aplikasitrackingpendayagunaan.adapter.TahapanAdapter
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

// ============================================================
// MONITORING PROGRAM - HALAMAN UPDATE PROGRESS
// ============================================================
// Fungsi:
// 1. Menampilkan 8 tahapan program dalam RecyclerView
// 2. Setiap tahapan memiliki form deskripsi dan upload foto
// 3. Upload foto ke server dengan multipart
// 4. Update progress per tahapan
// 5. Tombol "Update Progress" untuk update semua tahapan
// 6. Tombol "Progress Selesai" untuk menyelesaikan program
// ============================================================
class MonitoringProgram : AppCompatActivity() {

    // ============================================================
    // DEKLARASI VARIABEL
    // ============================================================

    // SessionManager: menyimpan data session user (token, role, avatar)
    private lateinit var sessionManager: SessionManager

    // ID program yang sedang dimonitor (dikirim dari DetailTracking)
    private var programId: Int = 0

    // Adapter untuk RecyclerView tahapan
    private lateinit var adapter: TahapanAdapter

    // Data tahapan (id, nama, status, deskripsi, foto)
    private var tahapanItems = mutableListOf<TahapanAdapter.TahapanItem>()

    // Posisi tahapan yang sedang diupload fotonya
    private var currentUploadPosition = -1

    // Tag untuk logging
    companion object {
        private const val TAG = "MonitoringProgram"
    }

    // ============================================================
    // LAUNCHER: PICK IMAGE (GALERI)
    // ============================================================
    // Register activity result untuk memilih gambar dari galeri.
    // Ketika user selesai memilih gambar, akan dipanggil uploadFoto().
    // ============================================================
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "pickImageLauncher: resultCode = ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data

            // Cek apakah ada gambar yang dipilih dan posisi valid
            if (imageUri != null && currentUploadPosition != -1) {
                Log.d(TAG, "pickImageLauncher: Upload foto untuk posisi $currentUploadPosition, URI: $imageUri")
                uploadFoto(currentUploadPosition, imageUri)
            } else {
                Log.e(TAG, "pickImageLauncher: imageUri null atau currentUploadPosition = $currentUploadPosition")
            }
        }
    }

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    // Dipanggil saat activity pertama kali dibuat.
    // Inisialisasi komponen UI, setup RecyclerView, load data.
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: START")

        // ============================================================
        // SETUP FULLSCREEN
        // ============================================================
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        enableEdgeToEdge()

        // Menghubungkan layout XML ke activity
        setContentView(R.layout.activity_monitoring_program)

        // Setup padding untuk system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ============================================================
        // INISIALISASI SESSION & PROGRAM ID
        // ============================================================
        sessionManager = SessionManager(this)
        programId = intent.getIntExtra("program_id", 0)
        Log.d(TAG, "onCreate: programId = $programId")

        // Validasi ID program
        if (programId == 0) {
            Log.e(TAG, "onCreate: programId = 0, finish activity")
            Toast.makeText(this, "ID Program tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ============================================================
        // SETUP RECYCLERVIEW (8 TAHAPAN)
        // ============================================================
        val rvTahapan = findViewById<RecyclerView>(R.id.rvTahapan)
        rvTahapan.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "onCreate: RecyclerView setup")

        // ============================================================
        // SETUP IMG PROFILE -> KE HALAMAN PROFILE
        // ============================================================
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // ============================================================
        // DATA TAHAPAN (MATCH DENGAN DATABASE ID 1-8)
        // ============================================================
        val tahapanData = listOf(
            TahapanAdapter.TahapanItem(1, "Penerima Ditentukan", isFirst = true, isFormVisible = true),
            TahapanAdapter.TahapanItem(2, "Pengadaan Barang", isFirst = false, isFormVisible = false),
            TahapanAdapter.TahapanItem(3, "Penyerahan Barang", isFirst = false, isFormVisible = false),
            TahapanAdapter.TahapanItem(4, "Monitoring Bulanan", isFirst = false, isFormVisible = false),
            TahapanAdapter.TahapanItem(5, "Evaluasi Progress", isFirst = false, isFormVisible = false),
            TahapanAdapter.TahapanItem(6, "Tindak Lanjut", isFirst = false, isFormVisible = false),
            TahapanAdapter.TahapanItem(7, "Laporan Periode", isFirst = false, isFormVisible = false),
            TahapanAdapter.TahapanItem(8, "Laporan Final", isFirst = false, isFormVisible = false)
        )
        tahapanItems = tahapanData.toMutableList()
        Log.d(TAG, "onCreate: tahapanItems size = ${tahapanItems.size}")

        // ============================================================
        // SETUP ADAPTER
        // ============================================================
        // Adapter menangani:
        // 1. Menampilkan setiap item tahapan
        // 2. Tombol "Tambah Bukti" -> menampilkan form
        // 3. Tombol upload foto -> membuka galeri
        // ============================================================
        adapter = TahapanAdapter(
            tahapanItems,
            // Callback saat tombol "Tambah Bukti" diklik
            onTambahBuktiClick = { position, _ ->
                Log.d(TAG, "onTambahBuktiClick: position = $position")
                // Tampilkan form (EditText + Upload Foto)
                adapter.updateFormVisibility(position, true)
                // Scroll ke posisi yang diklik
                rvTahapan.smoothScrollToPosition(position)
            },
            // Callback saat tombol upload foto diklik
            onUploadClick = { position ->
                Log.d(TAG, "onUploadClick: position = $position")
                // Simpan posisi untuk digunakan di launcher
                currentUploadPosition = position
                // Buka galeri untuk memilih gambar
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImageLauncher.launch(intent)
            }
        )
        rvTahapan.adapter = adapter

        // ============================================================
        // SETUP TOMBOL UPDATE PROGRESS & SELESAI
        // ============================================================
        val btnUpdateProgress = findViewById<Button>(R.id.btnUpdateProgress)
        val btnProgressSelesai = findViewById<Button>(R.id.btnProgressSelesai)

        // Tombol "Update Progress": update semua tahapan yang sudah diisi
        btnUpdateProgress.setOnClickListener {
            Log.d(TAG, "btnUpdateProgress clicked")
            updateAllProgress()
        }

        // Tombol "Progress Selesai": update tahapan 8 (Laporan Final)
        btnProgressSelesai.setOnClickListener {
            Log.d(TAG, "btnProgressSelesai clicked")
            updateSingleProgress(8)
        }

        // ============================================================
        // LOAD DATA
        // ============================================================
        // 1. loadExistingProgress(): mengambil data dari server
        // 2. loadUserProfile(): mengambil avatar user
        // ============================================================
        loadExistingProgress()
        loadUserProfile()
        Log.d(TAG, "onCreate: END")
    }

    // ============================================================
    // LIFECYCLE: onResume()
    // ============================================================
    // Refresh avatar saat kembali ke halaman ini.
    // ============================================================
    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    // ============================================================
    // FUNGSI LOAD AVATAR USER
    // ============================================================
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
                                Glide.with(this@MonitoringProgram)
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
    // FUNGSI UPLOAD FOTO
    // ============================================================
    // Proses upload foto ke server:
    // 1. Baca file dari URI
    // 2. Buat MultipartBody.Part
    // 3. Kirim ke API updateProgressWithFoto
    // 4. Refresh data setelah berhasil
    // ============================================================
    private fun uploadFoto(position: Int, imageUri: Uri) {
        Log.d(TAG, "uploadFoto: START position=$position, imageUri=$imageUri")

        val token = sessionManager.getToken()
        if (token == null) {
            Log.e(TAG, "uploadFoto: token null")
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Baca file dari URI
                val stream: InputStream? = contentResolver.openInputStream(imageUri)
                val bytes = stream?.readBytes()
                stream?.close()
                Log.d(TAG, "uploadFoto: bytes size = ${bytes?.size ?: 0}")

                if (bytes != null) {
                    // Buat nama file (contoh: foto_1_1700000000000.jpg)
                    val tahapanId = tahapanItems[position].id
                    val fileName = "foto_${tahapanId}_${System.currentTimeMillis()}.jpg"
                    Log.d(TAG, "uploadFoto: tahapanId=$tahapanId, fileName=$fileName")

                    // Buat RequestBody untuk file
                    val reqBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val fotoPart = MultipartBody.Part.createFormData("foto", fileName, reqBody)

                    // Buat RequestBody untuk field lainnya
                    val programIdBody = programId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val tahapanIdBody = tahapanId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val statusBody = "SELESAI".toRequestBody("text/plain".toMediaTypeOrNull())

                    // Ambil deskripsi yang sudah diisi
                    val deskripsiText = tahapanItems[position].deskripsi
                    val deskripsiBody = if (deskripsiText.isNotEmpty()) {
                        deskripsiText.toRequestBody("text/plain".toMediaTypeOrNull())
                    } else null

                    // Panggil API upload foto
                    Log.d(TAG, "uploadFoto: calling API updateProgressWithFoto")
                    val response = ApiClient.apiService.updateProgressWithFoto(
                        token = token,
                        programId = programIdBody,
                        tahapanId = tahapanIdBody,
                        status = statusBody,
                        deskripsi = deskripsiBody,
                        foto = fotoPart
                    )

                    // Proses response
                    if (response.isSuccessful && response.body()?.success == true) {
                        Log.d(TAG, "uploadFoto: SUCCESS, foto berhasil diupload")
                        Toast.makeText(this@MonitoringProgram, "Foto berhasil diupload", Toast.LENGTH_SHORT).show()
                        // Refresh data agar foto tampil
                        loadExistingProgress()
                    } else {
                        Log.e(TAG, "uploadFoto: FAILED")
                        Toast.makeText(this@MonitoringProgram, "Gagal upload foto", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "uploadFoto: EXCEPTION ${e.message}", e)
                Toast.makeText(this@MonitoringProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ============================================================
    // FUNGSI LOAD EXISTING PROGRESS
    // ============================================================
    // Mengambil data progress dari server dan menampilkan di UI.
    // Data yang diambil:
    // 1. Status setiap tahapan (SELESAI / null)
    // 2. Deskripsi yang sudah diisi
    // 3. Foto yang sudah diupload
    // ============================================================
    private fun loadExistingProgress() {
        Log.d(TAG, "loadExistingProgress: START")
        val token = sessionManager.getToken()
        if (token == null) {
            Log.e(TAG, "loadExistingProgress: token null")
            return
        }

        lifecycleScope.launch {
            try {
                // Panggil API detail program
                Log.d(TAG, "loadExistingProgress: calling API getTrackingDetail for programId=$programId")
                val response = ApiClient.apiService.getTrackingDetail(token, programId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    Log.d(TAG, "loadExistingProgress: total tahapan from API = ${data.tahapan.size}")

                    // Loop setiap tahapan dari API
                    data.tahapan.forEach { tahapan ->
                        // Cari index di tahapanItems berdasarkan id
                        val index = tahapanItems.indexOfFirst { it.id == tahapan.tahapan_id }

                        if (index != -1) {
                            Log.d(TAG, "loadExistingProgress: tahapan_id=${tahapan.tahapan_id}, status=${tahapan.status}, deskripsi=${tahapan.deskripsi}, foto=${tahapan.foto}")

                            // Update deskripsi jika ada
                            if (!tahapan.deskripsi.isNullOrEmpty()) {
                                adapter.updateDeskripsi(index, tahapan.deskripsi)
                            }

                            // Jika status SELESAI, tampilkan form
                            if (tahapan.status == "SELESAI") {
                                adapter.updateFormVisibility(index, true)
                            }

                            // Update foto jika ada
                            if (!tahapan.foto.isNullOrEmpty()) {
                                adapter.updateFoto(index, tahapan.foto)
                            }
                        } else {
                            Log.w(TAG, "loadExistingProgress: tahapan_id ${tahapan.tahapan_id} not found in tahapanItems")
                        }
                    }
                    Toast.makeText(this@MonitoringProgram, "Data berhasil dimuat", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "loadExistingProgress: API call failed")
                    Toast.makeText(this@MonitoringProgram, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadExistingProgress: EXCEPTION ${e.message}", e)
                Toast.makeText(this@MonitoringProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ============================================================
    // FUNGSI UPDATE SINGLE PROGRESS
    // ============================================================
    // Update satu tahapan tertentu (biasanya untuk tahapan 8).
    // Dipanggil saat tombol "Progress Selesai" diklik.
    // ============================================================
    private fun updateSingleProgress(tahapanId: Int) {
        Log.d(TAG, "updateSingleProgress: START tahapanId=$tahapanId")
        val token = sessionManager.getToken()
        if (token == null) {
            Log.e(TAG, "updateSingleProgress: token null")
            return
        }

        lifecycleScope.launch {
            try {
                // Buat RequestBody
                val programIdBody = programId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val tahapanIdBody = tahapanId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val statusBody = "SELESAI".toRequestBody("text/plain".toMediaTypeOrNull())

                // Panggil API update (tanpa foto)
                Log.d(TAG, "updateSingleProgress: calling API updateProgressWithFoto")
                val response = ApiClient.apiService.updateProgressWithFoto(
                    token = token,
                    programId = programIdBody,
                    tahapanId = tahapanIdBody,
                    status = statusBody,
                    deskripsi = null,
                    foto = null
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "updateSingleProgress: SUCCESS")
                    Toast.makeText(this@MonitoringProgram, "Program selesai!", Toast.LENGTH_SHORT).show()
                    // Refresh data
                    loadExistingProgress()
                } else {
                    Log.e(TAG, "updateSingleProgress: FAILED")
                    Toast.makeText(this@MonitoringProgram, "Gagal menyelesaikan program", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateSingleProgress: EXCEPTION ${e.message}", e)
                Toast.makeText(this@MonitoringProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ============================================================
    // FUNGSI UPDATE ALL PROGRESS
    // ============================================================
    // Update semua tahapan yang form-nya sudah ditampilkan (isFormVisible = true)
    // dan deskripsinya tidak kosong.
    // Dipanggil saat tombol "Update Progress" diklik.
    // ============================================================
    private fun updateAllProgress() {
        Log.d(TAG, "updateAllProgress: START")
        val token = sessionManager.getToken()
        if (token == null) {
            Log.e(TAG, "updateAllProgress: token null")
            return
        }

        lifecycleScope.launch {
            var allSuccess = true

            // Loop semua tahapan
            for (i in tahapanItems.indices) {
                val item = tahapanItems[i]

                // Hanya proses jika form terlihat dan deskripsi tidak kosong
                if (item.isFormVisible) {
                    val deskripsi = item.deskripsi
                    Log.d(TAG, "updateAllProgress: i=$i, tahapanId=${item.id}, deskripsi='$deskripsi'")

                    if (deskripsi.isNotEmpty()) {
                        try {
                            // Buat RequestBody
                            val programIdBody = programId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            val tahapanIdBody = item.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            val statusBody = "SELESAI".toRequestBody("text/plain".toMediaTypeOrNull())
                            val deskripsiBody = deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())

                            // Panggil API update (tanpa foto)
                            val response = ApiClient.apiService.updateProgressWithFoto(
                                token = token,
                                programId = programIdBody,
                                tahapanId = tahapanIdBody,
                                status = statusBody,
                                deskripsi = deskripsiBody,
                                foto = null
                            )

                            if (!response.isSuccessful || response.body()?.success != true) {
                                Log.e(TAG, "updateAllProgress: Gagal update tahapan ${item.id}")
                                allSuccess = false
                            } else {
                                Log.d(TAG, "updateAllProgress: Sukses update tahapan ${item.id}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "updateAllProgress: EXCEPTION for tahapan ${item.id}", e)
                            allSuccess = false
                        }
                    }
                }
            }

            // Tampilkan hasil
            if (allSuccess) {
                Log.d(TAG, "updateAllProgress: ALL SUCCESS")
                Toast.makeText(this@MonitoringProgram, "Semua progress berhasil diupdate", Toast.LENGTH_SHORT).show()
                loadExistingProgress() // Refresh data
            } else {
                Log.e(TAG, "updateAllProgress: SOME FAILED")
                Toast.makeText(this@MonitoringProgram, "Beberapa update gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}