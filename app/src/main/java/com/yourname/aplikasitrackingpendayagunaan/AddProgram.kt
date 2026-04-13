package com.yourname.aplikasitrackingpendayagunaan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddProgram : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_program)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val options = listOf("UPZPRENEUR", "Gerobak Berkah" , "Z-Mart" , "Z-Auto" )
        val adapter = ArrayAdapter(this, R.layout.list_item, options)

        val autoComplete  = findViewById<AutoCompleteTextView>(R.id.autoComplete)
        autoComplete.setAdapter(adapter)
    }


}