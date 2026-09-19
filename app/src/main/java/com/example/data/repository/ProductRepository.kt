package com.example.data.repository

import com.example.data.local.ProductDao
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.ProductWithDetails
import com.example.data.model.PurchasePrice
import com.example.data.model.SellingPrice
import com.example.data.model.UnitConversion
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<ProductWithDetails>> = productDao.getAllProductsWithDetails()
    val favoriteProducts: Flow<List<ProductWithDetails>> = productDao.getFavoriteProducts()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()
    val allBrands: Flow<List<String>> = productDao.getAllBrands()
    val productCount: Flow<Int> = productDao.getProductCount()

    fun getProductById(id: Long): Flow<ProductWithDetails?> {
        return productDao.getProductWithDetailsById(id)
    }

    suspend fun getProductByIdOnce(id: Long): ProductWithDetails? {
        return productDao.getProductWithDetailsByIdOnce(id)
    }

    fun getPriceHistory(productId: Long): Flow<List<PriceHistory>> {
        return productDao.getPriceHistoryForProduct(productId)
    }

    suspend fun toggleFavorite(productId: Long, currentStatus: Boolean) {
        productDao.updateFavoriteStatus(productId, !currentStatus)
    }

    suspend fun deleteProductById(productId: Long) {
        productDao.deleteProductById(productId)
    }

    suspend fun saveProduct(
        product: Product,
        purchasePrice: PurchasePrice?,
        conversion: UnitConversion?,
        sellingPrices: List<SellingPrice>,
        recordedHistory: List<PriceHistory> = emptyList()
    ): Long {
        return productDao.saveFullProduct(
            product = product,
            purchasePrice = purchasePrice,
            conversion = conversion,
            sellingPrices = sellingPrices,
            recordedHistory = recordedHistory
        )
    }
}
