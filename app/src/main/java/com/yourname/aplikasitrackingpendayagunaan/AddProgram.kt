package com.yourname.aplikasitrackingpendayagunaan

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
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
import java.util.Calendar

class AddProgram : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            val btnUpload = findViewById<android.widget.TextView>(R.id.btnUploadFoto)
            btnUpload.text = "✓ Foto dipilih"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_program)

        sessionManager = SessionManager(this)

        val options = listOf("Gerobak Berkah", "Micropreneur", "ZChicken", "Zmart")
        val adapter = ArrayAdapter(this, R.layout.list_item, options)
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.tvPilihProgram)
        autoComplete.setAdapter(adapter)
        autoComplete.setOnClickListener { autoComplete.showDropDown() }

        val etNamaMustahiq = findViewById<TextInputEditText>(R.id.tvNamaMustahiq)
        val etAlamat = findViewById<TextInputEditText>(R.id.tvAlamat)
        val etJenisUsaha = findViewById<TextInputEditText>(R.id.tvJenisUsaha)
        val etTotalDana = findViewById<TextInputEditText>(R.id.tvTotalDana)
        val etTanggalMulai = findViewById<TextInputEditText>(R.id.tvTanggalMulai)
        val canvasPhoto = findViewById<android.widget.LinearLayout>(R.id.canvasPhoto)
        val btnTambah = findViewById<AppCompatButton>(R.id.btnTambah)

        // IMG PROFILE - klik ke halaman Profile
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // DatePicker untuk Tanggal Mulai
        etTanggalMulai.setOnClickListener {
            showDatePickerDialog()
        }

        canvasPhoto.setOnClickListener { pickImage.launch("image/*") }

        btnTambah.setOnClickListener {
            val namaMustahiq = etNamaMustahiq.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()
            val namaProgram = autoComplete.text.toString().trim()
            val jenisUsaha = etJenisUsaha.text.toString().trim()
            val totalDana = etTotalDana.text.toString().trim()
            val tanggalMulai = etTanggalMulai.text.toString().trim()

            if (namaMustahiq.isEmpty() || alamat.isEmpty() || namaProgram.isEmpty() ||
                jenisUsaha.isEmpty() || totalDana.isEmpty() || tanggalMulai.isEmpty()
            ) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnTambah.isEnabled = false
            btnTambah.text = "Loading..."

            val token = sessionManager.getToken() ?: return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val rNama = namaMustahiq.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rAlamat = alamat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rProgram = namaProgram.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rUsaha = jenisUsaha.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rDana = totalDana.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rTanggal = tanggalMulai.toRequestBody("text/plain".toMediaTypeOrNull())

                    var fotoPart: MultipartBody.Part? = null
                    selectedImageUri?.let { uri ->
                        val stream: InputStream? = contentResolver.openInputStream(uri)
                        val bytes = stream?.readBytes()
                        stream?.close()
                        if (bytes != null) {
                            val reqBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                            fotoPart = MultipartBody.Part.createFormData("foto", "foto.jpg", reqBody)
                        }
                    }

                    val response = ApiClient.apiService.tambahProgram(
                        token = token,
                        namaMustahiq = rNama,
                        alamat = rAlamat,
                        namaProgram = rProgram,
                        jenisUsaha = rUsaha,
                        totalDana = rDana,
                        tanggalMulai = rTanggal,
                        foto = fotoPart
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@AddProgram,
                            "Program berhasil ditambahkan!",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@AddProgram, MenuTracking::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddProgram,
                            response.body()?.message ?: "Gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AddProgram", "Error: ${e.message}", e)
                    Toast.makeText(this@AddProgram, "Koneksi gagal: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                } finally {
                    btnTambah.isEnabled = true
                    btnTambah.text = "Tambah"
                }
            }
        }

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
                                Glide.with(this@AddProgram)
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

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                findViewById<TextInputEditText>(R.id.tvTanggalMulai).setText(formattedDate)
            },
            year, month, day
        )
        datePicker.show()
    }
}