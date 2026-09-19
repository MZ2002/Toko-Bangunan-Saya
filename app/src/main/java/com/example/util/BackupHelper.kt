package com.example.util

import com.example.data.model.Expense
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.ProductWithDetails
import com.example.data.model.PurchasePrice
import com.example.data.model.SellingPrice
import com.example.data.model.UnitConversion
import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val products: List<ProductWithDetails>,
    val expenses: List<Expense>
)

object BackupHelper {

    fun exportToJson(products: List<ProductWithDetails>, expenses: List<Expense>): String {
        val root = JSONObject()
        root.put("app", "TB Jaya Abadi")
        root.put("exported_at", System.currentTimeMillis())

        val productsArray = JSONArray()
        products.forEach { pwd ->
            val pObj = JSONObject()
            val prod = pwd.product
            pObj.put("id", prod.id)
            pObj.put("name", prod.name)
            pObj.put("category", prod.category)
            pObj.put("brand", prod.brand)
            pObj.put("variant", prod.variant)
            pObj.put("notes", prod.notes)
            pObj.put("is_favorite", prod.isFavorite)
            pObj.put("created_at", prod.createdAt)
            pObj.put("updated_at", prod.updatedAt)

            // Purchase prices
            val ppArray = JSONArray()
            pwd.purchasePrices.forEach { pp ->
                val ppObj = JSONObject()
                ppObj.put("price", pp.price)
                ppObj.put("unit", pp.unit)
                ppObj.put("date", pp.date)
                ppObj.put("notes", pp.notes)
                ppArray.put(ppObj)
            }
            pObj.put("purchase_prices", ppArray)

            // Conversions
            val convArray = JSONArray()
            pwd.conversions.forEach { conv ->
                val cObj = JSONObject()
                cObj.put("from_unit", conv.fromUnit)
                cObj.put("quantity", conv.quantity)
                cObj.put("to_unit", conv.toUnit)
                convArray.put(cObj)
            }
            pObj.put("conversions", convArray)

            // Selling prices
            val spArray = JSONArray()
            pwd.sellingPrices.forEach { sp ->
                val spObj = JSONObject()
                spObj.put("price", sp.price)
                spObj.put("unit", sp.unit)
                spArray.put(spObj)
            }
            pObj.put("selling_prices", spArray)

            // Price history
            val phArray = JSONArray()
            pwd.priceHistory.forEach { ph ->
                val phObj = JSONObject()
                phObj.put("type", ph.type)
                phObj.put("unit", ph.unit)
                phObj.put("old_price", ph.oldPrice)
                phObj.put("new_price", ph.newPrice)
                phObj.put("changed_at", ph.changedAt)
                phArray.put(phObj)
            }
            pObj.put("price_history", phArray)

            productsArray.put(pObj)
        }
        root.put("products", productsArray)

        val expensesArray = JSONArray()
        expenses.forEach { exp ->
            val expObj = JSONObject()
            expObj.put("date", exp.date)
            expObj.put("category", exp.category)
            expObj.put("description", exp.description)
            expObj.put("amount", exp.amount)
            expObj.put("notes", exp.notes)
            expensesArray.put(expObj)
        }
        root.put("expenses", expensesArray)

        return root.toString(2)
    }

    fun exportToCsv(products: List<ProductWithDetails>): String {
        val sb = StringBuilder()
        sb.append("Nama Barang,Kategori,Merek,Ukuran/Varian,Harga Beli Supplier,Satuan Beli,Konversi,Daftar Harga Jual,Catatan\n")
        products.forEach { pwd ->
            val p = pwd.product
            val purchase = pwd.latestPurchasePrice
            val conv = pwd.conversion
            val convStr = if (conv != null) "1 ${conv.fromUnit} = ${Formatters.formatNumber(conv.quantity)} ${conv.toUnit}" else "-"
            val sellingStr = pwd.getCalculatedSellingPrices().joinToString(" | ") { calc ->
                "${Formatters.formatRupiah(calc.sellingPrice.price)} / ${calc.sellingPrice.unit}" +
                    (if (calc.profit != null) " (Untung ${Formatters.formatRupiah(calc.profit)})" else "")
            }

            fun escapeCsv(s: String) = "\"${s.replace("\"", "\"\"")}\""

            sb.append(escapeCsv(p.name)).append(",")
            sb.append(escapeCsv(p.category)).append(",")
            sb.append(escapeCsv(p.brand)).append(",")
            sb.append(escapeCsv(p.variant)).append(",")
            sb.append(purchase?.price ?: "").append(",")
            sb.append(escapeCsv(purchase?.unit ?: "")).append(",")
            sb.append(escapeCsv(convStr)).append(",")
            sb.append(escapeCsv(sellingStr)).append(",")
            sb.append(escapeCsv(p.notes)).append("\n")
        }
        return sb.toString()
    }

    fun parseFromJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)
        val productList = mutableListOf<ProductWithDetails>()

        if (root.has("products")) {
            val pArray = root.getJSONArray("products")
            for (i in 0 until pArray.length()) {
                val pObj = pArray.getJSONObject(i)
                val prod = Product(
                    id = 0L, // will re-insert with new autogenerated ID
                    name = pObj.optString("name", ""),
                    category = pObj.optString("category", ""),
                    brand = pObj.optString("brand", ""),
                    variant = pObj.optString("variant", ""),
                    notes = pObj.optString("notes", ""),
                    isFavorite = pObj.optBoolean("is_favorite", false),
                    createdAt = pObj.optLong("created_at", System.currentTimeMillis()),
                    updatedAt = pObj.optLong("updated_at", System.currentTimeMillis())
                )

                val ppList = mutableListOf<PurchasePrice>()
                if (pObj.has("purchase_prices")) {
                    val ppArray = pObj.getJSONArray("purchase_prices")
                    for (j in 0 until ppArray.length()) {
                        val ppObj = ppArray.getJSONObject(j)
                        ppList.add(
                            PurchasePrice(
                                id = 0L,
                                productId = 0L,
                                price = ppObj.optDouble("price", 0.0),
                                unit = ppObj.optString("unit", ""),
                                date = ppObj.optLong("date", System.currentTimeMillis()),
                                notes = ppObj.optString("notes", "")
                            )
                        )
                    }
                }

                val convList = mutableListOf<UnitConversion>()
                if (pObj.has("conversions")) {
                    val cArray = pObj.getJSONArray("conversions")
                    for (j in 0 until cArray.length()) {
                        val cObj = cArray.getJSONObject(j)
                        convList.add(
                            UnitConversion(
                                id = 0L,
                                productId = 0L,
                                fromUnit = cObj.optString("from_unit", ""),
                                quantity = cObj.optDouble("quantity", 1.0),
                                toUnit = cObj.optString("to_unit", "")
                            )
                        )
                    }
                }

                val spList = mutableListOf<SellingPrice>()
                if (pObj.has("selling_prices")) {
                    val spArray = pObj.getJSONArray("selling_prices")
                    for (j in 0 until spArray.length()) {
                        val spObj = spArray.getJSONObject(j)
                        spList.add(
                            SellingPrice(
                                id = 0L,
                                productId = 0L,
                                price = spObj.optDouble("price", 0.0),
                                unit = spObj.optString("unit", "")
                            )
                        )
                    }
                }

                val phList = mutableListOf<PriceHistory>()
                if (pObj.has("price_history")) {
                    val phArray = pObj.getJSONArray("price_history")
                    for (j in 0 until phArray.length()) {
                        val phObj = phArray.getJSONObject(j)
                        phList.add(
                            PriceHistory(
                                id = 0L,
                                productId = 0L,
                                type = phObj.optString("type", "PURCHASE"),
                                unit = phObj.optString("unit", ""),
                                oldPrice = phObj.optDouble("old_price", 0.0),
                                newPrice = phObj.optDouble("new_price", 0.0),
                                changedAt = phObj.optLong("changed_at", System.currentTimeMillis())
                            )
                        )
                    }
                }

                productList.add(
                    ProductWithDetails(
                        product = prod,
                        purchasePrices = ppList,
                        conversions = convList,
                        sellingPrices = spList,
                        priceHistory = phList
                    )
                )
            }
        }

        val expenseList = mutableListOf<Expense>()
        if (root.has("expenses")) {
            val expArray = root.getJSONArray("expenses")
            for (i in 0 until expArray.length()) {
                val expObj = expArray.getJSONObject(i)
                expenseList.add(
                    Expense(
                        id = 0L,
                        date = expObj.optLong("date", System.currentTimeMillis()),
                        category = expObj.optString("category", ""),
                        description = expObj.optString("description", ""),
                        amount = expObj.optDouble("amount", 0.0),
                        notes = expObj.optString("notes", ""),
                        createdAt = expObj.optLong("created_at", System.currentTimeMillis())
                    )
                )
            }
        }

        return BackupData(productList, expenseList)
    }
}
