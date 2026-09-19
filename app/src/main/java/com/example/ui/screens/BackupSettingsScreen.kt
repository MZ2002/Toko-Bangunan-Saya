package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppCard
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.MainViewModel
import com.example.util.BackupData
import com.example.util.BackupHelper
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    val prefs = remember { context.getSharedPreferences("tb_jaya_abadi_settings", Context.MODE_PRIVATE) }
    var lastBackupTime by remember {
        mutableStateOf(prefs.getLong("last_backup_timestamp", 0L))
    }

    fun updateLastBackupTime() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong("last_backup_timestamp", now).apply()
        lastBackupTime = now
    }

    var showJsonDialog by remember { mutableStateOf(false) }
    var jsonDialogContent by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var csvDialogContent by remember { mutableStateOf("") }
    var pendingRestoreData by remember { mutableStateOf<BackupData?>(null) }

    // Dialog to view & copy JSON backup
    if (showJsonDialog) {
        AlertDialog(
            onDismissRequest = { showJsonDialog = false },
            title = { Text("Backup Data JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Salin seluruh teks JSON di bawah ini dan simpan di catatan atau email Anda:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonDialogContent,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 10,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("TB Jaya Abadi Backup", jsonDialogContent)
                        clipboard.setPrimaryClip(clip)
                        updateLastBackupTime()
                        Toast.makeText(context, "Data JSON berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        showJsonDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJsonDialog = false }) {
                    Text("Tutup")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog to export CSV
    if (showCsvDialog) {
        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            title = { Text("Ekspor CSV (Excel / Spreadsheet)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Format tabel CSV untuk dibuka di Microsoft Excel atau Google Sheets:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = csvDialogContent,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 10,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("TB Jaya Abadi CSV", csvDialogContent)
                        clipboard.setPrimaryClip(clip)
                        updateLastBackupTime()
                        Toast.makeText(context, "Data CSV berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        showCsvDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvDialog = false }) {
                    Text("Tutup")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog to paste & parse JSON
    if (showImportDialog) {
        var importInputText by remember { mutableStateOf("") }
        var importError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore / Import Data", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Tempelkan kode JSON backup yang sudah Anda simpan sebelumnya:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = {
                            importInputText = it
                            importError = null
                        },
                        placeholder = { Text("Tempel teks JSON di sini...") },
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (importError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = importError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val backupData = BackupHelper.parseFromJson(importInputText)
                            if (backupData.products.isEmpty() && backupData.expenses.isEmpty()) {
                                importError = "File JSON tidak berisi data barang atau pengeluaran."
                            } else {
                                pendingRestoreData = backupData
                                showImportDialog = false
                            }
                        } catch (e: Exception) {
                            importError = "Format JSON tidak valid: ${e.localizedMessage}"
                        }
                    }
                ) {
                    Text("Lanjutkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirmation dialog before actual restore
    if (pendingRestoreData != null) {
        val data = pendingRestoreData!!
        AlertDialog(
            onDismissRequest = { pendingRestoreData = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Konfirmasi Restore Data", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Ditemukan data yang siap diimpor:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ${data.products.size} Barang",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• ${data.expenses.size} Catatan Pengeluaran",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Data ini akan ditambahkan ke dalam database toko Anda. Apakah Anda yakin ingin memproses restore sekarang?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toRestore = pendingRestoreData
                        pendingRestoreData = null
                        if (toRestore != null) {
                            viewModel.importData(toRestore) { pCount, eCount ->
                                Toast.makeText(
                                    context,
                                    "Berhasil memulihkan $pCount barang dan $eCount pengeluaran!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("Ya, Restore Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreData = null }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan & Backup", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Store Info Card
            item {
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "TB Jaya Abadi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Buku Harga Digital & Manajemen Toko",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Appearance Section
            item {
                AppCard {
                    SectionHeader(
                        title = "Tampilan",
                        subtitle = "Atur tema aplikasi sesuai kenyamanan Anda"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mode Gelap (Dark Mode)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isDarkMode) "Aktif (Disarankan)" else "Nonaktif (Light Mode)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Backup & Restore Section
            item {
                AppCard {
                    SectionHeader(
                        title = "Cadangan Data (Backup & Restore)",
                        subtitle = "Amankan data daftar harga toko Anda secara berkala"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Last Backup Status
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Status Backup Terakhir",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (lastBackupTime > 0) {
                                        Formatters.formatDate(lastBackupTime)
                                    } else {
                                        "Belum pernah melakukan backup"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Export JSON
                    Button(
                        onClick = {
                            jsonDialogContent = viewModel.getExportJson()
                            showJsonDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Backup JSON (${allProducts.size} Barang)")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export CSV
                    OutlinedButton(
                        onClick = {
                            csvDialogContent = viewModel.getExportCsv()
                            showCsvDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV (Excel / Spreadsheet)")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Restore JSON
                    OutlinedButton(
                        onClick = {
                            showImportDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore / Import Data dari JSON")
                    }
                }
            }

            // App Concept & Policy Note
            item {
                AppCard {
                    SectionHeader(
                        title = "Tentang Aplikasi",
                        subtitle = "Daftar Harga & Pengeluaran Toko Pribadi"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplikasi ini dirancang khusus untuk pemilik TB Jaya Abadi sebagai buku catatan digital pribadi yang cepat, fleksibel dalam berbagai satuan (meter, bal, kg, karung, pcs, pack, dll.), dan 100% offline tanpa kebocoran data.\n\nDatabase barang kosong ketika pertama kali dijalankan sehingga seluruh data diisi murni oleh pemilik toko.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
