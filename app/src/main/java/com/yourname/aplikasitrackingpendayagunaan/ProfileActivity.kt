package com.yourname.aplikasitrackingpendayagunaan

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
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

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var ivAvatar: ShapeableImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var btnChangeAvatar: Button
    private lateinit var btnSave: Button

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = uri
            ivAvatar.setImageURI(uri)
            uploadAvatar(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        ivAvatar = findViewById(R.id.ivAvatar)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar)
        btnSave = findViewById(R.id.btnSave)

        btnChangeAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            updateProfile()
        }

        loadProfile()
    }

    private fun loadProfile() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Silakan login kembali", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getProfile(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.data
                        user?.let {
                            etName.setText(it.name)
                            etEmail.setText(it.email)
                            etPhone.setText(it.phone ?: "")

                            if (!it.avatar.isNullOrEmpty()) {
                                var fileName = it.avatar
                                if (fileName.contains("/")) {
                                    fileName = fileName.substringAfterLast("/")
                                }
                                val avatarUrl = "http://10.0.2.2/bakti_bersama/uploads/$fileName"
                                Glide.with(this@ProfileActivity)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.dot_active)
                                    .error(R.drawable.dot_active)
                                    .into(ivAvatar)
                            }
                        }
                    } else {
                        Toast.makeText(this@ProfileActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateProfile() {
        val token = sessionManager.getToken() ?: return
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan email wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Menyimpan..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = JSONObject().apply {
                    put("name", name)
                    put("email", email)
                    put("phone", phone)
                }.toString()

                val body = requestBody.toRequestBody("application/json".toMediaTypeOrNull())
                val response = ApiClient.apiService.updateProfile(token, body)

                withContext(Dispatchers.Main) {
                    btnSave.isEnabled = true
                    btnSave.text = "Simpan Perubahan"

                    if (response.isSuccessful && response.body()?.success == true) {
                        // Update session
                        sessionManager.saveSession(
                            token = sessionManager.getToken()!!,
                            userId = sessionManager.getUserId(),
                            name = name,
                            email = email,
                            role = sessionManager.getRole() ?: "donatur"
                        )
                        Toast.makeText(this@ProfileActivity, "Profil berhasil diupdate", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Gagal update profil", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnSave.isEnabled = true
                    btnSave.text = "Simpan Perubahan"
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadAvatar(imageUri: Uri) {
        val token = sessionManager.getToken() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stream: InputStream? = contentResolver.openInputStream(imageUri)
                val bytes = stream?.readBytes()
                stream?.close()

                if (bytes != null) {
                    val reqBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val fotoPart = MultipartBody.Part.createFormData("avatar", "avatar_${System.currentTimeMillis()}.jpg", reqBody)

                    val response = ApiClient.apiService.updateAvatar(token, fotoPart)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@ProfileActivity, "Avatar berhasil diupdate", Toast.LENGTH_SHORT).show()
                            loadProfile()
                        } else {
                            Toast.makeText(this@ProfileActivity, "Gagal update avatar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error upload: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}