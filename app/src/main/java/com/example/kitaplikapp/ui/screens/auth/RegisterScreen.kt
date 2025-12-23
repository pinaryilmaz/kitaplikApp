package com.example.kitaplikapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kitaplikapp.ui.components.AuthBackground
import com.example.kitaplikapp.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authVm: AuthViewModel
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val message by authVm.message.collectAsState()

    // Kayıt başarılıysa yönlendirir
    LaunchedEffect(message) {
        if (message == "Kayıt başarılı") onRegisterSuccess()
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        AuthBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    val listState = rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding(),
                        contentPadding = PaddingValues(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        item {
                            Surface(shape = CircleShape, tonalElevation = 2.dp) {
                                Icon(
                                    imageVector = Icons.Filled.MenuBook,
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .padding(18.dp)
                                        .size(34.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        item { Spacer(Modifier.height(18.dp)) }

                        item {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Ad Soyad") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        item { Spacer(Modifier.height(10.dp)) }

                        item {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-posta") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        item { Spacer(Modifier.height(10.dp)) }

                        item {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Kullanıcı adı") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        item { Spacer(Modifier.height(10.dp)) }

                        item {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Şifre") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                visualTransformation = if (passwordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible)
                                                Icons.Filled.VisibilityOff
                                            else
                                                Icons.Filled.Visibility,
                                            contentDescription = "Şifre görünürlüğü"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        item { Spacer(Modifier.height(14.dp)) }

                        item {
                            Button(
                                onClick = {
                                    authVm.register(
                                        fullName = fullName.trim(),
                                        email = email.trim(),
                                        username = username.trim(),
                                        password = password
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Kaydol") }
                        }

                        if (!message.isNullOrBlank()) {
                            item {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = message ?: "",
                                    color = if (message == "Kayıt başarılı")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Zaten hesabın var mı?")
                                Spacer(Modifier.width(6.dp))
                                TextButton(
                                    onClick = onNavigateToLogin,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Giriş Yap")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}