package com.yourname.aplikasitrackingpendayagunaan

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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

// ============================================================
// ADD PROGRAM - HALAMAN TAMBAH PROGRAM BARU
// ============================================================
// Fungsi:
// 1. Menampilkan form tambah program (nama mustahiq, alamat, program, jenis usaha, total dana, tanggal mulai)
// 2. Upload foto mustahiq (opsional)
// 3. DatePicker untuk memilih tanggal mulai
// 4. Dropdown pilihan program (4 program tetap)
// 5. Simpan data ke server via API
// 6. Avatar user di header (klik ke profile)
// ============================================================
class AddProgram : AppCompatActivity() {

    // ============================================================
    // DEKLARASI VARIABEL
    // ============================================================

    // SessionManager: menyimpan data session user (token, avatar)
    private lateinit var sessionManager: SessionManager

    // URI gambar yang dipilih dari galeri
    private var selectedImageUri: Uri? = null

    // ============================================================
    // LAUNCHER: PICK IMAGE (GALERI)
    // ============================================================
    // Register activity result untuk memilih gambar dari galeri.
    // Ketika user memilih gambar, update TextView "btnUploadFoto".
    // ============================================================
    private val pickImage = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Simpan URI gambar yang dipilih
            selectedImageUri = it

            // Update teks tombol upload menjadi "✓ Foto dipilih"
            val btnUpload = findViewById<android.widget.TextView>(R.id.btnUploadFoto)
            btnUpload.text = "✓ Foto dipilih"
        }
    }

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    // Dipanggil saat activity pertama kali dibuat.
    // Inisialisasi komponen UI, setup dropdown, date picker, dan load data.
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghubungkan layout XML activity_add_program ke activity ini
        setContentView(R.layout.activity_add_program)
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
        sessionManager = SessionManager(this)

        // ============================================================
        // SETUP DROPDOWN PILIH PROGRAM (4 PROGRAM TETAP)
        // ============================================================
        // Daftar program yang tersedia (hardcoded dari database)
        val options = listOf("Gerobak Berkah", "Micropreneur", "ZChicken", "Zmart")

        // Buat adapter untuk AutoCompleteTextView
        val adapter = ArrayAdapter(this, R.layout.list_item, options)

        // Cari AutoCompleteTextView di layout
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.tvPilihProgram)

        // Set adapter ke AutoCompleteTextView
        autoComplete.setAdapter(adapter)

        // Tampilkan dropdown saat diklik
        autoComplete.setOnClickListener { autoComplete.showDropDown() }

        // ============================================================
        // INISIALISASI KOMPONEN UI
        // ============================================================
        val etNamaMustahiq = findViewById<TextInputEditText>(R.id.tvNamaMustahiq)
        val etAlamat = findViewById<TextInputEditText>(R.id.tvAlamat)
        val etJenisUsaha = findViewById<TextInputEditText>(R.id.tvJenisUsaha)
        val etTotalDana = findViewById<TextInputEditText>(R.id.tvTotalDana)
        val etTanggalMulai = findViewById<TextInputEditText>(R.id.tvTanggalMulai)
        val canvasPhoto = findViewById<android.widget.LinearLayout>(R.id.canvasPhoto)
        val btnTambah = findViewById<AppCompatButton>(R.id.btnTambah)

        // ============================================================
        // SETUP IMG PROFILE -> KE HALAMAN PROFILE
        // ============================================================
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // ============================================================
        // SETUP DATE PICKER UNTUK TANGGAL MULAI
        // ============================================================
        // Saat user klik field tanggal, muncul DatePickerDialog
        etTanggalMulai.setOnClickListener {
            showDatePickerDialog()
        }

        // ============================================================
        // SETUP UPLOAD FOTO (KLIK AREA UPLOAD)
        // ============================================================
        // Saat user klik area upload foto, buka galeri
        canvasPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        // ============================================================
        // EVENT CLICK: TOMBOL TAMBAH
        // ============================================================
        // Validasi semua field, lalu kirim data ke server.
        // ============================================================
        btnTambah.setOnClickListener {
            // Ambil input user dan bersihkan spasi
            val namaMustahiq = etNamaMustahiq.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()
            val namaProgram = autoComplete.text.toString().trim()
            val jenisUsaha = etJenisUsaha.text.toString().trim()
            val totalDana = etTotalDana.text.toString().trim()
            val tanggalMulai = etTanggalMulai.text.toString().trim()

            // ============================================================
            // VALIDASI INPUT
            // ============================================================
            if (namaMustahiq.isEmpty() || alamat.isEmpty() || namaProgram.isEmpty() ||
                jenisUsaha.isEmpty() || totalDana.isEmpty() || tanggalMulai.isEmpty()
            ) {
                // Jika ada field kosong, tampilkan pesan error
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ============================================================
            // PROSES SIMPAN DATA
            // ============================================================

            // Nonaktifkan tombol tambah
            btnTambah.isEnabled = false
            btnTambah.text = "Loading..."

            // Ambil token dari session
            val token = sessionManager.getToken()
            if (token == null) {
                // Jika token null, kembali ke login
                btnTambah.isEnabled = true
                btnTambah.text = "Tambah"
                return@setOnClickListener
            }

            // ============================================================
            // KIRIM DATA KE SERVER VIA API
            // ============================================================
            lifecycleScope.launch {
                try {
                    // ============================================================
                    // BUAT REQUEST BODY UNTUK SETIAP FIELD
                    // ============================================================
                    // Setiap field dikonversi menjadi RequestBody dengan tipe "text/plain"
                    val rNama = namaMustahiq.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rAlamat = alamat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rProgram = namaProgram.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rUsaha = jenisUsaha.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rDana = totalDana.toRequestBody("text/plain".toMediaTypeOrNull())
                    val rTanggal = tanggalMulai.toRequestBody("text/plain".toMediaTypeOrNull())

                    // ============================================================
                    // PROSES UPLOAD FOTO (JIKA ADA)
                    // ============================================================
                    var fotoPart: MultipartBody.Part? = null

                    // Jika user memilih foto, proses menjadi MultipartBody.Part
                    selectedImageUri?.let { uri ->
                        try {
                            // Baca file dari URI
                            val stream: InputStream? = contentResolver.openInputStream(uri)
                            val bytes = stream?.readBytes()
                            stream?.close()

                            if (bytes != null) {
                                // Buat RequestBody untuk file foto
                                val reqBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

                                // Buat MultipartBody.Part dengan nama field "foto"
                                fotoPart = MultipartBody.Part.createFormData("foto", "foto.jpg", reqBody)
                            }
                        } catch (e: Exception) {
                            // Jika error saat baca file, ignore
                            e.printStackTrace()
                        }
                    }

                    // ============================================================
                    // PANGGIL API TAMBAH PROGRAM
                    // ============================================================
                    val response = ApiClient.apiService.tambahProgram(
                        token = token,
                        namaMustahiq = rNama,
                        alamat = rAlamat,
                        namaProgram = rProgram,
                        jenisUsaha = rUsaha,
                        totalDana = rDana,
                        tanggalMulai = rTanggal,
                        foto = fotoPart  // Bisa null jika tidak upload foto
                    )

                    // ============================================================
                    // PROSES RESPONSE DARI SERVER
                    // ============================================================
                    if (response.isSuccessful && response.body()?.success == true) {
                        // ============================================================
                        // TAMBAH PROGRAM BERHASIL
                        // ============================================================
                        Toast.makeText(
                            this@AddProgram,
                            "Program berhasil ditambahkan!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Kembali ke halaman MenuTracking
                        startActivity(Intent(this@AddProgram, MenuTracking::class.java))
                        finish()

                    } else {
                        // ============================================================
                        // TAMBAH PROGRAM GAGAL
                        // ============================================================
                        Toast.makeText(
                            this@AddProgram,
                            response.body()?.message ?: "Gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    // ============================================================
                    // ERROR KONEKSI / EXCEPTION
                    // ============================================================
                    android.util.Log.e("AddProgram", "Error: ${e.message}", e)
                    Toast.makeText(
                        this@AddProgram,
                        "Koneksi gagal: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                } finally {
                    // ============================================================
                    // AKHIR PROSES (SELALU DIJALANKAN)
                    // ============================================================
                    // Aktifkan kembali tombol tambah
                    btnTambah.isEnabled = true
                    btnTambah.text = "Tambah"
                }
            }
        }

        // ============================================================
        // LOAD AVATAR USER
        // ============================================================
        loadUserProfile()
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

    // ============================================================
    // FUNGSI SHOW DATE PICKER DIALOG
    // ============================================================
    // Menampilkan dialog pemilihan tanggal.
    // Setelah user memilih tanggal, hasilnya di-set ke field etTanggalMulai.
    // ============================================================
    private fun showDatePickerDialog() {
        // Ambil tanggal sekarang sebagai default
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Buat DatePickerDialog
        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format tanggal: YYYY-MM-DD
                val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"

                // Set hasil ke field tanggal mulai
                findViewById<TextInputEditText>(R.id.tvTanggalMulai).setText(formattedDate)
            },
            year, month, day
        )

        // Tampilkan dialog
        datePicker.show()
    }
}