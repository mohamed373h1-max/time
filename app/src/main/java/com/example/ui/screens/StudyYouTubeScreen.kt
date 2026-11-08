package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyYouTubeScreen(
    studyRepository: StudyRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var inputUrl by remember { mutableStateOf("https://www.youtube.com/watch?v=dQw4w9WgXcQ") }
    var currentVideoId by remember { mutableStateOf("dQw4w9WgXcQ") }
    var noteTitle by remember { mutableStateOf("ملاحظات المحاضرة") }
    var noteContent by remember { mutableStateOf("") }
    var noteSubject by remember { mutableStateOf("عام") }
    var isNoteSaved by remember { mutableStateOf(false) }

    fun extractVideoId(url: String): String {
        val trimmed = url.trim()
        if (trimmed.length == 11 && !trimmed.contains("/") && !trimmed.contains(".")) {
            return trimmed
        }
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=).+?(?=[&?\\n#]|$)".toRegex()
        val match = pattern.find(trimmed)
        return match?.value ?: "dQw4w9WgXcQ"
    }

    val sampleLessons = listOf(
        Triple("رياضيات: التفاضل والتكامل", "https://www.youtube.com/watch?v=WUvTyaaNkzM", "رياضيات"),
        Triple("فيزياء: قوانين الحركة والنيوتن", "https://www.youtube.com/watch?v=kKKM8Y-u7ds", "فيزياء"),
        Triple("كيمياء: الجدول الدوري والروابط", "https://www.youtube.com/watch?v=0RRVV4Diomg", "كيمياء"),
        Triple("لغة إنجليزية: قواعد وتراكيب أساسية", "https://www.youtube.com/watch?v=36n_e8-t54U", "إنجليزي"),
        Triple("نصائح للمذاكرة بذكاء وبدون تشتت", "https://www.youtube.com/watch?v=IlU-zDU6aQ0", "تطوير دراسي")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "يوتيوب الدراسي بدون تشتت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // URL Input Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ضع رابط الفيديو الدراسي أو الدرس:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            placeholder = { Text("https://youtube.com/watch?v=...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val id = extractVideoId(inputUrl)
                                currentVideoId = id
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تشغيل")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Embedded Player Container
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                StudyWebView(videoId = currentVideoId)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sample Recommended Study Lessons
            Text(
                text = "قنوات وشروحات دراسية مقترحة:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (lesson in sampleLessons.take(3)) {
                    AssistChip(
                        onClick = {
                            inputUrl = lesson.second
                            currentVideoId = extractVideoId(lesson.second)
                            noteTitle = "ملاحظات: " + lesson.first
                            noteSubject = lesson.third
                        },
                        label = { Text(lesson.first.substringBefore(":"), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Integrated Note Taking Pad
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "دفتر ملاحظات المحاضرة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                if (noteContent.isNotBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            studyRepository.addNote(
                                                title = noteTitle.ifBlank { "ملاحظة فيديو" },
                                                content = noteContent,
                                                subject = noteSubject.ifBlank { "عام" }
                                            )
                                            isNoteSaved = true
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    }
                                }
                            },
                            enabled = noteContent.isNotBlank(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isNoteSaved) "تم الحفظ ✓" else "حفظ الملاحظة")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = {
                            noteTitle = it
                            isNoteSaved = false
                        },
                        label = { Text("عنوان الملاحظة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = {
                            noteContent = it
                            isNoteSaved = false
                        },
                        placeholder = { Text("اكتب النقاط المهمة والتعريفات والمعادلات أثناء مشاهدة الشرح...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun StudyWebView(videoId: String) {
    val embedHtml = remember(videoId) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
            <style>
                body { margin: 0; padding: 0; background-color: #000; display: flex; justify-content: center; align-items: center; height: 100vh; overflow: hidden; }
                iframe { width: 100%; height: 100%; border: none; }
            </style>
        </head>
        <body>
            <iframe src="https://www.youtube.com/embed/$videoId?autoplay=1&modestbranding=1&rel=0&playsinline=1" allowfullscreen allow="autoplay"></iframe>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
        },
        modifier = Modifier.fillMaxSize()
    )
}
