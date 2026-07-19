package com.example.inventarisbarang.screen

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.inventarisbarang.data.BarangRepository
import kotlinx.coroutines.launch

/**
 * DetailBarangScreen menampilkan informasi lengkap satu barang.
 *
 * Analogi: Seperti halaman produk di Tokopedia —
 * menampilkan foto besar, nama, kategori, harga, stok,
 * SKU, berat, status ketersediaan, dan deskripsi lengkap.
 *
 * Layout:
 * ┌─────────────────────────────────────────────┐
 * │  [←] Detail Barang              [⋮]         │ ← TopAppBar
 * ├─────────────────────────────────────────────┤
 * │  ┌─────────────────────────────────────┐    │
 * │  │                                     │    │
 * │  │          GAMBAR BESAR               │    │ ← 220dp tinggi
 * │  │                                     │    │
 * │  └─────────────────────────────────────┘    │
 * │                                             │
 * │  Tas Ransel Premium                         │ ← Nama
 * │  Kategori: Aksesoris                        │ ← Kategori
 * │                                             │
 * │  ┌─────────────────────────────────────┐    │
 * │  │ Harga          Rp245.000            │    │
 * │  │─────────────────────────────────────│    │
 * │  │ Stok           24 unit              │    │ ← Info Card
 * │  │─────────────────────────────────────│    │
 * │  │ SKU            AKS-2024-001         │    │
 * │  │─────────────────────────────────────│    │
 * │  │ Berat          0.8 kg               │    │
 * │  │─────────────────────────────────────│    │
 * │  │ Status         [Tersedia]           │    │
 * │  └─────────────────────────────────────┘    │
 * │                                             │
 * │  ┌─────────────────────────────────────┐    │
 * │  │ Deskripsi                           │    │ ← Deskripsi Card
 * │  │ Tas ransel premium berbahan...      │    │
 * │  └─────────────────────────────────────┘    │
 * │                                             │
 * │  ┌──────────┐  ┌──────────┐                 │
 * │  │  ✎ Edit  │  │  🗑 Hapus │                 │ ← Action Buttons
 * │  └──────────┘  └──────────┘                 │
 * │                                             │
 * │  ┌──────────────────────────────────────┐   │
 * │  │  Snackbar: "Barang berhasil dihapus" │   │ ← Snackbar
 * │  └──────────────────────────────────────┘   │
 * └─────────────────────────────────────────────┘
 *
 * @param navController Controller navigasi untuk berpindah screen
 * @param barangId ID barang yang ingin ditampilkan detailnya
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailBarangScreen(
    navController: NavHostController,
    barangId: Long
) {
    val context = LocalContext.current
    val repository = remember { BarangRepository(context) }

    // State: data barang yang ditampilkan
    var barang by remember { mutableStateOf(repository.getBarangById(barangId)) }

    // State: apakah dialog konfirmasi hapus sedang ditampilkan
    var showDeleteDialog by remember { mutableStateOf(false) }

    // SnackbarHostState untuk menampilkan pesan notifikasi
    val snackbarHostState = remember { SnackbarHostState() }

    // CoroutineScope diperlukan karena showSnackbar() adalah suspend function
    val scope = rememberCoroutineScope()

    // Refresh data saat kembali dari screen edit
    // Ini memastikan data terbaru ditampilkan setelah user mengedit barang
    LaunchedEffect(navController.currentBackStackEntry) {
        barang = repository.getBarangById(barangId)
    }

    // === GUARD: Barang Tidak Ditemukan ===
    if (barang == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Barang tidak ditemukan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text("Kembali")
                }
            }
        }
        return
    }

    // Variabel non-null untuk kemudahan akses (menghindari !! di banyak tempat)
    val item = barang!!

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        // === TOP APP BAR ===
        topBar = {
            TopAppBar(
                title = { Text("Detail Barang") },
                navigationIcon = {
                    // Tombol kembali (←) ke screen sebelumnya
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // Snackbar muncul di bagian bawah layar
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())  // Konten bisa di-scroll vertikal
        ) {
            // ==========================================
            // GAMBAR BESAR
            // ==========================================
            if (item.gambarUri.isNotEmpty()) {
                // Tampilkan gambar dari URI
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(item.gambarUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = item.nama,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder: kotak berwarna dengan inisial nama
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.nama.take(1).uppercase(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // ==========================================
            // NAMA & KATEGORI
            // ==========================================
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = item.nama,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kategori: ${item.kategori}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // CARD INFORMASI DETAIL
            // ==========================================
            // Semua informasi detail ditampilkan dalam card yang rapi
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Baris: Harga
                    DetailRow(label = "Harga", value = formatRupiah(item.harga))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Baris: Stok
                    DetailRow(label = "Stok Tersedia", value = "${item.stok} unit")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Baris: SKU (tampilkan "-" jika kosong)
                    DetailRow(
                        label = "SKU",
                        value = item.sku.ifEmpty { "-" }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Baris: Berat
                    DetailRow(
                        label = "Berat",
                        value = if (item.berat > 0) "${item.berat} kg" else "-"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Baris: Status (dengan badge warna)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Badge status: hijau/biru jika tersedia, merah jika habis
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (item.stok > 0)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (item.stok > 0) "Tersedia" else "Habis",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium,
                                color = if (item.stok > 0)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // ==========================================
            // DESKRIPSI (jika ada)
            // ==========================================
            if (item.deskripsi.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Deskripsi",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.deskripsi,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // TOMBOL EDIT & HAPUS
            // ==========================================
            // Kedua tombol ditampilkan berdampingan (side by side)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- TOMBOL EDIT ---
                // OutlinedButton = tombol dengan border saja (tidak filled)
                // Klik → navigasi ke TambahBarangScreen dalam mode edit
                OutlinedButton(
                    onClick = {
                        // navigate("edit/{id}") membuka TambahBarangScreen
                        // dengan parameter editBarangId = item.id
                        navController.navigate("edit/${item.id}")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }

                // --- TOMBOL HAPUS ---
                // Button filled dengan warna error (merah)
                // Klik → tampilkan dialog konfirmasi dulu (bukan langsung hapus!)
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus")
                }
            }
        }

        // ==========================================
        // DIALOG KONFIRMASI HAPUS
        // ==========================================
        // AlertDialog muncul di atas konten saat showDeleteDialog = true
        // User harus memilih "Hapus" atau "Batal" sebelum bisa kembali
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    // Klik di luar dialog atau tombol back → tutup dialog
                    showDeleteDialog = false
                },
                title = {
                    Text("Hapus Barang")
                },
                text = {
                    Text(
                        "Apakah Anda yakin ingin menghapus \"${item.nama}\"?\n\n" +
                        "Tindakan ini tidak dapat dibatalkan."
                    )
                },
                confirmButton = {
                    // Tombol konfirmasi hapus
                    TextButton(
                        onClick = {
                            // 1. Hapus barang dari SharedPreferences
                            repository.hapusBarang(item.id)
                            showDeleteDialog = false

                            // 2. Tampilkan Snackbar notifikasi berhasil
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "\"${item.nama}\" berhasil dihapus",
                                    duration = SnackbarDuration.Short
                                )
                            }

                            // 3. Kembali ke screen Daftar Barang
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error  // Warna merah
                        )
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    // Tombol batal — tutup dialog, tidak terjadi apa-apa
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

// =====================================================
// KOMPONEN: DetailRow
// =====================================================

/**
 * DetailRow menampilkan satu baris informasi dengan label di kiri dan nilai di kanan.
 *
 * Layout:
 * │ Label                    Nilai │
 *
 * @param label  Teks label (contoh: "Harga", "Stok", "SKU")
 * @param value  Teks nilai (contoh: "Rp245.000", "24 unit", "AKS-2024-001")
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =====================================================
// PREVIEW
// =====================================================

@Preview(showBackground = true, name = "Detail Barang - Light")
@Composable
fun PreviewDetailBarangScreen() {
    MaterialTheme {
        DetailBarangScreen(
            navController = rememberNavController(),
            barangId = 0L
        )
    }
}
