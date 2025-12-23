package com.example.kitaplikapp.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RemoteBooksDataSource {

    private val client = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .build()

    private val api: BooksApi = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BooksApi::class.java)

    suspend fun search(
        query: String,
        startIndex: Int = 0,
        maxResults: Int = 40
    ): GoogleBooksResponse {
        return api.searchBooks(
            query = query,
            startIndex = startIndex,
            maxResults = maxResults
        )
    }
}
