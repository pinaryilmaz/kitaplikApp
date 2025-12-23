package com.example.kitaplikapp.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kitaplikapp.viewmodel.AuthViewModel
import com.example.kitaplikapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    navController: NavController,
    authVm: AuthViewModel,
    vm: HomeViewModel = viewModel()
) {
    val user by authVm.loggedInUser.collectAsState()
    val username = user?.username?.trim().orEmpty()

    // Verileri çekiyoruz
    val books by vm.books.collectAsState()
    val libraryBooks by vm.libraryBooks.collectAsState()

    // Kitabı bul (Hem ana listeden hem kitaplıktan bakıyoruz)
    val book = remember(books, libraryBooks, bookId) {
        books.find { it.id == bookId } ?: libraryBooks.find { it.id == bookId }
    }

    // Kitaplıkta ekli mi kontrolü
    val libraryBook = remember(libraryBooks, bookId) {
        libraryBooks.find { it.id == bookId }
    }
    val isAdded = libraryBook != null
    val isRead = libraryBook?.isRead == true
    val isFavorite = libraryBook?.isFavorite == true

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Kitap bulunamadı :(")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Kapak Görseli ve Arka Plan Efekti
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                // Arka plan flu efekti (Opsiyonel: Box rengi ile de yapılabilir)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f) // Üst yarısı renkli
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Asıl Kapak Resmi
                AsyncImage(
                    model = book.coverUrl.ifBlank { null },
                    contentDescription = book.title,
                    modifier = Modifier
                        .width(160.dp)
                        .aspectRatio(0.66f)
                        .shadow(16.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Başlık ve Yazar Bilgisi
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.author.ifBlank { "Yazar Bilinmiyor" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. İstatistik Kartları (Sayfa, Dil, Kategori)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookInfoBadge(icon = Icons.Default.MenuBook, label = "${book.pageCount} Sayfa")
                BookInfoBadge(icon = Icons.Default.Language, label = book.language.uppercase())
                BookInfoBadge(icon = Icons.Default.Category, label = book.category)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Divider(modifier = Modifier.padding(horizontal = 32.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Aksiyon Butonları (Ekle, Favori, Okundu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kitaplığa Ekle / Çıkar
                Button(
                    onClick = {
                        if (isAdded) vm.removeFromLibrary(username, book.id)
                        else vm.addToLibrary(username, book)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdded) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = if (isAdded) Icons.Default.Delete else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isAdded) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isAdded) "Kitaplıktan Çıkar" else "Kitaplığa Ekle",
                        color = if (isAdded) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Favori Butonu (Sadece ekliyse veya değilse de çalışabilir, mantığına göre değişir)
                FilledTonalIconButton(
                    onClick = { vm.toggleFavorite(username, book.id) },
                    enabled = isAdded // Sadece kitaplıktaysa favorilenebilir (Opsiyonel)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Okundu Durumu (Sadece kitaplıktaysa göster)
            if (isAdded) {
                Spacer(Modifier.height(16.dp))
                FilterChip(
                    selected = isRead,
                    onClick = { vm.toggleReadStatus(username, book.id) },
                    label = { Text(if (isRead) "Okundu Olarak İşaretlendi" else "Okunmadı Olarak İşaretle") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isRead) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Açıklama / Özet Kısmı
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Kitap Hakkında",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description.ifBlank { "Bu kitap için henüz bir açıklama girilmemiş." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(100.dp)) // Alt boşluk
        }
    }
}

// Ufak Bilgi Kartı Bileşeni
@Composable
fun BookInfoBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}