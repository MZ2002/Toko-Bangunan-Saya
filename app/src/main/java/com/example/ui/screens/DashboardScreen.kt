package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ProductWithDetails
import com.example.ui.components.AppCard
import com.example.ui.components.SellingPriceCalculatorDialog
import com.example.ui.components.StockBadge
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.RecentActivity
import com.example.util.Formatters
import java.io.File

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductList: (String?) -> Unit,
    onNavigateToProductDetail: (Long) -> Unit,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val favoriteProducts by viewModel.favoriteProducts.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val thisMonthExpensesTotal by viewModel.thisMonthExpensesTotal.collectAsStateWithLifecycle()
    val recentPriceUpdatesCount by viewModel.recentPriceUpdatesCount.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()

    var showCalculator by remember { mutableStateOf(false) }

    if (showCalculator) {
        SellingPriceCalculatorDialog(
            onDismiss = { showCalculator = false }
        )
    }

    if (allProducts.isEmpty()) {
        // EXACT ZERO DATA REQUIREMENT ON FIRST LAUNCH
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "TB Jaya Abadi Logo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "TB Jaya Abadi",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Buku Harga Digital Toko",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada barang.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Database dalam keadaan kosong. Silakan tambahkan barang pertama Anda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onNavigateToAddProduct,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("add_first_product_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Tambah Barang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showCalculator = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kalkulator Harga Jual", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    } else {
        // REAL DATA DASHBOARD
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TB Jaya Abadi",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Buku Harga Digital Pribadi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Search Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigateToProductList(null) }
                            .testTag("dashboard_search_bar"),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Cari nama barang, SKU, merek, satuan...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Summary Stats Cards (2x2 Grid)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "Ringkasan Toko",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1: Total Barang & Stok Rendah
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Barang",
                            value = "${allProducts.size}",
                            subtitle = "Tersimpan di database",
                            icon = Icons.Default.Inventory,
                            tintColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToProductList(null) }
                        )

                        StatCard(
                            title = "Stok Rendah",
                            value = "${lowStockProducts.size}",
                            subtitle = if (lowStockProducts.isNotEmpty()) "Perlu ditambah" else "Stok aman",
                            icon = Icons.Default.Warning,
                            tintColor = if (lowStockProducts.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToProductList(null) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: Harga Diperbarui & Pengeluaran Bulan Ini
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Harga Diperbarui",
                            value = "$recentPriceUpdatesCount",
                            subtitle = "30 hari terakhir",
                            icon = Icons.Default.PriceCheck,
                            tintColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToProductList(null) }
                        )

                        StatCard(
                            title = "Pengeluaran Bulan Ini",
                            value = Formatters.formatRupiah(thisMonthExpensesTotal),
                            subtitle = "Bulan berjalan",
                            icon = Icons.Default.Payment,
                            tintColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToExpenses
                        )
                    }
                }
            }

            // Quick Actions: Tambah Barang, Pengeluaran, Kalkulator
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToAddProduct,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("dashboard_add_product_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "+ Barang",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToExpenses,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("dashboard_add_expense_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pengeluaran", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { showCalculator = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("dashboard_calculator_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kalkulator", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Aktivitas Terbaru (Recent Activities) Section
            if (recentActivities.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Aktivitas Terbaru",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AppCard {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                recentActivities.take(5).forEachIndexed { idx, act ->
                                    ActivityItemRow(act)
                                    if (idx < minOf(4, recentActivities.size - 1)) {
                                        androidx.compose.material3.HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Favorites Section
            item {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Barang Favorit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (favoriteProducts.isNotEmpty()) {
                            Text(
                                text = "${favoriteProducts.size} barang",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (favoriteProducts.isEmpty()) {
                        AppCard {
                            Text(
                                text = "Belum ada barang favorit.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tandai bintang (⭐) pada barang yang sering Anda cek harganya.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // If there are favorite products, show them
            if (favoriteProducts.isNotEmpty()) {
                items(favoriteProducts.take(4), key = { "fav_${it.product.id}" }) { pwd ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        ProductDashboardItem(
                            pwd = pwd,
                            onToggleFavorite = { viewModel.toggleFavorite(pwd.product.id, pwd.product.isFavorite) },
                            onClick = { onNavigateToProductDetail(pwd.product.id) }
                        )
                    }
                }
            }

            // Recent Added Products
            item {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daftar Barang Terbaru",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { onNavigateToProductList(null) }
                        ) {
                            Text("Lihat Semua")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            items(allProducts.take(6), key = { "recent_${it.product.id}" }) { pwd ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    ProductDashboardItem(
                        pwd = pwd,
                        onToggleFavorite = { viewModel.toggleFavorite(pwd.product.id, pwd.product.isFavorite) },
                        onClick = { onNavigateToProductDetail(pwd.product.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityItemRow(act: RecentActivity) {
    val badgeColor = when (act.badgeType) {
        "PRICE" -> MaterialTheme.colorScheme.primary
        "STOCK" -> MaterialTheme.colorScheme.secondary
        "PRODUCT" -> ProfitGreen
        else -> MaterialTheme.colorScheme.tertiary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(badgeColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = act.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = act.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = Formatters.formatDate(act.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tintColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AppCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = tintColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProductDashboardItem(
    pwd: ProductWithDetails,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Thumbnail image if available
            val imgUri = pwd.product.imageUri
            if (!imgUri.isNullOrBlank() && File(imgUri).exists()) {
                AsyncImage(
                    model = File(imgUri),
                    contentDescription = pwd.product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (pwd.product.sku.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = pwd.product.sku,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            if (pwd.product.category.isNotBlank()) {
                                Text(
                                    text = pwd.product.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (pwd.product.brand.isNotBlank()) {
                                Text(
                                    text = " • ${pwd.product.brand}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = pwd.product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (pwd.product.variant.isNotBlank()) {
                            Text(
                                text = "Varian: ${pwd.product.variant}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (pwd.product.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorit",
                            tint = if (pwd.product.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Stock indicator if configured
                if (pwd.product.stock > 0 || pwd.product.minimumStock > 0) {
                    StockBadge(
                        stock = pwd.product.stock,
                        stockUnit = pwd.product.stockUnit,
                        minimumStock = pwd.product.minimumStock
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Price breakdown row
                val purchase = pwd.latestPurchasePrice
                val sellingCalculations = pwd.getCalculatedSellingPrices()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        if (purchase != null) {
                            Text(
                                text = "Beli: ${Formatters.formatRupiah(purchase.price)}/${purchase.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (pwd.conversion != null) {
                            val c = pwd.conversion!!
                            Text(
                                text = "1 ${c.fromUnit} = ${Formatters.formatNumber(c.quantity)} ${c.toUnit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (sellingCalculations.isNotEmpty()) {
                            val first = sellingCalculations.first()
                            Text(
                                text = "Jual: ${Formatters.formatRupiah(first.sellingPrice.price)}/${first.sellingPrice.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (first.profit != null) {
                                Text(
                                    text = "+${Formatters.formatRupiah(first.profit)} (${Formatters.formatPercentage(first.profitPercentage)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ProfitGreen
                                )
                            }
                        } else {
                            Text(
                                text = "Belum ada harga jual",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
