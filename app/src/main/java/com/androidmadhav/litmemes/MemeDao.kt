package com.androidmadhav.litmemes

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MemeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(savedMeme: SavedMeme)

    @Query("Delete from saved_memes where url = :url")
    suspend fun delete(url: String)

    @Query("Select * from saved_memes order by id ASC")
    fun getAllNotes(): LiveData<List<SavedMeme>>
}