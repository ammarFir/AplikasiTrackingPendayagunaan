package com.yourname.aplikasitrackingpendayagunaan.adapter

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

class CampaignAdapter(private val list: List<Campaign>) :
    RecyclerView.Adapter<CampaignAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageView8)
        val title: TextView = itemView.findViewById(R.id.textView8)
        val organizer: TextView = itemView.findViewById(R.id.textView9)
        val collected: TextView = itemView.findViewById(R.id.textView11)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.campaign_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.title
        holder.organizer.text = item.description

        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val targetAmount = item.target.toDoubleOrNull() ?: 0.0
        holder.collected.text = "Terkumpul ${formatter.format(targetAmount)}"

        // Load gambar dari server - FIXED PATH
        if (item.image.isNotEmpty()) {
            val imageUrl = "${RetrofitClient.BASE_URL}uploads/${item.image}"
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bahleeell)
                .error(R.drawable.bahleeell)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.bahleeell)
        }
    }

    override fun getItemCount() = list.size
}