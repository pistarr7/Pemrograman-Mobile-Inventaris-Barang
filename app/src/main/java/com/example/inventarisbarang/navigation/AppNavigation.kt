package com.example.inventarisbarang.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.inventarisbarang.screen.MainScreen
import com.example.inventarisbarang.screen.DetailBarangScreen
import com.example.inventarisbarang.screen.TambahBarangScreen

/**
 * AppNavigation mengatur seluruh alur navigasi (perpindahan screen) di aplikasi.
 *
 * Analogi: NavHost itu seperti "peta jalan" aplikasi.
 * Setiap composable() adalah satu "halte" (screen) yang bisa dikunjungi.
 * NavController adalah "sopir" yang membawa user dari satu halte ke halte lain.
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │                    PETA NAVIGASI APLIKASI                    │
 * │                                                              │
 * │   ┌─────────┐   klik card    ┌──────────┐   klik Edit       │
 * │   │ daftar  │ ─────────────► │detail/{id}│ ──────────────┐  │
 * │   │         │                │          │                │  │
 * │   │  FAB +  │                │  Hapus ──► Dialog         │  │
 * │   └────┬────┘                └──────────┘                │  │
 * │        │                                                  │  │
 * │   klik FAB                                               ▼  │
 * │        │                                          ┌──────────┐│
 * │        └────────────────────────────────────────► │ tambah   ││
 * │                                                   │ edit/{id}││
 * │                                                   └──────────┘│
 * └──────────────────────────────────────────────────────────────┘
 *
 * Route (rute) yang tersedia:
 * - "daftar"      → Screen 1: Daftar Barang (halaman utama)
 * - "detail/{id}" → Screen 2: Detail Barang (id = ID barang yang diklik)
 * - "tambah"      → Screen 3a: Tambah Barang Baru (form kosong)
 * - "edit/{id}"   → Screen 3b: Edit Barang (form terisi data existing)
 *
 * @param navController Controller navigasi yang dibuat di MainActivity
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main"    // Screen pertama yang muncul saat app dibuka
    ) {
        // ==========================================
        // MAIN SCREEN (Beranda, Kategori, Profil)
        // ==========================================
        composable("main") {
            MainScreen(navController = navController)
        }

        // ==========================================
        // SCREEN 2: DETAIL BARANG
        // ==========================================
        // Route dengan parameter {id} — nilai berubah sesuai barang yang diklik
        // Contoh: navController.navigate("detail/1717123456789")
        //         → backStackEntry.arguments?.getLong("id") = 1717123456789
        composable(
            route = "detail/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType  // Tipe parameter: Long (angka besar)
                }
            )
        ) { backStackEntry ->
            // Ambil nilai id dari URL route
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            DetailBarangScreen(
                navController = navController,
                barangId = id
            )
        }

        // ==========================================
        // SCREEN 3a: TAMBAH BARANG BARU
        // ==========================================
        // editBarangId = null → TambahBarangScreen dalam mode "tambah baru"
        composable("tambah") {
            TambahBarangScreen(navController = navController)
        }

        // ==========================================
        // SCREEN 3b: EDIT BARANG YANG SUDAH ADA
        // ==========================================
        // editBarangId = id → TambahBarangScreen dalam mode "edit"
        // Menggunakan screen yang sama untuk menghindari duplikasi kode
        composable(
            route = "edit/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            TambahBarangScreen(
                navController = navController,
                editBarangId = id
            )
        }
    }
}
