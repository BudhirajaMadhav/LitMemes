package com.androidmadhav.litmemes

import androidx.lifecycle.LiveData

class MemeRepository(val memeDao: MemeDao)  {

    val allSavedMeme: LiveData<List<SavedMeme>> = memeDao.getAllNotes()

    suspend fun insert(savedMeme: SavedMeme){
        memeDao.insert(savedMeme)
    }

    suspend fun delete(url: String){
        memeDao.delete(url)
    }

}