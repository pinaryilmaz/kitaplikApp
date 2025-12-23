package com.example.kitaplikapp.data.remote

data class GoogleBooksResponse(
    val items: List<Item>?
)

data class Item(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val categories: List<String>?,
    val imageLinks: ImageLinks?,
    val language: String? = null,
    val pageCount: Int? = null
)

data class ImageLinks(
    val thumbnail: String?
)
