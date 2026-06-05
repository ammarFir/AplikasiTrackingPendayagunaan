package com.yourname.aplikasitrackingpendayagunaan.adapter

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

class LaporanAdapter(
    private var list: List<TrackingProgram>,
    private val onExportClick: (TrackingProgram) -> Unit
) : RecyclerView.Adapter<LaporanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_laporan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], onExportClick)
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<TrackingProgram>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaLaporan: TextView = itemView.findViewById(R.id.namaLaporan)
        private val tvUsaha: TextView = itemView.findViewById(R.id.usahaLaporan)
        private val tvNominal: TextView = itemView.findViewById(R.id.nominalLaporan)
        private val tvAlamat: TextView = itemView.findViewById(R.id.alamatLaporan)
        private val tvBanner: TextView = itemView.findViewById(R.id.bannerLaporan)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val btnExport: Button = itemView.findViewById(R.id.btnExport)
        private val imgFoto: ShapeableImageView = itemView.findViewById(R.id.imageLaporan)

        fun bind(program: TrackingProgram, onExportClick: (TrackingProgram) -> Unit) {
            tvNamaLaporan.text = program.nama_mustahiq ?: "-"
            tvUsaha.text = program.jenis_usaha ?: "-"
            tvNominal.text = "Rp ${String.format("%,.0f", program.total_dana)}"
            tvAlamat.text = program.alamat_mustahiq ?: "-"
            tvBanner.text = program.nama_program

            // Tampilkan progress
            val progressText = "Progress: ${program.progress_persen}% (${program.total_tahapan_selesai}/8)"
            tvProgress.text = progressText

            // Tampilkan foto
            if (!program.foto_mustahiq.isNullOrEmpty()) {
                var fileName = program.foto_mustahiq
                if (fileName.contains("/")) {
                    fileName = fileName.substringAfterLast("/")
                }
                val fotoUrl = "http://10.0.2.2/bakti_bersama/uploads/$fileName"
                Glide.with(itemView.context)
                    .load(fotoUrl)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(imgFoto)
            }

            btnExport.setOnClickListener {
                onExportClick(program)
            }
        }
    }
}