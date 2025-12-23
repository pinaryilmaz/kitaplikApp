package com.example.kitaplikapp.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kitaplikapp.viewmodel.AuthViewModel
import com.example.kitaplikapp.viewmodel.HomeViewModel
import com.example.kitaplikapp.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    authVm: AuthViewModel,
    vm: SettingsViewModel = viewModel(),
    homeVm: HomeViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val user by authVm.loggedInUser.collectAsState()
    val darkMode by vm.darkMode.collectAsState()
    val profileUriStr by vm.profileUri.collectAsState()
    val libraryBooks by homeVm.libraryBooks.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    // istatistik hesaplamaları için
    val totalBooks = libraryBooks.size
    val readBooksCount = libraryBooks.count { it.isRead }
    val totalPagesRead = libraryBooks.filter { it.isRead }.sumOf { it.pageCount }
    val favoriteGenre = remember(libraryBooks) {
        if (libraryBooks.isEmpty()) "-"
        else libraryBooks.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key ?: "-"
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> vm.setProfileUri(uri?.toString()) }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = user?.fullName ?: "",
            currentUsername = user?.username ?: "",
            currentPass = user?.password ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { newName, newUser, newPass ->
                authVm.updateUser(newName, newUser, newPass)
                showEditDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Profil ve Ayarlar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // profil kartı
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val uri = profileUriStr?.let { Uri.parse(it) }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.size(72.dp)
                    ) {
                        if (uri != null) {
                            AsyncImage(
                                model = uri, contentDescription = "Profil fotoğrafı",
                                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(user?.fullName ?: "Giriş yapılmadı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("@${user?.username ?: "kullanici"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))

                        Spacer(Modifier.height(4.dp))

                        TextButton(
                            onClick = { launcher.launch("image/*") },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Fotoğrafı değiştir", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // kalem butonu
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // istatistikler
            Text("Okuma İstatistiklerim", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Default.LibraryBooks, totalBooks.toString(), "Kitaplıkta", MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
                StatCard(Icons.Default.CheckCircle, readBooksCount.toString(), "Bitirilen", MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Default.AutoStories, totalPagesRead.toString(), "Sayfa Okundu", MaterialTheme.colorScheme.errorContainer, Modifier.weight(1f))
                StatCard(Icons.Default.Category, favoriteGenre, "Favori Tür", MaterialTheme.colorScheme.surfaceVariant, Modifier.weight(1f))
            }

            // light-dark mode ayarı
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (darkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode, null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Görünüm", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(if (darkMode) "Karanlık Mod" else "Aydınlık Mod", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    }
                    Switch(checked = darkMode, onCheckedChange = { vm.setDarkMode(it) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // çıkış yap butonu
            OutlinedButton(
                onClick = { authVm.logout(); onLogout() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Çıkış Yap")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentUsername: String,
    currentPass: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var username by remember { mutableStateOf(currentUsername) }
    var password by remember { mutableStateOf(currentPass) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profili Düzenle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ad Soyad") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Kullanıcı Adı") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Yeni Şifre") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, username, password) }) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun StatCard(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier.height(100.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}