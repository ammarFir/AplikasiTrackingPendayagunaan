package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuTracking : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_tracking)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set posisi aktif di Tracking
        bottomNav.selectedItemId = R.id.nav_tracking

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish() // tutup MenuTracking supaya back stack bersih
                    true
                }

                R.id.nav_tracking -> {
                    // Sudah di tracking, tidak perlu pindah
                    true
                }

                else -> false
            }
        }


        //perpindahan onclick
        fun pindahActivity (view: View) {
            val intent = Intent(this , DetailTracking::class.java)
            startActivity(intent)
        }




            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}