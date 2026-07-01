package com.yourname.aplikasitrackingpendayagunaan.model

// ============================================================
// IMPORT LIBRARY
// ============================================================
import com.google.gson.annotations.SerializedName

// ============================================================
// MODELS - SEMUA DATA CLASS UNTUK REQUEST & RESPONSE API
// ============================================================
// Fungsi:
// 1. Mendefinisikan struktur data untuk request (kirim ke server)
// 2. Mendefinisikan struktur data untuk response (terima dari server)
// 3. Menggunakan data class agar mudah digunakan (copy, toString, equals)
// 4. Menggunakan @SerializedName jika nama field di JSON berbeda
// ============================================================

// ============================================================
// AUTHENTICATION (LOGIN & REGISTER)
// ============================================================

/**
 * LoginRequest
 * Request untuk login user
 * Field: email, password
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * LoginResponse
 * Response dari login
 * success: true jika berhasil
 * message: pesan dari server
 * data: LoginData (token, user_id, name, email, phone, role, avatar)
 */
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)

/**
 * LoginData
 * Data user yang diterima setelah login
 * token: token autentikasi (disimpan di SessionManager)
 * user_id: ID user
 * name: nama user
 * email: email user
 * phone: nomor telepon (bisa null)
 * role: admin atau donatur
 * avatar: nama file avatar (bisa null)
 */
data class LoginData(
    val token: String,
    val user_id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val avatar: String?
)

/**
 * RegisterRequest
 * Request untuk registrasi user
 * Field: name, email, password, phone
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
)

/**
 * RegisterResponse
 * Response dari registrasi
 * success: true jika berhasil
 * message: pesan dari server
 * data: RegisterData (user_id, name, email, phone, role)
 */
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val data: RegisterData?
)

/**
 * RegisterData
 * Data user yang diterima setelah registrasi
 */
data class RegisterData(
    val user_id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String
)

// ============================================================
// PROFILE
// ============================================================

/**
 * ProfileResponse
 * Response untuk get profile
 * data: ProfileData (id, name, email, phone, avatar)
 */
data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val data: ProfileData?
)

/**
 * ProfileData
 * Data profil user
 */
data class ProfileData(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar: String?
)

// ============================================================
// MUSTAHIQ
// ============================================================

/**
 * MustahiqResponse
 * Response untuk list mustahiq
 * data: List<Mustahiq>
 */
data class MustahiqResponse(
    val success: Boolean,
    val message: String,
    val data: List<Mustahiq>?
)

/**
 * Mustahiq
 * Data mustahiq (penerima zakat)
 */
data class Mustahiq(
    val id: Int,
    val nama: String,
    val nik: String?,
    val alamat: String?,
    val no_hp: String?,
    val foto: String?,
    val status: String
)

// ============================================================
// TRACKING PROGRAM
// ============================================================

/**
 * TrackingListResponse
 * Response untuk list program tracking
 * data: TrackingListData (summary + programs)
 */
data class TrackingListResponse(
    val success: Boolean,
    val message: String,
    val data: TrackingListData?
)

/**
 * TrackingListData
 * Data list program + summary cards
 * summary: program_aktif, program_selesai, total_dana_digunakan
 * programs: List<TrackingProgram>
 */
data class TrackingListData(
    val summary: TrackingSummary,
    val programs: List<TrackingProgram>
)

/**
 * TrackingSummary
 * Data ringkasan (summary cards)
 * program_aktif: jumlah program aktif
 * program_selesai: jumlah program selesai
 * total_dana_digunakan: total dana yang digunakan
 */
data class TrackingSummary(
    val program_aktif: Int,
    val program_selesai: Int,
    val total_dana_digunakan: Double
)

/**
 * TrackingProgram
 * Data program tracking (untuk list)
 * id: ID program
 * kode_program: kode unik program
 * nama_program: nama program
 * jenis_usaha: jenis usaha mustahiq
 * total_dana: total dana program
 * tanggal_mulai: tanggal mulai program
 * status: AKTIF atau SELESAI
 * nama_mustahiq: nama mustahiq
 * foto_mustahiq: foto mustahiq
 * progress_persen: progress dalam persen
 * total_tahapan_selesai: jumlah tahapan selesai
 * alamat_mustahiq: alamat mustahiq
 */
data class TrackingProgram(
    val id: Int,
    val kode_program: String?,
    val nama_program: String,
    val jenis_usaha: String?,
    val total_dana: Double,
    val tanggal_mulai: String?,
    val status: String,
    val nama_mustahiq: String?,
    val foto_mustahiq: String?,
    val progress_persen: Int,
    val total_tahapan_selesai: Int,
    val alamat_mustahiq: String?
)

// ============================================================
// TRACKING DETAIL
// ============================================================

/**
 * TrackingDetailResponse
 * Response untuk detail program
 * data: TrackingDetail
 */
data class TrackingDetailResponse(
    val success: Boolean,
    val message: String,
    val data: TrackingDetail?
)

/**
 * TrackingDetail
 * Data detail program + 8 tahapan
 * tahapan: List<Tahapan>
 */
data class TrackingDetail(
    val id: Int,
    val kode_program: String?,
    val nama_program: String,
    val jenis_usaha: String?,
    val total_dana: Double,
    val tanggal_mulai: String?,
    val foto_awal: String?,
    val status: String,
    val mustahiq_id: Int,
    val nama_mustahiq: String?,
    val nik: String?,
    val alamat: String?,
    val no_hp: String?,
    val foto_mustahiq: String?,
    val progress_persen: Int,
    val total_tahapan_selesai: Int,
    val tahapan: List<Tahapan>
)

/**
 * Tahapan
 * Data satu tahapan dalam program
 * tahapan_id: ID tahapan (1-8)
 * urutan: urutan tahapan
 * nama_tahapan: nama tahapan
 * deskripsi_tahapan: deskripsi dari master
 * progress_id: ID di tabel tracking_progress (null jika belum ada)
 * status: SELESAI atau null (belum)
 * nominal: nominal jika ada
 * foto: nama file foto (null jika belum upload)
 * deskripsi: deskripsi yang diisi admin
 * kendala: kendala jika ada
 * updated_at: tanggal update (null jika belum)
 */
data class Tahapan(
    val tahapan_id: Int,
    val urutan: Int,
    val nama_tahapan: String,
    val deskripsi_tahapan: String?,
    val progress_id: Int?,
    val status: String?,
    val nominal: Double?,
    val foto: String?,
    val deskripsi: String?,
    val kendala: String?,
    val updated_at: String?
)

// ============================================================
// UPDATE PROGRESS
// ============================================================

/**
 * UpdateProgressRequest
 * Request untuk update progress (JSON)
 * Field: program_id, tahapan_id, status, nominal, deskripsi, kendala, foto
 */
data class UpdateProgressRequest(
    val program_id: Int,
    val tahapan_id: Int,
    val status: String,
    val nominal: Double?,
    val deskripsi: String?,
    val kendala: String?,
    val foto: String?
)

/**
 * UpdateProgressResponse
 * Response dari update progress
 * success: true jika berhasil
 * message: pesan dari server
 */
data class UpdateProgressResponse(
    val success: Boolean,
    val message: String
)

// ============================================================
// TAMBAH PROGRAM
// ============================================================

/**
 * TambahProgramRequest
 * Request untuk tambah program baru
 * Field: nama_mustahiq, nik, alamat, no_hp, nama_program, jenis_usaha, total_dana, tanggal_mulai
 */
data class TambahProgramRequest(
    val nama_mustahiq: String,
    val nik: String? = null,
    val alamat: String? = null,
    val no_hp: String? = null,
    val nama_program: String,
    val jenis_usaha: String,
    val total_dana: Double,
    val tanggal_mulai: String
)

/**
 * TambahProgramResponse
 * Response dari tambah program
 * data: TambahProgramData (program_id, kode_program, nama_program)
 */
data class TambahProgramResponse(
    val success: Boolean,
    val message: String,
    val data: TambahProgramData?
)

/**
 * TambahProgramData
 * Data program yang baru ditambahkan
 */
data class TambahProgramData(
    val program_id: Int,
    val kode_program: String,
    val nama_program: String
)

// ============================================================
// BASE RESPONSE
// ============================================================

/**
 * BaseResponse
 * Response dasar (untuk update profile, upload avatar)
 * success: true jika berhasil
 * message: pesan dari server
 */
data class BaseResponse(
    val success: Boolean,
    val message: String
)

// ============================================================
// AVATAR
// ============================================================

/**
 * AvatarResponse
 * Response dari upload avatar
 * data: AvatarData (nama file avatar)
 */
data class AvatarResponse(
    val success: Boolean,
    val message: String,
    val data: AvatarData?
)

/**
 * AvatarData
 * Data avatar yang diupload
 * avatar: nama file avatar
 */
data class AvatarData(
    val avatar: String
)

// ============================================================
// CAMPAIGN
// ============================================================

/**
 * CampaignResponse
 * Response untuk list campaign
 * data: List<Campaign>
 */
data class CampaignResponse(
    val success: Boolean,
    val message: String,
    val data: List<Campaign>
)

/**
 * Campaign
 * Data campaign/donasi
 * id: ID campaign
 * title: judul campaign
 * description: deskripsi campaign
 * target: target donasi
 * image: path gambar campaign
 * start: tanggal mulai
 * end: tanggal berakhir
 * status: status campaign
 * created_at: tanggal dibuat
 */
data class Campaign(
    val id: Int,
    val title: String,
    val description: String,
    val target: String,
    val image: String,
    val start: String?,
    val end: String?,
    val status: String,
    val created_at: String
)

// ============================================================
// SAYINGS (DOA)
// ============================================================

/**
 * SayingsResponse
 * Response untuk list doa
 * data: List<Saying>
 */
data class SayingsResponse(
    val success: Boolean,
    val message: String,
    val data: List<Saying>
)

/**
 * Saying
 * Data doa/ucapan
 * id: ID doa
 * body: isi doa
 * name: nama pengirim
 * campaign_title: judul campaign
 * created_at: tanggal dibuat
 */
data class Saying(
    val id: Int,
    val body: String,
    val name: String,
    val campaign_title: String,
    val created_at: String
)

// ============================================================
// TESTIMONIALS
// ============================================================

/**
 * TestimonialResponse
 * Response untuk list testimonial
 * data: List<Testimonial>
 */
data class TestimonialResponse(
    val success: Boolean,
    val message: String,
    val data: List<Testimonial>
)

/**
 * Testimonial
 * Data testimonial
 * id: ID testimonial
 * name: nama pemberi testimonial
 * position: posisi/jabatan
 * review: isi testimonial
 * rating: rating (1-5)
 * avatar: foto avatar (bisa null)
 * created_at: tanggal dibuat
 */
data class Testimonial(
    val id: Int,
    val name: String,
    val position: String?,
    val review: String,
    val rating: Int,
    val avatar: String?,
    val created_at: String
)

// ============================================================
// UPLOAD FOTO
// ============================================================

/**
 * UploadFotoResponse
 * Response dari upload foto progress
 * data: UploadFotoData (foto_path)
 */
data class UploadFotoResponse(
    val success: Boolean,
    val message: String,
    val data: UploadFotoData?
)

/**
 * UploadFotoData
 * Data foto yang diupload
 * foto_path: path file foto di server
 */
data class UploadFotoData(
    val foto_path: String
)