package com.example.kitaplikapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kitaplikapp.data.model.User
import com.example.kitaplikapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(app.applicationContext)

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser = _loggedInUser.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun login(username: String, password: String) {
        val user = repo.login(username, password)
        if (user == null) _message.value = "Giriş başarısız"
        else _loggedInUser.value = user
    }

    fun register(fullName: String, email: String, username: String, password: String) {
        val err = repo.registerWithMessage(fullName, email, username, password)
        _message.value = err ?: "Kayıt başarılı"
    }

    fun logout() {
        _loggedInUser.value = null
    }

    fun updateUser(name: String, username: String, pass: String) {
        val currentUser = _loggedInUser.value
        if (currentUser != null) {
            _loggedInUser.value = currentUser.copy(
                fullName = name,
                username = username,
                password = pass
            )
        }
    }
}