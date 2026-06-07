package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yourname.aplikasitrackingpendayagunaan.adapter.CampaignAdapter
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_tracking -> {
                    startActivity(Intent(this, MenuTracking::class.java))
                    finish()
                    true
                }
                R.id.nav_laporan -> {
                    startActivity(Intent(this, LaporanProgram::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ViewPager slider
        val images = listOf(
            R.drawable.bahleeell,
            R.drawable.bahleeell2,
            R.drawable.bahleeell3,
        )
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = SliderAdapter(images)

        // Load campaign dari API
        loadCampaigns()

        // LOGO PROFILE - klik ke halaman Profile
        val logoProfile = findViewById<ImageView>(R.id.logoProfile)
        logoProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // 3 DOT MENU - hanya untuk Logout
        val btnOption = findViewById<LinearLayout>(R.id.btnOption)
        btnOption.setOnClickListener {
            showLogoutBottomSheet()
        }

        refreshUserData()
    }

    override fun onResume() {
        super.onResume()
        refreshUserData()
    }

    private fun loadCampaigns() {
        val token = sessionManager.getToken()
        if (token == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getCampaigns(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val campaigns = response.body()?.data ?: emptyList()

                        val rvCampaign = findViewById<RecyclerView>(R.id.rvCampaign)
                        rvCampaign.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        rvCampaign.adapter = CampaignAdapter(campaigns)
                    } else {
                        Toast.makeText(this@MainActivity, "Gagal memuat campaign", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshUserData() {
        // Refresh nama
        val tvWelcome = findViewById<TextView>(R.id.textView7)
        val userName = sessionManager.getName() ?: "Pengguna"
        tvWelcome.text = "Selamat Datang, $userName"

        // Refresh avatar
        val logoProfile = findViewById<ImageView>(R.id.logoProfile)
        val avatar = sessionManager.getAvatar()
        if (!avatar.isNullOrEmpty()) {
            var fileName = avatar
            if (fileName.contains("/")) {
                fileName = fileName.substringAfterLast("/")
            }
            val avatarUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.logokoceng)
                .error(R.drawable.logokoceng)
                .circleCrop()
                .into(logoProfile)
        } else {
            logoProfile.setImageResource(R.drawable.logokoceng)
        }
    }

    private fun showLogoutBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_logout, null)
        bottomSheetDialog.setContentView(view)

        val tvLogout = view.findViewById<TextView>(R.id.tvLogout)

        tvLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}