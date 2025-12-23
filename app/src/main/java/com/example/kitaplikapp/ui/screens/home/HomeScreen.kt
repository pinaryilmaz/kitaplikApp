package com.example.kitaplikapp.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kitaplikapp.data.model.Book
import com.example.kitaplikapp.viewmodel.AuthViewModel
import com.example.kitaplikapp.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authVm: AuthViewModel,
    vm: HomeViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }

    val books by vm.books.collectAsState()
    val libraryBooks by vm.libraryBooks.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    val user by authVm.loggedInUser.collectAsState()
    val username = user?.username?.trim().orEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var filterSheetOpen by remember { mutableStateOf(false) }

    // filtre için state'ler
    var appliedCategory by remember { mutableStateOf<String?>(null) }
    var appliedAuthor by remember { mutableStateOf("") }
    var appliedLanguage by remember { mutableStateOf<String?>(null) }
    var appliedMinPages by remember { mutableStateOf(0) }
    var appliedMaxPages by remember { mutableStateOf(5000) }

    var tmpCategory by remember { mutableStateOf<String?>(null) }
    var tmpAuthor by remember { mutableStateOf("") }
    var tmpLanguage by remember { mutableStateOf<String?>(null) }
    var tmpMinPages by remember { mutableStateOf(0f) }
    var tmpMaxPages by remember { mutableStateOf(5000f) }

    if (username.isBlank()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Devam etmek için giriş yapmalısın.", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    LaunchedEffect(username) {
        vm.refreshLibrary(username)
        vm.loadDefault(username)
    }

    LaunchedEffect(username, query) {
        delay(450)
        if (query.isBlank()) vm.loadDefault(username)
        else vm.searchSmart(username, query)
    }

    val categories = remember(books) {
        books.map { it.category.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val languages = remember(books) {
        books.map { it.language.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val maxPagesInResults = remember(books) {
        max(1, books.maxOfOrNull { it.pageCount } ?: 0)
    }

    val filteredBooks = remember(
        books, appliedCategory, appliedAuthor, appliedLanguage, appliedMinPages, appliedMaxPages, maxPagesInResults
    ) {
        val authorQ = appliedAuthor.trim().lowercase()
        val pageFilterActive = appliedMinPages > 0 || appliedMaxPages < maxPagesInResults

        books.asSequence()
            .filter { book -> appliedCategory == null || book.category.equals(appliedCategory, ignoreCase = true) }
            .filter { book -> appliedLanguage == null || book.language.equals(appliedLanguage, ignoreCase = true) }
            .filter { book -> authorQ.isBlank() || book.author.lowercase().contains(authorQ) }
            .filter { book ->
                if (!pageFilterActive) true
                else book.pageCount > 0 && book.pageCount in appliedMinPages..appliedMaxPages
            }
            .toList()
    }

    if (filterSheetOpen) {
        LaunchedEffect(Unit) {
            tmpCategory = appliedCategory
            tmpAuthor = appliedAuthor
            tmpLanguage = appliedLanguage
            tmpMinPages = appliedMinPages.toFloat()
            tmpMaxPages = appliedMaxPages.toFloat()
        }

        ModalBottomSheet(onDismissRequest = { filterSheetOpen = false }) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(16.dp).imePadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { Text("Filtrele", style = MaterialTheme.typography.titleLarge) }

                item { Text("Tür", style = MaterialTheme.typography.titleMedium) }
                item {
                    var catMenu by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = catMenu, onExpandedChange = { catMenu = !catMenu }) {
                        OutlinedTextField(
                            value = tmpCategory ?: "Tümü", onValueChange = {}, readOnly = true,
                            label = { Text("Tür seç") }, modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catMenu) }
                        )
                        ExposedDropdownMenu(expanded = catMenu, onDismissRequest = { catMenu = false }) {
                            DropdownMenuItem(text = { Text("Tümü") }, onClick = { tmpCategory = null; catMenu = false })
                            categories.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { tmpCategory = c; catMenu = false }) }
                        }
                    }
                }

                item { Text("Yazar", style = MaterialTheme.typography.titleMedium) }
                item {
                    OutlinedTextField(
                        value = tmpAuthor, onValueChange = { tmpAuthor = it },
                        label = { Text("Yazar adı") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }

                item { Text("Dil", style = MaterialTheme.typography.titleMedium) }
                item {
                    var langMenu by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = langMenu, onExpandedChange = { langMenu = !langMenu }) {
                        OutlinedTextField(
                            value = tmpLanguage ?: "Tümü", onValueChange = {}, readOnly = true,
                            label = { Text("Dil seç") }, modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langMenu) }
                        )
                        ExposedDropdownMenu(expanded = langMenu, onDismissRequest = { langMenu = false }) {
                            DropdownMenuItem(text = { Text("Tümü") }, onClick = { tmpLanguage = null; langMenu = false })
                            languages.forEach { l -> DropdownMenuItem(text = { Text(l) }, onClick = { tmpLanguage = l; langMenu = false }) }
                        }
                    }
                }

                item { Text("Sayfa sayısı: ${tmpMinPages.toInt()} - ${tmpMaxPages.toInt()}", style = MaterialTheme.typography.bodyMedium) }
                item {
                    RangeSlider(
                        value = tmpMinPages..tmpMaxPages,
                        onValueChange = {
                            tmpMinPages = it.start; tmpMaxPages = it.endInclusive
                        },
                        valueRange = 0f..maxPagesInResults.toFloat().coerceAtLeast(1f)
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { tmpCategory = null; tmpAuthor = ""; tmpLanguage = null; tmpMinPages = 0f; tmpMaxPages = maxPagesInResults.toFloat() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Temizle") }
                        Button(
                            onClick = {
                                appliedCategory = tmpCategory; appliedAuthor = tmpAuthor.trim(); appliedLanguage = tmpLanguage
                                appliedMinPages = tmpMinPages.toInt(); appliedMaxPages = tmpMaxPages.toInt(); filterSheetOpen = false
                            }, modifier = Modifier.weight(1f)
                        ) { Text("Uygula") }
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding()
        ) {
            // search bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Kitap, yazar, tür ara...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, "Ara") },
                    trailingIcon = { if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { filterSheetOpen = true }) {
                    Icon(Icons.Filled.FilterList, "Filtrele")
                }
            }

            if (error != null) Text(text = error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))

            Spacer(Modifier.height(16.dp))
            Text("Keşfet", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBooks, key = { it.id }) { book ->
                    val isAdded = libraryBooks.any { it.id == book.id }

                    HomeBookItem(
                        book = book,
                        isAdded = isAdded,
                        onCardClick = { navController.navigate("detail/${book.id}") },
                        onAdd = {
                            val added = vm.addToLibrary(username, book)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    message = if (added) "Kitap kitaplığa eklendi" else "Zaten kitaplıkta",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// kart bileşeni
@Composable
fun HomeBookItem(
    book: Book,
    isAdded: Boolean,
    onAdd: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.66f)
            ) {
                AsyncImage(
                    model = book.coverUrl.ifBlank { null },
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clickable { onAdd() },
                    shape = CircleShape,
                    color = if (isAdded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isAdded) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = "Ekle",
                            tint = if (isAdded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = book.title.ifBlank { "İsimsiz" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = book.author.ifBlank { "Yazar yok" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}