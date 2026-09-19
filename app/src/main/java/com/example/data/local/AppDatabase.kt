package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Expense
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.PurchasePrice
import com.example.data.model.SellingPrice
import com.example.data.model.UnitConversion

@Database(
    entities = [
        Product::class,
        PurchasePrice::class,
        UnitConversion::class,
        SellingPrice::class,
        PriceHistory::class,
        Expense::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tb_jaya_abadi_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
