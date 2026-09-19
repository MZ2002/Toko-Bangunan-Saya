package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppCard
import com.example.ui.components.ProductImagePicker
import com.example.ui.components.ProfitBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.SellingPriceCalculatorDialog
import com.example.ui.components.UnitDropdownSelector
import com.example.ui.components.UnitSelector
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Formatters

class SellingPriceInputState(
    initialPrice: String = "",
    initialUnit: String = ""
) {
    var priceText by mutableStateOf(initialPrice)
    var unit by mutableStateOf(initialUnit)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Long?,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val existingProduct = remember(productId, allProducts) {
        if (productId != null && productId > 0) {
            allProducts.find { it.product.id == productId }
        } else null
    }

    // Basic fields
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var variant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<String?>(null) }

    // Stock fields
    var stockText by remember { mutableStateOf("") }
    var stockUnit by remember { mutableStateOf("") }
    var minStockText by remember { mutableStateOf("") }

    // Purchase fields
    var purchasePriceText by remember { mutableStateOf("") }
    var purchaseUnit by remember { mutableStateOf("") }
    var purchaseNotes by remember { mutableStateOf("") }

    // Conversion fields
    var hasConversion by remember { mutableStateOf(false) }
    var conversionFromUnit by remember { mutableStateOf("") }
    var conversionQtyText by remember { mutableStateOf("") }
    var conversionToUnit by remember { mutableStateOf("") }

    // Selling prices list
    val sellingPrices = remember { mutableStateListOf<SellingPriceInputState>() }

    var nameError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showCalcDialog by remember { mutableStateOf(false) }

    // Load existing product if editing
    LaunchedEffect(existingProduct) {
        if (existingProduct != null) {
            val p = existingProduct.product
            name = p.name
            sku = p.sku
            category = p.category
            brand = p.brand
            variant = p.variant
            notes = p.notes
            isFavorite = p.isFavorite
            imageUri = p.imageUri

            if (p.stock > 0) {
                stockText = if (p.stock % 1.0 == 0.0) p.stock.toLong().toString() else p.stock.toString()
            }
            stockUnit = p.stockUnit
            if (p.minimumStock > 0) {
                minStockText = if (p.minimumStock % 1.0 == 0.0) p.minimumStock.toLong().toString() else p.minimumStock.toString()
            }

            val pp = existingProduct.latestPurchasePrice
            if (pp != null) {
                purchasePriceText = if (pp.price % 1.0 == 0.0) pp.price.toLong().toString() else pp.price.toString()
                purchaseUnit = pp.unit
                purchaseNotes = pp.notes
            }

            val conv = existingProduct.conversion
            if (conv != null) {
                hasConversion = true
                conversionFromUnit = conv.fromUnit
                conversionQtyText = if (conv.quantity % 1.0 == 0.0) conv.quantity.toLong().toString() else conv.quantity.toString()
                conversionToUnit = conv.toUnit
            }

            sellingPrices.clear()
            existingProduct.sellingPrices.forEach { sp ->
                val pStr = if (sp.price % 1.0 == 0.0) sp.price.toLong().toString() else sp.price.toString()
                sellingPrices.add(SellingPriceInputState(initialPrice = pStr, initialUnit = sp.unit))
            }
        }
    }

    // Dynamic Helper Calculation for Live Modal & Profit Preview
    val parsedPurchasePrice = purchasePriceText.toDoubleOrNull()
    val parsedConversionQty = conversionQtyText.toDoubleOrNull()

    fun calculateLiveModal(targetUnit: String): Double? {
        if (parsedPurchasePrice == null || parsedPurchasePrice <= 0) return null
        val target = targetUnit.trim()
        val pUnit = purchaseUnit.trim()
        if (target.isBlank() || pUnit.isBlank()) return null

        // 1. Jika satuan jual sama dengan satuan beli
        if (target.equals(pUnit, ignoreCase = true)) {
            return parsedPurchasePrice
        }

        // 2. Jika konversi aktif dan terdefinisi: 1 Satuan Beli (pUnit) = parsedConversionQty Satuan Isi (toUnit)
        if (hasConversion && parsedConversionQty != null && parsedConversionQty > 0) {
            val to = conversionToUnit.trim()

            // Target adalah Satuan Isi (contoh: pcs vs pack): modal = hargaBeli / jumlahIsi
            if (target.equals(to, ignoreCase = true)) {
                return parsedPurchasePrice / parsedConversionQty
            }
            // Target adalah Satuan Beli dan purchaseUnit adalah Satuan Isi
            if (pUnit.equals(to, ignoreCase = true) && target.equals(conversionFromUnit.trim(), ignoreCase = true)) {
                return parsedPurchasePrice * parsedConversionQty
            }
        }
        return null
    }

    if (showCalcDialog) {
        SellingPriceCalculatorDialog(
            initialModal = parsedPurchasePrice,
            initialUnit = purchaseUnit,
            onUsePrice = { price, unit ->
                val u = unit.ifBlank { purchaseUnit.ifBlank { "pcs" } }
                val pStr = if (price % 1.0 == 0.0) price.toLong().toString() else price.toString()
                sellingPrices.add(SellingPriceInputState(initialPrice = pStr, initialUnit = u))
            },
            onDismiss = { showCalcDialog = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (productId != null && productId > 0) "Edit Barang" else "Tambah Barang Baru",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (validationError != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = validationError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            validationError = null

                            // Validasi Nama
                            if (name.isBlank()) {
                                nameError = true
                                validationError = "Nama barang wajib diisi"
                                return@Button
                            }

                            // Validasi Harga Beli
                            if (purchasePriceText.isNotBlank()) {
                                if (parsedPurchasePrice == null || parsedPurchasePrice <= 0) {
                                    validationError = "Harga beli harus lebih besar dari Rp 0"
                                    return@Button
                                }
                                if (purchaseUnit.isBlank()) {
                                    validationError = "Satuan beli wajib dipilih"
                                    return@Button
                                }
                            }

                            // Validasi Konversi Satuan
                            if (hasConversion) {
                                if (purchaseUnit.isBlank()) {
                                    validationError = "Satuan beli wajib dipilih terlebih dahulu untuk konversi"
                                    return@Button
                                }
                                if (parsedConversionQty == null || parsedConversionQty <= 0) {
                                    validationError = "Jumlah isi dalam satuan beli harus lebih besar dari 0"
                                    return@Button
                                }
                                if (conversionToUnit.isBlank()) {
                                    validationError = "Satuan isi wajib dipilih jika konversi digunakan"
                                    return@Button
                                }
                                if (purchaseUnit.trim().equals(conversionToUnit.trim(), ignoreCase = true)) {
                                    validationError = "Satuan isi tidak boleh sama dengan satuan beli"
                                    return@Button
                                }
                            }

                            // Validasi Daftar Harga Jual
                            for (sp in sellingPrices) {
                                val p = sp.priceText.toDoubleOrNull()
                                if (sp.priceText.isNotBlank() && p == null) {
                                    validationError = "Format harga jual '${sp.priceText}' tidak valid"
                                    return@Button
                                }
                                if (p != null && p < 0) {
                                    validationError = "Harga jual tidak boleh negatif"
                                    return@Button
                                }
                                if (p != null && p > 0 && sp.unit.isBlank()) {
                                    validationError = "Satuan harga jual wajib dipilih"
                                    return@Button
                                }
                            }

                            // Validasi tidak boleh ada duplikasi satuan harga jual
                            val filledSellingPrices = sellingPrices.filter { it.unit.isNotBlank() }
                            val duplicateUnits = filledSellingPrices
                                .groupBy { it.unit.trim().lowercase() }
                                .filter { it.value.size > 1 }
                            if (duplicateUnits.isNotEmpty()) {
                                val dupName = duplicateUnits.values.first().first().unit
                                validationError = "Terdapat harga jual ganda untuk satuan '$dupName'. Setiap satuan hanya boleh memiliki satu harga jual."
                                return@Button
                            }

                            val sellingList = sellingPrices.mapNotNull {
                                val price = it.priceText.toDoubleOrNull()
                                if (price != null && price > 0 && it.unit.isNotBlank()) {
                                    Pair(price, it.unit)
                                } else null
                            }

                            val parsedStock = stockText.toDoubleOrNull() ?: 0.0
                            val parsedMinStock = minStockText.toDoubleOrNull() ?: 0.0
                            val finalStockUnit = stockUnit.ifBlank { purchaseUnit.ifBlank { "pcs" } }

                            viewModel.saveProduct(
                                id = productId ?: 0L,
                                name = name,
                                sku = sku,
                                category = category,
                                brand = brand,
                                variant = variant,
                                notes = notes,
                                imageUri = imageUri,
                                stock = parsedStock,
                                stockUnit = finalStockUnit,
                                minimumStock = parsedMinStock,
                                isFavorite = isFavorite,
                                purchasePrice = parsedPurchasePrice,
                                purchaseUnit = purchaseUnit,
                                purchaseNotes = purchaseNotes,
                                conversionFromUnit = if (hasConversion) purchaseUnit else "",
                                conversionQuantity = if (hasConversion) parsedConversionQty else null,
                                conversionToUnit = if (hasConversion) conversionToUnit else "",
                                sellingPrices = sellingList,
                                onComplete = { savedId ->
                                    onSaved(savedId)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_product_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan Barang",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 0: Foto Barang (Opsional)
            item {
                AppCard {
                    ProductImagePicker(
                        imagePath = imageUri,
                        onImageSelected = { imageUri = it }
                    )
                }
            }

            // Section 1: Informasi Dasar Barang
            item {
                AppCard {
                    SectionHeader(
                        title = "Informasi Dasar Barang",
                        subtitle = "Nama barang, kode/SKU, kategori, merek, dan spesifikasi"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Nama Barang *") },
                        placeholder = { Text("Contoh: Karpet Plastik, Pipa PVC, Semen Gresik") },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("Nama barang wajib diisi", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("Kode / SKU") },
                            placeholder = { Text("TB-001, BRG-09") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Kategori") },
                            placeholder = { Text("Pipa, Cat, Semen") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Merek") },
                            placeholder = { Text("Rucika, Dulux, Tiga Roda") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = variant,
                            onValueChange = { variant = it },
                            label = { Text("Ukuran / Varian") },
                            placeholder = { Text("1/2 inch, 50kg, Tebal 0.5mm") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan") },
                        placeholder = { Text("Lokasi rak gudang, nomor seri, kontak supplier...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }
            }

            // Section 2: Stok Barang (Opsional)
            item {
                AppCard {
                    SectionHeader(
                        title = "Informasi Stok Barang",
                        subtitle = "Jumlah ketersediaan stok fisik awal dan batas minimum peringatan"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Stok Awal") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = stockUnit.ifBlank { purchaseUnit },
                            onValueChange = { stockUnit = it },
                            label = { Text("Satuan Stok") },
                            placeholder = { Text("pcs, sak, roll") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Batas Stok Minimum (Peringatan)") },
                        placeholder = { Text("Contoh: 5 (aplikasi akan menandai jika stok <= 5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Section 3: Harga Beli
            item {
                AppCard {
                    SectionHeader(
                        title = "Harga Beli",
                        subtitle = "Harga modal pembelian dan satuan saat dibeli dari supplier"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { input ->
                            purchasePriceText = input.replace(',', '.').filter { char -> char.isDigit() || char == '.' }
                        },
                        label = { Text("Harga Beli (Rp)") },
                        placeholder = { Text("Contoh: 10000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_purchase_price_input"),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            if (parsedPurchasePrice != null) {
                                Text(
                                    text = Formatters.formatRupiah(parsedPurchasePrice),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    UnitDropdownSelector(
                        label = "Satuan Beli",
                        selectedUnit = purchaseUnit,
                        placeholder = "Pilih Satuan Beli (contoh: Pack, Dus, Bal, Karung)",
                        onUnitSelected = {
                            purchaseUnit = it
                            if (stockUnit.isBlank()) {
                                stockUnit = it
                            }
                        }
                    )

                    if (parsedPurchasePrice != null && purchaseUnit.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Artinya: ${Formatters.formatRupiah(parsedPurchasePrice)} / $purchaseUnit",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = purchaseNotes,
                        onValueChange = { purchaseNotes = it },
                        label = { Text("Nama Supplier / Catatan Pembelian") },
                        placeholder = { Text("Toko Distributor ABC, Jl. Raya") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Section 4: Isi / Konversi Satuan
            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Isi / Konversi Satuan (Opsional)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Aktifkan jika 1 satuan beli berisi beberapa satuan isi (misal 1 Pack = 10 Pcs, 1 Karung = 50 Kg, 1 Bal = 20 Meter)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!hasConversion) {
                        OutlinedButton(
                            onClick = {
                                hasConversion = true
                                if (conversionToUnit.isBlank()) {
                                    conversionToUnit = "pcs"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Atur Isi / Konversi Satuan")
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
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
                                        text = "Hubungan Satuan Beli & Isi:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    IconButton(
                                        onClick = {
                                            hasConversion = false
                                            conversionQtyText = ""
                                            conversionToUnit = ""
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Hapus konversi", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val effectivePurchaseUnit = purchaseUnit.ifBlank { "Satuan Beli" }

                                // Visual formula row: 1 [ Pack ] = [ 10 ] [ Pcs ▼ ]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1 [ Satuan Beli ]
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.weight(1.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "1 $effectivePurchaseUnit",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Text("=", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                                    // Isi (angka)
                                    OutlinedTextField(
                                        value = conversionQtyText,
                                        onValueChange = { input ->
                                            conversionQtyText = input.filter { char -> char.isDigit() || char == '.' }
                                        },
                                        label = { Text("Isi") },
                                        placeholder = { Text("10") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(0.9f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Satuan Isi Selector
                                UnitDropdownSelector(
                                    label = "Satuan Isi",
                                    selectedUnit = conversionToUnit,
                                    placeholder = "Pilih Satuan Isi (contoh: Pcs, Meter, Kg)",
                                    onUnitSelected = { conversionToUnit = it }
                                )

                                // Hasil Konversi & Perhitungan Modal Otomatis
                                if (conversionQtyText.isNotBlank() && conversionToUnit.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Hasil: 1 $effectivePurchaseUnit = $conversionQtyText $conversionToUnit",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            if (parsedPurchasePrice != null && parsedConversionQty != null && parsedConversionQty > 0) {
                                                val modalPerIsi = parsedPurchasePrice / parsedConversionQty
                                                Spacer(modifier = Modifier.height(8.dp))
                                                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Harga modal otomatis:",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "• ${Formatters.formatRupiah(parsedPurchasePrice)} / $effectivePurchaseUnit (Satuan Beli)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "• ${Formatters.formatRupiah(modalPerIsi)} / $conversionToUnit (Satuan Isi: ${Formatters.formatRupiah(parsedPurchasePrice)} ÷ ${Formatters.formatNumber(parsedConversionQty)})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
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

            // Section 5: Harga Jual (Dapat Memiliki Banyak Harga Jual)
            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daftar Harga Jual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tentukan harga jual per satuan. Keuntungan dan margin dihitung otomatis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showCalcDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = "Kalkulator Harga", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (sellingPrices.isEmpty()) {
                        Button(
                            onClick = {
                                val defaultUnit = if (purchaseUnit.isNotBlank()) {
                                    purchaseUnit
                                } else if (hasConversion && conversionToUnit.isNotBlank()) {
                                    conversionToUnit
                                } else {
                                    "pcs"
                                }
                                sellingPrices.add(SellingPriceInputState(initialPrice = "", initialUnit = defaultUnit))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_first_selling_price_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Tambah Harga Jual")
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            sellingPrices.forEachIndexed { index, itemState ->
                                val parsedPrice = itemState.priceText.toDoubleOrNull()
                                val liveModal = calculateLiveModal(itemState.unit)
                                val liveProfit = if (parsedPrice != null && liveModal != null) parsedPrice - liveModal else null
                                val liveMarginPct = if (liveProfit != null && parsedPrice != null && parsedPrice > 0) {
                                    (liveProfit / parsedPrice) * 100.0
                                } else null

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                                                text = "Harga Jual #${index + 1}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = { sellingPrices.removeAt(index) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Hapus harga jual",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Unit Selector with Dropdown & Custom support
                                        UnitDropdownSelector(
                                            label = "Satuan Penjualan",
                                            selectedUnit = itemState.unit,
                                            placeholder = "Pilih Satuan Jual...",
                                            onUnitSelected = { itemState.unit = it }
                                        )

                                        // Shortcut chips for purchase unit & conversion unit
                                        if (purchaseUnit.isNotBlank() || (hasConversion && conversionToUnit.isNotBlank())) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (purchaseUnit.isNotBlank()) {
                                                    FilterChip(
                                                        selected = itemState.unit.equals(purchaseUnit, ignoreCase = true),
                                                        onClick = { itemState.unit = purchaseUnit },
                                                        label = { Text("$purchaseUnit (Beli)", fontSize = 11.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                                        )
                                                    )
                                                }
                                                if (hasConversion && conversionToUnit.isNotBlank()) {
                                                    FilterChip(
                                                        selected = itemState.unit.equals(conversionToUnit, ignoreCase = true),
                                                        onClick = { itemState.unit = conversionToUnit },
                                                        label = { Text("$conversionToUnit (Isi)", fontSize = 11.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = itemState.priceText,
                                            onValueChange = { input ->
                                                itemState.priceText = input.replace(',', '.').filter { char -> char.isDigit() || char == '.' }
                                            },
                                            label = { Text("Harga Jual (Rp)") },
                                            placeholder = { Text("Contoh: 15000") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("selling_price_input_${index}"),
                                            shape = RoundedCornerShape(10.dp),
                                            supportingText = {
                                                if (parsedPrice != null) {
                                                    Text(
                                                        text = Formatters.formatRupiah(parsedPrice) + if (itemState.unit.isNotBlank()) " / ${itemState.unit}" else "",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        )

                                        // Analysis calculation card
                                        if (liveModal != null || parsedPrice != null) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Card(
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (liveProfit != null && liveProfit >= 0) {
                                                        ProfitGreen.copy(alpha = 0.08f)
                                                    } else if (liveProfit != null && liveProfit < 0) {
                                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                                    } else {
                                                        MaterialTheme.colorScheme.surface
                                                    }
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (liveProfit != null && liveProfit >= 0) ProfitGreen.copy(alpha = 0.3f)
                                                    else MaterialTheme.colorScheme.outlineVariant
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    val unitLabel = if (itemState.unit.isNotBlank()) " / ${itemState.unit}" else ""
                                                    if (liveModal != null) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text("Modal:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text("${Formatters.formatRupiah(liveModal)}$unitLabel", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    if (parsedPrice != null) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text("Harga Jual:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text("${Formatters.formatRupiah(parsedPrice)}$unitLabel", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    if (liveProfit != null) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                if (liveProfit >= 0) "Keuntungan:" else "Kerugian:",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = if (liveProfit >= 0) ProfitGreen else MaterialTheme.colorScheme.error,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Text(
                                                                "${Formatters.formatRupiah(kotlin.math.abs(liveProfit))}$unitLabel",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = if (liveProfit >= 0) ProfitGreen else MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    }
                                                    if (liveMarginPct != null) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                "Margin Keuntungan:",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (liveProfit != null && liveProfit >= 0) ProfitGreen else MaterialTheme.colorScheme.error
                                                            )
                                                            Text(
                                                                Formatters.formatPercentage(liveMarginPct),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = if (liveProfit != null && liveProfit >= 0) ProfitGreen else MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    } else if (liveModal == null && itemState.unit.isNotBlank()) {
                                                        Text(
                                                            text = "Catatan: Modal untuk satuan '${itemState.unit}' belum dapat dihitung (sesuaikan satuan beli atau atur konversi satuan)",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            // Potensi Jika Seluruh Isi Terjual (khusus untuk satuan isi/eceran)
                                            if (hasConversion && parsedConversionQty != null && parsedConversionQty > 0 &&
                                                parsedPurchasePrice != null && parsedPrice != null && parsedPrice > 0 &&
                                                itemState.unit.trim().equals(conversionToUnit.trim(), ignoreCase = true)
                                            ) {
                                                val potRevenue = parsedConversionQty * parsedPrice
                                                val potProfit = potRevenue - parsedPurchasePrice
                                                val effectivePurchaseUnit = purchaseUnit.ifBlank { "Satuan Beli" }

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                Icons.Default.Calculate,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.tertiary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "Potensi jika seluruh isi terjual pada harga ini:",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                                            )
                                                        }
                                                        Text(
                                                            text = "• Potensi Omzet: ${Formatters.formatNumber(parsedConversionQty)} ${itemState.unit} × ${Formatters.formatRupiah(parsedPrice)} = ${Formatters.formatRupiah(potRevenue)}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = "• Modal Pembelian: ${Formatters.formatRupiah(parsedPurchasePrice)} / $effectivePurchaseUnit",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = "• Potensi Keuntungan: ${Formatters.formatRupiah(potProfit)}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (potProfit >= 0) ProfitGreen else MaterialTheme.colorScheme.error
                                                        )
                                                        Text(
                                                            text = "*Estimasi potensi keuntungan kotor jika seluruh 1 $effectivePurchaseUnit (${Formatters.formatNumber(parsedConversionQty)} ${itemState.unit}) terjual habis secara eceran.",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val existingUnits = sellingPrices.map { it.unit.lowercase() }
                                        val nextUnit = if (purchaseUnit.isNotBlank() && !existingUnits.contains(purchaseUnit.lowercase())) {
                                            purchaseUnit
                                        } else if (hasConversion && conversionToUnit.isNotBlank() && !existingUnits.contains(conversionToUnit.lowercase())) {
                                            conversionToUnit
                                        } else {
                                            ""
                                        }
                                        sellingPrices.add(SellingPriceInputState(initialPrice = "", initialUnit = nextUnit))
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("add_another_selling_price_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Harga Jual Lain", fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = { showCalcDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kalkulator", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
