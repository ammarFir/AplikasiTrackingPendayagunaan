package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourname.aplikasitrackingpendayagunaan.adapter.TrackingAdapter
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuTracking : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TrackingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_tracking)

        sessionManager = SessionManager(this)

        // Setup RecyclerView
        val rvPenerima = findViewById<RecyclerView>(R.id.rvPenerima)
        adapter = TrackingAdapter(emptyList()) { program ->
            val intent = Intent(this, DetailTracking::class.java)
            intent.putExtra("program_id", program.id)
            startActivity(intent)
        }
        rvPenerima.layoutManager = LinearLayoutManager(this)
        rvPenerima.adapter = adapter

        // Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_tracking
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tracking -> true
                R.id.nav_laporan -> {
                    startActivity(Intent(this, LaporanProgram::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Tombol tambah program
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddProgram::class.java))
        }

        // IMG PROFILE - klik ke halaman Profile
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Fetch data
        fetchTrackingList()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val token = sessionManager.getToken() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getProfile(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.data
                        user?.let {
                            val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                            if (!it.avatar.isNullOrEmpty()) {
                                var fileName = it.avatar
                                if (fileName.contains("/")) {
                                    fileName = fileName.substringAfterLast("/")
                                }
                                val avatarUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"
                                Glide.with(this@MenuTracking)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.logokoceng)
                                    .error(R.drawable.logokoceng)
                                    .circleCrop()
                                    .into(imgProfile)
                            } else {
                                imgProfile.setImageResource(R.drawable.logokoceng)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
        fetchTrackingList()
    }

    private fun fetchTrackingList() {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTrackingList(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!

                    // Update RecyclerView
                    adapter.updateData(data.programs)

                    // Update summary cards
                    findViewById<TextView>(R.id.tvTotalPenerima).text = data.summary.program_aktif.toString()
                    findViewById<TextView>(R.id.tvProgramSelesai).text = data.summary.program_selesai.toString()
                    findViewById<TextView>(R.id.tvTotalDana).text = "Rp ${String.format("%,.0f", data.summary.total_dana_digunakan)}"

                    // LOG UNTUK CEK DATA PROGRESS
                    data.programs.forEach { program ->
                        android.util.Log.d("TRACKING_DATA", "Program: ${program.nama_program}, Progress: ${program.progress_persen}%, Selesai: ${program.total_tahapan_selesai}/8")
                    }

                } else {
                    Toast.makeText(this@MenuTracking, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MenuTracking, "Koneksi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}