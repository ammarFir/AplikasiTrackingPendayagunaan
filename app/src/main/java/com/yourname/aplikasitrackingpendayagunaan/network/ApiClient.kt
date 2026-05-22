package com.yourname.aplikasitrackingpendayagunaan.network

object ApiClient {
    val apiService: ApiService by lazy {
        RetrofitClient.instance.create(ApiService::class.java)
    }
}