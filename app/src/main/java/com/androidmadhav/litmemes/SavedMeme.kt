package com.androidmadhav.litmemes

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_memes")
class SavedMeme(@Embedded val jsonResponse: MemeJsonResponse,
                val imageCachePath: String) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}