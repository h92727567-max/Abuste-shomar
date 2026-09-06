package com.abusteh.shomar.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abusteh.shomar.data.entity.GameEntity
import com.abusteh.shomar.data.entity.RoundEntity

/**
 * پایگاه‌داده Room برنامه (روی SQLite). برنامه کاملاً Offline است؛ این تنها
 * محل ذخیره‌سازی دائمی داده‌هاست.
 */
@Database(
    entities = [GameEntity::class, RoundEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun roundDao(): RoundDao

    companion object {
        private const val DATABASE_NAME = "abusteh_shomar.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
        }
    }
}
