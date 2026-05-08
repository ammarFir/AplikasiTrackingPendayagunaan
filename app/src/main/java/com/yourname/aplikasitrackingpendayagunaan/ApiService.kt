package com.yourname.aplikasitrackingpendayagunaan

import com.yourname.aplikasitrackingpendayagunaan.model.DetailPenerimaResponse
import com.yourname.aplikasitrackingpendayagunaan.model.PenerimaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("get_penerima.php")
    fun getPenerima(): Call<PenerimaResponse>

    @GET("get_detail_penerima.php")
    fun getDetailPenerima(
        @Query("penerima_id") penerimaId: Int
    ): Call<DetailPenerimaResponse>
}