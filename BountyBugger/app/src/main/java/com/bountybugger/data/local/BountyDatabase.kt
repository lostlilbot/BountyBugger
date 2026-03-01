package com.bountybugger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database for storing bounty programs
 */
@Database(
    entities = [BountyProgramEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BountyConverters::class)
abstract class BountyDatabase : RoomDatabase() {

    abstract fun bountyProgramDao(): BountyProgramDao

    companion object {
        private const val DATABASE_NAME = "bounty_bugger_db"

        @Volatile
        private var INSTANCE: BountyDatabase? = null

        fun getInstance(context: Context): BountyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BountyDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
