package com.yourname.aplikasitrackingpendayagunaan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView

import androidx.recyclerview.widget.RecyclerView
import com.yourname.aplikasitrackingpendayagunaan.R
import com.yourname.aplikasitrackingpendayagunaan.model.Penerima

class PenerimaAdapter (

    private val context: Context,
    private  val list: List<Penerima>, //data diambil dalam bentuk list
    private val onDetailClick: (Penerima) -> Unit
) : RecyclerView.Adapter<PenerimaAdapter.ViewHolder>() {

    // ViewHolder = "pegangan" ke view2 di dalam 1 item card
    // Tujuannya biar ga perlu findViewById() berulang2 tiap item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaProgram: TextView = itemView.findViewById(R.id.tvNamaProgram)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvJenisUsaha: TextView = itemView.findViewById(R.id.tvJenisUsaha)
        val tvLokasi: TextView = itemView.findViewById(R.id.tvLokasi)
        val btnSeeDetail: TextView = itemView.findViewById(R.id.btnSeeDetail)
    }

    // onCreateViewHolder = "cetak" layout item_penerima.xml jadi View
    // Dipanggil sekali per item yang pertama kali muncul di layar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_penerima, parent, false)
        return  ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val penerima= list[position]//ambil data penerima ke n dari list
        holder.tvNamaProgram.text = penerima.nama_program
        holder.tvNama.text = penerima.nama
        holder.tvJenisUsaha.text = penerima.jenis_usaha
        holder.tvLokasi.text = penerima.lokasi
        holder.btnSeeDetail.setOnClickListener {
            onDetailClick(penerima)
        }
    }

    override fun getItemCount() = list.size

}
