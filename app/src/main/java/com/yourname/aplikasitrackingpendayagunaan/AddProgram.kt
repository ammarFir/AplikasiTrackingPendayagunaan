package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.yourname.aplikasitrackingpendayagunaan.model.TambahProgramRequest
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.launch
import androidx.appcompat.widget.AppCompatButton

class AddProgram : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: android.net.Uri? = null

    private val pickImage = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            val canvasPhoto = findViewById<android.widget.LinearLayout>(R.id.canvasPhoto)
            val btnUpload   = findViewById<android.widget.TextView>(R.id.btnUploadFoto)
            btnUpload.text  = "✓ Foto dipilih"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_program)

        sessionManager = SessionManager(this)

        // Dropdown pilih program
        val options = listOf("Gerobak Berkah", "Micropreneur", "ZChicken", "Zmart")
        val adapter = ArrayAdapter(this, R.layout.list_item, options)
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.tvPilihProgram)
        autoComplete.setAdapter(adapter)
        autoComplete.setOnClickListener {
            autoComplete.showDropDown()
        }

        val etNamaMustahiq = findViewById<TextInputEditText>(R.id.tvNamaMustahiq)
        val etJenisUsaha   = findViewById<TextInputEditText>(R.id.tvJenisUsaha)
        val etTotalDana    = findViewById<TextInputEditText>(R.id.tvTotalDana)
        val etTanggalMulai = findViewById<TextInputEditText>(R.id.tvTanggalMulai)
        val canvasPhoto = findViewById<android.widget.LinearLayout>(R.id.canvasPhoto)
        canvasPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }
        val btnTambah      = findViewById<AppCompatButton>(R.id.btnTambah)

        btnTambah.setOnClickListener {
            val namaMustahiq = etNamaMustahiq.text.toString().trim()
            val namaProgram  = autoComplete.text.toString().trim()
            val jenisUsaha   = etJenisUsaha.text.toString().trim()
            val totalDana    = etTotalDana.text.toString().trim()
            val tanggalMulai = etTanggalMulai.text.toString().trim()

            // Validasi
            if (namaMustahiq.isEmpty() || namaProgram.isEmpty() ||
                jenisUsaha.isEmpty() || totalDana.isEmpty() || tanggalMulai.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnTambah.isEnabled = false
            btnTambah.text = "Loading..."

            val token = sessionManager.getToken() ?: return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val response = ApiClient.apiService.tambahProgram(
                        token = token,
                        request = TambahProgramRequest(
                            nama_mustahiq = namaMustahiq,
                            nama_program  = namaProgram,
                            jenis_usaha   = jenisUsaha,
                            total_dana    = totalDana.toDouble(),
                            tanggal_mulai = tanggalMulai
                        )
                    )
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@AddProgram, "Program berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AddProgram, MenuTracking::class.java))
                        finish()
                    } else {

                        Toast.makeText(this@AddProgram, response.body()?.message ?: "Gagal", Toast.LENGTH_SHORT).show()

                    }
                } catch (e: Exception) {
                    android.util.Log.e("AddProgram", "Error: ${e.message}", e)
                    Toast.makeText(this@AddProgram, "Koneksi gagal: ${e.message}", Toast.LENGTH_SHORT).show()

                } finally {
                    btnTambah.isEnabled = true
                    btnTambah.text = "Tambah"
                }
            }
        }
    }
}