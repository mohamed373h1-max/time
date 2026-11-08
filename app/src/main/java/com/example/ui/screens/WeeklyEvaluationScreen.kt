package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WeeklyEvaluationItem
import com.example.data.repository.StudyRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyEvaluationScreen(
    studyRepository: StudyRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val currentWeekId = remember {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        "$year-W$week"
    }

    // Observed data
    val existingEval by studyRepository.observeWeeklyEvaluation(currentWeekId).collectAsStateWithLifecycle(null)
    val tasks by studyRepository.observeTasks().collectAsStateWithLifecycle(emptyList())
    val khatma by studyRepository.observeKhatma().collectAsStateWithLifecycle(null)
    val quizHistory by studyRepository.observeQuizResults().collectAsStateWithLifecycle(emptyList())

    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size
    val averageQuizScore = if (quizHistory.isNotEmpty()) quizHistory.map { it.scorePercentage }.average() else 0.0

    var reflectionText by remember { mutableStateOf("") }
    var userRating by remember { mutableIntStateOf(4) }
    var aiAdviceText by remember { mutableStateOf("") }
    var isGeneratingAiAdvice by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }

    LaunchedEffect(existingEval) {
        existingEval?.let {
            reflectionText = it.reflectionNotes
            userRating = it.selfRating
            aiAdviceText = it.aiAdvice
        }
    }

    val aiModel by remember {
        lazy {
            Firebase.ai.generativeModel("gemini-2.5-flash")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "التقييم الأسبوعي والتحليل الذكي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تقرير الإنجاز، مؤشرات الأداء، وتوجيهات التفوق",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Week Header Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("الأسبوع الدراسي الحالي", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                Text(currentWeekId, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text("تقييم شامل", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4 Key Metric Tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricTile(
                                modifier = Modifier.weight(1f),
                                label = "المهام المنجزة",
                                value = "$completedTasksCount / $totalTasksCount",
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF10B981)
                            )
                            MetricTile(
                                modifier = Modifier.weight(1f),
                                label = "معدل الاختبارات",
                                value = "${averageQuizScore.toInt()}%",
                                icon = Icons.Default.Quiz,
                                color = Color(0xFF6366F1)
                            )
                            MetricTile(
                                modifier = Modifier.weight(1f),
                                label = "صفحات الختمة",
                                value = "${khatma?.pagesReadToday ?: 0} صفحة",
                                icon = Icons.Default.AutoStories,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            // Self Rating Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("تقييمي الذاتي لمذاكرة هذا الأسبوع:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { userRating = star }) {
                                    Icon(
                                        imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star نجوم",
                                        tint = if (star <= userRating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Self Reflection Notes
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ملاحظاتي ونقاط التحسين للأسبوع القادم:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reflectionText,
                            onValueChange = { reflectionText = it },
                            placeholder = { Text("مثال: التركيز على حل مسائل الحث في الفيزياء ومراجعة الكيمياء العضوية يوم الإثنين...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // AI Performance Coaching & Advice
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("توجيهات المساعد الدراسي الذكي", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    isGeneratingAiAdvice = true
                                    coroutineScope.launch {
                                        try {
                                            val prompt = """
                                                أنت مرشد أكاديمي متميز لطلاب الثانوية العامة المصرية.
                                                بيانات أداء الطالب هذا الأسبوع:
                                                - المهام المنجزة: $completedTasksCount من $totalTasksCount
                                                - متوسط درجات الاختبارات: ${averageQuizScore.toInt()}%
                                                - تقييمه الذاتي: $userRating من 5
                                                - ملاحظات الطالب: "$reflectionText"
                                                
                                                قدم تقريراً توجيهياً سريعاً ومحفزاً (في 3-4 نقاط محددة وعملية) للأسبوع القادم، مع التركيز على تنظيم الوقت، رفع معدل الحل في المواد العلمية، والتغلب على التوتر.
                                            """.trimIndent()

                                            val response = aiModel.generateContent(prompt)
                                            aiAdviceText = response.text ?: "استمر في المثابرة، أداؤك في تقدم مستمر!"
                                        } catch (e: Exception) {
                                            aiAdviceText = "بارك الله في همتك! التزامك بالمهام ومواصلة حل الاختبارات الأسبوعية هما مفتاح تفوقك. ركز في الأسبوع القادم على مراجعة الأخطاء أولاً بأول."
                                        } finally {
                                            isGeneratingAiAdvice = false
                                        }
                                    }
                                },
                                enabled = !isGeneratingAiAdvice
                            ) {
                                if (isGeneratingAiAdvice) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text("توليد توجيهات أسبوعية")
                                }
                            }
                        }

                        if (aiAdviceText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = aiAdviceText,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Save Evaluation Button
            item {
                Button(
                    onClick = {
                        isSaving = true
                        coroutineScope.launch {
                            val evalItem = WeeklyEvaluationItem(
                                weekIdentifier = currentWeekId,
                                studyHoursCompleted = 20.0,
                                studyHoursTarget = 25.0,
                                tasksFinished = completedTasksCount,
                                tasksTotal = totalTasksCount,
                                quizzesTaken = quizHistory.size,
                                averageQuizScore = averageQuizScore,
                                khatmaPagesRead = khatma?.pagesReadToday ?: 0,
                                selfRating = userRating,
                                reflectionNotes = reflectionText,
                                aiAdvice = aiAdviceText
                            )
                            studyRepository.saveWeeklyEvaluation(evalItem)
                            isSaving = false
                            showSuccessToast = true
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ التقييم في ملفي السحابي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                if (showSuccessToast) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✅ تم حفظ التقييم الأسبوعي بنجاح في حسابك!",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}
