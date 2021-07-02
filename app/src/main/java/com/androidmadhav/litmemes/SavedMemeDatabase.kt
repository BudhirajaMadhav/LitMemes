package com.androidmadhav.litmemes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(SavedMeme::class), version = 1, exportSchema = false)
abstract class SavedMemeDatabase: RoomDatabase() {

    abstract fun getMemeDao(): MemeDao

    companion object{

        @Volatile
        private var INSTANCE: SavedMemeDatabase? = null

        fun getDatabase(context: Context): SavedMemeDatabase {

            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavedMemeDatabase::class.java,
                    "meme_database"
                ).build()

                INSTANCE = instance
                instance
            }

        }

    }

}