package com.yourname.aplikasitrackingpendayagunaan.adapter

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourname.aplikasitrackingpendayagunaan.R

// ============================================================
// SLIDER ADAPTER - ADAPTER UNTUK VIEWPAGER SLIDER DI MAIN ACTIVITY
// ============================================================
// Fungsi:
// 1. Menampilkan gambar slider dari daftar URL gambar (campaign acak)
// 2. Setiap item menampilkan satu gambar
// 3. Menggunakan Glide untuk load gambar dari URL
// 4. ViewPager2 akan menampilkan 3 gambar secara bergantian (auto-slide)
// ============================================================
class SliderAdapter(
    // ============================================================
    // PARAMETER KONSTRUKTOR
    // ============================================================
    // imageUrls: daftar URL gambar yang akan ditampilkan di slider
    // (biasanya 3 gambar campaign yang dipilih secara acak)
    // ============================================================
    private val imageUrls: List<String>
) : RecyclerView.Adapter<SliderAdapter.ViewHolder>() {

    // ============================================================
    // VIEW HOLDER - MENYIMPAN REFERENSI KOMPONEN UI PER ITEM
    // ============================================================
    // Setiap item memiliki:
    // - imageView: ImageView untuk menampilkan gambar slider
    // ============================================================
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    // ============================================================
    // onCreateViewHolder()
    // ============================================================
    // Men- inflate layout item_slider.xml menjadi View.
    // Layout item_slider.xml berisi satu ImageView full screen.
    // ============================================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slider, parent, false)
        return ViewHolder(view)
    }

    // ============================================================
    // onBindViewHolder()
    // ============================================================
    // Mengisi data ke ViewHolder pada posisi tertentu.
    // Menggunakan Glide untuk load gambar dari URL.
    // ============================================================
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // ============================================================
        // LOAD GAMBAR DARI URL MENGGUNAKAN GLIDE
        // ============================================================
        Glide.with(holder.itemView.context)
            .load(imageUrls[position])          // URL gambar
            .placeholder(R.drawable.bahleeell)  // Default saat loading
            .error(R.drawable.bahleeell)        // Default jika error loading
            .into(holder.imageView)              // Target ImageView
    }

    // ============================================================
    // getItemCount()
    // ============================================================
    // Mengembalikan jumlah gambar yang akan ditampilkan.
    // Biasanya 3 gambar campaign acak.
    // ============================================================
    override fun getItemCount(): Int = imageUrls.size
}