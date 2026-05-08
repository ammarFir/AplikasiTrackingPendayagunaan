package com.yourname.aplikasitrackingpendayagunaan.model

data class Penerima(
    val id: Int,
    val program_id: Int,
    val nama: String,
    val foto: String?,
    val jenis_usaha: String,
    val lokasi: String,
    val total_dana: Long,
    val tanggal_mulai: String,
    val status: String,
    val nama_program: String
)

data class PenerimaResponse(
    val status: String,
    val data: List<Penerima>
)

data class Tracking(
    val id: Int,
    val penerima_id: Int,
    val step_ke: Int,
    val nama_step: String,
    val status: String,
    val tanggal_selesai: String?
)

data class DetailPenerimaResponse(
    val status: String,
    val penerima: Penerima,
    val tracking: List<Tracking>
)