package com.example.kitaplikapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kitaplikapp.data.model.Book
import com.example.kitaplikapp.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BookRepository(app.applicationContext)

    private val _myBooks = MutableStateFlow<List<Book>>(emptyList())
    val myBooks: StateFlow<List<Book>> = _myBooks.asStateFlow()

    fun refresh(username: String) {
        if (username.isBlank()) {
            _myBooks.value = emptyList()
            return
        }
        _myBooks.value = repo.getMyLibrary(username)
    }

    fun toggleRead(username: String, bookId: String) {
        if (username.isBlank()) return
        repo.toggleRead(username, bookId)
        refresh(username)
    }

    fun toggleFavorite(username: String, bookId: String) {
        if (username.isBlank()) return
        repo.toggleFavorite(username, bookId)
        refresh(username)
    }

    fun removeFromLibrary(username: String, bookId: String) {
        if (username.isBlank()) return
        repo.removeFromLibrary(username, bookId)
        refresh(username)
    }
}
