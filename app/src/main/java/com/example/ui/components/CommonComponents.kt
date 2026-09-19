package com.example.ui.components

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.ProfitGreen
import com.example.util.Formatters
import com.example.util.ImageStorageHelper
import java.io.File

val COMMON_UNITS = listOf(
    "pcs", "buah", "unit", "meter", "cm", "kg", "gram",
    "pack", "dus", "box", "bal", "roll", "karung", "batang", "lembar", "liter"
)

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun UnitDropdownSelector(
    label: String,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Pilih satuan...",
    allowCustom: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var isCustomMode by remember(selectedUnit) {
        mutableStateOf(selectedUnit.isNotBlank() && !COMMON_UNITS.contains(selectedUnit.lowercase()))
    }
    var customText by remember(selectedUnit, isCustomMode) {
        mutableStateOf(if (isCustomMode) selectedUnit else "")
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedUnit.isNotBlank()) selectedUnit else placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selectedUnit.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedUnit.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Pilih satuan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                COMMON_UNITS.forEach { unit ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = unit,
                                fontWeight = if (selectedUnit.equals(unit, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedUnit.equals(unit, ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingIcon = if (selectedUnit.equals(unit, ignoreCase = true)) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        } else null,
                        onClick = {
                            isCustomMode = false
                            onUnitSelected(unit)
                            expanded = false
                        }
                    )
                }

                if (allowCustom) {
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "✏️ Custom (Ketik Satuan Lain...)",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        onClick = {
                            isCustomMode = true
                            expanded = false
                        }
                    )
                }
            }
        }

        // Quick chip shortcuts below dropdown for fast 1-tap selection
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            COMMON_UNITS.forEach { unit ->
                val isSelected = selectedUnit.equals(unit, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        isCustomMode = false
                        onUnitSelected(unit)
                    },
                    label = { Text(unit, fontSize = 12.sp) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        if (isCustomMode) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customText,
                onValueChange = {
                    customText = it
                    onUnitSelected(it)
                },
                placeholder = { Text("Ketik nama satuan custom...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}

@Composable
fun UnitSelector(
    label: String,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    customAllowed: Boolean = true
) {
    UnitDropdownSelector(
        label = label,
        selectedUnit = selectedUnit,
        onUnitSelected = onUnitSelected,
        modifier = modifier,
        allowCustom = customAllowed
    )
}

@Composable
fun ProfitBadge(
    profit: Double?,
    profitPercentage: Double?,
    modifier: Modifier = Modifier
) {
    if (profit == null) return
    val isProfitable = profit >= 0
    val color = if (isProfitable) ProfitGreen else MaterialTheme.colorScheme.error

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isProfitable) "Untung: " else "Rugi: ",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = Formatters.formatRupiah(kotlin.math.abs(profit)),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (profitPercentage != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(Margin: ${Formatters.formatPercentage(profitPercentage)})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun StockBadge(
    stock: Double,
    stockUnit: String,
    minimumStock: Double,
    modifier: Modifier = Modifier
) {
    val isLowStock = minimumStock > 0 && stock <= minimumStock
    val color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLowStock) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Stok Rendah",
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Stok Rendah: ${Formatters.formatNumber(stock)} ${stockUnit.ifBlank { "unit" }}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            } else {
                Text(
                    text = "Stok: ${Formatters.formatNumber(stock)} ${stockUnit.ifBlank { "unit" }}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String = "Konfirmasi Hapus",
    message: String = "Apakah Anda yakin ingin menghapus data ini?",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Ya, Hapus",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Kalkulator Harga Jual Sederhana:
 * Mode 0: Modal + Target Laba (%) -> Rekomendasi Harga Jual & Keuntungan (Rp)
 * Mode 1: Modal + Harga Jual -> Keuntungan (Rp) & Margin (%)
 */
@Composable
fun SellingPriceCalculatorDialog(
    initialModal: Double? = null,
    initialUnit: String = "",
    onUsePrice: ((price: Double, unit: String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var modalText by remember {
        mutableStateOf(if (initialModal != null && initialModal > 0) initialModal.toLong().toString() else "")
    }
    var unitText by remember { mutableStateOf(initialUnit) }
    var targetPercentText by remember { mutableStateOf("15") }
    var targetPriceText by remember { mutableStateOf("") }

    val modal = modalText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kalkulator Harga Jual", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Target Laba (%)", fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Hitung Margin", fontSize = 13.sp) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = modalText,
                    onValueChange = { modalText = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Harga Modal (Rp)") },
                    placeholder = { Text("Contoh: 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = targetPercentText,
                        onValueChange = { targetPercentText = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Target Keuntungan (%)") },
                        placeholder = { Text("15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    val percent = targetPercentText.toDoubleOrNull() ?: 0.0
                    val calcProfit = modal * (percent / 100.0)
                    val calculatedPrice = modal + calcProfit

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Hasil Perhitungan:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Harga Jual Rekomendasi:")
                                Text(Formatters.formatRupiah(calculatedPrice), fontWeight = FontWeight.Bold, color = ProfitGreen)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Keuntungan Nominal:")
                                Text(Formatters.formatRupiah(calcProfit), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (onUsePrice != null && calculatedPrice > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onUsePrice(calculatedPrice, unitText)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Gunakan Sebagai Harga Jual")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = targetPriceText,
                        onValueChange = { targetPriceText = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Rencana Harga Jual (Rp)") },
                        placeholder = { Text("Contoh: 60000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    val salePrice = targetPriceText.toDoubleOrNull() ?: 0.0
                    val profit = salePrice - modal
                    val marginPct = if (modal > 0) (profit / modal) * 100.0 else 0.0

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Hasil Analisis:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Keuntungan Nominal:")
                                Text(
                                    Formatters.formatRupiah(profit),
                                    fontWeight = FontWeight.Bold,
                                    color = if (profit >= 0) ProfitGreen else MaterialTheme.colorScheme.error
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Persentase Margin:")
                                Text(
                                    Formatters.formatPercentage(marginPct),
                                    fontWeight = FontWeight.Bold,
                                    color = if (marginPct >= 0) ProfitGreen else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    if (onUsePrice != null && salePrice > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onUsePrice(salePrice, unitText)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Gunakan Sebagai Harga Jual")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Dialog Penyesuaian Stok (Tambah, Kurangi, Penyesuaian Manual)
 */
@Composable
fun StockAdjustDialog(
    productName: String,
    currentStock: Double,
    unit: String,
    onConfirm: (type: String, quantity: Double, resultingStock: Double, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf("TAMBAH") } // "TAMBAH", "KURANG", "PENYESUAIAN"
    var quantityText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    val parsedQty = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0

    val resultingStock = when (mode) {
        "TAMBAH" -> currentStock + parsedQty
        "KURANG" -> maxOf(0.0, currentStock - parsedQty)
        else -> maxOf(0.0, parsedQty)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Kelola Stok Barang", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stok saat ini: ${Formatters.formatNumber(currentStock)} ${unit.ifBlank { "unit" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Mode selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = mode == "TAMBAH",
                        onClick = { mode = "TAMBAH" },
                        label = { Text("+ Tambah", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = mode == "KURANG",
                        onClick = { mode = "KURANG" },
                        label = { Text("- Kurangi", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = mode == "PENYESUAIAN",
                        onClick = { mode = "PENYESUAIAN" },
                        label = { Text("= Set Manual", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                    label = {
                        Text(
                            when (mode) {
                                "TAMBAH" -> "Jumlah Ditambah (${unit.ifBlank { "unit" }})"
                                "KURANG" -> "Jumlah Dikurang (${unit.ifBlank { "unit" }})"
                                else -> "Stok Baru Sebenarnya (${unit.ifBlank { "unit" }})"
                            }
                        )
                    },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Misal: Kiriman supplier, Barang retur, Rusak") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stok Akhir:")
                        Text(
                            text = "${Formatters.formatNumber(resultingStock)} ${unit.ifBlank { "unit" }}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedQty >= 0) {
                        onConfirm(mode, parsedQty, resultingStock, noteText.trim())
                    }
                },
                enabled = parsedQty > 0 || mode == "PENYESUAIAN"
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Component Pemilih Foto Produk (Galeri via PhotoPicker & Kamera opsional)
 */
@Composable
fun ProductImagePicker(
    imagePath: String?,
    onImageSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageFromUri(context, uri)
            if (savedPath != null) {
                onImageSelected(savedPath)
            } else {
                Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = ImageStorageHelper.saveBitmap(context, bitmap)
            if (savedPath != null) {
                onImageSelected(savedPath)
            } else {
                Toast.makeText(context, "Gagal menyimpan foto kamera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Foto Barang (Opsional)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (!imagePath.isNullOrBlank() && File(imagePath).exists()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = "Foto Barang",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )

                // Delete photo button
                IconButton(
                    onClick = {
                        ImageStorageHelper.deleteImageFile(imagePath)
                        onImageSelected(null)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus Foto",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Change photo chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ganti: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                        Text(
                            "Galeri",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        Text(" • ", color = Color.White)
                        Text(
                            "Kamera",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        )
                    }
                }
            }
        } else {
            // Placeholder and pick options
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Belum ada foto barang",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dari Galeri", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kamera", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
