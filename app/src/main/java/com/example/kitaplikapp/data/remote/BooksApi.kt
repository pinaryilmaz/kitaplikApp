package com.example.kitaplikapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApi {
    @GET("books/v1/volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 40,   // 1..40
        @Query("startIndex") startIndex: Int = 0,    // paging
        @Query("printType") printType: String = "books",
        @Query("orderBy") orderBy: String = "relevance",
        @Query("langRestrict") langRestrict: String? = null
    ): GoogleBooksResponse
}

