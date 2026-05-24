package com.yourname.aplikasitrackingpendayagunaan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yourname.aplikasitrackingpendayagunaan.R
import com.yourname.aplikasitrackingpendayagunaan.model.TrackingProgram

class TrackingAdapter(
    private var list: List<TrackingProgram>,
    private val onClick: (TrackingProgram) -> Unit
) : RecyclerView.Adapter<TrackingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJenisProgram  : TextView = view.findViewById(R.id.tvJenisProgram)
        val tvNama          : TextView = view.findViewById(R.id.tvNama)
        val tvBantuanProgram: TextView = view.findViewById(R.id.tvBantuanProgram)
        val tvAlamat        : TextView = view.findViewById(R.id.tvAlamat)
        val tvSeeDetail     : TextView = view.findViewById(R.id.tvSeeDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tracking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvJenisProgram.text   = item.nama_program
        holder.tvNama.text           = item.nama_mustahiq ?: "-"
        holder.tvBantuanProgram.text = item.jenis_usaha ?: "-"
        holder.tvAlamat.text = item.alamat_mustahiq ?: "-"
        holder.tvSeeDetail.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<TrackingProgram>) {
        list = newList
        notifyDataSetChanged()
    }
}