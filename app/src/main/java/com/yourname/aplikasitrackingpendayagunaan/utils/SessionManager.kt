package com.yourname.aplikasitrackingpendayagunaan.utils

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.content.Context
import android.content.SharedPreferences

// ============================================================
// SESSION MANAGER - KELOLA SESSION USER
// ============================================================
// Fungsi:
// 1. Menyimpan data session user (token, user_id, name, email, role, avatar)
// 2. Mengambil data session yang tersimpan
// 3. Mengecek status login (isLoggedIn)
// 4. Menghapus session saat logout (clearSession)
// 5. Menggunakan SharedPreferences sebagai penyimpanan lokal
// ============================================================
class SessionManager(context: Context) {

    // ============================================================
    // SHARED PREFERENCES - PENYIMPANAN DATA LOKAL
    // ============================================================
    // Nama file: "bakti_bersama_session"
    // Mode: Context.MODE_PRIVATE (hanya bisa diakses oleh aplikasi ini)
    // ============================================================
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bakti_bersama_session", Context.MODE_PRIVATE)

    // ============================================================
    // KEY CONSTANTS - NAMA KOLOM DI SHARED PREFERENCES
    // ============================================================
    companion object {
        const val KEY_TOKEN = "token"       // Token autentikasi
        const val KEY_USER_ID = "user_id"   // ID user
        const val KEY_NAME = "name"         // Nama user
        const val KEY_EMAIL = "email"       // Email user
        const val KEY_ROLE = "role"         // Role user (admin/donatur)
        const val KEY_AVATAR = "avatar"     // Nama file avatar
    }

    // ============================================================
    // FUNGSI SAVE SESSION
    // ============================================================
    // Menyimpan semua data session ke SharedPreferences
    // Dipanggil setelah login berhasil atau update profile
    // ============================================================
    fun saveSession(
        token: String,
        userId: Int,
        name: String,
        email: String,
        role: String,
        avatar: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)           // Simpan token
            putInt(KEY_USER_ID, userId)           // Simpan user_id
            putString(KEY_NAME, name)             // Simpan nama
            putString(KEY_EMAIL, email)           // Simpan email
            putString(KEY_ROLE, role)             // Simpan role
            if (avatar != null) putString(KEY_AVATAR, avatar) // Simpan avatar (jika ada)
            apply() // Simpan secara asynchronous
        }
    }

    // ============================================================
    // FUNGSI GET DATA SESSION
    // ============================================================
    // Mengambil data dari SharedPreferences
    // Jika tidak ada, return nilai default (null atau -1)
    // ============================================================

    /** Ambil token user */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /** Ambil ID user (default -1 jika tidak ada) */
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    /** Ambil nama user (null jika tidak ada) */
    fun getName(): String? = prefs.getString(KEY_NAME, null)

    /** Ambil email user (null jika tidak ada) */
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /** Ambil role user (null jika tidak ada) */
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    /** Ambil nama file avatar (null jika tidak ada) */
    fun getAvatar(): String? = prefs.getString(KEY_AVATAR, null)

    // ============================================================
    // FUNGSI SAVE AVATAR (UPDATE)
    // ============================================================
    // Menyimpan avatar baru (dipanggil setelah upload avatar berhasil)
    // ============================================================
    fun saveAvatar(avatar: String) {
        prefs.edit().putString(KEY_AVATAR, avatar).apply()
    }

    // ============================================================
    // FUNGSI CEK STATUS LOGIN
    // ============================================================
    // Return true jika token ada (user sudah login)
    // Return false jika token null (user belum login)
    // ============================================================
    fun isLoggedIn(): Boolean = getToken() != null

    // ============================================================
    // FUNGSI CLEAR SESSION (LOGOUT)
    // ============================================================
    // Menghapus semua data session dari SharedPreferences
    // Dipanggil saat user logout
    // ============================================================
    fun clearSession() = prefs.edit().clear().apply()
}