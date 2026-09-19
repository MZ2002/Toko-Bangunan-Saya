package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String = "",
    val category: String = "",
    val brand: String = "",
    val variant: String = "", // Ukuran / varian (misal 2 inch, 100m, 4x6)
    val notes: String = "",
    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,
    val stock: Double = 0.0,
    @ColumnInfo(name = "stock_unit")
    val stockUnit: String = "",
    @ColumnInfo(name = "minimum_stock")
    val minimumStock: Double = 0.0,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
