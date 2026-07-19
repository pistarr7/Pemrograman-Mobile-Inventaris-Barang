package com.example.inventarisbarang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.inventarisbarang.navigation.AppNavigation
import com.example.inventarisbarang.ui.theme.InventarisBarangTheme

/**
 * MainActivity adalah titik masuk (entry point) aplikasi Android.
 *
 * Analogi: MainActivity itu seperti "pintu depan" rumah —
 * semua tamu (user) masuk melalui pintu ini.
 * Di dalam, kita menyiapkan "ruangan" (NavController + NavHost)
 * agar tamu bisa berpindah dari satu ruangan ke ruangan lain.
 *
 * Lifecycle (siklus hidup) yang terjadi:
 * 1. onCreate()  → App pertama kali dibuka, inisialisasi UI
 * 2. onStart()   → App terlihat di layar
 * 3. onResume()  → App di foreground, user bisa berinteraksi
 * 4. onPause()   → App kehilangan fokus (misal: dialog muncul)
 * 5. onStop()    → App tidak terlihat (misal: user pindah app)
 * 6. onDestroy() → App ditutup total
 *
 * Kita hanya perlu override onCreate() karena Compose menangani
 * sisanya secara otomatis melalui recomposition.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge() membuat tampilan full-screen
        // Konten app bisa "menembus" ke belakang status bar dan navigation bar
        enableEdgeToEdge()

        // setContent {} menentukan UI yang ditampilkan menggunakan Jetpack Compose
        // Ini pengganti setContentView(R.layout.activity_main) di XML-based UI
        setContent {
            // Terapkan tema aplikasi (mendukung light/dark mode + dynamic color)
            InventarisBarangTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Buat NavController yang "mengingat" posisi navigasi
                    // rememberNavController() memastikan controller bertahan
                    // saat configuration change (rotasi layar, dll)
                    val navController = rememberNavController()

                    // Jalankan sistem navigasi
                    // AppNavigation berisi semua route (rute) screen
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
