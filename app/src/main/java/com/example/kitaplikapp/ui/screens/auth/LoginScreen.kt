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
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authVm: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loggedInUser by authVm.loggedInUser.collectAsState()
    val message by authVm.message.collectAsState()

    LaunchedEffect(loggedInUser) {
        if (loggedInUser != null) onLoginSuccess()
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

                        // header: başlık + slogan + logo
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "MorKitap",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = "Kişisel kitaplığın",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )

                                Spacer(Modifier.height(14.dp))

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
                        }

                        item { Spacer(Modifier.height(18.dp)) }

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
                                onClick = { authVm.login(username.trim(), password) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Giriş Yap") }
                        }

                        if (!message.isNullOrBlank()) {
                            item {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = message ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        item { Spacer(Modifier.height(14.dp)) }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Hesabın yok mu?")
                                Spacer(Modifier.width(6.dp))
                                TextButton(
                                    onClick = onNavigateToRegister,
                                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                                ) {
                                    Text("Kaydol")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}