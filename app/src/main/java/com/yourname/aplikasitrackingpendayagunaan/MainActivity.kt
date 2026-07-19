package com.yourname.aplikasitrackingpendayagunaan

// ============================================================
// IMPORT LIBRARY
// ============================================================
import android.content.Intent // buat berpindah halaman
import android.os.Bundle // nyimpen data sementara
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView // untuk tampil gambar
import android.widget.LinearLayout //susuna komponen
import android.widget.TextView // tampil text
import android.widget.Toast // pop up
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide //lobrari load gambar dri internet url
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yourname.aplikasitrackingpendayagunaan.adapter.CampaignAdapter
import com.yourname.aplikasitrackingpendayagunaan.adapter.SliderAdapter
import com.yourname.aplikasitrackingpendayagunaan.model.Saying
import com.yourname.aplikasitrackingpendayagunaan.model.Testimonial
import com.yourname.aplikasitrackingpendayagunaan.network.ApiClient // memanggil api
import com.yourname.aplikasitrackingpendayagunaan.network.RetrofitClient // base url + config
import com.yourname.aplikasitrackingpendayagunaan.utils.SessionManager // simpan data user
import kotlinx.coroutines.CoroutineScope // proses background
import kotlinx.coroutines.Dispatchers //proses background
import kotlinx.coroutines.launch //proses background
import kotlinx.coroutines.withContext //proses background

// ============================================================
// MAIN ACTIVITY - HALAMAN UTAMA / DASHBOARD
// ============================================================
// Fungsi: Menampilkan halaman utama aplikasi yang berisi:
// 1. Slider gambar campaign (dari database)
// 2. List campaign horizontal (RecyclerView)
// 3. Card doa (dari tabel sayings)
// 4. Card testimonial (dari tabel testimonials)
// 5. Avatar & nama user di header
// 6. Bottom navigation (Home, Tracking, Laporan)
// 7. Menu logout (3 dot di header)
// ============================================================
class MainActivity : AppCompatActivity() {
//MainActivity mewarisi dari AppCompatActivity atau inheritance
    //yg mana appcompatact itu berisi constructor kosong

    // SessionManager: menyimpan data session user seperti token, nama, email, role, avatar
    //privat variable yg lateinit(inisiasinya nanti) yg tipe datanya Object SessionManager
    private lateinit var sessionManager: SessionManager

    // -------------------- TESTIMONIAL --------------------
    // testimonialList: daftar semua testimonial dari API
    // currentTestimonialIndex: index testimonial yang sedang ditampilkan (0 = pertama)
    private var testimonialList = mutableListOf<Testimonial>()
    //membuat  lis kosong yg isinya data Testimonial

    private var currentTestimonialIndex = 0
    //dimuldai nilai awalnya 0

    // -------------------- DOA --------------------
    // doaList: daftar semua doa dari API
    // currentDoaIndex: index doa yang sedang ditampilkan (0 = pertama)
    private var doaList = mutableListOf<Saying>()
    private var currentDoaIndex = 0

    // ============================================================
    // LIFECYCLE: onCreate()
    // ============================================================
    // Dipanggil saat activity pertama kali dibuat.
    // Di sini kita inisialisasi semua komponen UI dan load data.
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        //override menimpa fungsi baawaan dari app compat
        //Saya mau menimpa fungsi yang sudah ada di class induk (AppCompatActivity) dengan fungsi versi saya sendiri
        super.onCreate(savedInstanceState)

        // ============================================================
        // SETUP FULLSCREEN & LAYOUT
        // ============================================================

        // Mengaktifkan mode edge-to-edge (konten meresap ke system bar)
        enableEdgeToEdge()

        // Kode ini mengatur tampilan layar menjadi fullscreen.
        // window.decorView.systemUiVisibility digunakan untuk mengontrol status bar dan navigation bar.
        // Kita menggabungkan 3 flag: SYSTEM_UI_FLAG_FULLSCREEN untuk menyembunyikan status bar atas,
        // SYSTEM_UI_FLAG_HIDE_NAVIGATION untuk menyembunyikan navigation bar bawah,
        // dan SYSTEM_UI_FLAG_IMMERSIVE_STICKY agar saat user swipe dari tepi, bar muncul sebentar lalu otomatis hilang lagi.
        // Hasilnya aplikasi tampil penuh di seluruh layar tanpa gangguan elemen sistem.
        // FULLSCREEN - hilangkan status bar dan navigation bar
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )


        // Menghubungkan layout XML ke activity ini
        // R.layout.activity_main adalah file XML di res/layout/activity_main.xml
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        //Kode ini bikin objek SessionManager buat ngatur data user login.
        // Dikasih this (konteks halaman ini) biar bisa akses penyimpanan internal HP buat nyimpen & baca data
        // kayak token, nama, sama avatar.

        // Mencari komponen BottomNavigationView dari layout
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Menandai menu "Home" sebagai menu yang aktif (warna icon berubah hijau)
        bottomNav.selectedItemId = R.id.nav_home

        // Menambahkan listener untuk menangani klik pada setiap menu
        // Ketika user klik salah satu menu, aplikasi akan pindah ke halaman yang sesuai
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Menu Home: tetap di halaman ini (tidak pindah kemana-mana)
                R.id.nav_home -> {
                    true
                }
                // Menu Tracking: pindah ke halaman MenuTracking
                R.id.nav_tracking -> {
                    startActivity(Intent(this, MenuTracking::class.java))
                    finish() // Menutup activity saat ini agar tidak menumpuk
                    true
                }
                // Menu Laporan: pindah ke halaman LaporanProgram
                R.id.nav_laporan -> {
                    startActivity(Intent(this, LaporanProgram::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // ============================================================
        // SETUP WINDOW INSETS (PADDING SYSTEM BARS)
        // ============================================================
        // Menambahkan padding pada konten agar tidak tertutup oleh status bar
        // dan navigation bar di perangkat Android modern.
        // ============================================================
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ============================================================
        // LOAD DATA DARI API
        // ============================================================
        // Semua data di halaman utama diambil dari server via API.
        // Menggunakan Coroutine untuk menjalankan proses secara asynchronous
        // agar tidak mengganggu UI thread.
        // ============================================================

        // 1. Load gambar untuk slider (diambil dari campaign secara acak)
        loadSliderImages()

        // 2. Load daftar campaign untuk RecyclerView horizontal
        loadCampaigns()

        // 3. Load testimonial untuk card testimonial
        loadTestimonials()

        // 4. Load doa untuk card doa
        loadDoa()

        // ============================================================
        // SETUP LOGO PROFILE -> KE HALAMAN PROFILE
        // ============================================================
        // Logo profile di header (sebelah kanan) dapat diklik.
        // Ketika diklik, user akan pindah ke halaman ProfileActivity.
        // ============================================================

        // Mencari ImageView logo profile dari layout
        val logoProfile = findViewById<ImageView>(R.id.logoProfile)

        // Menambahkan event klik pada logo profile
        logoProfile.setOnClickListener {
            // Membuat intent untuk pindah ke ProfileActivity
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // ============================================================
        // SETUP 3 DOT MENU -> LOGOUT
        // ============================================================
        // Tiga titik (dot) di pojok kanan header berfungsi sebagai menu logout.
        // Saat diklik, akan muncul bottom sheet berisi tombol logout.
        // ============================================================

        // Mencari LinearLayout yang berisi 3 dot
        val btnOption = findViewById<LinearLayout>(R.id.btnOption)

        // Menambahkan event klik pada 3 dot
        btnOption.setOnClickListener {
            // Menampilkan bottom sheet logout
            showLogoutBottomSheet()
        }

        // ============================================================
        // SETUP REVIEW NAVIGATION (SEBELUMNYA / SELANJUTNYA)
        // ============================================================
        // Tombol "Sebelumnya" dan "Selanjutnya" di bawah card testimonial.
        // Berguna untuk melihat testimonial lain tanpa harus reload halaman.
        // ============================================================

        // Mencari tombol navigasi review
        val reviewSebelum = findViewById<TextView>(R.id.reviewSebelum)
        val reviewSesudah = findViewById<TextView>(R.id.reviewSesudah)

        // Klik "Sebelumnya" -> pindah ke testimonial sebelumnya
        reviewSebelum.setOnClickListener {
            prevTestimonial()
        }

        // Klik "Selanjutnya" -> pindah ke testimonial berikutnya
        reviewSesudah.setOnClickListener {
            nextTestimonial()
        }

        // ============================================================
        // SETUP DOA NAVIGATION (SEBELUMNYA / SELANJUTNYA)
        // ============================================================
        // Tombol "Sebelumnya" dan "Selanjutnya" di bawah card doa.
        // Berguna untuk melihat doa lain tanpa harus reload halaman.
        // ============================================================

        // Mencari tombol navigasi doa
        val ucapanSebelum = findViewById<TextView>(R.id.ucapanSebelum)
        val ucapanSesudah = findViewById<TextView>(R.id.ucapanSesudah)

        // Klik "Sebelumnya" -> pindah ke doa sebelumnya
        ucapanSebelum.setOnClickListener {
            prevDoa()
        }

        // Klik "Selanjutnya" -> pindah ke doa berikutnya
        ucapanSesudah.setOnClickListener {
            nextDoa()
        }

        // ============================================================
        // REFRESH DATA USER (NAMA & AVATAR)
        // ============================================================
        // Mengambil data user dari SessionManager dan menampilkannya di header.
        // ============================================================
        refreshUserData()
    }

    // ============================================================
    // LIFECYCLE: onResume()
    // ============================================================
    // Dipanggil setiap kali activity muncul kembali ke layar,
    // misalnya setelah user kembali dari halaman lain (ProfileActivity, dll).
    // ============================================================
    override fun onResume() {
        super.onResume()

        // Refresh data user (nama & avatar) karena mungkin berubah di ProfileActivity
        refreshUserData()

        // Refresh testimonial, doa, dan slider untuk memastikan data terbaru
        loadTestimonials()
        loadDoa()
        loadSliderImages()
    }

    // ============================================================
    // FUNGSI DOA
    // ============================================================

    /**
     * loadDoa()
     * Fungsi untuk mengambil data doa dari API.
     * Data doa diambil dari tabel 'sayings' di database.
     * Setelah berhasil, doa pertama akan ditampilkan di card doa.
     */
    private fun loadDoa() {
        // Ambil token dari session
        val token = sessionManager.getToken()

        // Jika token null (belum login), hentikan fungsi
        if (token == null) return

        // Jalankan proses di background thread (IO) agar tidak mengganggu UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Panggil API untuk mendapatkan daftar doa
                val response = ApiClient.apiService.getSayings(token)

                // Kembali ke Main Thread untuk update UI
                withContext(Dispatchers.Main) {
                    // Cek apakah response sukses
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Ambil data doa dari response
                        doaList = response.body()?.data?.toMutableList() ?: mutableListOf()

                        // Jika ada data doa, tampilkan doa pertama
                        if (doaList.isNotEmpty()) {
                            currentDoaIndex = 0
                            displayCurrentDoa()
                        }
                    }
                }
            } catch (e: Exception) {
                // Jika terjadi error (koneksi, dll), print stack trace
                e.printStackTrace()
            }
        }
    }

    /**
     * displayCurrentDoa()
     * Menampilkan doa yang sedang dipilih (sesuai currentDoaIndex)
     * ke TextView yang ada di card doa.
     */
    private fun displayCurrentDoa() {
        // Jika tidak ada doa, hentikan
        if (doaList.isEmpty()) return

        // Ambil doa berdasarkan index saat ini
        val doa = doaList[currentDoaIndex]

        // Cari komponen TextView di layout
        val tvNamaDoa = findViewById<TextView>(R.id.tvNamaDoa)
        val tvDoaCampaign = findViewById<TextView>(R.id.tvDoaCampaign)
        val tvUcapanDoa = findViewById<TextView>(R.id.tvUcapanDoa)

        // Isi TextView dengan data doa
        tvNamaDoa.text = doa.name                       // Nama pengirim doa
        tvDoaCampaign.text = "Donasi ${doa.campaign_title}" // Nama campaign
        tvUcapanDoa.text = doa.body                     // Isi doa
    }

    /**
     * nextDoa()
     * Pindah ke doa berikutnya (index + 1)
     * Jika sudah di doa terakhir, tampilkan toast pemberitahuan.
     */
    private fun nextDoa() {
        if (doaList.isEmpty()) return

        // Cek apakah masih ada doa berikutnya
        if (currentDoaIndex < doaList.size - 1) {
            currentDoaIndex++ // Pindah ke index berikutnya
            displayCurrentDoa() // Tampilkan doa baru
        } else {
            // Sudah di doa terakhir
            Toast.makeText(this, "Ini doa terakhir", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * prevDoa()
     * Pindah ke doa sebelumnya (index - 1)
     * Jika sudah di doa pertama, tampilkan toast pemberitahuan.
     */
    private fun prevDoa() {
        if (doaList.isEmpty()) return

        // Cek apakah masih ada doa sebelumnya
        if (currentDoaIndex > 0) {
            currentDoaIndex-- // Pindah ke index sebelumnya
            displayCurrentDoa() // Tampilkan doa baru
        } else {
            // Sudah di doa pertama
            Toast.makeText(this, "Ini doa pertama", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================================
    // FUNGSI SLIDER
    // ============================================================

    /**
     * loadSliderImages()
     * Mengambil 3 gambar campaign secara acak dari API untuk ditampilkan di slider.
     * Gambar diambil dari kolom 'image' di tabel 'campaigns'.
     * Setiap kali halaman dimuat, gambar akan diacak.
     */
    private fun loadSliderImages() {
        val token = sessionManager.getToken()
        if (token == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getCampaigns(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val campaigns = response.body()?.data ?: emptyList()

                        // Filter campaign yang memiliki gambar (tidak null dan tidak kosong)
                        val campaignsWithImage = campaigns.filter { !it.image.isNullOrEmpty() }

                        if (campaignsWithImage.isNotEmpty()) {
                            // Acak dan ambil 3 campaign
                            val shuffledCampaigns = campaignsWithImage.shuffled().take(3)

                            // Buat URL gambar dengan menambahkan base URL + folder uploads
                            val imageUrls = shuffledCampaigns.map { campaign ->
                                "${RetrofitClient.BASE_URL}uploads/${campaign.image}"
                            }

                            // Set adapter ke ViewPager
                            val viewPager = findViewById<ViewPager2>(R.id.viewPager)
                            viewPager.adapter = SliderAdapter(imageUrls)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ============================================================
    // FUNGSI TESTIMONIAL
    // ============================================================

    /**
     * loadTestimonials()
     * Mengambil data testimonial dari API.
     * Data diambil dari tabel 'testimonials'.
     * Setelah berhasil, testimonial pertama akan ditampilkan di card.
     */
    private fun loadTestimonials() {
        val token = sessionManager.getToken()
        if (token == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getTestimonials(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        testimonialList = response.body()?.data?.toMutableList() ?: mutableListOf()
                        if (testimonialList.isNotEmpty()) {
                            currentTestimonialIndex = 0
                            displayCurrentTestimonial()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * displayCurrentTestimonial()
     * Menampilkan testimonial yang sedang dipilih ke card testimonial.
     * Termasuk: nama, review, rating bintang, dan avatar (jika ada).
     */
    private fun displayCurrentTestimonial() {
        if (testimonialList.isEmpty()) return
        val item = testimonialList[currentTestimonialIndex]

        // Cari komponen di layout
        val txtNamaReview = findViewById<TextView>(R.id.txtNamaReview)
        val txtReview = findViewById<TextView>(R.id.txtReview)
        val reviewStar = findViewById<TextView>(R.id.reviewStar)
        val imgProfileReview = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.imgProfileReview)

        // Isi data
        txtNamaReview.text = item.name                     // Nama pemberi testimonial
        txtReview.text = item.review                       // Isi testimonial

        // Rating bintang: ★★★★★ (sesuai rating)
        // contoh: rating 4 -> "★★★★☆"
        reviewStar.text = "★".repeat(item.rating) + "☆".repeat(5 - item.rating)

        // Tampilkan avatar jika ada
        if (!item.avatar.isNullOrEmpty()) {
            var fileName = item.avatar
            if (fileName.contains("/")) {
                fileName = fileName.substringAfterLast("/")
            }
            val avatarUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"

            // Menggunakan Glide untuk load gambar dari URL
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.dot_active)    // Gambar default saat loading
                .error(R.drawable.ic_dot_blue)         // Gambar default jika error
                .circleCrop()                           // Membuat avatar berbentuk lingkaran
                .into(imgProfileReview)
        } else {
            // Jika tidak ada avatar, pakai gambar default
            imgProfileReview.setImageResource(R.drawable.dot_active)
        }
    }

    /**
     * nextTestimonial()
     * Pindah ke testimonial berikutnya.
     */
    private fun nextTestimonial() {
        if (testimonialList.isEmpty()) return
        if (currentTestimonialIndex < testimonialList.size - 1) {
            currentTestimonialIndex++
            displayCurrentTestimonial()
        } else {
            Toast.makeText(this, "Ini testimonial terakhir", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * prevTestimonial()
     * Pindah ke testimonial sebelumnya.
     */
    private fun prevTestimonial() {
        if (testimonialList.isEmpty()) return
        if (currentTestimonialIndex > 0) {
            currentTestimonialIndex--
            displayCurrentTestimonial()
        } else {
            Toast.makeText(this, "Ini testimonial pertama", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================================
    // FUNGSI CAMPAIGN
    // ============================================================

    /**
     * loadCampaigns()
     * Mengambil data campaign dari API dan menampilkannya di RecyclerView horizontal.
     * RecyclerView menggunakan LinearLayoutManager dengan orientasi HORIZONTAL.
     * Setiap item campaign menampilkan: gambar, judul, deskripsi, dan total donasi.
     */
    private fun loadCampaigns() {
        val token = sessionManager.getToken()
        if (token == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getCampaigns(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val campaigns = response.body()?.data ?: emptyList()

                        // Setup RecyclerView
                        val rvCampaign = findViewById<RecyclerView>(R.id.rvCampaign)

                        // LinearLayoutManager dengan orientasi horizontal (scroll kiri-kanan)
                        rvCampaign.layoutManager = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )

                        // Set adapter dengan data campaign
                        rvCampaign.adapter = CampaignAdapter(campaigns)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Gagal memuat campaign",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ============================================================
    // FUNGSI USER DATA
    // ============================================================

    /**
     * refreshUserData()
     * Menampilkan nama dan avatar user di header.
     * Data diambil dari SessionManager (disimpan saat login).
     */
    private fun refreshUserData() {
        // ============================================================
        // REFRESH NAMA USER
        // ============================================================
        // Mencari TextView untuk sapaan user
        val tvWelcome = findViewById<TextView>(R.id.tvUsername)

        // Ambil nama dari SessionManager, jika null pakai "Pengguna" sebagai default
        val userName = sessionManager.getName() ?: "Pengguna"

        // Set teks sapaan: "Selamat Datang, [nama]"
        tvWelcome.text = "Selamat Datang, $userName"

        // ============================================================
        // REFRESH AVATAR USER
        // ============================================================
        // Mencari ImageView untuk avatar user
        val logoProfile = findViewById<ImageView>(R.id.logoProfile)

        // Ambil avatar dari SessionManager
        val avatar = sessionManager.getAvatar()

        if (!avatar.isNullOrEmpty()) {
            // Jika ada avatar, tampilkan dari URL
            var fileName = avatar

            // Jika path mengandung "/", ambil bagian setelah "/" terakhir
            // Contoh: "uploads/avatar_1.jpg" -> "avatar_1.jpg"
            if (fileName.contains("/")) {
                fileName = fileName.substringAfterLast("/")
            }

            // Buat URL lengkap: base_url + uploads/ + nama file
            val avatarUrl = "${RetrofitClient.BASE_URL}uploads/$fileName"

            // Menggunakan Glide untuk load gambar
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.logokoceng)    // Default saat loading
                .error(R.drawable.logokoceng)          // Default jika error
                .circleCrop()                           // Bentuk lingkaran
                .into(logoProfile)
        } else {
            // Jika tidak ada avatar, pakai gambar default
            logoProfile.setImageResource(R.drawable.logokoceng)
        }
    }

    // ============================================================
    // FUNGSI LOGOUT
    // ============================================================

    /**
     * showLogoutBottomSheet()
     * Menampilkan bottom sheet yang berisi tombol logout.
     * Saat tombol logout diklik:
     * 1. Session dihapus (clearSession)
     * 2. Pindah ke halaman Login
     * 3. Tutup semua activity sebelumnya
     */
    private fun showLogoutBottomSheet() {
        // Membuat objek BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate layout bottom_sheet_logout.xml menjadi View
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_logout, null)

        // Set view ke bottom sheet
        bottomSheetDialog.setContentView(view)

        // Cari tombol logout di dalam bottom sheet
        val tvLogout = view.findViewById<TextView>(R.id.tvLogout)

        // Event klik tombol logout
        tvLogout.setOnClickListener {
            // 1. Hapus semua data session
            sessionManager.clearSession()

            // 2. Pindah ke halaman Login
            val intent = Intent(this, Login::class.java)

            // FLAG_ACTIVITY_NEW_TASK: memulai activity baru
            // FLAG_ACTIVITY_CLEAR_TASK: menghapus semua activity sebelumnya
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            // 3. Tutup activity ini
            finish()

            // 4. Tutup bottom sheet
            bottomSheetDialog.dismiss()
        }

        // Tampilkan bottom sheet
        bottomSheetDialog.show()
    }
}