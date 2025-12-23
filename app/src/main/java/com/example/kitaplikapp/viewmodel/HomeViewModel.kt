package com.example.kitaplikapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kitaplikapp.data.model.Book
import com.example.kitaplikapp.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BookRepository(app.applicationContext)

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _libraryBooks = MutableStateFlow<List<Book>>(emptyList())
    val libraryBooks: StateFlow<List<Book>> = _libraryBooks.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Kullanıcı değiştiğinde (login/logout) veya ekran açıldığında çağır.
     * Library ekranında filtrelerin doğru çalışması için de önemli.
     */
    fun refreshLibrary(username: String) {
        val u = username.trim()
        if (u.isBlank()) return

        viewModelScope.launch {
            try {
                _libraryBooks.value = repo.getMyLibrary(u)
            } catch (e: Exception) {
                _libraryBooks.value = emptyList()
            }
        }
    }

    // ✅ GÜNCELLENDİ: Hata durumunda manuel liste yükleniyor
    fun loadDefault(username: String) {
        val u = username.trim()
        if (u.isBlank()) return

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Varsayılan keşfet listesi (API'den çekmeyi dener)
                _books.value = repo.searchFromApiOrFallback(u, "subject:fiction")

                // Eğer API boş dönerse de fallback devreye girsin istersen:
                if (_books.value.isEmpty()) {
                    _books.value = getOfflineFallbackBooks()
                }

            } catch (e: Exception) {
                // 🚨 B PLANINI DEVREYE SOK (Hata alırsak)
                _error.value = "Bağlantı sorunu! Örnek veriler gösteriliyor."
                _books.value = getOfflineFallbackBooks()
            } finally {
                _loading.value = false
            }
        }
    }

    // ✅ GÜNCELLENDİ: Hata durumunda manuel liste yükleniyor
    private fun load(username: String, query: String) {
        val u = username.trim()
        if (u.isBlank()) return

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _books.value = repo.searchFromApiOrFallback(u, query)
            } catch (e: Exception) {
                // 🚨 B PLANINI DEVREYE SOK
                _error.value = "Arama başarısız. Örnek kitaplar gösteriliyor."
                _books.value = getOfflineFallbackBooks()
            } finally {
                _loading.value = false
            }
        }
    }

    fun search(username: String, userQuery: String) {
        val u = username.trim()
        if (u.isBlank()) return

        val q = userQuery.trim()
        if (q.isBlank()) {
            loadDefault(u)
            return
        }
        load(u, q)
    }

    // ✅ GÜNCELLENDİ: Akıllı arama hata verirse manuel liste yükleniyor
    fun searchSmart(username: String, input: String) {
        val u = username.trim()
        if (u.isBlank()) return

        val q = input.trim()
        if (q.isBlank()) {
            loadDefault(u)
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val lower = q.lowercase()

                val results = when {
                    lower.startsWith("yazar:") || lower.startsWith("author:") -> {
                        val term = q.substringAfter(":").trim()
                        repo.searchFromApiOrFallback(u, "inauthor:$term")
                    }

                    lower.startsWith("tur:") || lower.startsWith("tür:")
                            || lower.startsWith("kategori:") || lower.startsWith("category:") -> {
                        val term = q.substringAfter(":").trim()
                        repo.searchFromApiOrFallback(u, "subject:$term")
                    }

                    lower.startsWith("ad:") || lower.startsWith("isim:")
                            || lower.startsWith("title:") -> {
                        val term = q.substringAfter(":").trim()
                        repo.searchFromApiOrFallback(u, "intitle:$term")
                    }

                    else -> {
                        val byTitle = repo.searchFromApiOrFallback(u, "intitle:$q")
                        val byAuthor = repo.searchFromApiOrFallback(u, "inauthor:$q")
                        val byCategory = repo.searchFromApiOrFallback(u, "subject:$q")
                        (byTitle + byAuthor + byCategory).distinctBy { it.id }
                    }
                }

                if (results.isEmpty()) {
                    _error.value = "Sonuç bulunamadı."
                    // İstersen burada da fallback gösterebilirsin ama boş liste daha mantıklı olabilir.
                    _books.value = emptyList()
                } else {
                    _books.value = results
                }

            } catch (e: Exception) {
                // 🚨 B PLANINI DEVREYE SOK
                _error.value = "Akıllı arama yapılamadı. Örnek kitaplar:"
                _books.value = getOfflineFallbackBooks()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * @return true -> eklendi, false -> zaten vardı
     */
    fun addToLibrary(username: String, book: Book): Boolean {
        val u = username.trim()
        if (u.isBlank()) return false

        // Manuel eklenen kitapların ID'si çakışmasın diye basit kontrol
        // (Repository zaten handle ediyordur ama yine de UI güncellensin)
        val added = repo.addToLibrary(u, book)

        // UI'daki "ekli mi" ikonları için anında refresh
        _libraryBooks.value = repo.getMyLibrary(u)
        return added
    }

    fun toggleReadStatus(username: String, bookId: String) {
        val u = username.trim()
        if (u.isBlank()) return

        repo.toggleRead(u, bookId)
        _libraryBooks.value = repo.getMyLibrary(u)
    }

    fun toggleFavorite(username: String, bookId: String) {
        val u = username.trim()
        if (u.isBlank()) return

        repo.toggleFavorite(u, bookId)
        _libraryBooks.value = repo.getMyLibrary(u)
    }

    fun removeFromLibrary(username: String, bookId: String) {
        val u = username.trim()
        if (u.isBlank()) return

        repo.removeFromLibrary(u, bookId)
        _libraryBooks.value = repo.getMyLibrary(u)
    }

    fun updateNote(username: String, bookId: String, note: String) {
        _libraryBooks.value = _libraryBooks.value.map { b ->
            if (b.id == bookId) b.copy(note = note) else b
        }
        viewModelScope.launch {
            repo.updateNote(username, bookId, note)
        }
    }

    // 🌟 YENİ EKLENEN MANUEL KİTAP LİSTESİ (B PLANI)
    // 🌟 GÜNCELLENMİŞ MANUEL LİSTE (DRAWABLE RESİMLERİ İLE)
    private fun getOfflineFallbackBooks(): List<Book> {
        // Uygulamanın paket ismini alıyoruz (Resim yolunu bulmak için lazım)
        val packageName = getApplication<Application>().packageName

        return listOf(
            Book(
                id = "manual_1",
                title = "Suç ve Ceza",
                author = "Fyodor Dostoyevski",
                // 👇 DİKKAT: R.drawable.cover_suc_ve_ceza senin resim dosyanın adı olmalı
                coverUrl = "android.resource://$packageName/${com.example.kitaplikapp.R.drawable.svc}",
                description = "Rus edebiyatının en büyük eserlerinden biri...",
                pageCount = 687,
                category = "Fiction",
                language = "tr"
            ),
            Book(
                id = "manual_2",
                title = "Simyacı",
                author = "Paulo Coelho",
                // 👇 Resim ismini kendine göre düzelt
                coverUrl = "android.resource://$packageName/${com.example.kitaplikapp.R.drawable.smyc}",
                description = "Dünyaca ünlü bir kendini bulma hikayesi.",
                pageCount = 188,
                category = "Philosophy",
                language = "tr"
            ),
            Book(
                id = "manual_3",
                title = "1984",
                author = "George Orwell",
                coverUrl = "android.resource://$packageName/${com.example.kitaplikapp.R.drawable.bdysd}",
                description = "Distopik bir geleceği anlatan kült roman.",
                pageCount = 352,
                category = "Science Fiction",
                language = "tr"
            ),
            Book(
                id = "manual_4",
                title = "Harry Potter",
                author = "J.K. Rowling",
                coverUrl = "android.resource://$packageName/${com.example.kitaplikapp.R.drawable.hp}",
                description = "Büyücülük dünyasına ilk adım.",
                pageCount = 223,
                category = "Fantasy",
                language = "tr"
            )
        )
    }
}