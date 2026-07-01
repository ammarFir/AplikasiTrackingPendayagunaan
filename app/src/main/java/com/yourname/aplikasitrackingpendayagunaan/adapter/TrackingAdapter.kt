package com.yourname.aplikasitrackingpendayagunaan.adapter

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yourname.aplikasitrackingpendayagunaan.R
import com.yourname.aplikasitrackingpendayagunaan.model.TrackingProgram

// ============================================================
// TRACKING ADAPTER - ADAPTER UNTUK LIST PROGRAM DI MENU TRACKING
// ============================================================
// Fungsi:
// 1. Menampilkan daftar program tracking dalam RecyclerView
// 2. Setiap item menampilkan: nama program, nama mustahiq, jenis usaha, alamat
// 3. Tombol "See Detail" untuk melihat detail program
// 4. Data dapat di-update (refresh) saat ada perubahan
// ============================================================
class TrackingAdapter(
    // ============================================================
    // PARAMETER KONSTRUKTOR
    // ============================================================
    // list: data program yang akan ditampilkan (bisa berubah)
    // onClick: callback ketika user klik "See Detail"
    // ============================================================
    private var list: List<TrackingProgram>,
    private val onClick: (TrackingProgram) -> Unit
) : RecyclerView.Adapter<TrackingAdapter.ViewHolder>() {

    // ============================================================
    // VIEW HOLDER - MENYIMPAN REFERENSI KOMPONEN UI PER ITEM
    // ============================================================
    // Setiap item memiliki:
    // - tvJenisProgram: nama program (contoh: "Zmart")
    // - tvNama: nama mustahiq (contoh: "Amat")
    // - tvBantuanProgram: jenis usaha (contoh: "markettt")
    // - tvAlamat: alamat mustahiq (contoh: "Aluh Aluh")
    // - tvSeeDetail: tombol "See Detail" untuk melihat detail program
    // ============================================================
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJenisProgram: TextView = view.findViewById(R.id.tvJenisProgram)
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvBantuanProgram: TextView = view.findViewById(R.id.tvBantuanProgram)
        val tvAlamat: TextView = view.findViewById(R.id.tvAlamat)
        val tvSeeDetail: TextView = view.findViewById(R.id.tvSeeDetail)
    }

    // ============================================================
    // onCreateViewHolder()
    // ============================================================
    // Dipanggil saat RecyclerView membutuhkan ViewHolder baru.
    // Men- inflate layout item_tracking.xml menjadi View.
    // ============================================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout item_tracking.xml (satu item program)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tracking, parent, false)

        // Bungkus view ke dalam ViewHolder
        return ViewHolder(view)
    }

    // ============================================================
    // onBindViewHolder()
    // ============================================================
    // Dipanggil untuk mengisi data ke ViewHolder pada posisi tertentu.
    // Mengambil data dari list[position] dan menampilkannya ke UI.
    // ============================================================
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Ambil data program pada posisi ini
        val item = list[position]

        // ============================================================
        // SET DATA KE KOMPONEN UI
        // ============================================================
        // Nama program (contoh: "Zmart")
        holder.tvJenisProgram.text = item.nama_program

        // Nama mustahiq (jika null, tampilkan "-")
        holder.tvNama.text = item.nama_mustahiq ?: "-"

        // Jenis usaha (jika null, tampilkan "-")
        holder.tvBantuanProgram.text = item.jenis_usaha ?: "-"

        // Alamat mustahiq (jika null, tampilkan "-")
        holder.tvAlamat.text = item.alamat_mustahiq ?: "-"

        // ============================================================
        // EVENT CLICK: TOMBOL "SEE DETAIL"
        // ============================================================
        // Ketika tombol "See Detail" diklik, panggil callback onClick
        // dengan mengirim data program yang dipilih.
        // ============================================================
        holder.tvSeeDetail.setOnClickListener {
            onClick(item)
        }
    }

    // ============================================================
    // getItemCount()
    // ============================================================
    // Mengembalikan jumlah item dalam list.
    // Digunakan oleh RecyclerView untuk mengetahui berapa banyak
    // item yang harus ditampilkan.
    // ============================================================
    override fun getItemCount() = list.size

    // ============================================================
    // updateData()
    // ============================================================
    // Fungsi untuk mengupdate data adapter.
    // Dipanggil saat data berubah (misal: setelah tambah program).
    // ============================================================
    fun updateData(newList: List<TrackingProgram>) {
        // Update data list dengan list baru
        list = newList

        // Beritahu RecyclerView bahwa data berubah,
        // sehingga UI akan di-render ulang
        notifyDataSetChanged()
    }
}