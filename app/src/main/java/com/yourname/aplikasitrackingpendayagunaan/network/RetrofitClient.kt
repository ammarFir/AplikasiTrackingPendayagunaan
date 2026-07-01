package com.yourname.aplikasitrackingpendayagunaan.network

// ============================================================
// IMPORT LIBRARY
// ============================================================
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ============================================================
// RETROFIT CLIENT - KONFIGURASI KONEKSI KE SERVER
// ============================================================
// Fungsi:
// 1. Mengatur BASE_URL (alamat server)
// 2. Mengatur logging interceptor untuk debugging (lihat request/response di Logcat)
// 3. Membuat instance Retrofit yang digunakan oleh ApiClient
// 4. Menggunakan Gson untuk konversi JSON ke Object Kotlin
// ============================================================
object RetrofitClient {

    // ============================================================
    // BASE URL - ALAMAT SERVER
    // ============================================================
    // Ganti dengan IP komputer Anda (bukan localhost)
    // IP ini harus bisa diakses dari emulator/HP
    // Contoh: "http://10.96.174.182/bakti_bersama/"
    // ============================================================
    const val BASE_URL = "http://10.96.174.182/bakti_bersama/"

    // ============================================================
    // LOGGING INTERCEPTOR
    // ============================================================
    // Digunakan untuk menampilkan log request dan response di Logcat
    // Level BODY: menampilkan semua detail (header, body, dll)
    // Sangat berguna untuk debugging API
    // ============================================================
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ============================================================
    // OKHTTP CLIENT
    // ============================================================
    // Konfigurasi HTTP client dengan interceptor logging
    // ============================================================
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)  // Tambahkan interceptor untuk logging
        .build()

    // ============================================================
    // RETROFIT INSTANCE
    // ============================================================
    // Membuat objek Retrofit dengan konfigurasi:
    // 1. baseUrl: alamat server
    // 2. client: OkHttpClient dengan interceptor
    // 3. converter: Gson untuk parse JSON
    // ============================================================
    // lazy: instance dibuat hanya saat pertama kali digunakan
    // ============================================================
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                      // Alamat server
            .client(httpClient)                     // HTTP client dengan interceptor
            .addConverterFactory(GsonConverterFactory.create()) // JSON parser
            .build()
    }
}