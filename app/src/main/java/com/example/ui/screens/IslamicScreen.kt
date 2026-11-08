package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.IslamicDataProvider
import com.example.data.Reciter
import com.example.data.model.AyahModel
import com.example.data.model.AzkarItem
import com.example.data.model.QuranSurah
import com.example.data.repository.StudyRepository
import com.example.util.Governorate
import com.example.util.PrayerTimesCalculator
import com.example.util.QuranAudioForegroundService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicScreen(
    studyRepository: StudyRepository,
    onNavigateBack: () -> Unit
) {
    val tabs = listOf("التلاوة (صوت بالخلفية)", "قراءة عثمانية وتفسير", "متابعة الختمة", "الأذكار والتسبيح", "مواقيت الصلاة بمصر")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "المنصة القرآنية والإسلامية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "رسم عثماني، تفسير، تلاوة بالخلفية، ومواقيت الصلاة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> QuranAudioPlayerSection()
                1 -> QuranUthmaniAndTafsirSection()
                2 -> QuranKhatmaTrackerSection(studyRepository)
                3 -> AzkarAndTasbeehSection()
                4 -> EgyptPrayerTimesSection()
            }
        }
    }
}

// 1. Quran Audio Player Section with Background Foreground Service
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranAudioPlayerSection() {
    val context = LocalContext.current
    var selectedReciter by remember { mutableStateOf(IslamicDataProvider.reciters[0]) } // Sheikh Yasser Al-Dossari is index 0
    var showReciterMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Collect background service state
    val audioState by QuranAudioForegroundService.audioState.collectAsStateWithLifecycle()

    val filteredSurahs = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) IslamicDataProvider.surahs
        else IslamicDataProvider.surahs.filter {
            it.nameAr.contains(q) || it.englishName.contains(q, ignoreCase = true) || it.number.toString() == q
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        // Reciter Selection Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("القارئ المختار للتلاوة بالخلفية:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(selectedReciter.nameAr, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }
                Box {
                    Button(onClick = { showReciterMenu = true }) {
                        Text("تغيير الشيخ")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showReciterMenu,
                        onDismissRequest = { showReciterMenu = false }
                    ) {
                        IslamicDataProvider.reciters.forEach { reciter ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        reciter.nameAr,
                                        fontWeight = if (reciter.id == selectedReciter.id) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedReciter = reciter
                                    showReciterMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Player Bar if active or playing
        if (audioState.isPlaying || audioState.isBuffering || audioState.surahName.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (audioState.isBuffering) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("سورة ${audioState.surahName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${audioState.reciterName} • شغال في الخلفية", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (audioState.isPlaying) {
                                    QuranAudioForegroundService.pause(context)
                                } else {
                                    QuranAudioForegroundService.resume(context)
                                }
                            }
                        ) {
                            Icon(
                                if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "تشغيل / إيقاف",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { QuranAudioForegroundService.stop(context) }) {
                            Icon(Icons.Default.Stop, contentDescription = "إيقاف تام", tint = Color.Red)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Search Surah Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Surahs List (114)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredSurahs) { surah ->
                val isThisPlaying = audioState.surahNumber == surah.number && audioState.isPlaying

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isThisPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val audioUrl = IslamicDataProvider.getAudioUrl(selectedReciter, surah.number)
                            QuranAudioForegroundService.play(
                                context = context,
                                audioUrl = audioUrl,
                                surahNumber = surah.number,
                                surahName = surah.nameAr,
                                reciterName = selectedReciter.nameAr
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${surah.number}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "سورة ${surah.nameAr}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${surah.revelationType} • ${surah.ayahsCount} آيات • ص ${surah.startPage}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isThisPlaying) {
                                    QuranAudioForegroundService.pause(context)
                                } else {
                                    val audioUrl = IslamicDataProvider.getAudioUrl(selectedReciter, surah.number)
                                    QuranAudioForegroundService.play(
                                        context = context,
                                        audioUrl = audioUrl,
                                        surahNumber = surah.number,
                                        surahName = surah.nameAr,
                                        reciterName = selectedReciter.nameAr
                                    )
                                }
                            }
                        ) {
                            Icon(
                                if (isThisPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "استماع",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 2. Quran Uthmani Script & Tafsir Section
@Composable
fun QuranUthmaniAndTafsirSection() {
    var selectedSurah by remember { mutableStateOf(IslamicDataProvider.surahs[0]) } // Al-Fatihah
    var showSurahPicker by remember { mutableStateOf(false) }
    var fontScale by remember { mutableFloatStateOf(20f) }
    var showAllTafsir by remember { mutableStateOf(true) }

    val ayahs = remember(selectedSurah) {
        IslamicDataProvider.getSurahAyahs(selectedSurah.number)
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        // Controls Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("السورة المعروضة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text("سورة ${selectedSurah.nameAr} (${selectedSurah.ayahsCount} آيات)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(onClick = { showSurahPicker = true }) {
                        Text("اختيار سورة")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("حجم الخط:", fontSize = 12.sp)
                        IconButton(onClick = { fontScale = (fontScale - 2f).coerceAtLeast(16f) }) {
                            Icon(Icons.Default.TextDecrease, contentDescription = null)
                        }
                        IconButton(onClick = { fontScale = (fontScale + 2f).coerceAtMost(32f) }) {
                            Icon(Icons.Default.TextIncrease, contentDescription = null)
                        }
                    }

                    FilterChip(
                        selected = showAllTafsir,
                        onClick = { showAllTafsir = !showAllTafsir },
                        label = { Text(if (showAllTafsir) "إخفاء التفسير" else "عرض التفسير الميسر", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ayahs List with Uthmani Calligraphy and Tafsir
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(ayahs) { ayah ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Uthmani Text
                        Text(
                            text = ayah.textUthmani,
                            fontSize = fontScale.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Right,
                            lineHeight = (fontScale * 1.6).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Tafsir
                        if (showAllTafsir && ayah.tafsir.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "📖 التفسير الميسر [آية ${ayah.number}]:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = ayah.tafsir,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Surah Picker Dialog
    if (showSurahPicker) {
        AlertDialog(
            onDismissRequest = { showSurahPicker = false },
            title = { Text("اختر سورة لقراءتها بالرسم العثماني", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(350.dp)) {
                    items(IslamicDataProvider.surahs) { s ->
                        Text(
                            text = "${s.number}. سورة ${s.nameAr} (${s.revelationType})",
                            fontSize = 14.sp,
                            fontWeight = if (s.number == selectedSurah.number) FontWeight.Bold else FontWeight.Normal,
                            color = if (s.number == selectedSurah.number) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSurah = s
                                    showSurahPicker = false
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSurahPicker = false }) { Text("إغلاق") }
            }
        )
    }
}

// 3. Quran Khatma Tracker Section
@Composable
fun QuranKhatmaTrackerSection(studyRepository: StudyRepository) {
    val coroutineScope = rememberCoroutineScope()
    val khatmaState by studyRepository.observeKhatma().collectAsStateWithLifecycle(null)

    var pageInput by remember { mutableStateOf("1") }
    var dailyTargetInput by remember { mutableStateOf("4") }

    LaunchedEffect(khatmaState) {
        khatmaState?.let {
            pageInput = it.currentPage.toString()
            dailyTargetInput = it.dailyPagesTarget.toString()
        }
    }

    val currentPage = khatmaState?.currentPage ?: 1
    val currentJuz = ((currentPage - 1) / 20) + 1
    val progressFraction = (currentPage.toFloat() / 604f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Khatma Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "متابعة ختم القرآن الكريم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$currentPage",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "من 604 صفحة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "أنت الآن في الجزء $currentJuz • نسبة الإنجاز: ${(progressFraction * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Quick page increment buttons
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تسجيل قراءة سريعة:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 4, 10).forEach { count ->
                        Button(
                            onClick = {
                                val next = (currentPage + count).coerceAtMost(604)
                                coroutineScope.launch {
                                    studyRepository.updateKhatma(
                                        currentPage = next,
                                        currentJuz = ((next - 1) / 20) + 1,
                                        pagesReadToday = (khatmaState?.pagesReadToday ?: 0) + count,
                                        dailyTarget = khatmaState?.dailyPagesTarget ?: 4
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+$count ص", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Set Target & Current Page directly
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تحديث يدوي للصفحة والورد اليومي:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { pageInput = it },
                    label = { Text("الصفحة الحالية (1 - 604)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dailyTargetInput,
                    onValueChange = { dailyTargetInput = it },
                    label = { Text("الورد اليومي المستهدف (صفحات)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val p = pageInput.toIntOrNull()?.coerceIn(1, 604) ?: 1
                        val t = dailyTargetInput.toIntOrNull()?.coerceIn(1, 100) ?: 4
                        coroutineScope.launch {
                            studyRepository.updateKhatma(
                                currentPage = p,
                                currentJuz = ((p - 1) / 20) + 1,
                                pagesReadToday = khatmaState?.pagesReadToday ?: 0,
                                dailyTarget = t
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ تقدم الختمة")
                }
            }
        }
    }
}

// 4. Azkar and Tasbeeh Section
@Composable
fun AzkarAndTasbeehSection() {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("morning") }
    var tasbeehCount by remember { mutableIntStateOf(0) }
    var tasbeehTarget by remember { mutableIntStateOf(33) }
    var currentZikrText by remember { mutableStateOf("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ") }

    fun triggerVibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    val azkarList = remember(selectedCategory) {
        IslamicDataProvider.getAzkarByCategory(selectedCategory)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Electronic Tasbeeh Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("المسبحة الإلكترونية التفاعلية", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(currentZikrText, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(12.dp))

                // Tasbeeh Tap Button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            triggerVibrate()
                            tasbeehCount++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$tasbeehCount",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "انقر هنا",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { tasbeehCount = 0 }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصفير العداد", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Azkar Categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "morning" to "أذكار الصباح",
                "evening" to "أذكار المساء",
                "study" to "أذكار المذاكرة",
                "sleep" to "أذكار النوم"
            ).forEach { (catId, catTitle) ->
                val isSelected = selectedCategory == catId
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = catId },
                    label = { Text(catTitle, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Azkar List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(azkarList) { item ->
                var counter by remember(item.id) { mutableIntStateOf(0) }
                val isDone = counter >= item.repeatCount

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = item.text,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (item.virtue.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✨ الفضل: ${item.virtue}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "التكرار: $counter / ${item.repeatCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = {
                                    triggerVibrate()
                                    if (counter < item.repeatCount) counter++
                                },
                                enabled = !isDone,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isDone) "تمت قراءتها ✅" else "تسبيح (${counter + 1})", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. Egypt Prayer Times Section
data class PrayerDisplayItem(val name: String, val time: String, val icon: ImageVector)

@Composable
fun EgyptPrayerTimesSection() {
    var selectedGovernorate by remember { mutableStateOf(PrayerTimesCalculator.egyptianGovernorates[0]) } // Cairo
    var showGovMenu by remember { mutableStateOf(false) }

    val prayerTimes = remember(selectedGovernorate) {
        PrayerTimesCalculator.calculateTimes(selectedGovernorate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Governorate Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("المحافظة المختارة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("محافظة ${selectedGovernorate.nameAr}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                    Text("طبقا لهيئة المساحة المصرية", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }

                Box {
                    Button(onClick = { showGovMenu = true }) {
                        Text("المحافظات (27)")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showGovMenu,
                        onDismissRequest = { showGovMenu = false },
                        modifier = Modifier.height(300.dp)
                    ) {
                        PrayerTimesCalculator.egyptianGovernorates.forEach { gov ->
                            DropdownMenuItem(
                                text = { Text(gov.nameAr, fontWeight = if (gov.id == selectedGovernorate.id) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    selectedGovernorate = gov
                                    showGovMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Daily Prayers List
        val prayerList = listOf(
            PrayerDisplayItem("الفجر", prayerTimes.fajr, Icons.Default.WbTwilight),
            PrayerDisplayItem("الشروق", prayerTimes.sunrise, Icons.Default.WbSunny),
            PrayerDisplayItem("الظهر", prayerTimes.dhuhr, Icons.Default.LightMode),
            PrayerDisplayItem("العصر", prayerTimes.asr, Icons.Default.WbCloudy),
            PrayerDisplayItem("المغرب", prayerTimes.maghrib, Icons.Default.NightsStay),
            PrayerDisplayItem("العشاء", prayerTimes.isha, Icons.Default.DarkMode)
        )

        prayerList.forEach { item ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = item.time,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
