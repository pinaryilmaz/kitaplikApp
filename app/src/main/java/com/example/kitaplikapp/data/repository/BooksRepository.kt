package com.example.kitaplikapp.data.repository

import android.content.Context
import com.example.kitaplikapp.data.local.JsonStorage
import com.example.kitaplikapp.data.model.Book
import com.example.kitaplikapp.data.remote.RemoteBooksDataSource
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

class BookRepository(
    private val context: Context
) {
    private val remote = RemoteBooksDataSource()
    private val storage = JsonStorage(context)

    private fun libraryFileName(username: String): String {
        val safe = username.trim().lowercase()
        return "library_${safe}.json"
    }

    private fun libraryFile(username: String): File =
        File(context.filesDir, libraryFileName(username))

    fun getMyLibrary(username: String): List<Book> {
        val fileName = libraryFileName(username)
        return storage.readList(fileName, object : TypeToken<List<Book>>() {})
    }

    fun saveMyLibrary(username: String, list: List<Book>) {
        val fileName = libraryFileName(username)
        storage.writeList(fileName, list)
    }

    /**
     * Daha fazla sonuç için 2 sayfa çekiyoruz (Google Books maxResults üst sınır: 40)
     */
    suspend fun searchFromApiOrFallback(username: String, query: String): List<Book> {
        return try {
            val r1 = remote.search(query = query, startIndex = 0, maxResults = 40)
            val r2 = remote.search(query = query, startIndex = 40, maxResults = 40)

            val items = (r1.items.orEmpty() + r2.items.orEmpty())
                .filterNotNull()
                // id null olabilirse, UUID ile map'te hallediyoruz; burada da id bazlı tekilleştiriyoruz
                .distinctBy { it.id ?: "" }

            val mapped = items.map { item ->
                val info = item.volumeInfo

                val lang = info.language.orEmpty()
                val pages = info.pageCount ?: 0

                Book(
                    id = item.id ?: UUID.randomUUID().toString(),
                    title = info.title.orEmpty().ifBlank { "Başlık yok" },
                    author = info.authors?.firstOrNull().orEmpty().ifBlank { "Yazar yok" },
                    category = info.categories?.firstOrNull().orEmpty().ifBlank { "Kategori yok" },
                    coverUrl = info.imageLinks?.thumbnail
                        ?.replace("http://", "https://")
                        .orEmpty(),
                    language = lang,
                    pageCount = pages,   // ✅ Int
                    note = "",
                    createdByUser = false
                )
            }

            if (mapped.isEmpty()) getMyLibrary(username) else mapped
        } catch (e: Exception) {
            getMyLibrary(username)
        }
    }

    fun addToLibrary(username: String, book: Book): Boolean {
        val current = getMyLibrary(username).toMutableList()

        val already = current.any { it.id == book.id }
        if (already) return false

        current.add(book.copy(createdByUser = true))
        saveMyLibrary(username, current)
        return true
    }

    fun toggleRead(username: String, bookId: String) {
        val current = getMyLibrary(username).map {
            if (it.id == bookId) it.copy(isRead = !it.isRead) else it
        }
        saveMyLibrary(username, current)
    }

    fun toggleFavorite(username: String, bookId: String) {
        val current = getMyLibrary(username).map {
            if (it.id == bookId) it.copy(isFavorite = !it.isFavorite) else it
        }
        saveMyLibrary(username, current)
    }

    fun removeFromLibrary(username: String, bookId: String) {
        val current = getMyLibrary(username).filterNot { it.id == bookId }
        saveMyLibrary(username, current)
    }

    fun updateNote(username: String, bookId: String, note: String) {
        val current = getMyLibrary(username).map { b ->
            if (b.id == bookId) b.copy(note = note) else b
        }
        saveMyLibrary(username, current)
    }
}
