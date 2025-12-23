package com.example.kitaplikapp.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonStorage(private val context: Context) {

    private val gson = Gson()

    fun <T> readList(fileName: String, typeToken: TypeToken<List<T>>): List<T> {
        return try {
            val file = context.getFileStreamPath(fileName)
            if (!file.exists()) return emptyList()
            val json = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            gson.fromJson(json, typeToken.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun <T> writeList(fileName: String, list: List<T>) {
        val json = gson.toJson(list)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { it.write(json.toByteArray()) }
    }
}
