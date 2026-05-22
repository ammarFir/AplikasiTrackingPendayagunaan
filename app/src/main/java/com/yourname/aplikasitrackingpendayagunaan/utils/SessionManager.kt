package com.yourname.aplikasitrackingpendayagunaan.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bakti_bersama_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_TOKEN   = "token"
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME    = "name"
        const val KEY_EMAIL   = "email"
        const val KEY_ROLE    = "role"
    }

    fun saveSession(token: String, userId: Int, name: String, email: String, role: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_NAME, name)
            putString(KEY_EMAIL, email)
            putString(KEY_ROLE, role)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): Int    = prefs.getInt(KEY_USER_ID, -1)
    fun getName(): String?  = prefs.getString(KEY_NAME, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getRole(): String?  = prefs.getString(KEY_ROLE, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() = prefs.edit().clear().apply()
}