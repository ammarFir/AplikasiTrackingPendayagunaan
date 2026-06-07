package com.yourname.aplikasitrackingpendayagunaan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class MonitoringProgram : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var programId: Int = 0

    private data class TahapanData(
        val tahapanId: Int,
        val editText: EditText,
        val btnUpload: View,
        val imageView: ImageView,
        var currentPhotoUri: Uri? = null
    )

    private val tahapanList = mutableListOf<TahapanData>()

    private var currentTahapanId = 0
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data
            if (imageUri != null) {
                val index = tahapanList.indexOfFirst { it.tahapanId == currentTahapanId }
                if (index != -1) {
                    val tahapan = tahapanList[index]
                    tahapan.imageView.setImageURI(imageUri)
                    tahapan.imageView.visibility = View.VISIBLE
                    tahapanList[index] = tahapan.copy(currentPhotoUri = imageUri)
                    Log.d("FOTO_CEK", "Tahapan ${tahapan.tahapanId} foto dipilih: $imageUri")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        if (programId == 0) {
            Toast.makeText(this, "ID Program tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initTahapanForms()

        val btnUpdateProgress = findViewById<Button>(R.id.btnUpdateProgress)
        val btnProgressSelesai = findViewById<Button>(R.id.btnProgressSelesai)

        btnUpdateProgress.setOnClickListener {
            updateAllProgress()
        }

        btnProgressSelesai.setOnClickListener {
            lifecycleScope.launch {
                val token = sessionManager.getToken() ?: return@launch

                val programIdBody = programId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val tahapanIdBody = "8".toRequestBody("text/plain".toMediaTypeOrNull())
                val statusBody = "SELESAI".toRequestBody("text/plain".toMediaTypeOrNull())

                try {
                    val response = ApiClient.apiService.updateProgressWithFoto(
                        token = token,
                        programId = programIdBody,
                        tahapanId = tahapanIdBody,
                        status = statusBody,
                        deskripsi = null,
                        foto = null
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@MonitoringProgram, "Program selesai!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MonitoringProgram, DetailTracking::class.java)
                        intent.putExtra("program_id", programId)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MonitoringProgram, "Gagal menyelesaikan program", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MonitoringProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadExistingProgress()
    }

    private fun initTahapanForms() {
        setupTahapan(1, R.id.etDeskripsi1, R.id.btnUpload1, R.id.ivFoto1)
        setupTahapan(2, R.id.etDeskripsi2, R.id.btnUpload2, R.id.ivFoto2)
        setupTahapan(3, R.id.etDeskripsi3, R.id.btnUpload3, R.id.ivFoto3)
        setupTahapan(4, R.id.etDeskripsi4, R.id.btnUpload4, R.id.ivFoto4)
        setupTahapan(5, R.id.etDeskripsi5, R.id.btnUpload5, R.id.ivFoto5)
        setupTahapan(6, R.id.etDeskripsi6, R.id.btnUpload6, R.id.ivFoto6)
        setupTahapan(7, R.id.etDeskripsi7, R.id.btnUpload7, R.id.ivFoto7)
    }

    private fun setupTahapan(tahapanId: Int, editTextId: Int, btnUploadId: Int, imageViewId: Int) {
        val editText = findViewById<EditText>(editTextId)
        val btnUpload = findViewById<View>(btnUploadId)
        val imageView = findViewById<ImageView>(imageViewId)

        btnUpload.setOnClickListener {
            currentTahapanId = tahapanId
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        tahapanList.add(TahapanData(tahapanId, editText, btnUpload, imageView))
    }

    private fun loadExistingProgress() {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTrackingDetail(token, programId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!

                    data.tahapan.forEach { tahapan ->
                        val index = tahapanList.indexOfFirst { it.tahapanId == tahapan.tahapan_id }
                        if (index != -1) {
                            if (!tahapan.deskripsi.isNullOrEmpty()) {
                                tahapanList[index].editText.setText(tahapan.deskripsi)
                            }

                            if (!tahapan.foto.isNullOrEmpty()) {
                                var fileName = tahapan.foto
                                if (fileName.contains("/")) {
                                    fileName = fileName.substringAfterLast("/")
                                }
                                val fotoUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"
                                Glide.with(this@MonitoringProgram)
                                    .load(fotoUrl)
                                    .into(tahapanList[index].imageView)
                                tahapanList[index].imageView.visibility = View.VISIBLE
                            } else {
                                tahapanList[index].imageView.setImageDrawable(null)
                                tahapanList[index].imageView.visibility = View.GONE
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MonitoringProgram, "Gagal load data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAllProgress() {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            var anyUpdated = false

            for (tahapan in tahapanList) {
                val deskripsi = tahapan.editText.text.toString()
                val adaDeskripsi = deskripsi.isNotEmpty()
                val adaFoto = tahapan.currentPhotoUri != null

                // Hanya update jika ada deskripsi ATAU ada foto
                if (adaDeskripsi || adaFoto) {
                    anyUpdated = true

                    val programIdBody = programId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val tahapanIdBody = tahapan.tahapanId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val statusBody = "SELESAI".toRequestBody("text/plain".toMediaTypeOrNull())
                    val deskripsiBody = if (adaDeskripsi) {
                        deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())
                    } else null

                    var fotoPart: MultipartBody.Part? = null
                    tahapan.currentPhotoUri?.let { uri ->
                        try {
                            val stream: InputStream? = contentResolver.openInputStream(uri)
                            val bytes = stream?.readBytes()
                            stream?.close()
                            if (bytes != null) {
                                val reqBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                fotoPart = MultipartBody.Part.createFormData("foto", "foto_${tahapan.tahapanId}_${System.currentTimeMillis()}.jpg", reqBody)
                                Log.d("UPDATE_PROGRESS", "Tahapan ${tahapan.tahapanId} upload foto, size: ${bytes.size} bytes")
                            }
                        } catch (e: Exception) {
                            Log.e("UPDATE_PROGRESS", "Error preparing foto: ${e.message}")
                        }
                    }

                    try {
                        val response = ApiClient.apiService.updateProgressWithFoto(
                            token = token,
                            programId = programIdBody,
                            tahapanId = tahapanIdBody,
                            status = statusBody,
                            deskripsi = deskripsiBody,
                            foto = fotoPart
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            Log.d("UPDATE_PROGRESS", "Tahapan ${tahapan.tahapanId} berhasil diupdate")
                        } else {
                            Log.e("UPDATE_PROGRESS", "Tahapan ${tahapan.tahapanId} gagal: ${response.body()?.message}")
                            Toast.makeText(this@MonitoringProgram, "Gagal update tahapan ${tahapan.tahapanId}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("UPDATE_PROGRESS", "Error: ${e.message}")
                        Toast.makeText(this@MonitoringProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("UPDATE_PROGRESS", "Tahapan ${tahapan.tahapanId} dilewati (kosong)")
                }
            }

            if (anyUpdated) {
                Toast.makeText(this@MonitoringProgram, "Progress berhasil diupdate", Toast.LENGTH_SHORT).show()
                // Refresh data dan kembali ke DetailTracking
                val intent = Intent(this@MonitoringProgram, DetailTracking::class.java)
                intent.putExtra("program_id", programId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@MonitoringProgram, "Tidak ada progress yang diupdate (isi deskripsi atau upload foto)", Toast.LENGTH_LONG).show()
            }
        }
    }
}