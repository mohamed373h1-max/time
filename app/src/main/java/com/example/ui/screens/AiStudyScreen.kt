package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "AiStudyAssistant"

data class ChatMessage(
    val sender: String, // "user" or "model"
    val text: String,
    val isStreaming: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudyScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Ultra-fast and accurate AI Model configured with lower temperature for sharp academic answers
    val model by remember {
        lazy {
            Firebase.ai.generativeModel(
                modelName = "gemini-2.5-flash",
                generationConfig = generationConfig {
                    temperature = 0.3f
                    topK = 32
                    topP = 0.85f
                }
            )
        }
    }

    var userInput by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "model",
                text = "أهلاً بك يا بطل! 👋 أنا رفيقك الدراسي الذكي فائق السرعة والمطوّر خصيصاً لمساعدتك في كل مناهج الثانوية العامة والأكاديمية.\n\nيسعدني:\n• حل أصعب مسائل الفيزياء والكيمياء والرياضيات خطوة بخطوة\n• تصحيح الأسئلة المقالية ونموذج الإجابة والدرجات\n• تلخيص الدروس المعقدة واستخراج القوانين\n• اختبارك في أي درس تختاره فورياً!\n\nاكتب سؤالك أو اختر أحد النماذج السريعة بالأسفل لنبدأ فوراً 🚀"
            )
        )
    }

    val quickActionPills = listOf(
        "⚡ حل مسألة كيرشوف وقانون أوم",
        "🧪 معادلات الكيمياء العضوية",
        "🧬 آلية تخليق البروتين في الأحياء",
        "🪨 تراكيب الصخور والفوالق في الجيولوجيا",
        "📝 تصحيح سؤال مقالي بالدرجات",
        "🎯 لخص لي درس في 5 نقاط ذهبية"
    )

    fun sendMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank() || isGenerating) return

        messages.add(ChatMessage("user", trimmed))
        userInput = ""
        errorMessage = null
        isGenerating = true

        val modelMessageIndex = messages.size
        messages.add(ChatMessage("model", "", isStreaming = true))

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            val accumulated = StringBuilder()

            try {
                withTimeout(45_000L) {
                    val systemContext = """
                        أنت المساعد الأكاديمي والتربوي الأول للثانوية العامة المصرية والمناهج العلمية.
                        المميزات المطلوبة في إجابتك:
                        1. السرعة الفائقة والتركيز المباشر على لب السؤال بدون مقدمات طويلة.
                        2. الترتيب الأنيق في نقاط وخطوات مرقمة واستخدام الرموز التوضيحية.
                        3. لو كان السؤال مسألة: اكتب (المعطيات -> القانون -> التعويض الرياضي -> الناتج بالوحدة).
                        4. لو كان السؤال مقالياً: وضح نموذج الإجابة وتوزيع الدرجات الوزاري.
                        
                        سؤال الطالب: $trimmed
                    """.trimIndent()

                    model.generateContentStream(systemContext).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        accumulated.append(text)
                        messages[modelMessageIndex] = ChatMessage("model", accumulated.toString(), isStreaming = true)
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
                messages[modelMessageIndex] = ChatMessage("model", accumulated.toString(), isStreaming = false)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Request timed out", e)
                messages[modelMessageIndex] = ChatMessage(
                    "model",
                    accumulated.toString().ifEmpty { "عذراً يا بطل، استغرقت الاستجابة وقتاً أطول من المعتاد. يرجى المحاولة مرة أخرى." },
                    isStreaming = false
                )
                errorMessage = "استغرقت الإجابة وقتاً طويلاً. تأكد من جودة الإنترنت وحاول ثانية."
            } catch (e: CancellationException) {
                // User navigated away
            } catch (e: Exception) {
                Log.e(TAG, "AI Generation error: ${e.message}", e)
                messages[modelMessageIndex] = ChatMessage(
                    "model",
                    accumulated.toString().ifEmpty { "حدث خطأ أثناء معالجة الطلب: ${e.localizedMessage}." },
                    isStreaming = false
                )
                errorMessage = e.localizedMessage
            } finally {
                isGenerating = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المساعد الدراسي الذكي (Turbo AI)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "فائق السرعة • حل مسائل وتصحيح مقالي ومراجعات",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            messages.clear()
                            messages.add(
                                ChatMessage(
                                    sender = "model",
                                    text = "تم بدء جلسة استذكار جديدة! كيف يمكنني مساعدتك في دروسك الآن؟ 🚀"
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "جلسة جديدة", tint = MaterialTheme.colorScheme.outline)
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
            // Quick Action Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickActionPills) { pill ->
                    AssistChip(
                        onClick = { sendMessage(pill.substringAfter(" ")) },
                        label = { Text(pill, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatMessageCard(
                        message = message,
                        onCopyText = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AI Explanation", message.text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الشرح للمفكرة!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Error Banner if any
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Input Bar
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        placeholder = { Text("اسأل عن مسألة، قانون، أو درس في أي مادة...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isGenerating
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { sendMessage(userInput) },
                        enabled = userInput.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (userInput.isNotBlank() && !isGenerating) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = if (userInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageCard(
    message: ChatMessage,
    onCopyText: () -> Unit
) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text.ifEmpty { if (message.isStreaming) "جاري صياغة الحل الأكاديمي..." else "" },
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                if (!isUser && message.text.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onCopyText,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "نسخ الشرح",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
