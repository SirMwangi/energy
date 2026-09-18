package com.example.energy.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.energy.data.local.dao.AccountConnectionDao
import com.example.energy.data.local.dao.AccountDao
import com.example.energy.data.local.dao.TransactionDao
import com.example.energy.data.local.dao.UserDao
import com.example.energy.data.local.entity.AccountConnectionEntity
import com.example.energy.data.local.entity.AccountEntity
import com.example.energy.data.local.entity.TransactionEntity
import com.example.energy.data.local.entity.UserEntity

/**
 * Local Room cache database.
 * Serves purely as an offline-first cache layer; Supabase remains the single source of truth.
 */
@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        AccountConnectionEntity::class,
        TransactionEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun accountConnectionDao(): AccountConnectionDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "energy_cache_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
