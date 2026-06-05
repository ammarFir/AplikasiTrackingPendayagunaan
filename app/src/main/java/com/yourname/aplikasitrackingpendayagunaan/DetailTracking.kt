package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.launch

class DetailTracking : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var tanggalMulaiProgram: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_tracking)

        sessionManager = SessionManager(this)

        val programId = intent.getIntExtra("program_id", -1)
        if (programId == -1) {
            Toast.makeText(this, "Program tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchDetail(programId)

        val btnUpdate = findViewById<Button>(R.id.btnExport)
        btnUpdate.setOnClickListener {
            val intent = Intent(this, MonitoringProgram::class.java)
            intent.putExtra("program_id", programId)
            startActivity(intent)
        }
    }

    private fun fetchDetail(programId: Int) {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTrackingDetail(token, programId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!

                    // Simpan tanggal mulai program
                    tanggalMulaiProgram = data.tanggal_mulai ?: ""

                    // Header card
                    findViewById<TextView>(R.id.tvJenisProgram).text = data.nama_program
                    findViewById<TextView>(R.id.tvNamaMustahiq).text = data.nama_mustahiq ?: "-"
                    findViewById<TextView>(R.id.tvUsahaMustahiq).text = data.jenis_usaha ?: "-"
                    findViewById<TextView>(R.id.tvAlamatMustahiq).text = data.alamat ?: "-"

                    // Badge
                    findViewById<TextView>(R.id.tvBanner).text = data.nama_program

                    // Card body
                    findViewById<TextView>(R.id.tvNama).text = data.nama_mustahiq ?: "-"
                    findViewById<TextView>(R.id.tvUsaha).text = data.jenis_usaha ?: "-"
                    findViewById<TextView>(R.id.tvDana).text = "Rp ${String.format("%,.0f", data.total_dana)}"
                    findViewById<TextView>(R.id.tvLokasi).text = data.alamat ?: "-"

                    // Tampilkan foto
                    val imgDetail = findViewById<ShapeableImageView>(R.id.imgDetail)
                    val fotoUrl = data.foto_mustahiq
                    if (!fotoUrl.isNullOrEmpty()) {
                        var fileName = fotoUrl
                        if (fotoUrl.contains("/")) {
                            fileName = fotoUrl.substringAfterLast("/")
                        }
                        val fullUrl = "http://10.0.2.2/bakti_bersama/uploads/$fileName"
                        Glide.with(this@DetailTracking)
                            .load(fullUrl)
                            .placeholder(R.drawable.img)
                            .error(R.drawable.img)
                            .into(imgDetail)
                    } else {
                        imgDetail.setImageResource(R.drawable.img)
                    }

                    // Stepper tanggal per tahapan
                    val tglViews = listOf(
                        R.id.tvTglProses1, R.id.tvTglProses2, R.id.tvTglProses3,
                        R.id.tvTglProses4, R.id.tvTglProses5, R.id.tvTglProses6,
                        R.id.tvTglProses7, R.id.tvTglProses8
                    )

                    data.tahapan.forEachIndexed { index, tahapan ->
                        if (index < tglViews.size) {
                            var tgl = "-"
                            if (index == 0 && tanggalMulaiProgram.isNotEmpty()) {
                                // Tahapan 1 pakai tanggal_mulai program
                                tgl = tanggalMulaiProgram.substring(0, 10)
                            } else {
                                // Tahapan 2-8 pakai updated_at
                                if (!tahapan.updated_at.isNullOrEmpty()) {
                                    tgl = tahapan.updated_at.substring(0, 10)
                                }
                            }
                            findViewById<TextView>(tglViews[index]).text = tgl
                        }
                    }

                    // Dot stepper dinamis
                    val dotViews = listOf(
                        R.id.dot1, R.id.dot2, R.id.dot3, R.id.dot4,
                        R.id.dot5, R.id.dot6, R.id.dot7, R.id.dot8
                    )

                    data.tahapan.forEachIndexed { index, tahapan ->
                        if (index < dotViews.size) {
                            val dot = findViewById<View>(dotViews[index])
                            if (tahapan.status == "SELESAI") {
                                dot.setBackgroundResource(R.drawable.dot_active)
                            } else {
                                dot.setBackgroundResource(R.drawable.dot_inactive)
                            }
                        }
                    }

                } else {
                    Toast.makeText(this@DetailTracking, "Gagal memuat detail", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailTracking, "Koneksi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}