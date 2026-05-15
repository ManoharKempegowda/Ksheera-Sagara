package com.example.dairyfarm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dairyfarm.data.dao.DairyDao
import com.example.dairyfarm.data.entities.Cow
import com.example.dairyfarm.data.entities.ExpenseEntry
import com.example.dairyfarm.data.entities.IncomeEntry
import com.example.dairyfarm.data.entities.User

@Database(entities = [Cow::class, IncomeEntry::class, ExpenseEntry::class, User::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dairyDao(): DairyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dairy_farm_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
