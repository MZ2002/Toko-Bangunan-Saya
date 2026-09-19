package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_history",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["product_id"])]
)
data class StockHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    val type: String, // "TAMBAH", "KURANG", "PENYESUAIAN"
    val quantity: Double,
    val unit: String = "",
    val note: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
