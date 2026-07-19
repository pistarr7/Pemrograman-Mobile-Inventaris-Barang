package com.example.inventarisbarang.model

/**
 * Data class Barang merepresentasikan satu item barang di inventaris.
 *
 * Analogi: Seperti satu kartu katalog di perpustakaan —
 * setiap kartu menyimpan informasi lengkap tentang satu buku.
 * Di sini, setiap objek Barang menyimpan info lengkap satu item inventaris.
 *
 * Data class otomatis meng-generate:
 * - equals() & hashCode()  → untuk perbandingan objek
 * - toString()              → untuk debugging
 * - copy()                  → untuk membuat salinan dengan modifikasi
 * - componentN()            → untuk destructuring
 *
 * @param id         ID unik barang (auto-generate menggunakan System.currentTimeMillis)
 * @param nama       Nama barang, contoh: "Tas Ransel Premium"
 * @param kategori   Kategori barang, contoh: "Aksesoris"
 * @param harga      Harga dalam Rupiah, contoh: 245000.0
 * @param stok       Jumlah stok tersedia, contoh: 24
 * @param sku        Stock Keeping Unit — kode unik produk, contoh: "AKS-2024-001"
 * @param berat      Berat dalam kilogram, contoh: 0.8
 * @param deskripsi  Penjelasan detail tentang barang
 * @param gambarUri  URI gambar dari galeri (disimpan sebagai String di SharedPreferences)
 */
data class Barang(
    val id: Long = System.currentTimeMillis(),
    val nama: String = "",
    val kategori: String = "",
    val harga: Double = 0.0,
    val stok: Int = 0,
    val sku: String = "",
    val berat: Double = 0.0,
    val deskripsi: String = "",
    val gambarUri: String = ""
)
