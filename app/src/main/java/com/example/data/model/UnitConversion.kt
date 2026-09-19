package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unit_conversions",
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
data class UnitConversion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "from_unit")
    val fromUnit: String, // e.g. "Bal", "Karung", "Pack", "Roll", "Dus"
    val quantity: Double, // e.g. 100.0, 50.0, 24.0
    @ColumnInfo(name = "to_unit")
    val toUnit: String    // e.g. "Meter", "Kg", "Pcs"
)
