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
    val priceHistory: List<PriceHistory> = emptyList()
) {
    val latestPurchasePrice: PurchasePrice?
        get() = purchasePrices.maxByOrNull { it.date }

    val conversion: UnitConversion?
        get() = conversions.firstOrNull()

    /**
     * Hitung harga modal untuk satuan tertentu berdasarkan harga beli supplier dan konversi.
     */
    fun calculateModalPriceForUnit(targetUnit: String): Double? {
        val purchase = latestPurchasePrice ?: return null
        val trimmedTarget = targetUnit.trim()
        val trimmedPurchaseUnit = purchase.unit.trim()

        // 1. Jika satuan jual sama dengan satuan beli
        if (trimmedTarget.equals(trimmedPurchaseUnit, ignoreCase = true)) {
            return purchase.price
        }

        // 2. Jika ada konversi
        val conv = conversion ?: return null
        val trimmedFrom = conv.fromUnit.trim()
        val trimmedTo = conv.toUnit.trim()

        // Jika satuan beli adalah fromUnit (e.g. Bal), dan target unit adalah toUnit (e.g. Meter):
        // 1 Bal = 100 Meter. Harga Bal = 500.000. Modal per Meter = 500.000 / 100 = 5.000
        if (trimmedPurchaseUnit.equals(trimmedFrom, ignoreCase = true) &&
            trimmedTarget.equals(trimmedTo, ignoreCase = true) &&
            conv.quantity > 0
        ) {
            return purchase.price / conv.quantity
        }

        // Jika sebaliknya: satuan beli adalah toUnit (e.g. Meter), target adalah fromUnit (e.g. Bal)
        if (trimmedPurchaseUnit.equals(trimmedTo, ignoreCase = true) &&
            trimmedTarget.equals(trimmedFrom, ignoreCase = true) &&
            conv.quantity > 0
        ) {
            return purchase.price * conv.quantity
        }

        return null
    }

    /**
     * Menghitung rincian harga jual: harga modal, keuntungan, dan persentase keuntungan.
     */
    fun getCalculatedSellingPrices(): List<CalculatedSellingPrice> {
        return sellingPrices.map { sp ->
            val modal = calculateModalPriceForUnit(sp.unit)
            val profit = if (modal != null) sp.price - modal else null
            val profitPct = if (modal != null && modal > 0) (profit!! / modal) * 100.0 else null

            CalculatedSellingPrice(
                sellingPrice = sp,
                modalPrice = modal,
                profit = profit,
                profitPercentage = profitPct
            )
        }
    }
}

data class CalculatedSellingPrice(
    val sellingPrice: SellingPrice,
    val modalPrice: Double?,
    val profit: Double?,
    val profitPercentage: Double?
)
