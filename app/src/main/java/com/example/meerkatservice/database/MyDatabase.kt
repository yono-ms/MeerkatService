package com.example.meerkatservice.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MyDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var Instance: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, MyDatabase::class.java, "my_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
