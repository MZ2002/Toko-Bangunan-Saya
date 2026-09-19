package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.ProductWithDetails
import com.example.data.model.PurchasePrice
import com.example.data.model.SellingPrice
import com.example.data.model.UnitConversion
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Transaction
    @Query("SELECT * FROM products ORDER BY updated_at DESC")
    fun getAllProductsWithDetails(): Flow<List<ProductWithDetails>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductWithDetailsById(id: Long): Flow<ProductWithDetails?>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductWithDetailsByIdOnce(id: Long): ProductWithDetails?

    @Transaction
    @Query("SELECT * FROM products WHERE is_favorite = 1 ORDER BY updated_at DESC")
    fun getFavoriteProducts(): Flow<List<ProductWithDetails>>

    @Query("SELECT DISTINCT category FROM products WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT brand FROM products WHERE brand != '' ORDER BY brand ASC")
    fun getAllBrands(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("UPDATE products SET is_favorite = :isFavorite, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchasePrice(purchasePrice: PurchasePrice): Long

    @Query("DELETE FROM purchase_prices WHERE product_id = :productId")
    suspend fun deletePurchasePricesByProductId(productId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitConversion(conversion: UnitConversion): Long

    @Query("DELETE FROM unit_conversions WHERE product_id = :productId")
    suspend fun deleteConversionsByProductId(productId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSellingPrices(sellingPrices: List<SellingPrice>)

    @Query("DELETE FROM selling_prices WHERE product_id = :productId")
    suspend fun deleteSellingPricesByProductId(productId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistory): Long

    @Query("SELECT * FROM price_history WHERE product_id = :productId ORDER BY changed_at DESC")
    fun getPriceHistoryForProduct(productId: Long): Flow<List<PriceHistory>>

    @Transaction
    suspend fun saveFullProduct(
        product: Product,
        purchasePrice: PurchasePrice?,
        conversion: UnitConversion?,
        sellingPrices: List<SellingPrice>,
        recordedHistory: List<PriceHistory> = emptyList()
    ): Long {
        val productId = if (product.id == 0L) {
            insertProduct(product)
        } else {
            updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
            product.id
        }

        // Simpan harga beli supplier
        deletePurchasePricesByProductId(productId)
        if (purchasePrice != null) {
            insertPurchasePrice(purchasePrice.copy(productId = productId))
        }

        // Simpan konversi
        deleteConversionsByProductId(productId)
        if (conversion != null && conversion.quantity > 0) {
            insertUnitConversion(conversion.copy(productId = productId))
        }

        // Simpan daftar harga jual
        deleteSellingPricesByProductId(productId)
        val preparedSellingPrices = sellingPrices.map { it.copy(productId = productId) }
        if (preparedSellingPrices.isNotEmpty()) {
            insertSellingPrices(preparedSellingPrices)
        }

        // Catat riwayat jika ada
        recordedHistory.forEach { history ->
            insertPriceHistory(history.copy(productId = productId))
        }

        return productId
    }
}
