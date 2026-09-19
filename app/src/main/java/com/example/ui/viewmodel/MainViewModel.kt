package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Expense
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.ProductWithDetails
import com.example.data.model.PurchasePrice
import com.example.data.model.SellingPrice
import com.example.data.model.StockHistory
import com.example.data.model.UnitConversion
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.ProductRepository
import com.example.util.BackupData
import com.example.util.BackupHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ExpenseDateFilter(val label: String) {
    ALL("Semua"),
    TODAY("Hari Ini"),
    THIS_WEEK("Minggu Ini"),
    THIS_MONTH("Bulan Ini"),
    THIS_YEAR("Tahun Ini")
}

data class RecentActivity(
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val badgeType: String // "PRICE", "STOCK", "PRODUCT", "EXPENSE"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("tb_jaya_abadi_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getDatabase(application)
    private val productRepository = ProductRepository(database.productDao())
    private val expenseRepository = ExpenseRepository(database.expenseDao())

    val isDarkMode = MutableStateFlow(true) // Dark Mode as primary theme

    val allProducts: StateFlow<List<ProductWithDetails>> = productRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProducts: StateFlow<List<ProductWithDetails>> = productRepository.favoriteProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<String>> = productRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBrands: StateFlow<List<String>> = productRepository.allBrands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productCount: StateFlow<Int> = productRepository.productCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)

    // Filtered Products for Search (Name, SKU, Brand, Category, Variant, Units)
    val filteredProducts: StateFlow<List<ProductWithDetails>> = combine(
        allProducts,
        searchQuery,
        selectedCategory
    ) { products, query, cat ->
        var list = products

        if (!cat.isNullOrBlank()) {
            list = list.filter { it.product.category.equals(cat, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { pwd ->
                val p = pwd.product
                p.name.lowercase().contains(q) ||
                    p.sku.lowercase().contains(q) ||
                    p.brand.lowercase().contains(q) ||
                    p.category.lowercase().contains(q) ||
                    p.variant.lowercase().contains(q) ||
                    pwd.latestPurchasePrice?.unit?.lowercase()?.contains(q) == true ||
                    pwd.sellingPrices.any { it.unit.lowercase().contains(q) } ||
                    pwd.conversion?.let {
                        it.fromUnit.lowercase().contains(q) || it.toUnit.lowercase().contains(q)
                    } == true
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Low stock products
    val lowStockProducts: StateFlow<List<ProductWithDetails>> = allProducts.combine(MutableStateFlow(Unit)) { products, _ ->
        products.filter { it.isLowStock }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses
    val allExpenses: StateFlow<List<Expense>> = expenseRepository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenseCategories: StateFlow<List<String>> = expenseRepository.allExpenseCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseFilter = MutableStateFlow(ExpenseDateFilter.THIS_MONTH)

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses,
        expenseFilter
    ) { expenses, filter ->
        val calendar = Calendar.getInstance()

        when (filter) {
            ExpenseDateFilter.ALL -> expenses
            ExpenseDateFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                expenses.filter { it.date >= startOfDay }
            }
            ExpenseDateFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfWeek = calendar.timeInMillis
                expenses.filter { it.date >= startOfWeek }
            }
            ExpenseDateFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                expenses.filter { it.date >= startOfMonth }
            }
            ExpenseDateFilter.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfYear = calendar.timeInMillis
                expenses.filter { it.date >= startOfYear }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpensesTotal: StateFlow<Double> = filteredExpenses.combine(MutableStateFlow(Unit)) { list, _ ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Total pengeluaran bulan ini untuk Dashboard
    val thisMonthExpensesTotal: StateFlow<Double> = allExpenses.combine(MutableStateFlow(Unit)) { expenses, _ ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        expenses.filter { it.date >= startOfMonth }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Jumlah harga yang baru diperbarui dalam 30 hari terakhir
    val recentPriceUpdatesCount: StateFlow<Int> = allProducts.combine(MutableStateFlow(Unit)) { products, _ ->
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        products.count { pwd ->
            pwd.priceHistory.any { it.changedAt >= thirtyDaysAgo }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Aktivitas terbaru untuk Dashboard (harga diperbarui, barang baru, stok diubah, pengeluaran)
    val recentActivities: StateFlow<List<RecentActivity>> = combine(
        allProducts,
        allExpenses
    ) { products, expenses ->
        val activities = mutableListOf<RecentActivity>()

        // 1. Aktivitas perubahan harga
        products.forEach { pwd ->
            pwd.priceHistory.take(3).forEach { ph ->
                activities.add(
                    RecentActivity(
                        title = "Harga ${ph.type.lowercase()} ${pwd.product.name} diperbarui",
                        subtitle = "${ph.unit}: Rp${ph.oldPrice.toLong()} → Rp${ph.newPrice.toLong()}",
                        timestamp = ph.changedAt,
                        badgeType = "PRICE"
                    )
                )
            }
        }

        // 2. Aktivitas stok
        products.forEach { pwd ->
            pwd.stockHistory.take(2).forEach { sh ->
                val typeDesc = when (sh.type) {
                    "TAMBAH" -> "Penambahan stok"
                    "KURANG" -> "Pengurangan stok"
                    else -> "Penyesuaian stok"
                }
                activities.add(
                    RecentActivity(
                        title = "$typeDesc: ${pwd.product.name}",
                        subtitle = "${sh.quantity} ${sh.unit} (${sh.note.ifBlank { "Manual" }})",
                        timestamp = sh.createdAt,
                        badgeType = "STOCK"
                    )
                )
            }
        }

        // 3. Aktivitas barang baru
        products.sortedByDescending { it.product.createdAt }.take(5).forEach { pwd ->
            activities.add(
                RecentActivity(
                    title = "Barang ditambahkan: ${pwd.product.name}",
                    subtitle = listOfNotNull(pwd.product.category.takeIf { it.isNotBlank() }, pwd.product.brand.takeIf { it.isNotBlank() }).joinToString(" • "),
                    timestamp = pwd.product.createdAt,
                    badgeType = "PRODUCT"
                )
            )
        }

        // 4. Aktivitas pengeluaran
        expenses.sortedByDescending { it.date }.take(5).forEach { exp ->
            activities.add(
                RecentActivity(
                    title = "Pengeluaran: ${exp.description.ifBlank { exp.category }}",
                    subtitle = "Rp${exp.amount.toLong()}",
                    timestamp = exp.date,
                    badgeType = "EXPENSE"
                )
            )
        }

        activities.sortedByDescending { it.timestamp }.take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastBackupTimestamp = MutableStateFlow(prefs.getLong("last_backup_time", 0L))

    fun setLastBackupTime(time: Long) {
        prefs.edit().putLong("last_backup_time", time).apply()
        lastBackupTimestamp.value = time
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun toggleFavorite(productId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            productRepository.toggleFavorite(productId, currentStatus)
        }
    }

    fun deleteProduct(productId: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            productRepository.deleteProductById(productId)
            onDeleted()
        }
    }

    fun adjustStock(
        productId: Long,
        type: String,
        quantity: Double,
        newStock: Double,
        unit: String,
        note: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val safeStock = maxOf(0.0, newStock)
            productRepository.adjustStock(
                productId = productId,
                type = type,
                quantity = quantity,
                newStock = safeStock,
                unit = unit,
                note = note
            )
            onComplete()
        }
    }

    fun saveProduct(
        id: Long = 0L,
        name: String,
        sku: String = "",
        category: String,
        brand: String,
        variant: String,
        notes: String,
        imageUri: String? = null,
        stock: Double = 0.0,
        stockUnit: String = "",
        minimumStock: Double = 0.0,
        isFavorite: Boolean,
        purchasePrice: Double?,
        purchaseUnit: String,
        purchaseNotes: String,
        conversionFromUnit: String,
        conversionQuantity: Double?,
        conversionToUnit: String,
        sellingPrices: List<Pair<Double, String>>, // (Price, Unit)
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val existingProduct = if (id > 0) productRepository.getProductByIdOnce(id) else null

            val safeStock = maxOf(0.0, stock)
            val safeMinStock = maxOf(0.0, minimumStock)

            val product = Product(
                id = id,
                name = name.trim(),
                sku = sku.trim(),
                category = category.trim(),
                brand = brand.trim(),
                variant = variant.trim(),
                notes = notes.trim(),
                imageUri = imageUri,
                stock = safeStock,
                stockUnit = stockUnit.trim(),
                minimumStock = safeMinStock,
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )

            val purchase = if (purchasePrice != null && purchasePrice > 0 && purchaseUnit.isNotBlank()) {
                PurchasePrice(
                    productId = id,
                    price = purchasePrice,
                    unit = purchaseUnit.trim(),
                    notes = purchaseNotes.trim(),
                    date = System.currentTimeMillis()
                )
            } else null

            val conversion = if (conversionQuantity != null &&
                !conversionQuantity.isNaN() &&
                conversionQuantity > 0.0 &&
                conversionFromUnit.isNotBlank() &&
                conversionToUnit.isNotBlank()
            ) {
                UnitConversion(
                    productId = id,
                    fromUnit = conversionFromUnit.trim(),
                    quantity = conversionQuantity,
                    toUnit = conversionToUnit.trim()
                )
            } else null

            val sellingList = sellingPrices.filter { it.first > 0 && it.second.isNotBlank() }.map {
                SellingPrice(
                    productId = id,
                    price = it.first,
                    unit = it.second.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            }

            // Catat riwayat perubahan harga jika harga modal atau harga jual berubah
            val history = mutableListOf<PriceHistory>()
            val now = System.currentTimeMillis()

            if (existingProduct != null) {
                // 1. Cek perubahan harga beli
                val oldPurchase = existingProduct.latestPurchasePrice
                if (purchase != null) {
                    if (oldPurchase != null && (oldPurchase.price != purchase.price || !oldPurchase.unit.equals(purchase.unit, ignoreCase = true))) {
                        history.add(
                            PriceHistory(
                                productId = id,
                                type = "Harga Modal",
                                unit = purchase.unit,
                                oldPrice = oldPurchase.price,
                                newPrice = purchase.price,
                                changedAt = now
                            )
                        )
                    }
                }

                // 2. Cek perubahan harga jual
                sellingList.forEach { newSp ->
                    val oldSp = existingProduct.sellingPrices.find { it.unit.equals(newSp.unit, ignoreCase = true) }
                    if (oldSp != null && oldSp.price != newSp.price) {
                        history.add(
                            PriceHistory(
                                productId = id,
                                type = "Harga Jual",
                                unit = newSp.unit,
                                oldPrice = oldSp.price,
                                newPrice = newSp.price,
                                changedAt = now
                            )
                        )
                    }
                }
            }

            // Riwayat stok awal jika produk baru dengan stok > 0
            val initialStockHistory = mutableListOf<StockHistory>()
            if (id == 0L && safeStock > 0.0) {
                initialStockHistory.add(
                    StockHistory(
                        productId = 0L,
                        type = "PENYESUAIAN",
                        quantity = safeStock,
                        unit = stockUnit.trim(),
                        note = "Stok awal barang",
                        createdAt = now
                    )
                )
            }

            val savedId = productRepository.saveProduct(
                product = product,
                purchasePrice = purchase,
                conversion = conversion,
                sellingPrices = sellingList,
                recordedHistory = history,
                stockHistory = initialStockHistory
            )
            onComplete(savedId)
        }
    }

    fun addExpense(
        category: String,
        description: String,
        amount: Double,
        notes: String,
        date: Long = System.currentTimeMillis(),
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            expenseRepository.insertExpense(
                Expense(
                    date = date,
                    category = category.trim(),
                    description = description.trim(),
                    amount = amount,
                    notes = notes.trim()
                )
            )
            onComplete()
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            expenseRepository.deleteExpenseById(id)
        }
    }

    fun getExportJson(): String {
        return BackupHelper.exportToJson(allProducts.value, allExpenses.value)
    }

    fun getExportCsv(): String {
        return BackupHelper.exportToCsv(allProducts.value)
    }

    fun importData(backupData: BackupData, onComplete: (Int, Int) -> Unit) {
        viewModelScope.launch {
            var prodCount = 0
            var expCount = 0

            backupData.products.forEach { pwd ->
                productRepository.saveProduct(
                    product = pwd.product.copy(id = 0L),
                    purchasePrice = pwd.latestPurchasePrice?.copy(id = 0L, productId = 0L),
                    conversion = pwd.conversion?.copy(id = 0L, productId = 0L),
                    sellingPrices = pwd.sellingPrices.map { it.copy(id = 0L, productId = 0L) },
                    recordedHistory = pwd.priceHistory.map { it.copy(id = 0L, productId = 0L) },
                    stockHistory = pwd.stockHistory.map { it.copy(id = 0L, productId = 0L) }
                )
                prodCount++
            }

            backupData.expenses.forEach { exp ->
                expenseRepository.insertExpense(exp.copy(id = 0L))
                expCount++
            }

            setLastBackupTime(System.currentTimeMillis())
            onComplete(prodCount, expCount)
        }
    }
}
