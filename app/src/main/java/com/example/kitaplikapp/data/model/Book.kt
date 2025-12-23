package com.example.kitaplikapp.data.model

data class Book(
    val id: String= "",
    val title: String= "",
    val author: String= "",
    val category: String= "",
    val coverUrl: String = "",
    val isRead: Boolean = false,
    val createdByUser: Boolean = false, // API mi yoksa kullanıcı mı ekledi ayırt ederiz
    val isFavorite: Boolean = false,
    val note: String = "",
    val language: String = "",
    val pageCount: Int = 0, // eğer sayfa sayısı yoksa 0 tutarız
    val description: String = ""
)
