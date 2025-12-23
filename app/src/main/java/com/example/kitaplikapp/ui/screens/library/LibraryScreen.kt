package com.example.kitaplikapp.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kitaplikapp.data.model.Book
import com.example.kitaplikapp.viewmodel.AuthViewModel
import com.example.kitaplikapp.viewmodel.HomeViewModel
import kotlin.math.max

private enum class LibraryFilter { ALL, READ, UNREAD, FAVORITES }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    authVm: AuthViewModel,
    vm: HomeViewModel = viewModel()
) {
    val user by authVm.loggedInUser.collectAsState()
    val username = user?.username?.trim().orEmpty()

    val libraryBooks by vm.libraryBooks.collectAsState()
    var selectedFilter by remember { mutableStateOf(LibraryFilter.ALL) } // Tab filtreleri (Okundu, Favori vb.)
    var query by remember { mutableStateOf("") } // Arama kutusu

    var noteDialogOpen by remember { mutableStateOf(false) }
    var noteBookId by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }

    var filterSheetOpen by remember { mutableStateOf(false) }

    // --- DETAYLI FİLTRE DEĞİŞKENLERİ (Uygulananlar) ---
    var appliedCategory by remember { mutableStateOf<String?>(null) }
    var appliedAuthor by remember { mutableStateOf("") }
    var appliedLanguage by remember { mutableStateOf<String?>(null) }
    var appliedMinPages by remember { mutableStateOf(0) }
    var appliedMaxPages by remember { mutableStateOf(5000) }

    // --- GEÇİCİ DEĞİŞKENLER (Sheet içinde oynananlar) ---
    var tmpCategory by remember { mutableStateOf<String?>(null) }
    var tmpAuthor by remember { mutableStateOf("") }
    var tmpLanguage by remember { mutableStateOf<String?>(null) }
    var tmpMinPages by remember { mutableStateOf(0f) }
    var tmpMaxPages by remember { mutableStateOf(5000f) }

    if (username.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Devam etmek için giriş yapmalısın.", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    LaunchedEffect(username) { vm.refreshLibrary(username) }

    // Kitaplığındaki mevcut kitaplardan kategori/dil listelerini çıkar
    val categories = remember(libraryBooks) {
        libraryBooks.map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val languages = remember(libraryBooks) {
        libraryBooks.map { it.language.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val maxPagesInLibrary = remember(libraryBooks) {
        max(1, libraryBooks.maxOfOrNull { it.pageCount } ?: 0)
    }

    // Not Ekleme Dialogu
    if (noteDialogOpen) {
        AlertDialog(
            onDismissRequest = { noteDialogOpen = false },
            title = { Text("Not ekle") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Not") },
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = noteBookId
                        if (!id.isNullOrBlank()) vm.updateNote(username, id, noteText.trim())
                        noteDialogOpen = false
                    }
                ) { Text("Kaydet") }
            },
            dismissButton = { TextButton(onClick = { noteDialogOpen = false }) { Text("İptal") } }
        )
    }

    // ✅ FİLTRELEME PENCERESİ (BURAYI DOLDURDUK)
    if (filterSheetOpen) {
        LaunchedEffect(Unit) {
            // Sheet açılınca mevcut uygulanan değerleri geçici değişkenlere yükle
            tmpCategory = appliedCategory
            tmpAuthor = appliedAuthor
            tmpLanguage = appliedLanguage
            tmpMinPages = appliedMinPages.toFloat()
            tmpMaxPages = appliedMaxPages.toFloat()
        }

        ModalBottomSheet(onDismissRequest = { filterSheetOpen = false }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { Text("Kitaplığımı Filtrele", style = MaterialTheme.typography.titleLarge) }

                // 1. Tür Seçimi
                item { Text("Tür", style = MaterialTheme.typography.titleMedium) }
                item {
                    var catMenu by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = catMenu,
                        onExpandedChange = { catMenu = !catMenu }
                    ) {
                        OutlinedTextField(
                            value = tmpCategory ?: "Tümü",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tür seç") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catMenu) }
                        )
                        ExposedDropdownMenu(expanded = catMenu, onDismissRequest = { catMenu = false }) {
                            DropdownMenuItem(text = { Text("Tümü") }, onClick = { tmpCategory = null; catMenu = false })
                            categories.forEach { c ->
                                DropdownMenuItem(text = { Text(c) }, onClick = { tmpCategory = c; catMenu = false })
                            }
                        }
                    }
                }

                // 2. Yazar Seçimi
                item { Text("Yazar", style = MaterialTheme.typography.titleMedium) }
                item {
                    OutlinedTextField(
                        value = tmpAuthor,
                        onValueChange = { tmpAuthor = it },
                        label = { Text("Yazar adı (opsiyonel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Dil Seçimi
                item { Text("Dil", style = MaterialTheme.typography.titleMedium) }
                item {
                    var langMenu by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = langMenu,
                        onExpandedChange = { langMenu = !langMenu }
                    ) {
                        OutlinedTextField(
                            value = tmpLanguage ?: "Tümü",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dil seç") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langMenu) }
                        )
                        ExposedDropdownMenu(expanded = langMenu, onDismissRequest = { langMenu = false }) {
                            DropdownMenuItem(text = { Text("Tümü") }, onClick = { tmpLanguage = null; langMenu = false })
                            languages.forEach { l ->
                                DropdownMenuItem(text = { Text(l) }, onClick = { tmpLanguage = l; langMenu = false })
                            }
                        }
                    }
                }

                // 4. Sayfa Sayısı
                item { Text("Sayfa sayısı", style = MaterialTheme.typography.titleMedium) }
                item {
                    Text(
                        text = "${tmpMinPages.toInt()} - ${tmpMaxPages.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    RangeSlider(
                        value = tmpMinPages..tmpMaxPages,
                        onValueChange = { range ->
                            tmpMinPages = range.start.coerceAtLeast(0f)
                            tmpMaxPages = range.endInclusive.coerceAtMost(maxPagesInLibrary.toFloat().coerceAtLeast(1f))
                            if (tmpMinPages > tmpMaxPages) tmpMinPages = tmpMaxPages
                        },
                        valueRange = 0f..maxPagesInLibrary.toFloat().coerceAtLeast(1f)
                    )
                }

                // 5. Butonlar (Temizle / Uygula)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                tmpCategory = null
                                tmpAuthor = ""
                                tmpLanguage = null
                                tmpMinPages = 0f
                                tmpMaxPages = maxPagesInLibrary.toFloat().coerceAtLeast(1f)
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Temizle") }

                        Button(
                            onClick = {
                                appliedCategory = tmpCategory
                                appliedAuthor = tmpAuthor.trim()
                                appliedLanguage = tmpLanguage
                                appliedMinPages = tmpMinPages.toInt()
                                appliedMaxPages = tmpMaxPages.toInt()
                                filterSheetOpen = false
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Uygula") }
                    }
                }
            }
        }
    }

    // FİLTRELEME MANTIĞI
    val filteredBooks = remember(
        libraryBooks, selectedFilter, appliedCategory, appliedAuthor, appliedLanguage, appliedMinPages, appliedMaxPages, query, maxPagesInLibrary
    ) {
        val q = query.trim().lowercase()
        val authorQ = appliedAuthor.trim().lowercase()
        val pageFilterActive = appliedMinPages > 0 || appliedMaxPages < maxPagesInLibrary

        libraryBooks.asSequence()
            .filter { book ->
                // Tab Filtreleri (Tümü, Okundu, Favori vs.)
                when (selectedFilter) {
                    LibraryFilter.ALL -> true
                    LibraryFilter.READ -> book.isRead
                    LibraryFilter.UNREAD -> !book.isRead
                    LibraryFilter.FAVORITES -> book.isFavorite
                }
            }
            // Detaylı Filtreler
            .filter { book -> appliedCategory == null || book.category.equals(appliedCategory, ignoreCase = true) }
            .filter { book -> appliedLanguage == null || book.language.equals(appliedLanguage, ignoreCase = true) }
            .filter { book -> authorQ.isBlank() || book.author.lowercase().contains(authorQ) }
            .filter { book ->
                if (!pageFilterActive) true
                else book.pageCount > 0 && book.pageCount in appliedMinPages..appliedMaxPages
            }
            // Arama Kutusu
            .filter { book ->
                if (q.isBlank()) true
                else {
                    val t = book.title.lowercase()
                    val a = book.author.lowercase()
                    val c = book.category.lowercase()
                    t.contains(q) || a.contains(q) || c.contains(q)
                }
            }
            .toList()
    }

    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            stickyHeader {
                Surface(
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Kitaplığım", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            IconButton(onClick = { filterSheetOpen = true }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filtrele", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Kitap adı, yazar veya tür ara") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Ara") },
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = selectedFilter == LibraryFilter.ALL,
                                onClick = { selectedFilter = LibraryFilter.ALL },
                                shape = SegmentedButtonDefaults.itemShape(0, 4)
                            ) { Text("Tümü", maxLines = 1) }

                            SegmentedButton(
                                selected = selectedFilter == LibraryFilter.READ,
                                onClick = { selectedFilter = LibraryFilter.READ },
                                shape = SegmentedButtonDefaults.itemShape(1, 4)
                            ) { Text("Okundu", maxLines = 1) }

                            SegmentedButton(
                                selected = selectedFilter == LibraryFilter.UNREAD,
                                onClick = { selectedFilter = LibraryFilter.UNREAD },
                                shape = SegmentedButtonDefaults.itemShape(2, 4)
                            ) { Text("Okunmadı", maxLines = 1) }

                            SegmentedButton(
                                selected = selectedFilter == LibraryFilter.FAVORITES,
                                onClick = { selectedFilter = LibraryFilter.FAVORITES },
                                shape = SegmentedButtonDefaults.itemShape(3, 4)
                            ) { Text("Favoriler", maxLines = 1) }
                        }
                    }
                }
            }

            item {
                when {
                    libraryBooks.isEmpty() -> Text("Henüz kitap eklemedin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    filteredBooks.isEmpty() -> Text("Bu filtrede / aramada kitap yok.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // SWIPE TO DELETE + TIKLAMA İLE DETAY
            items(filteredBooks, key = { it.id }) { book ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            vm.removeFromLibrary(username, book.id)
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp)
                                .background(color, RoundedCornerShape(14.dp))
                                .padding(end = 24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                ) {
                    LibraryBookItem(
                        book = book,
                        onToggleRead = { vm.toggleReadStatus(username, book.id) },
                        onToggleFavorite = { vm.toggleFavorite(username, book.id) },
                        onNoteClick = {
                            noteBookId = book.id
                            noteText = book.note
                            noteDialogOpen = true
                        },
                        // ✅ Detaya git
                        onClick = { navController.navigate("detail/${book.id}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryBookItem(
    book: Book,
    onToggleRead: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNoteClick: () -> Unit,
    onClick: () -> Unit // Yeni callback
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // ✅ Karta tıklayınca çalışır
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    AsyncImage(
                        model = book.coverUrl.ifBlank { null },
                        contentDescription = book.title,
                        modifier = Modifier
                            .width(80.dp)
                            .height(110.dp),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title.ifBlank { "Başlık yok" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(4.dp))

                        Text("Yazar: ${book.author.ifBlank { "Bilinmiyor" }}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Tür: ${book.category.ifBlank { "Bilinmiyor" }}", maxLines = 1, overflow = TextOverflow.Ellipsis)

                        val lang = book.language.ifBlank { "" }
                        val pages = book.pageCount
                        if (lang.isNotBlank() || pages > 0) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = buildString {
                                    if (lang.isNotBlank()) append("Dil: $lang")
                                    if (lang.isNotBlank() && pages > 0) append(" • ")
                                    if (pages > 0) append("Sayfa: $pages")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (book.isRead) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (book.isRead) "Okundu" else "Okunmadı",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Switch(
                        checked = book.isRead,
                        onCheckedChange = { onToggleRead() },
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            IconButton(onClick = onToggleFavorite, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = if (book.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (book.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onNoteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp)
            ) {
                Icon(Icons.Filled.StickyNote2, contentDescription = "Not ekle", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}