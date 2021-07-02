package com.androidmadhav.litmemes

import android.app.Application

class MemeApplication: Application() {

    val database by lazy { SavedMemeDatabase.getDatabase(this) }
    val repository by lazy { MemeRepository(database.getMemeDao()) }

}