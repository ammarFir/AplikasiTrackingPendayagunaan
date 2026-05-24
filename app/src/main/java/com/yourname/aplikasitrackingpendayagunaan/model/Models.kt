package com.yourname.aplikasitrackingpendayagunaan.model

import com.google.gson.annotations.SerializedName

// ── AUTH ──────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val token: String,
    val user_id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val avatar: String?
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val data: RegisterData?
)

data class RegisterData(
    val user_id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String
)

// ── MUSTAHIQ ──────────────────────────────────────────

data class MustahiqResponse(
    val success: Boolean,
    val message: String,
    val data: List<Mustahiq>?
)

data class Mustahiq(
    val id: Int,
    val nama: String,
    val nik: String?,
    val alamat: String?,
    val no_hp: String?,
    val foto: String?,
    val status: String
)

// ── TRACKING LIST ─────────────────────────────────────

data class TrackingListResponse(
    val success: Boolean,
    val message: String,
    val data: TrackingListData?
)

data class TrackingListData(
    val summary: TrackingSummary,
    val programs: List<TrackingProgram>
)

data class TrackingSummary(
    val program_aktif: Int,
    val program_selesai: Int,
    val total_dana_digunakan: Double
)

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

// ── TRACKING DETAIL ───────────────────────────────────

data class TrackingDetailResponse(
    val success: Boolean,
    val message: String,
    val data: TrackingDetail?
)

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

// ── UPDATE PROGRESS ───────────────────────────────────

data class UpdateProgressRequest(
    val program_id: Int,
    val tahapan_id: Int,
    val status: String,
    val nominal: Double?,
    val deskripsi: String?,
    val kendala: String?,
    val foto: String?
)

data class UpdateProgressResponse(
    val success: Boolean,
    val message: String
)

// ── LAPORAN ───────────────────────────────────────────

data class LaporanResponse(
    val success: Boolean,
    val message: String,
    val data: LaporanData?
)

data class LaporanData(
    val program: TrackingDetail,
    val ringkasan: Ringkasan,
    val tahapan: List<TahapanLaporan>
)

data class Ringkasan(
    val total_investasi: Double,
    val total_dana_digunakan: Double,
    val sisa_dana: Double,
    val progress_persen: Int,
    val status_label: String,
    val tahapan_selesai: Int,
    val total_tahapan: Int
)

data class TahapanLaporan(
    val urutan: Int,
    val nama_tahapan: String,
    val status: String?,
    val nominal: Double?,
    val foto: String?,
    val deskripsi: String?,
    val kendala: String?,
    val updated_at: String?
)