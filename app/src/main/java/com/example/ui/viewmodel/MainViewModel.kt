package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Expense
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.ProductWithDetails
import com.example.data.model.PurchasePrice
import com.example.data.model.SellingPrice
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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val productRepository = ProductRepository(database.productDao())
    private val expenseRepository = ExpenseRepository(database.expenseDao())

    val isDarkMode = MutableStateFlow(true) // User requested Dark Mode as primary

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

    // Filtered Products for Search and Category
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
        val now = System.currentTimeMillis()
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

    fun saveProduct(
        id: Long = 0L,
        name: String,
        category: String,
        brand: String,
        variant: String,
        notes: String,
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

            val product = Product(
                id = id,
                name = name.trim(),
                category = category.trim(),
                brand = brand.trim(),
                variant = variant.trim(),
                notes = notes.trim(),
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

            // Catat riwayat perubahan harga jika harga beli berubah pada barang yang sudah ada
            val history = mutableListOf<PriceHistory>()
            if (existingProduct != null && purchase != null) {
                val oldPurchase = existingProduct.latestPurchasePrice
                if (oldPurchase != null && (oldPurchase.price != purchase.price || oldPurchase.unit != purchase.unit)) {
                    history.add(
                        PriceHistory(
                            productId = id,
                            type = "HARGA BELI",
                            unit = purchase.unit,
                            oldPrice = oldPurchase.price,
                            newPrice = purchase.price,
                            changedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            val savedId = productRepository.saveProduct(
                product = product,
                purchasePrice = purchase,
                conversion = conversion,
                sellingPrices = sellingList,
                recordedHistory = history
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
                    recordedHistory = pwd.priceHistory.map { it.copy(id = 0L, productId = 0L) }
                )
                prodCount++
            }

            backupData.expenses.forEach { exp ->
                expenseRepository.insertExpense(exp.copy(id = 0L))
                expCount++
            }

            onComplete(prodCount, expCount)
        }
    }
}
