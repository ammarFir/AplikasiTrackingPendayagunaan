package com.yourname.aplikasitrackingpendayagunaan.network

import com.yourname.aplikasitrackingpendayagunaan.model.LoginRequest
import com.yourname.aplikasitrackingpendayagunaan.model.LoginResponse
import com.yourname.aplikasitrackingpendayagunaan.model.RegisterRequest
import com.yourname.aplikasitrackingpendayagunaan.model.RegisterResponse
import com.yourname.aplikasitrackingpendayagunaan.model.MustahiqResponse
import com.yourname.aplikasitrackingpendayagunaan.model.TrackingListResponse
import com.yourname.aplikasitrackingpendayagunaan.model.TrackingDetailResponse
import com.yourname.aplikasitrackingpendayagunaan.model.UpdateProgressRequest
import com.yourname.aplikasitrackingpendayagunaan.model.UpdateProgressResponse
import com.yourname.aplikasitrackingpendayagunaan.model.LaporanResponse
import retrofit2.Response
import retrofit2.http.*
import com.yourname.aplikasitrackingpendayagunaan.model.TambahProgramRequest
import com.yourname.aplikasitrackingpendayagunaan.model.TambahProgramResponse

interface ApiService {

    @POST("api/auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/mustahiq/list.php")
    suspend fun getMustahiq(@Header("Authorization") token: String): Response<MustahiqResponse>

    @GET("api/tracking/list.php")
    suspend fun getTrackingList(@Header("Authorization") token: String): Response<TrackingListResponse>

    @GET("api/tracking/detail.php")
    suspend fun getTrackingDetail(
        @Header("Authorization") token: String,
        @Query("id") id: Int
    ): Response<TrackingDetailResponse>

    @POST("api/tracking/update_progress.php")
    suspend fun updateProgress(
        @Header("Authorization") token: String,
        @Body request: UpdateProgressRequest
    ): Response<UpdateProgressResponse>

    @GET("api/tracking/laporan.php")
    suspend fun getLaporan(
        @Header("Authorization") token: String,
        @Query("id") id: Int
    ): Response<LaporanResponse>

    @POST("api/tracking/tambah_program.php")
    suspend fun tambahProgram(
        @Header("Authorization") token: String,
        @Body request: TambahProgramRequest
    ): Response<TambahProgramResponse>


}