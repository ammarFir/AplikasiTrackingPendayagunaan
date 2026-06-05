package com.yourname.aplikasitrackingpendayagunaan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourname.aplikasitrackingpendayagunaan.adapter.LaporanAdapter
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.launch

class LaporanProgram : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: LaporanAdapter
    private lateinit var rvLaporan: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_program)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        rvLaporan = findViewById(R.id.rvLaporanProgram)
        rvLaporan.layoutManager = LinearLayoutManager(this)

        adapter = LaporanAdapter(emptyList()) { program ->
            Toast.makeText(this, "Export PDF untuk: ${program.nama_program}", Toast.LENGTH_SHORT).show()
        }
        rvLaporan.adapter = adapter

        loadData()
    }

    private fun loadData() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTrackingList(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    adapter.updateData(data.programs)
                } else {
                    Toast.makeText(this@LaporanProgram, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LaporanProgram, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}