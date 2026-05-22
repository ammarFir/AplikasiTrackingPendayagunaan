package com.yourname.aplikasitrackingpendayagunaan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yourname.aplikasitrackingpendayagunaan.R
import com.yourname.aplikasitrackingpendayagunaan.model.TrackingProgram

class TrackingAdapter(
    private var list: List<TrackingProgram>,
    private val onClick: (TrackingProgram) -> Unit
) : RecyclerView.Adapter<TrackingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaProgram   : TextView    = view.findViewById(R.id.tvNamaProgram)
        val tvNamaMustahiq  : TextView    = view.findViewById(R.id.tvNamaMustahiq)
        val tvStatus        : TextView    = view.findViewById(R.id.tvStatus)
        val tvProgress      : TextView    = view.findViewById(R.id.tvProgress)
        val progressBar     : ProgressBar = view.findViewById(R.id.progressBar)
        val tvTotalDana     : TextView    = view.findViewById(R.id.tvTotalDana)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tracking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNamaProgram.text  = item.nama_program
        holder.tvNamaMustahiq.text = item.nama_mustahiq ?: "-"
        holder.tvStatus.text       = item.status
        holder.tvProgress.text     = "${item.progress_persen}%"
        holder.progressBar.progress = item.progress_persen
        holder.tvTotalDana.text    = "Rp ${String.format("%,.0f", item.total_dana)}"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<TrackingProgram>) {
        list = newList
        notifyDataSetChanged()
    }
}