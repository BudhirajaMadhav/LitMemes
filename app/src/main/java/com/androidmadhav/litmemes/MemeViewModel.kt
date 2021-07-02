package com.androidmadhav.litmemes

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemeViewModel(private val repository: MemeRepository): ViewModel() {

    val allSavedMeme: LiveData<List<SavedMeme>> = repository.allSavedMeme

    fun insert(savedMeme: SavedMeme) = viewModelScope.launch(Dispatchers.IO) {

        repository.insert(savedMeme)
    }

    fun delete(url: String) = viewModelScope.launch(Dispatchers.IO) {

        repository.delete(url)
    }
}

class MemeViewModelFactory(private val repository: MemeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
