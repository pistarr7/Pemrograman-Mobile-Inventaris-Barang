package com.example.inventarisbarang.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.inventarisbarang.data.BarangRepository
import com.example.inventarisbarang.model.Barang
import kotlinx.coroutines.launch

/**
 * TambahBarangScreen digunakan untuk 2 tujuan:
 * 1. TAMBAH BARANG BARU (editBarangId = null) → form kosong
 * 2. EDIT BARANG EXISTING (editBarangId = <id>) → form terisi data lama
 *
 * Analogi: Seperti formulir pendaftaran produk baru di toko —
 * user mengisi nama, pilih kategori, masukkan harga, stok, dll.
 * Jika sedang mengedit, formulir sudah terisi data yang lama dan
 * user tinggal mengubah bagian yang perlu.
 *
 * Layout:
 * ┌─────────────────────────────────────────────┐
 * │  [←] Tambah/Edit Barang            [✓]      │ ← TopAppBar + Save
 * ├─────────────────────────────────────────────┤
 * │                                             │
 * │  ┌─────────────────────────────────────┐    │
 * │  │      🖼 Tap untuk upload gambar     │    │ ← Image Picker
 * │  └─────────────────────────────────────┘    │
 * │                                             │
 * │  Nama Barang *                              │
 * │  ┌─────────────────────────────────────┐    │
 * │  │ Masukkan nama barang                │    │ ← Text Field
 * │  └─────────────────────────────────────┘    │
 * │                                             │
 * │  Kategori *                                 │
 * │  ┌──────────────────────────────── ▼ ──┐    │
 * │  │ Pilih kategori                      │    │ ← Dropdown
 * │  └─────────────────────────────────────┘    │
 * │                                             │
 * │  Harga (Rp) *           Stok *              │
 * │  ┌──────────────┐   ┌──────────────┐        │
 * │  │ 0            │   │ 0            │        │ ← Side-by-side
 * │  └──────────────┘   └──────────────┘        │
 * │                                             │
 * │  SKU                    Berat (kg)          │
 * │  ┌──────────────┐   ┌──────────────┐        │
 * │  │ Kode SKU     │   │ 0.0          │        │ ← Side-by-side
 * │  └──────────────┘   └──────────────┘        │
 * │                                             │
 * │  Deskripsi                                  │
 * │  ┌─────────────────────────────────────┐    │
 * │  │ Tulis deskripsi barang...           │    │ ← Multi-line
 * │  │                                     │    │
 * │  └─────────────────────────────────────┘    │
 * │                                             │
 * │  ┌─────────────────────────────────────┐    │
 * │  │         SIMPAN / PERBARUI           │    │ ← Save Button
 * │  └─────────────────────────────────────┘    │
 * │                                             │
 * │  ┌──────────────────────────────────────┐   │
 * │  │  Snackbar: "Barang berhasil..."      │   │ ← Snackbar
 * │  └──────────────────────────────────────┘   │
 * └─────────────────────────────────────────────┘
 *
 * FITUR UPLOAD GAMBAR + SHAREDPREFERENCES:
 * ─────────────────────────────────────────
 * Alur penyimpanan gambar:
 *
 * 1. User tap area gambar
 *    → imagePickerLauncher.launch("image/ *")
 *    → Android membuka galeri / file picker
 *
 * 2. User pilih gambar dari galeri
 *    → Callback menerima URI gambar (contoh: "content://media/external/images/123")
 *
 * 3. takePersistableUriPermission(uri)
 *    → Meminta izin PERMANEN untuk membaca URI ini
 *    → Tanpa ini, URI bisa expired setelah app restart
 *
 * 4. URI disimpan sebagai String di state: gambarUri = uri.toString()
 *
 * 5. Saat Simpan: URI string masuk ke objek Barang → diserialisasi ke JSON → SharedPreferences
 *
 * 6. Saat Load: JSON dari SharedPreferences → deserialisasi ke Barang → URI string → AsyncImage
 *
 * @param navController Controller navigasi
 * @param editBarangId  null = mode tambah baru, ada nilai = mode edit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahBarangScreen(
    navController: NavHostController,
    editBarangId: Long? = null
) {
    val context = LocalContext.current
    val repository = remember { BarangRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Tentukan mode: edit atau tambah baru
    val isEditMode = editBarangId != null

    // Jika mode edit, ambil data barang yang sudah ada dari SharedPreferences
    val existingBarang = if (isEditMode) {
        remember { repository.getBarangById(editBarangId!!) }
    } else null

    // ==========================================
    // FORM STATES
    // ==========================================
    // Setiap field form memiliki state sendiri (mutableStateOf)
    // Jika mode edit, state diisi dengan data existing
    // Jika mode tambah, state kosong/default

    var nama by remember {
        mutableStateOf(existingBarang?.nama ?: "")
    }
    var kategori by remember {
        mutableStateOf(existingBarang?.kategori ?: "")
    }
    var harga by remember {
        mutableStateOf(
            if (existingBarang != null && existingBarang.harga > 0)
                existingBarang.harga.toLong().toString()
            else ""
        )
    }
    var stok by remember {
        mutableStateOf(
            if (existingBarang != null && existingBarang.stok > 0)
                existingBarang.stok.toString()
            else ""
        )
    }
    var sku by remember {
        mutableStateOf(existingBarang?.sku ?: "")
    }
    var berat by remember {
        mutableStateOf(
            if (existingBarang != null && existingBarang.berat > 0)
                existingBarang.berat.toString()
            else ""
        )
    }
    var deskripsi by remember {
        mutableStateOf(existingBarang?.deskripsi ?: "")
    }
    var gambarUri by remember {
        mutableStateOf(existingBarang?.gambarUri ?: "")
    }

    // State untuk dropdown kategori (buka/tutup)
    var expandedKategori by remember { mutableStateOf(false) }

    // Daftar pilihan kategori
    val daftarKategori = listOf(
        "Elektronik",
        "Fashion",
        "Aksesoris",
        "Alat Tulis",
        "Makanan",
        "Minuman",
        "Peralatan Rumah",
        "Olahraga",
        "Lainnya"
    )

    // ==========================================
    // IMAGE PICKER LAUNCHER
    // ==========================================
    /**
     * rememberLauncherForActivityResult membuat "launcher" yang bisa
     * membuka aplikasi lain (dalam hal ini, galeri gambar).
     *
     * ActivityResultContracts.GetContent() = kontrak untuk memilih file.
     * Ketika dipanggil dengan launch("image/ *"), Android membuka
     * galeri / file picker yang menampilkan semua file gambar.
     *
     * Setelah user memilih gambar, callback dipanggil dengan URI gambar.
     * URI ini lah yang kita simpan ke SharedPreferences.
     */
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // uri bisa null jika user membatalkan pemilihan (tekan Back)
        uri?.let {
            // === PERSISTABLE URI PERMISSION ===
            // Secara default, izin akses URI dari galeri bersifat SEMENTARA.
            // Artinya, setelah app di-restart, URI tidak bisa diakses lagi.
            //
            // takePersistableUriPermission() membuat izin ini PERMANEN,
            // sehingga gambar tetap bisa ditampilkan kapan pun.
            //
            // Analogi: Seperti mendapatkan "kartu akses permanen" ke
            // perpustakaan, bukan "tiket masuk sekali pakai".
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Beberapa content provider tidak mendukung persistable permission
                // (contoh: beberapa app galeri pihak ketiga)
                // Gambar tetap bisa ditampilkan selama sesi ini
                e.printStackTrace()
            }

            // Simpan URI sebagai String ke state
            // String ini nantinya akan masuk ke objek Barang → JSON → SharedPreferences
            gambarUri = it.toString()
        }
    }

    // ==========================================
    // FUNGSI SIMPAN
    // ==========================================
    /**
     * Memvalidasi input dan menyimpan data barang.
     *
     * Validasi:
     * - Nama wajib diisi
     * - Kategori wajib dipilih
     *
     * Jika valid:
     * 1. Buat objek Barang dari data form
     * 2. Simpan ke SharedPreferences (tambah baru atau update)
     * 3. Tampilkan Snackbar sukses
     * 4. Kembali ke screen sebelumnya
     */
    fun simpanBarang() {
        // Validasi input wajib
        if (nama.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Nama barang wajib diisi!",
                    duration = SnackbarDuration.Short
                )
            }
            return
        }
        if (kategori.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Kategori wajib dipilih!",
                    duration = SnackbarDuration.Short
                )
            }
            return
        }

        // Buat objek Barang dari data form
        val barangBaru = Barang(
            // Jika edit: gunakan ID yang sama. Jika tambah: generate ID baru
            id = existingBarang?.id ?: System.currentTimeMillis(),
            nama = nama.trim(),
            kategori = kategori,
            // toDoubleOrNull() mengembalikan null jika string bukan angka valid
            // Elvis operator (?:) memberikan default 0.0 jika null
            harga = harga.toDoubleOrNull() ?: 0.0,
            stok = stok.toIntOrNull() ?: 0,
            sku = sku.trim(),
            berat = berat.toDoubleOrNull() ?: 0.0,
            deskripsi = deskripsi.trim(),
            gambarUri = gambarUri    // URI gambar dari galeri (String)
        )

        // Simpan ke SharedPreferences via repository
        if (isEditMode) {
            repository.updateBarang(barangBaru)   // Update data existing
        } else {
            repository.tambahBarang(barangBaru)    // Tambah data baru
        }

        // Tampilkan Snackbar sukses
        scope.launch {
            snackbarHostState.showSnackbar(
                message = if (isEditMode) "\"${nama}\" berhasil diperbarui"
                          else "\"${nama}\" berhasil ditambahkan",
                duration = SnackbarDuration.Short
            )
        }

        // Kembali ke screen sebelumnya (popBackStack = mundur 1 langkah)
        navController.popBackStack()
    }

    // ==========================================
    // UI LAYOUT
    // ==========================================
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Barang" else "Tambah Barang")
                },
                navigationIcon = {
                    // Tombol kembali
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // Tombol centang (✓) di kanan TopAppBar — alternatif tombol Simpan
                    IconButton(onClick = { simpanBarang() }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Simpan"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())   // Bisa scroll jika konten panjang
                .padding(16.dp)
        ) {
            // ==========================================
            // AREA UPLOAD GAMBAR
            // ==========================================
            // Klik area ini → buka galeri → pilih gambar → tampilkan preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (gambarUri.isNotEmpty()) 180.dp else 120.dp)
                    .clickable {
                        // launch("image/*") = buka picker untuk semua tipe gambar
                        // "image/*" mencakup: jpg, png, gif, webp, dll.
                        imagePickerLauncher.launch("image/*")
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (gambarUri.isNotEmpty()) {
                    // === PREVIEW GAMBAR YANG DIPILIH ===
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(gambarUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview gambar barang",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Badge keterangan di bawah gambar
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = "Tap untuk ganti gambar",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    // === PLACEHOLDER (belum ada gambar) ===
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = rememberPlaceholderIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap untuk upload gambar",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "JPG, PNG (dari galeri)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // FIELD: NAMA BARANG
            // ==========================================
            FormLabel(text = "Nama Barang *")
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                placeholder = { Text("Masukkan nama barang") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // FIELD: KATEGORI (Dropdown Menu)
            // ==========================================
            /**
             * ExposedDropdownMenuBox = komponen dropdown Material3.
             *
             * Cara kerja:
             * 1. User klik TextField → expanded = true → menu terbuka
             * 2. User pilih item dari menu → kategori = item yang dipilih
             * 3. Menu otomatis tertutup (expanded = false)
             *
             * readOnly = true → user tidak bisa mengetik manual,
             * hanya bisa memilih dari pilihan yang tersedia.
             * Ini mencegah typo dan memastikan konsistensi data.
             */
            FormLabel(text = "Kategori *")
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = expandedKategori,
                onExpandedChange = { expandedKategori = !expandedKategori }
            ) {
                OutlinedTextField(
                    value = kategori,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Pilih kategori") },
                    trailingIcon = {
                        // Ikon panah bawah yang otomatis berputar saat menu terbuka
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategori)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),   // Menandai TextField ini sebagai anchor menu
                    shape = RoundedCornerShape(8.dp)
                )
                // Menu dropdown yang berisi daftar kategori
                ExposedDropdownMenu(
                    expanded = expandedKategori,
                    onDismissRequest = { expandedKategori = false }
                ) {
                    daftarKategori.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                kategori = item              // Set kategori terpilih
                                expandedKategori = false     // Tutup menu
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // FIELD: HARGA & STOK (Side by side)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- HARGA ---
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel(text = "Harga (Rp) *")
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = harga,
                        onValueChange = { harga = it },
                        placeholder = { Text("0") },
                        singleLine = true,
                        // KeyboardType.Number = keyboard hanya menampilkan angka
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                // --- STOK ---
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel(text = "Stok *")
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = stok,
                        onValueChange = { stok = it },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // FIELD: SKU & BERAT (Side by side)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- SKU ---
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel(text = "SKU")
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        placeholder = { Text("Kode SKU") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                // --- BERAT ---
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel(text = "Berat (kg)")
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = berat,
                        onValueChange = { berat = it },
                        placeholder = { Text("0.0") },
                        singleLine = true,
                        // KeyboardType.Decimal = angka + titik desimal
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // FIELD: DESKRIPSI (Multi-line)
            // ==========================================
            FormLabel(text = "Deskripsi")
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                placeholder = { Text("Tulis deskripsi barang...") },
                minLines = 3,    // Minimal 3 baris tinggi
                maxLines = 5,    // Maksimal 5 baris (scroll jika lebih)
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // TOMBOL SIMPAN
            // ==========================================
            Button(
                onClick = { simpanBarang() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isEditMode) "Perbarui Barang" else "Simpan Barang",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Spacer bawah agar konten tidak terlalu mepet
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =====================================================
// KOMPONEN HELPER
// =====================================================

/**
 * FormLabel menampilkan label untuk field form.
 * Konsisten di semua field: ukuran 13sp, warna secondary, semi-bold.
 *
 * @param text Teks label yang ditampilkan
 */
@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Membuat ikon placeholder untuk area upload gambar.
 *
 * Menggunakan vector drawable sederhana berupa ikon gambar/foto.
 * Kita gunakan ikon dari Material Icons yang sudah tersedia.
 */
@Composable
fun rememberPlaceholderIcon(): androidx.compose.ui.graphics.painter.Painter {
    // Menggunakan ikon bawaan Material — AddPhotoAlternate style
    return androidx.compose.ui.res.painterResource(
        android.R.drawable.ic_menu_gallery
    )
}

// =====================================================
// PREVIEW
// =====================================================

@Preview(showBackground = true, name = "Tambah Barang - Light")
@Composable
fun PreviewTambahBarangScreen() {
    MaterialTheme {
        TambahBarangScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Tambah Barang - Dark")
@Composable
fun PreviewTambahBarangScreenDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        TambahBarangScreen(navController = rememberNavController())
    }
}
