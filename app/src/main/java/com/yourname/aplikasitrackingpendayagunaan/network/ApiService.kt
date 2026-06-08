package com.yourname.aplikasitrackingpendayagunaan.network

import com.yourname.aplikasitrackingpendayagunaan.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

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

    @Multipart
    @POST("api/tracking/update_progress.php")
    suspend fun updateProgressWithFoto(
        @Header("Authorization") token: String,
        @Part("program_id") programId: RequestBody,
        @Part("tahapan_id") tahapanId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): Response<UpdateProgressResponse>

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

    @Multipart
    @POST("api/tracking/tambah_program.php")
    suspend fun tambahProgram(
        @Header("Authorization") token: String,
        @Part("nama_mustahiq") namaMustahiq: RequestBody,
        @Part("alamat") alamat: RequestBody,
        @Part("nama_program") namaProgram: RequestBody,
        @Part("jenis_usaha") jenisUsaha: RequestBody,
        @Part("total_dana") totalDana: RequestBody,
        @Part("tanggal_mulai") tanggalMulai: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<TambahProgramResponse>

    @GET("api/auth/profile.php")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @Multipart
    @POST("api/auth/update_avatar.php")
    suspend fun updateAvatar(
        @Header("Authorization") token: String,
        @Part("user_id") userId: RequestBody,
        @Part avatar: MultipartBody.Part
    ): Response<BaseResponse>

    @Multipart
    @POST("api/auth/update_avatar.php")
    suspend fun updateAvatarSimple(
        @Header("Authorization") token: String,
        @Part avatar: MultipartBody.Part
    ): Response<BaseResponse>

    @POST("api/auth/update_profile.php")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body body: RequestBody
    ): Response<BaseResponse>

    @GET("api/campaign/list.php")
    suspend fun getCampaigns(@Header("Authorization") token: String): Response<CampaignResponse>

    @GET("api/sayings/list.php")
    suspend fun getSayings(
        @Header("Authorization") token: String
    ): Response<SayingsResponse>

    @GET("api/testimonials/list.php")
    suspend fun getTestimonials(
        @Header("Authorization") token: String
    ): Response<TestimonialResponse>
}