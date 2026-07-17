package com.yourname.aplikasitrackingpendayagunaan

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
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
import org.json.JSONObject
import java.io.InputStream

// ============================================================
// PROFILE ACTIVITY - HALAMAN PROFIL USER
// ============================================================
// Fungsi:
// 1. Menampilkan data profil user (nama, email, no HP, avatar)
// 2. Mengubah avatar (upload foto baru)
// 3. Mengedit profil (nama, email, no HP)
// 4. Menyimpan perubahan ke server
// 5. Update session setelah perubahan
// ============================================================
class ProfileActivity : AppCompatActivity() {

    // ============================================================
    // DEKLARASI VARIABEL
    // ============================================================

    // SessionManager: menyimpan data session user (token, avatar, role)
    private lateinit var sessionManager: SessionManager

    // Komponen UI
    private lateinit var ivAvatar: ShapeableImageView           // Avatar berbentuk lingkaran
    private lateinit var etName: TextInputEditText              // Input nama
    private lateinit var etEmail: TextInputEditText             // Input email
    private lateinit var etPhone: TextInputEditText             // Input no HP
    private lateinit var btnChangeAvatar: TextView                  // Tombol ganti foto
    private lateinit var btnSave: Button                        // Tombol simpan profil

    // Data foto yang dipilih
    private var selectedImageUri: Uri? = null                   // URI gambar yang dipilih dari galeri
    private var newAvatarUploaded = false                       // Flag apakah ada foto baru yang dipilih

    // ============================================================
    // LAUNCHER: PICK IMAGE (GALERI)
    // ============================================================
    // Register activity result untuk memilih gambar dari galeri.
    // Ketika user memilih gambar, update ImageView dan set flag.
    // ============================================================
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Simpan URI gambar yang dipilih
            selectedImageUri = uri

            // Tampilkan preview gambar di ImageView
            ivAvatar.setImageURI(uri)

            // Tandai bahwa ada avatar baru yang diupload
            newAvatarUploaded = true
        }
    }

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghubungkan layout XML activity_profile ke activity ini
        setContentView(R.layout.activity_profile)

        // ============================================================
        // INISIALISASI SESSION MANAGER
        // ============================================================
        sessionManager = SessionManager(this)

        // ============================================================
        // INISIALISASI KOMPONEN UI
        // ============================================================
        ivAvatar = findViewById(R.id.ivAvatar)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar)
        btnSave = findViewById(R.id.btnSave)

        // ============================================================
        // EVENT CLICK: GANTI FOTO (BUKA GALERI)
        // ============================================================
        btnChangeAvatar.setOnClickListener {
            // Buka galeri untuk memilih gambar
            pickImageLauncher.launch("image/*")
        }

        // ============================================================
        // EVENT CLICK: SIMPAN PERUBAHAN PROFIL
        // ============================================================
        btnSave.setOnClickListener {
            // Jika ada avatar baru yang dipilih, upload avatar dulu
            if (newAvatarUploaded && selectedImageUri != null) {
                // Upload avatar lalu update profile
                uploadAvatarThenUpdateProfile()
            } else {
                // Langsung update profile tanpa upload avatar
                updateProfile()
            }
        }

        // ============================================================
        // LOAD DATA PROFIL DARI SERVER
        // ============================================================
        loadProfile()
    }

    // ============================================================
    // FUNGSI LOAD PROFIL DARI SERVER
    // ============================================================
    // Mengambil data profil user dari server via API.
    // Data yang diambil: nama, email, no HP, avatar.
    // ============================================================
    private fun loadProfile() {
        // Ambil token dari session
        val token = sessionManager.getToken()

        // Jika token null, user harus login ulang
        if (token == null) {
            Toast.makeText(this, "Silakan login kembali", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Jalankan proses di background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Panggil API untuk mendapatkan data profil
                val response = ApiClient.apiService.getProfile(token)

                // Kembali ke Main Thread untuk update UI
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Ambil data user dari response
                        val user = response.body()?.data

                        user?.let {
                            // Set data ke EditText
                            etName.setText(it.name)
                            etEmail.setText(it.email)
                            etPhone.setText(it.phone ?: "")  // Jika null, set string kosong

                            // ============================================================
                            // TAMPILKAN AVATAR (JIKA ADA)
                            // ============================================================
                            if (!it.avatar.isNullOrEmpty()) {
                                // Proses path avatar
                                var fileName = it.avatar
                                if (fileName.contains("/")) {
                                    fileName = fileName.substringAfterLast("/")
                                }

                                // Buat URL lengkap
                                val avatarUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"

                                // Tampilkan avatar menggunakan Glide
                                Glide.with(this@ProfileActivity)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.dot_active)  // Default saat loading
                                    .error(R.drawable.dot_active)        // Default jika error
                                    .into(ivAvatar)

                                // Simpan avatar ke session (untuk digunakan di halaman lain)
                                sessionManager.saveAvatar(it.avatar)
                            }
                        }

                    } else {
                        // Jika gagal, tampilkan pesan error
                        Toast.makeText(
                            this@ProfileActivity,
                            "Gagal memuat profil",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                // Jika terjadi error koneksi
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ============================================================
    // FUNGSI UPLOAD AVATAR LALU UPDATE PROFIL
    // ============================================================
    // Proses:
    // 1. Upload foto avatar ke server
    // 2. Jika berhasil, lanjut update profil
    // ============================================================
    private fun uploadAvatarThenUpdateProfile() {
        // Ambil token dari session
        val token = sessionManager.getToken()
        if (token == null) return

        // Ambil URI gambar yang dipilih
        val imageUri = selectedImageUri
        if (imageUri == null) return

        // Nonaktifkan tombol save dan ubah teks
        btnSave.isEnabled = false
        btnSave.text = "Menyimpan..."

        // Jalankan proses di background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ============================================================
                // BACA FILE GAMBAR DARI URI
                // ============================================================
                val stream: InputStream? = contentResolver.openInputStream(imageUri)
                val bytes = stream?.readBytes()
                stream?.close()

                if (bytes != null) {
                    // ============================================================
                    // BUAT REQUEST BODY UNTUK UPLOAD
                    // ============================================================
                    // Buat nama file: avatar_[user_id]_[timestamp].jpg
                    val fileName = "avatar_${sessionManager.getUserId()}_${System.currentTimeMillis()}.jpg"

                    // Buat RequestBody untuk file
                    val reqBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

                    // Buat MultipartBody.Part dengan nama field "avatar"
                    val fotoPart = MultipartBody.Part.createFormData("avatar", fileName, reqBody)

                    // ============================================================
                    // PANGGIL API UPLOAD AVATAR
                    // ============================================================
                    val response = ApiClient.apiService.updateAvatarSimple(token, fotoPart)

                    // Kembali ke Main Thread untuk update UI
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Upload avatar berhasil, lanjut update profile
                            updateProfile()
                        } else {
                            // Upload avatar gagal
                            btnSave.isEnabled = true
                            btnSave.text = "Simpan Perubahan"
                            Toast.makeText(
                                this@ProfileActivity,
                                "Gagal upload avatar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            } catch (e: Exception) {
                // Jika terjadi error
                withContext(Dispatchers.Main) {
                    btnSave.isEnabled = true
                    btnSave.text = "Simpan Perubahan"
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ============================================================
    // FUNGSI UPDATE PROFIL
    // ============================================================
    // Mengirim data profil yang sudah diedit ke server.
    // Data yang dikirim: nama, email, no HP.
    // ============================================================
    private fun updateProfile() {
        // Ambil token dari session
        val token = sessionManager.getToken()
        if (token == null) return

        // Ambil input user dan bersihkan spasi
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // ============================================================
        // VALIDASI INPUT
        // ============================================================
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan email wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // Jalankan proses di background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ============================================================
                // BUAT REQUEST BODY (JSON)
                // ============================================================
                val requestBody = JSONObject().apply {
                    put("name", name)
                    put("email", email)
                    put("phone", phone)
                }.toString()

                // Konversi ke RequestBody dengan tipe application/json
                val body = requestBody.toRequestBody("application/json".toMediaTypeOrNull())

                // ============================================================
                // PANGGIL API UPDATE PROFIL
                // ============================================================
                val response = ApiClient.apiService.updateProfile(token, body)

                // Kembali ke Main Thread untuk update UI
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        // ============================================================
                        // UPDATE PROFIL BERHASIL
                        // ============================================================

                        // Ambil data profil terbaru dari server
                        val profileResponse = ApiClient.apiService.getProfile(token)

                        if (profileResponse.isSuccessful && profileResponse.body()?.success == true) {
                            val user = profileResponse.body()?.data

                            user?.let {
                                // Update session dengan data terbaru
                                sessionManager.saveSession(
                                    token = sessionManager.getToken()!!,
                                    userId = sessionManager.getUserId(),
                                    name = it.name,
                                    email = it.email,
                                    role = sessionManager.getRole() ?: "donatur",
                                    avatar = it.avatar
                                )
                            }
                        }

                        // Tampilkan pesan sukses
                        Toast.makeText(
                            this@ProfileActivity,
                            "Profil berhasil diupdate",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Kembali ke MainActivity dengan refresh data
                        val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()

                    } else {
                        // ============================================================
                        // UPDATE PROFIL GAGAL
                        // ============================================================
                        btnSave.isEnabled = true
                        btnSave.text = "Simpan Perubahan"
                        Toast.makeText(
                            this@ProfileActivity,
                            "Gagal update profil",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                // Jika terjadi error koneksi
                withContext(Dispatchers.Main) {
                    btnSave.isEnabled = true
                    btnSave.text = "Simpan Perubahan"
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}