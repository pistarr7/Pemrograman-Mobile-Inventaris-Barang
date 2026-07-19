# ProGuard Rules untuk Inventaris Barang
# Tambahkan aturan khusus di sini jika diperlukan

# Gson memerlukan aturan agar tidak menghapus field saat minify
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.inventarisbarang.model.** { *; }
