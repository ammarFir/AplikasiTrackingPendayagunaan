package com.yourname.aplikasitrackingpendayagunaan.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourname.aplikasitrackingpendayagunaan.R
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient

class TahapanAdapter(
    private val tahapanList: List<TahapanItem>,
    private val onTambahBuktiClick: (Int, View) -> Unit,
    private val onUploadClick: (Int) -> Unit
) : RecyclerView.Adapter<TahapanAdapter.ViewHolder>() {

    data class TahapanItem(
        val id: Int,
        val nama: String,
        val isFirst: Boolean = false,
        var isFormVisible: Boolean = false,
        var deskripsi: String = "",
        var fotoUrl: String? = null
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaTahapan: TextView = itemView.findViewById(R.id.tvNamaTahapan)
        val btnTambahBukti: LinearLayout = itemView.findViewById(R.id.btnTambahBukti)
        val formLayout: LinearLayout = itemView.findViewById(R.id.formLayout)
        val etDeskripsi: EditText = itemView.findViewById(R.id.etDeskripsi)
        val btnUpload: LinearLayout = itemView.findViewById(R.id.btnUpload)
        val ivFoto: ImageView = itemView.findViewById(R.id.ivFoto)
        val dot: View = itemView.findViewById(R.id.dot)
        val line: View = itemView.findViewById(R.id.line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tahapan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        if (currentPosition == RecyclerView.NO_POSITION) return

        val item = tahapanList[currentPosition]
        val isLastItem = currentPosition == tahapanList.size - 1

        holder.tvNamaTahapan.text = item.nama

        // Atur line (garis bawah dot)
        if (isLastItem) {
            holder.line.visibility = View.GONE
        } else {
            holder.line.visibility = View.VISIBLE
        }

        // Atur line2 (garis di ATAS dot) - hanya tampil untuk tahapan 2-8, tidak untuk tahapan 1
        val line2 = holder.itemView.findViewById<View>(R.id.line2)
        if (currentPosition == 0) {
            line2.visibility = View.GONE
        } else {
            line2.visibility = View.VISIBLE
        }

        // Atur margin top dot untuk tahapan pertama (Penerima Ditentukan)
        val density = holder.itemView.context.resources.displayMetrics.density
        val marginTop = if (currentPosition == 0) {
            (15 * density).toInt()
        } else {
            (0 * density).toInt()
        }
        val layoutParams = holder.dot.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = marginTop
        holder.dot.layoutParams = layoutParams

        // Atur warna dot berdasarkan status form
        if (item.isFormVisible) {
            holder.dot.setBackgroundResource(R.drawable.dot_active)
        } else {
            holder.dot.setBackgroundResource(R.drawable.dot_inactive)
        }

        if (item.isFormVisible) {
            holder.btnTambahBukti.visibility = View.GONE
            holder.formLayout.visibility = View.VISIBLE
            holder.etDeskripsi.setText(item.deskripsi)

            holder.etDeskripsi.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val pos = holder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION && s.toString() != tahapanList[pos].deskripsi) {
                        tahapanList[pos].deskripsi = s.toString()
                    }
                }
            })

            if (!item.fotoUrl.isNullOrEmpty()) {
                val fotoUrl = "${RetrofitClient.BASE_URL}uploads/${item.fotoUrl}"
                Glide.with(holder.itemView.context)
                    .load(fotoUrl)
                    .into(holder.ivFoto)
                holder.ivFoto.visibility = View.VISIBLE
            } else {
                holder.ivFoto.visibility = View.GONE
            }
        } else {
            holder.btnTambahBukti.visibility = View.VISIBLE
            holder.formLayout.visibility = View.GONE
        }

        holder.btnTambahBukti.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onTambahBuktiClick(pos, holder.btnTambahBukti)
            }
        }

        holder.btnUpload.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onUploadClick(pos)
            }
        }
    }

    override fun getItemCount() = tahapanList.size

    fun updateFormVisibility(position: Int, isVisible: Boolean) {
        if (position in tahapanList.indices) {
            (tahapanList[position] as TahapanItem).isFormVisible = isVisible
            notifyItemChanged(position)
        }
    }

    fun updateDeskripsi(position: Int, deskripsi: String) {
        if (position in tahapanList.indices) {
            (tahapanList[position] as TahapanItem).deskripsi = deskripsi
            notifyItemChanged(position)
        }
    }

    fun updateFoto(position: Int, fotoUrl: String) {
        if (position in tahapanList.indices) {
            (tahapanList[position] as TahapanItem).fotoUrl = fotoUrl
            notifyItemChanged(position)
        }
    }
}