package com.example.inventarisbarang.screen

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.inventarisbarang.data.BarangRepository
import com.example.inventarisbarang.model.Barang
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DaftarBarangContent(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialCategoryFilter: String = "",
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { BarangRepository(context) }

    var daftarBarang by remember { mutableStateOf(repository.getSemuaBarang()) }
    
    // Sinkronisasi searchQuery jika ada filter awal
    LaunchedEffect(initialCategoryFilter) {
        if (initialCategoryFilter.isNotEmpty()) {
            onSearchQueryChange(initialCategoryFilter)
        }
    }

    val filteredBarang = if (searchQuery.isEmpty()) {
        daftarBarang
    } else {
        daftarBarang.filter {
            it.nama.contains(searchQuery, ignoreCase = true) ||
            it.kategori.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        daftarBarang = repository.getSemuaBarang()
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (filteredBarang.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isEmpty()) "Belum ada barang"
                        else "Barang \"$searchQuery\" tidak ditemukan",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = filteredBarang, key = { it.id }) { barang ->
                    BarangCard(
                        barang = barang,
                        onClick = { navController.navigate("detail/${barang.id}") }
                    )
                }
            }
        }
    }
}

// Komponen BarangCard dan formatRupiah tetap sama (bisa diletakkan di sini atau dipindah ke file terpisah)
@Composable
fun BarangCard(barang: Barang, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (barang.gambarUri.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(barang.gambarUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = barang.nama.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = barang.nama,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = barang.kategori,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatRupiah(barang.harga),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (barang.stok <= 5) MaterialTheme.colorScheme.errorContainer 
                                else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Stok: ${barang.stok}",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (barang.stok <= 5) MaterialTheme.colorScheme.onErrorContainer 
                                    else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Text(text = "›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}
