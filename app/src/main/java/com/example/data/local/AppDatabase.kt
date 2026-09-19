package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Expense
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.PurchasePrice
import com.example.data.model.SellingPrice
import com.example.data.model.StockHistory
import com.example.data.model.UnitConversion

@Database(
    entities = [
        Product::class,
        PurchasePrice::class,
        UnitConversion::class,
        SellingPrice::class,
        PriceHistory::class,
        StockHistory::class,
        Expense::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN sku TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN image_uri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE products ADD COLUMN stock REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE products ADD COLUMN stock_unit TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN minimum_stock REAL NOT NULL DEFAULT 0.0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS stock_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        product_id INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        quantity REAL NOT NULL,
                        unit TEXT NOT NULL DEFAULT '',
                        note TEXT NOT NULL DEFAULT '',
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_history_product_id ON stock_history(product_id)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tb_jaya_abadi_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
