package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuTracking : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
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





            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnAdd = findViewById<Button>(R.id.btnAdd);
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddProgram::class.java);
            startActivity(intent)
        }


    }



    //perpindahan onclick
    fun pindahActivity (view: View) {
        val intent = Intent(this , DetailTracking::class.java)
        startActivity(intent)
    }


}