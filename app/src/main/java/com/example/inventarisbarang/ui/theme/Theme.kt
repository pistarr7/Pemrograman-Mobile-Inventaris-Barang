package com.example.inventarisbarang.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Definisi warna untuk tema aplikasi.
 *
 * Material3 menggunakan sistem "color roles" dimana setiap warna
 * memiliki peran tertentu (primary, secondary, surface, error, dll).
 *
 * Warna utama menggunakan ungu (Purple) sesuai mockup.
 */

// Warna untuk Light Theme (tema terang)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),             // Warna utama: ungu
    onPrimary = Color(0xFFFFFFFF),           // Teks di atas primary: putih
    primaryContainer = Color(0xFFE8DEF8),    // Container primary: ungu muda
    onPrimaryContainer = Color(0xFF21005E),  // Teks di atas primaryContainer
    secondary = Color(0xFF625B71),           // Warna sekunder
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1E192B),
    error = Color(0xFFBA1A1A),               // Warna error: merah
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),      // Container error: merah muda
    onErrorContainer = Color(0xFF410002),
    surface = Color(0xFFFFFBFF),             // Permukaan: putih
    onSurface = Color(0xFF1C1B1F),           // Teks di atas surface
    surfaceVariant = Color(0xFFE7E0EB),
    onSurfaceVariant = Color(0xFF49454E),
    outline = Color(0xFF7A757F)
)

// Warna untuk Dark Theme (tema gelap)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCFBCFF),             // Warna utama di dark: ungu terang
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFE8DEF8),
    secondary = Color(0xFFCBC2DB),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCAC4CF),
    outline = Color(0xFF948F99)
)

/**
 * Tema utama aplikasi Inventaris Barang.
 *
 * Menggunakan Dynamic Color (Material You) pada Android 12+ (API 31+),
 * yang secara otomatis menyesuaikan warna app dengan wallpaper user.
 *
 * Pada Android 11 ke bawah, menggunakan warna statis yang sudah didefinisikan di atas.
 *
 * @param darkTheme Apakah menggunakan tema gelap (mengikuti setting sistem)
 * @param dynamicColor Apakah menggunakan Dynamic Color (default: true)
 * @param content Konten Composable yang dibungkus tema ini
 */
@Composable
fun InventarisBarangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic Color hanya tersedia di Android 12+ (API 31+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
