package com.example.kitaplikapp.data.model

data class Book(
    val id: String= "",
    val title: String= "",
    val author: String= "",
    val category: String= "",
    val coverUrl: String = "",
    val isRead: Boolean = false,
    val createdByUser: Boolean = false, // API mi, kullanıcı mı ekledi ayırt etmek için
    val isFavorite: Boolean = false,
    val note: String = "",
    val language: String = "",      // "tr", "en" vb.
    val pageCount: Int = 0, // yoksa 0 tut
    val description: String = ""
)
