package com.yourname.aplikasitrackingpendayagunaan.network

// ============================================================
// API CLIENT - AKSES SERVICE API
// ============================================================
// Fungsi:
// 1. Menyediakan instance ApiService untuk dipanggil dari seluruh aplikasi
// 2. Menggunakan RetrofitClient.instance untuk membuat ApiService
// 3. lazy: instance dibuat hanya saat pertama kali digunakan (efisien)
// ============================================================
object ApiClient {

    // ============================================================
    // API SERVICE INSTANCE
    // ============================================================
    // ApiService adalah interface yang berisi semua endpoint API
    // Retrofit akan membuat implementasi dari interface ini secara otomatis
    // ============================================================
    // Cara penggunaan di Activity/Fragment:
    // ApiClient.apiService.login(...)
    // ApiClient.apiService.getTrackingList(...)
    // ApiClient.apiService.updateProgressWithFoto(...)
    // ============================================================
    val apiService: ApiService by lazy {
        // RetrofitClient.instance adalah objek Retrofit yang sudah dikonfigurasi
        // .create(ApiService::class.java) membuat implementasi dari interface ApiService
        RetrofitClient.instance.create(ApiService::class.java)
    }
}