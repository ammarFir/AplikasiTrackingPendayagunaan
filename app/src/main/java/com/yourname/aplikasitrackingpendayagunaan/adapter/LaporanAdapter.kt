package com.yourname.aplikasitrackingpendayagunaan.adapter

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.yourname.aplikasitrackingpendayagunaan.R
import com.yourname.aplikasitrackingpendayagunaan.model.TrackingProgram
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient

// ============================================================
// LAPORAN ADAPTER - ADAPTER UNTUK LIST PROGRAM DI LAPORAN PROGRAM
// ============================================================
// Fungsi:
// 1. Menampilkan daftar program dalam RecyclerView (di LaporanProgram)
// 2. Setiap item menampilkan: foto mustahiq, nama, usaha, nominal, alamat, progress
// 3. Tombol "Export PDF" untuk generate laporan program
// 4. Data dapat di-update (refresh) saat ada perubahan
// ============================================================
class LaporanAdapter(
    // ============================================================
    // PARAMETER KONSTRUKTOR
    // ============================================================
    // list: data program yang akan ditampilkan
    // onExportClick: callback ketika tombol "Export PDF" diklik
    // ============================================================
    private var list: List<TrackingProgram>,
    private val onExportClick: (TrackingProgram) -> Unit
) : RecyclerView.Adapter<LaporanAdapter.ViewHolder>() {

    // ============================================================
    // onCreateViewHolder()
    // ============================================================
    // Men- inflate layout item_laporan.xml menjadi View.
    // ============================================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_laporan, parent, false)
        return ViewHolder(view)
    }

    // ============================================================
    // onBindViewHolder()
    // ============================================================
    // Mengisi data ke ViewHolder pada posisi tertentu.
    // ============================================================
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Binding data program ke ViewHolder
        holder.bind(list[position], onExportClick)
    }

    // ============================================================
    // getItemCount()
    // ============================================================
    // Mengembalikan jumlah item dalam list.
    // ============================================================
    override fun getItemCount(): Int = list.size

    // ============================================================
    // updateData()
    // ============================================================
    // Fungsi untuk mengupdate data adapter.
    // Dipanggil saat data berubah (misal: setelah load dari API).
    // ============================================================
    fun updateData(newList: List<TrackingProgram>) {
        list = newList

        // Beritahu RecyclerView bahwa data berubah
        notifyDataSetChanged()
    }

    // ============================================================
    // VIEW HOLDER - MENYIMPAN REFERENSI KOMPONEN UI PER ITEM
    // ============================================================
    // Setiap item memiliki:
    // - tvNamaLaporan: TextView nama mustahiq
    // - tvUsaha: TextView jenis usaha
    // - tvNominal: TextView total dana
    // - tvAlamat: TextView alamat
    // - tvBanner: TextView nama program (badge)
    // - tvProgress: TextView progress program
    // - btnExport: Button untuk export PDF
    // - imgFoto: ShapeableImageView foto mustahiq
    // ============================================================
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Deklarasi komponen UI
        private val tvNamaLaporan: TextView = itemView.findViewById(R.id.namaLaporan)
        private val tvUsaha: TextView = itemView.findViewById(R.id.usahaLaporan)
        private val tvNominal: TextView = itemView.findViewById(R.id.nominalLaporan)
        private val tvAlamat: TextView = itemView.findViewById(R.id.alamatLaporan)
        private val tvBanner: TextView = itemView.findViewById(R.id.bannerLaporan)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val btnExport: Button = itemView.findViewById(R.id.btnExport)
        private val imgFoto: ShapeableImageView = itemView.findViewById(R.id.imageLaporan)

        // ============================================================
        // FUNGSI BIND - MENGISI DATA KE KOMPONEN UI
        // ============================================================
        fun bind(program: TrackingProgram, onExportClick: (TrackingProgram) -> Unit) {
            // ============================================================
            // SET DATA TEKS
            // ============================================================
            // Nama mustahiq (jika null, tampilkan "-")
            tvNamaLaporan.text = program.nama_mustahiq ?: "-"

            // Jenis usaha (jika null, tampilkan "-")
            tvUsaha.text = program.jenis_usaha ?: "-"

            // Total dana dengan format Rupiah
            tvNominal.text = "Rp ${String.format("%,.0f", program.total_dana)}"

            // Alamat mustahiq (jika null, tampilkan "-")
            tvAlamat.text = program.alamat_mustahiq ?: "-"

            // Nama program sebagai badge
            tvBanner.text = program.nama_program

            // ============================================================
            // TAMPILKAN PROGRESS
            // ============================================================
            // Format: "Progress: 88% (7/8)"
            val progressText = "Progress: ${program.progress_persen}% (${program.total_tahapan_selesai}/8)"
            tvProgress.text = progressText

            // ============================================================
            // TAMPILKAN FOTO MUSTAHIQ (JIKA ADA)
            // ============================================================
            if (!program.foto_mustahiq.isNullOrEmpty()) {
                // Proses path foto
                var fileName = program.foto_mustahiq
                if (fileName.contains("/")) {
                    fileName = fileName.substringAfterLast("/")
                }

                // Buat URL lengkap menggunakan RetrofitClient.BASE_URL
                val fotoUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"

                // Load foto menggunakan Glide
                Glide.with(itemView.context)
                    .load(fotoUrl)
                    .placeholder(R.drawable.img)  // Default saat loading
                    .error(R.drawable.img)        // Default jika error
                    .into(imgFoto)
            }

            // ============================================================
            // EVENT CLICK: TOMBOL EXPORT PDF
            // ============================================================
            // Saat tombol diklik, panggil callback dengan data program
            btnExport.setOnClickListener {
                onExportClick(program)
            }
        }
    }
}