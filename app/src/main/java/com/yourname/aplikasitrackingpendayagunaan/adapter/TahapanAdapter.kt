package com.yourname.aplikasitrackingpendayagunaan.adapter

// ============================================================
// IMPORT LIBRARY
// ============================================================
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

// ============================================================
// TAHAPAN ADAPTER - ADAPTER UNTUK 8 TAHAPAN DI MONITORING PROGRAM
// ============================================================
// Fungsi:
// 1. Menampilkan 8 tahapan dalam RecyclerView
// 2. Setiap tahapan memiliki: nama, dot (aktif/inaktif), garis penghubung
// 3. Tombol "Tambah Bukti" untuk menampilkan form (deskripsi + upload foto)
// 4. EditText deskripsi dengan TextWatcher (otomatis menyimpan perubahan)
// 5. Upload foto dengan preview menggunakan Glide
// 6. State form (visible/hidden) untuk setiap tahapan
// ============================================================
class TahapanAdapter(
    // ============================================================
    // PARAMETER KONSTRUKTOR
    // ============================================================
    // tahapanList: data 8 tahapan (id, nama, status form, deskripsi, foto)
    // onTambahBuktiClick: callback saat tombol "Tambah Bukti" diklik
    // onUploadClick: callback saat tombol upload foto diklik
    // ============================================================
    private val tahapanList: List<TahapanItem>,
    private val onTambahBuktiClick: (Int, View) -> Unit,
    private val onUploadClick: (Int) -> Unit
) : RecyclerView.Adapter<TahapanAdapter.ViewHolder>() {

    // ============================================================
    // DATA CLASS TAHAPAN ITEM
    // ============================================================
    // Setiap item memiliki:
    // - id: ID tahapan (1-8)
    // - nama: nama tahapan
    // - isFirst: apakah tahapan pertama?
    // - isFormVisible: apakah form (deskripsi + upload) terlihat?
    // - deskripsi: isi deskripsi yang sudah diinput
    // - fotoUrl: URL foto yang sudah diupload
    // ============================================================
    data class TahapanItem(
        val id: Int,
        val nama: String,
        val isFirst: Boolean = false,
        var isFormVisible: Boolean = false,
        var deskripsi: String = "",
        var fotoUrl: String? = null
    )

    // ============================================================
    // VIEW HOLDER - MENYIMPAN REFERENSI KOMPONEN UI PER ITEM
    // ============================================================
    // Setiap item memiliki:
    // - tvNamaTahapan: TextView nama tahapan
    // - btnTambahBukti: LinearLayout tombol "Tambah Bukti"
    // - formLayout: LinearLayout yang berisi form (deskripsi + upload)
    // - etDeskripsi: EditText untuk input deskripsi
    // - btnUpload: LinearLayout tombol upload foto
    // - ivFoto: ImageView preview foto
    // - dot: View dot (lingkaran) indikator status
    // - line: View garis di bawah dot (penghubung antar tahapan)
    // ============================================================
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

    // ============================================================
    // onCreateViewHolder()
    // ============================================================
    // Men- inflate layout item_tahapan.xml menjadi View.
    // ============================================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tahapan, parent, false)
        return ViewHolder(view)
    }

    // ============================================================
    // onBindViewHolder()
    // ============================================================
    // Mengisi data ke ViewHolder pada posisi tertentu.
    // ============================================================
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Gunakan adapterPosition agar akurat
        val currentPosition = holder.adapterPosition
        if (currentPosition == RecyclerView.NO_POSITION) return

        // Ambil data item pada posisi ini
        val item = tahapanList[currentPosition]
        val isLastItem = currentPosition == tahapanList.size - 1

        // ============================================================
        // SET NAMA TAHAPAN
        // ============================================================
        holder.tvNamaTahapan.text = item.nama

        // ============================================================
        // ATUR GARIS BAWAH DOT (PENGHUBUNG)
        // ============================================================
        // Jika item terakhir, garis bawah dot disembunyikan (GONE)
        if (isLastItem) {
            holder.line.visibility = View.GONE
        } else {
            holder.line.visibility = View.VISIBLE
        }

        // ============================================================
        // ATUR GARIS DI ATAS DOT (HANYA UNTUK TAHAPAN 2-8)
        // ============================================================
        // Tahapan pertama (Penerima Ditentukan) tidak punya garis di atas
        val line2 = holder.itemView.findViewById<View>(R.id.line2)
        if (currentPosition == 0) {
            line2.visibility = View.GONE
        } else {
            line2.visibility = View.VISIBLE
        }

        // ============================================================
        // ATUR MARGIN TOP DOT (KHUSUS TAHAPAN PERTAMA)
        // ============================================================
        // Tahapan pertama memiliki margin top 15dp agar sejajar dengan teks
        val density = holder.itemView.context.resources.displayMetrics.density
        val marginTop = if (currentPosition == 0) {
            (15 * density).toInt()
        } else {
            (0 * density).toInt()
        }
        val layoutParams = holder.dot.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = marginTop
        holder.dot.layoutParams = layoutParams

        // ============================================================
        // ATUR WARNA DOT (AKTIF/HIJAU ATAU INAKTIF/ABU)
        // ============================================================
        if (item.isFormVisible) {
            // Jika form terlihat (sudah diisi/ditampilkan), dot hijau (aktif)
            holder.dot.setBackgroundResource(R.drawable.dot_active)
        } else {
            // Jika form belum terlihat, dot abu (inaktif)
            holder.dot.setBackgroundResource(R.drawable.dot_inactive)
        }

        // ============================================================
        // ATUR TAMPILAN FORM (VISIBLE/GONE)
        // ============================================================
        if (item.isFormVisible) {
            // ============================================================
            // FORM TERLIHAT
            // ============================================================
            // Sembunyikan tombol "Tambah Bukti"
            holder.btnTambahBukti.visibility = View.GONE

            // Tampilkan form
            holder.formLayout.visibility = View.VISIBLE

            // Set teks deskripsi yang sudah ada
            holder.etDeskripsi.setText(item.deskripsi)

            // ============================================================
            // TEXTWATCHER - MENYIMPAN DESKRIPSI SECARA OTOMATIS
            // ============================================================
            // Setiap kali user mengetik, data deskripsi di tahapanList
            // akan langsung terupdate tanpa perlu tombol simpan.
            // ============================================================
            holder.etDeskripsi.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Ambil posisi terbaru dari adapter
                    val pos = holder.adapterPosition

                    // Jika posisi valid dan teks berubah, simpan ke tahapanList
                    if (pos != RecyclerView.NO_POSITION && s.toString() != tahapanList[pos].deskripsi) {
                        tahapanList[pos].deskripsi = s.toString()
                    }
                }
            })

            // ============================================================
            // TAMPILKAN FOTO (JIKA ADA)
            // ============================================================
            if (!item.fotoUrl.isNullOrEmpty()) {
                // Buat URL lengkap
                val fotoUrl = "${RetrofitClient.BASE_URL}uploads/${item.fotoUrl}"

                // Load foto menggunakan Glide
                Glide.with(holder.itemView.context)
                    .load(fotoUrl)
                    .into(holder.ivFoto)

                // Tampilkan ImageView
                holder.ivFoto.visibility = View.VISIBLE
            } else {
                // Jika tidak ada foto, sembunyikan ImageView
                holder.ivFoto.visibility = View.GONE
            }

        } else {
            // ============================================================
            // FORM TIDAK TERLIHAT (AWAL)
            // ============================================================
            // Tampilkan tombol "Tambah Bukti"
            holder.btnTambahBukti.visibility = View.VISIBLE

            // Sembunyikan form
            holder.formLayout.visibility = View.GONE
        }

        // ============================================================
        // EVENT CLICK: TOMBOL TAMBAH BUKTI
        // ============================================================
        holder.btnTambahBukti.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                // Panggil callback dengan posisi dan view
                onTambahBuktiClick(pos, holder.btnTambahBukti)
            }
        }

        // ============================================================
        // EVENT CLICK: TOMBOL UPLOAD FOTO
        // ============================================================
        holder.btnUpload.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                // Panggil callback dengan posisi
                onUploadClick(pos)
            }
        }
    }

    // ============================================================
    // getItemCount()
    // ============================================================
    // Mengembalikan jumlah item (8 tahapan).
    // ============================================================
    override fun getItemCount() = tahapanList.size

    // ============================================================
    // FUNGSI UPDATE FORM VISIBILITY
    // ============================================================
    // Menampilkan atau menyembunyikan form pada posisi tertentu.
    // Dipanggil saat tombol "Tambah Bukti" diklik.
    // ============================================================
    fun updateFormVisibility(position: Int, isVisible: Boolean) {
        if (position in tahapanList.indices) {
            // Update status form di data
            (tahapanList[position] as TahapanItem).isFormVisible = isVisible

            // Refresh item di posisi tersebut
            notifyItemChanged(position)
        }
    }

    // ============================================================
    // FUNGSI UPDATE DESKRIPSI
    // ============================================================
    // Mengupdate deskripsi pada posisi tertentu.
    // Dipanggil saat load data dari server.
    // ============================================================
    fun updateDeskripsi(position: Int, deskripsi: String) {
        if (position in tahapanList.indices) {
            // Update deskripsi di data
            (tahapanList[position] as TahapanItem).deskripsi = deskripsi

            // Refresh item di posisi tersebut
            notifyItemChanged(position)
        }
    }

    // ============================================================
    // FUNGSI UPDATE FOTO
    // ============================================================
    // Mengupdate foto pada posisi tertentu.
    // Dipanggil saat upload foto berhasil atau load data dari server.
    // ============================================================
    fun updateFoto(position: Int, fotoUrl: String) {
        if (position in tahapanList.indices) {
            // Update fotoUrl di data
            (tahapanList[position] as TahapanItem).fotoUrl = fotoUrl

            // Refresh item di posisi tersebut
            notifyItemChanged(position)
        }
    }
}