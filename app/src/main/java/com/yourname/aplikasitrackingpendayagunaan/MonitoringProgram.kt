package com.yourname.aplikasitrackingpendayagunaan

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

class MonitoringProgram : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var programId: Int = 0
    private lateinit var adapter: TahapanAdapter
    private var tahapanItems = mutableListOf<TahapanAdapter.TahapanItem>()
    private var currentUploadPosition = -1

    companion object {
        private const val TAG = "MonitoringProgram"
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "pickImageLauncher: resultCode = ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data
            if (imageUri != null && currentUploadPosition != -1) {
                Log.d(TAG, "pickImageLauncher: Upload foto untuk posisi $currentUploadPosition, URI: $imageUri")
                uploadFoto(currentUploadPosition, imageUri)
            } else {
                Log.e(TAG, "pickImageLauncher: imageUri null atau currentUploadPosition = $currentUploadPosition")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: START")

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        enableEdgeToEdge()
        setContentView(R.layout.activity_monitoring_program)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        programId = intent.getIntExtra("program_id", 0)
        Log.d(TAG, "onCreate: programId = $programId")

        if (programId == 0) {
            Log.e(TAG, "onCreate: programId = 0, finish activity")
            Toast.makeText(this, "ID Program tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup RecyclerView
        val rvTahapan = findViewById<RecyclerView>(R.id.rvTahapan)
        rvTahapan.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "onCreate: RecyclerView setup")

        // IMG PROFILE - klik ke halaman Profile
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Data tahapan (match dengan database id 1-8)
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

        adapter = TahapanAdapter(
            tahapanItems,
            onTambahBuktiClick = { position, _ ->
                Log.d(TAG, "onTambahBuktiClick: position = $position")
                adapter.updateFormVisibility(position, true)
                rvTahapan.smoothScrollToPosition(position)
            },
            onUploadClick = { position ->
                Log.d(TAG, "onUploadClick: position = $position")
                currentUploadPosition = position
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImageLauncher.launch(intent)
            }
        )
        rvTahapan.adapter = adapter

        val btnUpdateProgress = findViewById<Button>(R.id.btnUpdateProgress)
        val btnProgressSelesai = findViewById<Button>(R.id.btnProgressSelesai)

        btnUpdateProgress.setOnClickListener {
            Log.d(TAG, "btnUpdateProgress clicked")
            updateAllProgress()
        }

        btnProgressSelesai.setOnClickListener {
            Log.d(TAG, "btnProgressSelesai clicked")
            updateSingleProgress(8)
        }

        loadExistingProgress()
        loadUserProfile()
        Log.d(TAG, "onCreate: END")
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

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun uploadFoto(position: Int, imageUri: Uri) {
        Log.d(TAG, "uploadFoto: START position=$position, imageUri=$imageUri")

        val token = sessionManager.getToken()
        if (token == null) {
            Log.e(TAG, "uploadFoto: token null")
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "uploadFoto: token ada, length=${token.length}")

        lifecycleScope.launch {
            try {
                val stream: InputStream? = contentResolver.openInputStream(imageUri)
                val bytes = stream?.readBytes()
                stream?.close()

                Log.d(TAG, "uploadFoto: bytes size = ${bytes?.size ?: 0}")

                if (bytes != null) {
                    val tahapanId = tahapanItems[position].id
                    val fileName = "foto_${tahapanId}_${System.currentTimeMillis()}.jpg"
                    Log.d(TAG, "uploadFoto: tahapanId=$tahapanId, fileName=$fileName")

                    val reqBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val fotoPart = MultipartBody.Part.createFormData("foto", fileName, reqBody)

                    val programIdBody = programId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val tahapanIdBody = tahapanId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val statusBody = "SELESAI".toRequestBody("text/plain".toMediaTypeOrNull())

                    val deskripsiText = tahapanItems[position].deskripsi
                    Log.d(TAG, "uploadFoto: deskripsiText = '$deskripsiText'")
                    val deskripsiBody = if (deskripsiText.isNotEmpty()) {
                        deskripsiText.toRequestBody("text/plain".toMediaTypeOrNull())
                    } else null

                    Log.d(TAG, "uploadFoto: calling API updateProgressWithFoto")
                    val response = ApiClient.apiService.updateProgressWithFoto(
                        token = token,
                        programId = programIdBody,
                        tahapanId = tahapanIdBody,
                        status = statusBody,
                        deskripsi = deskripsiBody,
                        foto = fotoPart
                    )

                    Log.d(TAG, "uploadFoto: response.isSuccessful = ${response.isSuccessful}")
                    if (response.isSuccessful) {
                        Log.d(TAG, "uploadFoto: response.body() = ${response.body()}")
                    } else {
                        Log.e(TAG, "uploadFoto: error body = ${response.errorBody()?.string()}")
                    }

                    if (response.isSuccessful && response.body()?.success == true) {
                        Log.d(TAG, "uploadFoto: SUCCESS, foto berhasil diupload")
                        Toast.makeText(this@MonitoringProgram, "Foto berhasil diupload", Toast.LENGTH_SHORT).show()
                        loadExistingProgress()
                    } else {
                        Log.e(TAG, "uploadFoto: FAILED, response body success = ${response.body()?.success}")
                        Toast.makeText(this@MonitoringProgram, "Gagal upload foto", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "uploadFoto: bytes null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "uploadFoto: EXCEPTION ${e.message}", e)
                Toast.makeText(this@MonitoringProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadExistingProgress() {
        Log.d(TAG, "loadExistingProgress: START")
        val token = sessionManager.getToken()
        if (token == null) {
            Log.e(TAG, "loadExistingProgress: token null")
            return
        }

        lifecycleScope.launch {
            try {
                Log.d(TAG, "loadExistingProgress: calling API getTrackingDetail for programId=$programId")
                val response = ApiClient.apiService.getTrackingDetail(token, programId)
                Log.d(TAG, "loadExistingProgress: response.isSuccessful = ${response.isSuccessful}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    Log.d(TAG, "loadExistingProgress: total tahapan from API = ${data.tahapan.size}")

                    data.tahapan.forEach { tahapan ->
                        val index = tahapanItems.indexOfFirst { it.id == tahapan.tahapan_id }
                        if (index != -1) {
                            Log.d(TAG, "loadExistingProgress: tahapan_id=${tahapan.tahapan_id}, status=${tahapan.status}, deskripsi=${tahapan.deskripsi}, foto=${tahapan.foto}")

                            // Update deskripsi dari server
                            if (!tahapan.deskripsi.isNullOrEmpty()) {
                                adapter.updateDeskripsi(index, tahapan.deskripsi)
                            }

                            // Tampilkan form jika status SELESAI (tanpa menimpa deskripsi)
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
                    Log.e(TAG, "loadExistingProgress: API call failed, response body success = ${response.body()?.success}")
                    Toast.makeText(this@MonitoringProgram, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadExistingProgress: EXCEPTION ${e.message}", e)
                Toast.makeText(this@MonitoringProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSingleProgress(tahapanId: Int) {
        Log.d(TAG, "updateSingleProgress: START tahapanId=$tahapanId")
        val token = sessionManager.getToken()
        if (token == null) {
            Log.e(TAG, "updateSingleProgress: token null")
            return
        }

        lifecycleScope.launch {
            try {
                val programIdBody = programId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val tahapanIdBody = tahapanId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val statusBody = "SELESAI".toRequestBody("text/plain".toMediaTypeOrNull())

                Log.d(TAG, "updateSingleProgress: calling API updateProgressWithFoto")
                val response = ApiClient.apiService.updateProgressWithFoto(
                    token = token,
                    programId = programIdBody,
                    tahapanId = tahapanIdBody,
                    status = statusBody,
                    deskripsi = null,
                    foto = null
                )

                Log.d(TAG, "updateSingleProgress: response.isSuccessful = ${response.isSuccessful}")
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "updateSingleProgress: SUCCESS")
                    Toast.makeText(this@MonitoringProgram, "Program selesai!", Toast.LENGTH_SHORT).show()
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

    private fun updateAllProgress() {
        Log.d(TAG, "updateAllProgress: START")
        val token = sessionManager.getToken()
        if (token == null) {
            Log.e(TAG, "updateAllProgress: token null")
            return
        }

        lifecycleScope.launch {
            var allSuccess = true
            for (i in tahapanItems.indices) {
                val item = tahapanItems[i]
                if (item.isFormVisible) {
                    val deskripsi = item.deskripsi
                    Log.d(TAG, "updateAllProgress: i=$i, tahapanId=${item.id}, deskripsi='$deskripsi'")

                    // Update hanya jika deskripsi tidak kosong
                    if (deskripsi.isNotEmpty()) {
                        try {
                            val programIdBody = programId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            val tahapanIdBody = item.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            val statusBody = "SELESAI".toRequestBody("text/plain".toMediaTypeOrNull())
                            val deskripsiBody = deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())

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
            if (allSuccess) {
                Log.d(TAG, "updateAllProgress: ALL SUCCESS")
                Toast.makeText(this@MonitoringProgram, "Semua progress berhasil diupdate", Toast.LENGTH_SHORT).show()
                loadExistingProgress()
            } else {
                Log.e(TAG, "updateAllProgress: SOME FAILED")
                Toast.makeText(this@MonitoringProgram, "Beberapa update gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}