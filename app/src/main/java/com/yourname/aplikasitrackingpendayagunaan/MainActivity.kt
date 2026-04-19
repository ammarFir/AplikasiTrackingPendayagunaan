package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourname.aplikasitrackingpendayagunaan.adapter.CampaignAdapter
import com.yourname.aplikasitrackingpendayagunaan.model.CampaignModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set posisi aktif di Home
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Sudah di home, tidak perlu pindah
                    true
                }
                R.id.nav_tracking -> {
                    startActivity(Intent(this, MenuTracking::class.java))
                    finish() // tutup MainActivity supaya back stack bersih
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

        // RecyclerView Campaign
        val dummyData = listOf(
            CampaignModel(
                id = 1,
                slug = "free-palestine",
                title = "Free Palestine",
                description = "UPZ Bakti Bersama Banjarmasin",
                donasiterkumpul = 20000000,
                imageRes = R.drawable.bahleeell, // ← ganti drawable sesuai milik kamu
                start = null, end = null, target = null, status = null,
                image = null, category_id = null, user_id = null,
                create_at = null, update_at = null, agency_id = null, delete_at = null
            ),
            CampaignModel(
                id = 2,
                slug = "bantu-banjir",
                title = "Bantu Korban Banjir",
                description = "Yayasan Peduli Kalsel",
                donasiterkumpul = 5000000,
                imageRes = R.drawable.bahleeell3, // ← ganti drawable sesuai milik kamu
                start = null, end = null, target = null, status = null,
                image = null, category_id = null, user_id = null,
                create_at = null, update_at = null, agency_id = null, delete_at = null
            )
        )

        val rvCampaign = findViewById<RecyclerView>(R.id.rvCampaign)
        rvCampaign.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCampaign.adapter = CampaignAdapter(dummyData)



        val img = findViewById<ImageView>(R.id.logo)
        img.setOnClickListener {
            val intent = Intent(this, TestingCompose::class.java)
            startActivity(intent)
        }
    }


}