package com.yourname.aplikasitrackingpendayagunaan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourname.aplikasitrackingpendayagunaan.model.PenerimaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        bottomNav.selectedItemId = R.id.nav_tracking
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tracking -> true
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddProgram::class.java))
        }

        // Fetch data penerima
        fetchPenerima()
    }

    private fun fetchPenerima() {
        RetrofitClient.instance.getPenerima().enqueue(object : Callback<PenerimaResponse> {
            override fun onResponse(call: Call<PenerimaResponse>, response: Response<PenerimaResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    Log.d("MenuTracking", "Data penerima: $data")
                } else {
                    Log.e("MenuTracking", "Response gagal: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PenerimaResponse>, t: Throwable) {
                Log.e("MenuTracking", "Error: ${t.message}")
            }
        })
    }

    fun pindahActivity(view: View) {
        val intent = Intent(this, DetailTracking::class.java)
        startActivity(intent)
    }
}