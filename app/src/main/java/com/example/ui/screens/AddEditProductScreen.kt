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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.components.ProfitBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnitSelector
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
    var category by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var variant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

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

    // Load existing product if editing
    LaunchedEffect(existingProduct) {
        if (existingProduct != null) {
            val p = existingProduct.product
            name = p.name
            category = p.category
            brand = p.brand
            variant = p.variant
            notes = p.notes
            isFavorite = p.isFavorite

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

        if (target.equals(pUnit, ignoreCase = true)) {
            return parsedPurchasePrice
        }

        if (hasConversion && parsedConversionQty != null && parsedConversionQty > 0) {
            val from = conversionFromUnit.ifBlank { purchaseUnit }.trim()
            val to = conversionToUnit.trim()

            if (pUnit.equals(from, ignoreCase = true) && target.equals(to, ignoreCase = true)) {
                return parsedPurchasePrice / parsedConversionQty
            }
            if (pUnit.equals(to, ignoreCase = true) && target.equals(from, ignoreCase = true)) {
                return parsedPurchasePrice * parsedConversionQty
            }
        }
        return null
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
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }

                            val sellingList = sellingPrices.mapNotNull {
                                val price = it.priceText.toDoubleOrNull()
                                if (price != null && price > 0 && it.unit.isNotBlank()) {
                                    Pair(price, it.unit)
                                } else null
                            }

                            viewModel.saveProduct(
                                id = productId ?: 0L,
                                name = name,
                                category = category,
                                brand = brand,
                                variant = variant,
                                notes = notes,
                                isFavorite = isFavorite,
                                purchasePrice = parsedPurchasePrice,
                                purchaseUnit = purchaseUnit,
                                purchaseNotes = purchaseNotes,
                                conversionFromUnit = if (hasConversion) conversionFromUnit.ifBlank { purchaseUnit } else "",
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
            // Section 1: Informasi Dasar Barang
            item {
                AppCard {
                    SectionHeader(
                        title = "Informasi Dasar Barang",
                        subtitle = "Nama barang, kategori, merek, dan spesifikasi"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Nama Barang *") },
                        placeholder = { Text("Contoh: Karpet Plastik, Pipa PVC, Kabel NYM") },
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
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Kategori") },
                            placeholder = { Text("Pipa, Cat, Listrik") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Merek") },
                            placeholder = { Text("Rucika, Dulux") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = variant,
                        onValueChange = { variant = it },
                        label = { Text("Ukuran / Varian") },
                        placeholder = { Text("Contoh: 1/2 inch, Tebal 0.5mm, 100m") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan") },
                        placeholder = { Text("Lokasi rak, nomor seri, dll.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }
            }

            // Section 2: Harga Beli Supplier
            item {
                AppCard {
                    SectionHeader(
                        title = "Harga Beli Supplier",
                        subtitle = "Harga modal pembelian dan satuan saat dibeli dari supplier"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    UnitSelector(
                        label = "Satuan Pembelian (Saat Beli dari Supplier)",
                        selectedUnit = purchaseUnit,
                        onUnitSelected = {
                            purchaseUnit = it
                            if (conversionFromUnit.isBlank()) {
                                conversionFromUnit = it
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { input ->
                            purchasePriceText = input.replace(',', '.').filter { char -> char.isDigit() || char == '.' }
                        },
                        label = { Text("Harga Beli (Rp)") },
                        placeholder = { Text("Contoh: 500000") },
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

                    Spacer(modifier = Modifier.height(8.dp))

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

            // Section 3: Konversi Satuan
            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Konversi Satuan (Opsional)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Aktifkan jika 1 kemasan beli berisi beberapa satuan jual (misal 1 Bal = XX Meter, 1 Karung = XX Kg, 1 Dus = XX Pcs)",
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
                                if (conversionFromUnit.isBlank()) {
                                    conversionFromUnit = purchaseUnit.ifBlank { "Bal" }
                                }
                                if (conversionToUnit.isBlank()) {
                                    conversionToUnit = "Meter"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Atur Konversi Satuan")
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                                        text = "Hubungan Satuan:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            hasConversion = false
                                            conversionQtyText = ""
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Hapus konversi", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("1", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = conversionFromUnit.ifBlank { purchaseUnit },
                                        onValueChange = { conversionFromUnit = it },
                                        label = { Text("Satuan Utama") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Text("=", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = conversionQtyText,
                                        onValueChange = { conversionQtyText = it.filter { char -> char.isDigit() || char == '.' } },
                                        label = { Text("Isi (Angka)") },
                                        placeholder = { Text("100") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = conversionToUnit,
                                        onValueChange = { conversionToUnit = it },
                                        label = { Text("Satuan Isi") },
                                        placeholder = { Text("Meter") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                // Live Modal Calculation Info
                                if (parsedPurchasePrice != null && parsedConversionQty != null && parsedConversionQty > 0) {
                                    val modalPerIsi = parsedPurchasePrice / parsedConversionQty
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
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
                                                text = "Harga modal otomatis: ${Formatters.formatRupiah(modalPerIsi)} / ${conversionToUnit.ifBlank { "satuan isi" }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Harga Jual (Dapat Memiliki Banyak Harga Jual)
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
                                text = "Tentukan harga jual per satuan (misal harga / meter dan harga / bal). Keuntungan dihitung otomatis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (sellingPrices.isEmpty()) {
                        Button(
                            onClick = {
                                // Provide default unit suggestion if purchase unit or conversion unit exists
                                val defaultUnit = if (hasConversion && conversionToUnit.isNotBlank()) {
                                    conversionToUnit
                                } else {
                                    purchaseUnit.ifBlank { "pcs" }
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
                                val liveProfitPct = if (liveProfit != null && liveModal != null && liveModal > 0) {
                                    (liveProfit / liveModal) * 100.0
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
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
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

                                        // Unit Selector for this selling price
                                        UnitSelector(
                                            label = "Satuan Penjualan",
                                            selectedUnit = itemState.unit,
                                            onUnitSelected = { itemState.unit = it }
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = itemState.priceText,
                                            onValueChange = { input ->
                                                itemState.priceText = input.replace(',', '.').filter { char -> char.isDigit() || char == '.' }
                                            },
                                            label = { Text("Harga Jual (Rp)") },
                                            placeholder = { Text("Contoh: 6000") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("selling_price_input_${index}"),
                                            shape = RoundedCornerShape(10.dp),
                                            supportingText = {
                                                if (parsedPrice != null) {
                                                    Text(
                                                        text = Formatters.formatRupiah(parsedPrice),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        )

                                        // Real-time calculation feedback
                                        if (liveModal != null || liveProfit != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (liveModal != null) {
                                                    Text(
                                                        text = "Modal: ${Formatters.formatRupiah(liveModal)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                ProfitBadge(
                                                    profit = liveProfit,
                                                    profitPercentage = liveProfitPct
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    // Suggest remaining unit if Bal & Meter pattern
                                    val existingUnits = sellingPrices.map { it.unit.lowercase() }
                                    val nextUnit = if (hasConversion && !existingUnits.contains(conversionFromUnit.lowercase())) {
                                        conversionFromUnit
                                    } else if (!existingUnits.contains(purchaseUnit.lowercase())) {
                                        purchaseUnit
                                    } else {
                                        ""
                                    }
                                    sellingPrices.add(SellingPriceInputState(initialPrice = "", initialUnit = nextUnit))
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_another_selling_price_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Tambah Harga Jual Lain")
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
