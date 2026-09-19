package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_history",
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
data class PriceHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    val type: String, // "PURCHASE" or "SELLING"
    val unit: String,
    @ColumnInfo(name = "old_price")
    val oldPrice: Double,
    @ColumnInfo(name = "new_price")
    val newPrice: Double,
    @ColumnInfo(name = "changed_at")
    val changedAt: Long = System.currentTimeMillis()
)
