package com.example.kitaplikapp.data.repository

import android.content.Context
import android.util.Log
import com.example.kitaplikapp.data.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class AuthRepository(private val context: Context) {

    private val file: File = File(context.filesDir, "users.json")

    private fun loadUsers(): MutableList<User> {
        return try {
            if (!file.exists()) return mutableListOf()

            val text = file.readText()
            if (text.isBlank()) return mutableListOf()

            val arr = JSONArray(text)
            val list = mutableListOf<User>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    User(
                        username = obj.optString("username"),
                        password = obj.optString("password"),
                        fullName = obj.optString("fullName"),
                        email = obj.optString("email")
                    )
                )
            }
            list
        } catch (e: Exception) {
            // Dosya formatı bozulmuşsa uygulama çökmek yerine sıfırlanır
            Log.e("AUTH", "users.json okunamadı, sıfırlanıyor. Hata: ${e.message}")
            mutableListOf()
        }
    }

    private fun saveUsers(users: List<User>) {
        val arr = JSONArray()
        users.forEach { u ->
            val obj = JSONObject().apply {
                put("username", u.username)
                put("password", u.password)
                put("fullName", u.fullName)
                put("email", u.email)
            }
            arr.put(obj)
        }
        file.writeText(arr.toString())
        Log.d("AUTH", "users.json kaydedildi -> path=${file.absolutePath}, count=${users.size}")
    }

    fun register(fullName: String, email: String, username: String, password: String): Boolean {
        val n = fullName.trim()
        val e = email.trim()
        val u = username.trim()
        val p = password.trim()

        // Zorunlu alan doldurulması için
        if (n.isBlank() || e.isBlank() || u.isBlank() || p.isBlank()) return false

        // Ad Soyad için
        if (n.length < 3) return false

        // Kullanıcı adı için
        if (u.length < 3) return false
        if (u.contains(" ")) return false

        // Şifre için kontrol
        if (p.length < 6) return false

        // E-mail basit kontrol
        val emailOk = e.contains("@") && e.contains(".") && !e.contains(" ")
        if (!emailOk) return false

        val users = loadUsers()

        // Kullanıcı adı benzersiz olmalı
        val usernameExists = users.any { it.username.equals(u, ignoreCase = true) }
        if (usernameExists) return false


        val emailExists = users.any { it.email.equals(e, ignoreCase = true) }
        if (emailExists) return false

        users.add(User(username = u, password = p, fullName = n, email = e))
        saveUsers(users)
        return true
    }
    fun registerWithMessage(fullName: String, email: String, username: String, password: String): String? {
        val n = fullName.trim()
        val e = email.trim()
        val u = username.trim()
        val p = password.trim()

        if (n.isBlank() || e.isBlank() || u.isBlank() || p.isBlank()) return "Tüm alanları doldur"
        if (n.length < 3) return "Ad Soyad en az 3 karakter olmalı"
        if (u.length < 3) return "Kullanıcı adı en az 3 karakter olmalı"
        if (u.contains(" ")) return "Kullanıcı adında boşluk olamaz"
        if (p.length < 6) return "Şifre en az 6 karakter olmalı"

        val emailOk = e.contains("@") && e.contains(".") && !e.contains(" ")
        if (!emailOk) return "Geçerli bir e-mail gir"

        val users = loadUsers()
        if (users.any { it.username.equals(u, ignoreCase = true) }) return "Bu kullanıcı adı zaten var"
        if (users.any { it.email.equals(e, ignoreCase = true) }) return "Bu e-mail zaten kullanılıyor"

        users.add(User(username = u, password = p, fullName = n, email = e))
        saveUsers(users)
        return null
    }

    fun login(username: String, password: String): User? {
        val u = username.trim()
        val p = password.trim()

        val users = loadUsers()
        val found = users.firstOrNull {
            it.username.equals(u, ignoreCase = true) && it.password == p
        }

        Log.d("AUTH", "LOGIN: $u -> ${if (found != null) "OK" else "FAIL"} | usersCount=${users.size}")
        return found
    }

    fun dumpUsers(): String {
        val text = if (file.exists()) file.readText() else "<dosya yok>"
        Log.d("AUTH", "DUMP users.json = $text")
        return text
    }
}
