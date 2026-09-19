package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ProductWithDetails(
    @Embedded val product: Product,

    @Relation(
        parentColumn = "id",
        entityColumn = "product_id"
    )
    val purchasePrices: List<PurchasePrice> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "product_id"
    )
    val conversions: List<UnitConversion> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "product_id"
    )
    val sellingPrices: List<SellingPrice> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "product_id"
    )
    val priceHistory: List<PriceHistory> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "product_id"
    )
    val stockHistory: List<StockHistory> = emptyList()
) {
    val latestPurchasePrice: PurchasePrice?
        get() = purchasePrices.maxByOrNull { it.date }

    val conversion: UnitConversion?
        get() = conversions.firstOrNull()

    val isLowStock: Boolean
        get() = product.minimumStock > 0 && product.stock <= product.minimumStock

    val sortedPriceHistory: List<PriceHistory>
        get() = priceHistory.sortedByDescending { it.changedAt }

    val sortedStockHistory: List<StockHistory>
        get() = stockHistory.sortedByDescending { it.createdAt }

    /**
     * Hitung harga modal untuk satuan tertentu berdasarkan harga beli supplier dan konversi.
     * Konversi selalu dimulai dari Satuan Beli ke Satuan Isi (1 Satuan Beli = X Satuan Isi).
     */
    fun calculateModalPriceForUnit(targetUnit: String): Double? {
        val purchase = latestPurchasePrice ?: return null
        val trimmedTarget = targetUnit.trim()
        val trimmedPurchaseUnit = purchase.unit.trim()
        if (trimmedTarget.isBlank() || trimmedPurchaseUnit.isBlank()) return null

        // 1. Jika satuan jual sama dengan satuan beli
        if (trimmedTarget.equals(trimmedPurchaseUnit, ignoreCase = true)) {
            return purchase.price
        }

        // 2. Jika ada konversi langsung
        for (conv in conversions) {
            if (conv.quantity <= 0) continue
            val trimmedFrom = conv.fromUnit.trim()
            val trimmedTo = conv.toUnit.trim()

            // Contoh: 1 Pack = 10 Pcs. Satuan Beli = Pack, Target = Pcs -> modal = 10.000 / 10 = 1.000
            if (trimmedPurchaseUnit.equals(trimmedFrom, ignoreCase = true) &&
                trimmedTarget.equals(trimmedTo, ignoreCase = true)
            ) {
                return purchase.price / conv.quantity
            }

            // Sebaliknya: Satuan Beli = Pcs, Target = Pack -> modal = 1.000 * 10 = 10.000
            if (trimmedPurchaseUnit.equals(trimmedTo, ignoreCase = true) &&
                trimmedTarget.equals(trimmedFrom, ignoreCase = true)
            ) {
                return purchase.price * conv.quantity
            }
        }

        // 3. Multi-level conversion chaining (e.g. 1 Dus = 10 Pack, 1 Pack = 10 Pcs)
        val ratio = findConversionRatio(trimmedPurchaseUnit, trimmedTarget)
        if (ratio != null && ratio > 0) {
            return purchase.price / ratio
        }

        return null
    }

    private fun findConversionRatio(fromUnit: String, toUnit: String): Double? {
        if (fromUnit.equals(toUnit, ignoreCase = true)) return 1.0
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<Pair<String, Double>>()
        queue.add(Pair(fromUnit.lowercase(), 1.0))
        visited.add(fromUnit.lowercase())

        while (queue.isNotEmpty()) {
            val (currUnit, currRatio) = queue.removeFirst()
            if (currUnit.equals(toUnit.lowercase())) return currRatio

            for (conv in conversions) {
                if (conv.quantity <= 0) continue
                val f = conv.fromUnit.trim().lowercase()
                val t = conv.toUnit.trim().lowercase()

                if (currUnit == f && !visited.contains(t)) {
                    visited.add(t)
                    queue.add(Pair(t, currRatio * conv.quantity))
                }
                if (currUnit == t && !visited.contains(f)) {
                    visited.add(f)
                    queue.add(Pair(f, currRatio / conv.quantity))
                }
            }
        }
        return null
    }

    /**
     * Menghitung rincian harga jual: harga modal, keuntungan, persentase margin keuntungan,
     * serta potensi omzet dan keuntungan jika seluruh isi dijual dengan harga satuan isi.
     * Rumus margin: (Keuntungan / Harga Jual) * 100%
     */
    fun getCalculatedSellingPrices(): List<CalculatedSellingPrice> {
        val conv = conversion
        val purchase = latestPurchasePrice

        return sellingPrices.map { sp ->
            val modal = calculateModalPriceForUnit(sp.unit)
            val profit = if (modal != null) sp.price - modal else null
            val marginPct = if (profit != null && sp.price > 0) {
                (profit / sp.price) * 100.0
            } else null

            var potRev: Double? = null
            var potProf: Double? = null
            var potQty: Double? = null
            var potUnit: String? = null

            if (conv != null && conv.quantity > 0 && purchase != null) {
                val toU = conv.toUnit.trim()
                if (sp.unit.trim().equals(toU, ignoreCase = true)) {
                    potQty = conv.quantity
                    potUnit = toU
                    potRev = conv.quantity * sp.price
                    val totalModal = purchase.price
                    potProf = potRev - totalModal
                }
            }

            CalculatedSellingPrice(
                sellingPrice = sp,
                modalPrice = modal,
                profit = profit,
                profitPercentage = marginPct,
                potentialRevenue = potRev,
                potentialProfit = potProf,
                potentialQuantity = potQty,
                potentialUnit = potUnit
            )
        }
    }
}

data class CalculatedSellingPrice(
    val sellingPrice: SellingPrice,
    val modalPrice: Double?,
    val profit: Double?,
    val profitPercentage: Double?,
    val potentialRevenue: Double? = null,
    val potentialProfit: Double? = null,
    val potentialQuantity: Double? = null,
    val potentialUnit: String? = null
)
