package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.components.AppCard
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.ProfitBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.SellingPriceCalculatorDialog
import com.example.ui.components.StockAdjustDialog
import com.example.ui.components.StockBadge
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val productWithDetails = allProducts.find { it.product.id == productId }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }
    var showCalculatorDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Hapus Barang",
            message = "Apakah Anda yakin ingin menghapus barang ini secara permanen?",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteProduct(productId) {
                    onNavigateBack()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showCalculatorDialog && productWithDetails != null) {
        val latestModal = productWithDetails.latestPurchasePrice?.price
        val latestUnit = productWithDetails.latestPurchasePrice?.unit ?: ""
        SellingPriceCalculatorDialog(
            initialModal = latestModal,
            initialUnit = latestUnit,
            onDismiss = { showCalculatorDialog = false }
        )
    }

    if (showStockDialog && productWithDetails != null) {
        val currentStock = productWithDetails.product.stock
        val unit = productWithDetails.product.stockUnit.ifBlank {
            productWithDetails.latestPurchasePrice?.unit ?: "unit"
        }
        StockAdjustDialog(
            productName = productWithDetails.product.name,
            currentStock = currentStock,
            unit = unit,
            onConfirm = { type, qty, newStock, note ->
                showStockDialog = false
                viewModel.adjustStock(
                    productId = productId,
                    type = type,
                    quantity = qty,
                    newStock = newStock,
                    unit = unit,
                    note = note
                )
            },
            onDismiss = { showStockDialog = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Detail Barang", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (productWithDetails != null) {
                        val isFav = productWithDetails.product.isFavorite
                        IconButton(onClick = { viewModel.toggleFavorite(productId, isFav) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorit",
                                tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showCalculatorDialog = true }) {
                            Icon(Icons.Default.Calculate, contentDescription = "Kalkulator Harga")
                        }
                        IconButton(onClick = { onNavigateToEdit(productId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Barang")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus Barang",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (productWithDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Barang tidak ditemukan.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        val prod = productWithDetails.product
        val purchase = productWithDetails.latestPurchasePrice
        val conversion = productWithDetails.conversion
        val sellingCalculations = productWithDetails.getCalculatedSellingPrices()
        val priceHistories = productWithDetails.priceHistory
        val stockHistories = productWithDetails.stockHistory

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Product Image Hero (if available)
            if (!prod.imageUri.isNullOrBlank() && File(prod.imageUri).exists()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        AsyncImage(
                            model = File(prod.imageUri),
                            contentDescription = prod.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Basic Info Card
            item {
                AppCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (prod.sku.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "SKU: ${prod.sku}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                if (prod.category.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = prod.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            if (prod.brand.isNotBlank()) {
                                Text(
                                    text = "Merek: ${prod.brand}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = prod.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (prod.variant.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ukuran / Varian: ${prod.variant}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (prod.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Catatan: ${prod.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Stock Management Section
            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(
                            title = "Manajemen Stok",
                            subtitle = "Pantau dan kelola ketersediaan fisik barang"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val unitStr = prod.stockUnit.ifBlank { purchase?.unit ?: "unit" }
                    val isLow = prod.minimumStock > 0 && prod.stock <= prod.minimumStock

                    if (isLow) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Peringatan: Stok barang menipis atau berada di bawah batas minimum!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Stok Saat Ini",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${Formatters.formatNumber(prod.stock)} $unitStr",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            if (prod.minimumStock > 0) {
                                Text(
                                    text = "Batas minimum: ${Formatters.formatNumber(prod.minimumStock)} $unitStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showStockDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kelola Stok", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Purchase Price from Supplier
            item {
                AppCard {
                    SectionHeader(
                        title = "Harga Beli Supplier",
                        subtitle = "Harga modal pembelian dari pemasok"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (purchase != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Formatters.formatRupiah(purchase.price),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Satuan pembelian: ${purchase.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Diperbarui",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatDate(purchase.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (purchase.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pemasok / Catatan: ${purchase.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Belum ada data harga beli supplier.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Unit Conversion (if entered)
            if (conversion != null) {
                item {
                    AppCard {
                        SectionHeader(
                            title = "Konversi Satuan",
                            subtitle = "Hubungan antara kemasan beli dan satuan jual"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "1 ${conversion.fromUnit} = ${Formatters.formatNumber(conversion.quantity)} ${conversion.toUnit}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Harga Modal Otomatis Section
            if (purchase != null) {
                item {
                    AppCard {
                        SectionHeader(
                            title = "Harga Modal Otomatis",
                            subtitle = "Dihitung otomatis berdasarkan harga beli & konversi"
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Modal per ${purchase.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${Formatters.formatRupiah(purchase.price)} / ${purchase.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (conversion != null && conversion.quantity > 0) {
                                val modalPerIsi = purchase.price / conversion.quantity
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Modal per ${conversion.toUnit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${Formatters.formatRupiah(modalPerIsi)} / ${conversion.toUnit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selling Prices & Profit Section
            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(
                            title = "Daftar Harga Jual & Keuntungan",
                            subtitle = "Harga jual toko per satuan penjualan"
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (sellingCalculations.isEmpty()) {
                        Text(
                            text = "Belum ada harga jual yang ditambahkan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            sellingCalculations.forEach { calc ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Harga per ${calc.sellingPrice.unit}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "${Formatters.formatRupiah(calc.sellingPrice.price)} / ${calc.sellingPrice.unit}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                if (calc.modalPrice != null) {
                                                    Text(
                                                        text = "Modal: ${Formatters.formatRupiah(calc.modalPrice)} / ${calc.sellingPrice.unit}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            ProfitBadge(
                                                profit = calc.profit,
                                                profitPercentage = calc.profitPercentage
                                            )
                                        }

                                        // Potensi jika seluruh isi terjual pada harga satuan ini
                                        if (calc.potentialRevenue != null && calc.potentialProfit != null && calc.potentialQuantity != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    Text(
                                                        text = "Potensi jika seluruh isi terjual pada harga ini:",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                    Text(
                                                        text = "• Potensi Omzet: ${Formatters.formatNumber(calc.potentialQuantity)} ${calc.sellingPrice.unit} × ${Formatters.formatRupiah(calc.sellingPrice.price)} = ${Formatters.formatRupiah(calc.potentialRevenue)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "• Potensi Keuntungan: ${Formatters.formatRupiah(calc.potentialProfit)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (calc.potentialProfit >= 0) ProfitGreen else MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Price History (if any)
            if (priceHistories.isNotEmpty()) {
                item {
                    AppCard {
                        SectionHeader(
                            title = "Riwayat Perubahan Harga",
                            subtitle = "Catatan pembaruan harga lama ke harga baru"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            priceHistories.forEach { ph ->
                                val selisih = ph.newPrice - ph.oldPrice
                                val selisihPct = if (ph.oldPrice > 0) (selisih / ph.oldPrice) * 100.0 else 0.0

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${ph.type} (${ph.unit})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${Formatters.formatRupiah(ph.oldPrice)} → ${Formatters.formatRupiah(ph.newPrice)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Selisih: ${if (selisih >= 0) "+" else ""}${Formatters.formatRupiah(selisih)} (${Formatters.formatPercentage(selisihPct)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selisih >= 0) ProfitGreen else MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Text(
                                        text = Formatters.formatDate(ph.changedAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }

            // Stock History (if any)
            if (stockHistories.isNotEmpty()) {
                item {
                    AppCard {
                        SectionHeader(
                            title = "Riwayat Perubahan Stok",
                            subtitle = "Catatan mutasi penambahan, pengurangan, atau penyesuaian"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stockHistories.forEach { sh ->
                                val typeDesc = when (sh.type) {
                                    "TAMBAH" -> "Penambahan (+${Formatters.formatNumber(sh.quantity)} ${sh.unit})"
                                    "KURANG" -> "Pengurangan (-${Formatters.formatNumber(sh.quantity)} ${sh.unit})"
                                    else -> "Penyesuaian (${Formatters.formatNumber(sh.quantity)} ${sh.unit})"
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = typeDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (sh.type) {
                                                "TAMBAH" -> ProfitGreen
                                                "KURANG" -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                        if (sh.note.isNotBlank()) {
                                            Text(
                                                text = "Catatan: ${sh.note}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = Formatters.formatDate(sh.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigateToEdit(productId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("edit_product_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Barang", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("delete_product_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus Barang", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
