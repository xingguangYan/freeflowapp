package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DraftEntity::class, PublishTaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FreeFlowDatabase : RoomDatabase() {
    abstract fun freeFlowDao(): FreeFlowDao

    companion object {
        @Volatile
        private var INSTANCE: FreeFlowDatabase? = null

        fun getInstance(context: Context): FreeFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FreeFlowDatabase::class.java,
                    "freeflow_studio.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
