package com.example.aquasample.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aquasample.data.local.converter.Converters
import com.example.aquasample.data.local.dao.SampleDao
import com.example.aquasample.data.local.entity.SampleEntity

@Database(entities = [SampleEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aquasample_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
