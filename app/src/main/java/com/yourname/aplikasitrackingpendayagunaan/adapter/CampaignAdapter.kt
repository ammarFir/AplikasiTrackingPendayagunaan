package com.yourname.aplikasitrackingpendayagunaan.adapter

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourname.aplikasitrackingpendayagunaan.R
import com.yourname.aplikasitrackingpendayagunaan.model.Campaign
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient
import java.text.NumberFormat
import java.util.Locale

// ============================================================
// CAMPAIGN ADAPTER - ADAPTER UNTUK LIST CAMPAIGN HORIZONTAL
// ============================================================
// Fungsi:
// 1. Menampilkan daftar campaign dalam RecyclerView horizontal
// 2. Setiap item menampilkan: gambar, judul, deskripsi, total donasi terkumpul
// 3. Gambar di-load dari server menggunakan Glide
// 4. Format mata uang Rupiah untuk total donasi
// ============================================================
class CampaignAdapter(
    // ============================================================
    // PARAMETER KONSTRUKTOR
    // ============================================================
    // list: data campaign yang akan ditampilkan
    // ============================================================
    private val list: List<Campaign>
) : RecyclerView.Adapter<CampaignAdapter.ViewHolder>() {

    // ============================================================
    // VIEW HOLDER - MENYIMPAN REFERENSI KOMPONEN UI PER ITEM
    // ============================================================
    // Setiap item memiliki:
    // - image: ImageView untuk gambar campaign
    // - title: TextView judul campaign
    // - organizer: TextView deskripsi/organizer campaign
    // - collected: TextView total donasi terkumpul
    // ============================================================
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageView8)
        val title: TextView = itemView.findViewById(R.id.textView8)
        val organizer: TextView = itemView.findViewById(R.id.textView9)
        val collected: TextView = itemView.findViewById(R.id.textView11)
    }

    // ============================================================
    // onCreateViewHolder()
    // ============================================================
    // Men- inflate layout campaign_item.xml menjadi View.
    // ============================================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.campaign_item, parent, false)
        return ViewHolder(view)
    }

    // ============================================================
    // onBindViewHolder()
    // ============================================================
    // Mengisi data ke ViewHolder pada posisi tertentu.
    // ============================================================
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Ambil data campaign pada posisi ini
        val item = list[position]

        // ============================================================
        // SET DATA TEKS
        // ============================================================
        // Judul campaign
        holder.title.text = item.title

        // Deskripsi / organizer campaign
        holder.organizer.text = item.description

        // ============================================================
        // FORMAT TOTAL DONASI (MATA UANG RUPIAH)
        // ============================================================
        // Gunakan NumberFormat untuk format Rupiah (contoh: Rp 10.000.000)
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // Konversi target dari String ke Double (jika gagal, gunakan 0.0)
        val targetAmount = item.target.toDoubleOrNull() ?: 0.0

        // Set teks total donasi: "Terkumpul Rp 10.000.000"
        holder.collected.text = "Terkumpul ${formatter.format(targetAmount)}"

        // ============================================================
        // LOAD GAMBAR DARI SERVER MENGGUNAKAN GLIDE
        // ============================================================
        // Cek apakah item memiliki gambar
        if (item.image.isNotEmpty()) {
            // Buat URL lengkap: base_url + uploads/ + path gambar
            // Contoh: http://10.80.185.183/bakti_bersama/uploads/post-image/campaign_xxx.jpg
            val imageUrl = "${RetrofitClient.BASE_URL}uploads/${item.image}"

            // Load gambar menggunakan Glide
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bahleeell)  // Gambar default saat loading
                .error(R.drawable.bahleeell)        // Gambar default jika error
                .into(holder.image)
        } else {
            // Jika tidak ada gambar, pakai gambar default
            holder.image.setImageResource(R.drawable.bahleeell)
        }
    }

    // ============================================================
    // getItemCount()
    // ============================================================
    // Mengembalikan jumlah item dalam list.
    // ============================================================
    override fun getItemCount() = list.size
}