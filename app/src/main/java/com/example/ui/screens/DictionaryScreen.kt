package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DictionaryEntry
import com.example.data.DictionaryProvider
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    studyRepository: StudyRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var activeTab by remember { mutableIntStateOf(0) } // 0: All terms, 1: Favorites
    var showAddCustomDialog by remember { mutableStateOf(false) }

    // TTS
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Observe Favorites from Firestore
    val favoritesList by studyRepository.observeDictionaryFavorites().collectAsStateWithLifecycle(emptyList())
    val favoriteWordsSet = remember(favoritesList) {
        favoritesList.map { it.wordEn.lowercase() }.toSet()
    }

    val searchResults = remember(searchQuery, selectedCategory) {
        DictionaryProvider.search(searchQuery, selectedCategory)
    }

    val displayedFavorites = remember(favoritesList, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) favoritesList
        else favoritesList.filter {
            it.wordEn.lowercase().contains(q) ||
            it.wordAr.contains(q) ||
            it.definitionAr.contains(q)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "القاموس الأكاديمي والعلمي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مصطلحات علمية، معاني ونطق دقيق وحفظ سحابي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCustomDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "إضافة مصطلح", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Tabs: الكل vs المفضلة
            TabRow(
                selectedTabIndex = activeTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("المعجم العلمي (${DictionaryProvider.entries.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("المفضلة السحابية (${favoritesList.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن مصطلح علمي (عربي أو إنجليزي)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Category filter chips (only on All Terms tab)
            if (activeTab == 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DictionaryProvider.categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content List
            if (activeTab == 0) {
                Text(
                    text = "المصطلحات المطابقة (${searchResults.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { entry ->
                        val isFav = favoriteWordsSet.contains(entry.wordEn.lowercase())
                        DictionaryEntryCard(
                            entry = entry,
                            isFavorite = isFav,
                            onToggleFavorite = {
                                coroutineScope.launch {
                                    if (isFav) {
                                        studyRepository.removeDictionaryFavorite(entry.wordEn)
                                    } else {
                                        studyRepository.addDictionaryFavorite(entry)
                                    }
                                }
                            },
                            onSpeak = { text, isEnglish ->
                                tts?.let { player ->
                                    player.language = if (isEnglish) Locale.US else Locale("ar")
                                    player.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            }
                        )
                    }
                }
            } else {
                // Favorites Tab
                if (displayedFavorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.StarOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لا توجد مصطلحات في المفضلة حالياً",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "اضغط على أيقونة النجمة بجانب أي مصطلح لحفظه في حسابك السحابي",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayedFavorites) { fav ->
                            val entry = DictionaryEntry(
                                wordEn = fav.wordEn,
                                wordAr = fav.wordAr,
                                category = fav.category.ifEmpty { "مفضلات" },
                                partOfSpeech = "مصطلح محفوظ",
                                definitionAr = fav.definitionAr,
                                exampleEn = fav.exampleEn,
                                exampleAr = fav.exampleAr
                            )
                            DictionaryEntryCard(
                                entry = entry,
                                isFavorite = true,
                                onToggleFavorite = {
                                    coroutineScope.launch {
                                        studyRepository.removeDictionaryFavorite(fav.wordEn)
                                    }
                                },
                                onSpeak = { text, isEnglish ->
                                    tts?.let { player ->
                                        player.language = if (isEnglish) Locale.US else Locale("ar")
                                        player.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Custom Term Dialog
    if (showAddCustomDialog) {
        var newWordEn by remember { mutableStateOf("") }
        var newWordAr by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("مصطلحات أكاديمية") }
        var newDefAr by remember { mutableStateOf("") }
        var newExampleEn by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("إضافة مصطلح علمي مخصص", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newWordEn,
                        onValueChange = { newWordEn = it },
                        label = { Text("المصطلح بالإنجليزي (English Word)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newWordAr,
                        onValueChange = { newWordAr = it },
                        label = { Text("المعنى بالعربي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDefAr,
                        onValueChange = { newDefAr = it },
                        label = { Text("التعريف العلمي المبسط") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newExampleEn,
                        onValueChange = { newExampleEn = it },
                        label = { Text("مثال في جملة مفيدة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWordEn.isNotBlank() && newWordAr.isNotBlank()) {
                            val customEntry = DictionaryEntry(
                                wordEn = newWordEn.trim(),
                                wordAr = newWordAr.trim(),
                                category = newCategory,
                                partOfSpeech = "مصطلح مخصص",
                                definitionAr = newDefAr.trim().ifEmpty { "مصطلح علمي أضافه الطالب" },
                                exampleEn = newExampleEn.trim(),
                                exampleAr = ""
                            )
                            coroutineScope.launch {
                                studyRepository.addDictionaryFavorite(customEntry)
                                showAddCustomDialog = false
                            }
                        }
                    }
                ) {
                    Text("حفظ في المفضلة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun DictionaryEntryCard(
    entry: DictionaryEntry,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSpeak: (String, Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: English Word + Pronunciation + Category + Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.wordEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { onSpeak(entry.wordEn, true) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "نطق الإنجليزية",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "${entry.partOfSpeech} • ${entry.category}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Favorite Icon Button
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "المفضلة",
                        tint = if (isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Arabic Translation & Meaning
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.wordAr,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { onSpeak(entry.wordAr, false) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "نطق العربية",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Definition
            Text(
                text = entry.definitionAr,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            // Example Box if available
            if (entry.exampleEn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 مثال: \"${entry.exampleEn}\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.exampleAr.isNotEmpty()) {
                            Text(
                                text = "الترجمة: ${entry.exampleAr}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
