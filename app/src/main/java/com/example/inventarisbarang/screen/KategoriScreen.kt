package com.example.inventarisbarang.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventarisbarang.data.BarangRepository

@Composable
fun KategoriContent(
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { BarangRepository(context) }
    val daftarBarang = repository.getSemuaBarang()
    
    // Ambil kategori unik dari daftar barang
    val kategoriUnik = daftarBarang.map { it.kategori }.distinct().sorted()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding() // Tambahkan padding agar tidak tertutup notification bar
            .padding(16.dp)
    ) {
        Text(
            text = "Kategori Barang",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (kategoriUnik.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada kategori", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(kategoriUnik) { kategori ->
                    CategoryCard(
                        name = kategori,
                        icon = getCategoryIcon(kategori),
                        onClick = { onCategoryClick(kategori) }
                    )
                }
            }
        }
    }
}

/**
 * Mendapatkan ikon yang sesuai untuk setiap kategori.
 */
@Composable
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Elektronik" -> Icons.Default.Devices
        "Fashion" -> Icons.Default.Checkroom
        "Aksesoris" -> Icons.Default.Watch
        "Alat Tulis" -> Icons.Default.Edit
        "Makanan" -> Icons.Default.Restaurant
        "Minuman" -> Icons.Default.LocalDrink
        "Peralatan Rumah" -> Icons.Default.Home
        "Olahraga" -> Icons.Default.SportsBasketball
        else -> Icons.Default.Category
    }
}

@Composable
fun CategoryCard(name: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
