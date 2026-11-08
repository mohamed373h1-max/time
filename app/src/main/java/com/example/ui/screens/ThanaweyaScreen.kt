package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ThanaweyaDataProvider
import com.example.data.ThanaweyaSubjectItem
import com.example.data.model.QuestionType
import com.example.data.model.QuizResultItem
import com.example.data.model.ThanaweyaQuestion
import com.example.data.repository.StudyRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThanaweyaScreen(
    studyRepository: StudyRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Screen modes: 0: Subject Selection & Config, 1: Active Exam, 2: Quiz Results & History
    var screenMode by remember { mutableIntStateOf(0) }

    var selectedSubject by remember { mutableStateOf(ThanaweyaDataProvider.subjects.first()) }
    var selectedChapter by remember { mutableStateOf("الكل") }
    var selectedDifficulty by remember { mutableStateOf("متوسط") }
    var selectedQuestionCount by remember { mutableIntStateOf(6) }

    // Active Quiz State
    var activeQuestions by remember { mutableStateOf<List<ThanaweyaQuestion>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val userSelectedOptions = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> optionIndex
    val userEssayAnswers = remember { mutableStateMapOf<Int, String>() }   // questionIndex -> studentText
    val essayAiGradings = remember { mutableStateMapOf<Int, String>() }   // questionIndex -> AI feedback
    val essayAiScores = remember { mutableStateMapOf<Int, Double>() }     // questionIndex -> score
    var isAiGradingLoading by remember { mutableStateOf(false) }

    // Completed Quiz summary
    var lastQuizResult by remember { mutableStateOf<QuizResultItem?>(null) }

    // Observe Quiz History from Firestore
    val quizHistory by studyRepository.observeQuizResults().collectAsStateWithLifecycle(emptyList())

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
                            text = "بنك أسئلة ٣ ثانوي (علمي علوم)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "٥٠٪ اختياري + ٥٠٪ مقالي بتصحيح الذكاء الاصطناعي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (screenMode != 0) screenMode = 0 else onNavigateBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (screenMode == 0) {
                        IconButton(onClick = { screenMode = 2 }) {
                            Icon(Icons.Default.History, contentDescription = "سجل الاختبارات", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (screenMode) {
                0 -> SubjectAndConfigView(
                    selectedSubject = selectedSubject,
                    onSelectSubject = {
                        selectedSubject = it
                        selectedChapter = "الكل"
                    },
                    selectedChapter = selectedChapter,
                    onSelectChapter = { selectedChapter = it },
                    selectedDifficulty = selectedDifficulty,
                    onSelectDifficulty = { selectedDifficulty = it },
                    selectedQuestionCount = selectedQuestionCount,
                    onSelectQuestionCount = { selectedQuestionCount = it },
                    onStartExam = {
                        val questions = ThanaweyaDataProvider.getQuestions(
                            subjectId = selectedSubject.id,
                            chapter = if (selectedChapter == "الكل") null else selectedChapter,
                            difficulty = selectedDifficulty,
                            count = selectedQuestionCount
                        )
                        activeQuestions = questions
                        currentQuestionIndex = 0
                        userSelectedOptions.clear()
                        userEssayAnswers.clear()
                        essayAiGradings.clear()
                        essayAiScores.clear()
                        screenMode = 1
                    }
                )
                1 -> ActiveExamView(
                    questions = activeQuestions,
                    currentIndex = currentQuestionIndex,
                    onSelectIndex = { currentQuestionIndex = it },
                    userSelectedOptions = userSelectedOptions,
                    userEssayAnswers = userEssayAnswers,
                    essayAiGradings = essayAiGradings,
                    essayAiScores = essayAiScores,
                    isAiGradingLoading = isAiGradingLoading,
                    onGradeEssayWithAi = { qIndex, studentText, question ->
                        isAiGradingLoading = true
                        coroutineScope.launch {
                            try {
                                val prompt = """
                                    أنت مصحح امتحانات الثانوية العامة المصرية الأول في مادة ${question.subjectName}.
                                    السؤال المقالي: ${question.questionText}
                                    نموذج الإجابة الوزاري: ${question.modelAnswer}
                                    معايير التوزيع: ${question.gradingRubric}
                                    الدرجة العظمى: ${question.maxScore} درجات.
                                    
                                    إجابة الطالب: "$studentText"
                                    
                                    المطلوب منك بسرعة ودقة:
                                    1. أعطِ درجة تقديرية من ${question.maxScore} (مثلاً: 2 / 2 أو 1.5 / 2).
                                    2. تقييم دقيق: ما النقاط الصحيحة وما النقاط التي أغفلها الطالب؟
                                    3. نصيحة سريعة لتحصيل الدرجة النهائية في الامتحان الوزاري.
                                """.trimIndent()

                                val response = aiModel.generateContent(prompt)
                                val responseText = response.text ?: "تم التقييم بنجاح."
                                essayAiGradings[qIndex] = responseText

                                // Parse estimated score if available, or default to proportion
                                val score = if (responseText.contains("2 / 2") || responseText.contains("2/2")) 2.0
                                else if (responseText.contains("1.5") || responseText.contains("1 / 2") || responseText.contains("1/2")) 1.0
                                else 1.5
                                essayAiScores[qIndex] = score
                            } catch (e: Exception) {
                                essayAiGradings[qIndex] = "تعذر الاتصال بالذكاء الاصطناعي: ${e.localizedMessage}. تم تسجيل إجابتك بنجاح."
                                essayAiScores[qIndex] = 1.5
                            } finally {
                                isAiGradingLoading = false
                            }
                        }
                    },
                    onFinishExam = {
                        // Calculate score
                        var totalEarned = 0.0
                        var maxScore = 0.0
                        var mcqCorrect = 0
                        var mcqCount = 0
                        var essayCount = 0

                        activeQuestions.forEachIndexed { index, q ->
                            if (q.type == QuestionType.MULTIPLE_CHOICE) {
                                mcqCount++
                                maxScore += 2.0
                                val selectedOpt = userSelectedOptions[index]
                                if (selectedOpt == q.correctOptionIndex) {
                                    totalEarned += 2.0
                                    mcqCorrect++
                                }
                            } else {
                                essayCount++
                                maxScore += q.maxScore.toDouble()
                                val score = essayAiScores[index] ?: if ((userEssayAnswers[index]?.length ?: 0) > 10) 1.5 else 0.0
                                totalEarned += score
                            }
                        }

                        val percentage = if (maxScore > 0) (totalEarned / maxScore) * 100.0 else 0.0
                        val result = QuizResultItem(
                            subject = selectedSubject.name,
                            chapter = selectedChapter,
                            difficulty = selectedDifficulty,
                            totalQuestions = activeQuestions.size,
                            mcqCount = mcqCount,
                            essayCount = essayCount,
                            correctAnswers = mcqCorrect,
                            totalScore = totalEarned,
                            maxPossibleScore = maxScore,
                            scorePercentage = percentage,
                            aiEvaluationFeedback = "أداء ممتاز في مادة ${selectedSubject.name}! نسبة الإنجاز: ${String.format("%.1f", percentage)}%."
                        )

                        lastQuizResult = result
                        coroutineScope.launch {
                            studyRepository.saveQuizResult(result)
                        }
                        screenMode = 2
                    }
                )
                2 -> QuizResultsAndHistoryView(
                    lastResult = lastQuizResult,
                    history = quizHistory,
                    onRetake = { screenMode = 0 }
                )
            }
        }
    }
}

// 1. Subject & Config View
@Composable
private fun SubjectAndConfigView(
    selectedSubject: ThanaweyaSubjectItem,
    onSelectSubject: (ThanaweyaSubjectItem) -> Unit,
    selectedChapter: String,
    onSelectChapter: (String) -> Unit,
    selectedDifficulty: String,
    onSelectDifficulty: (String) -> Unit,
    selectedQuestionCount: Int,
    onSelectQuestionCount: (Int) -> Unit,
    onStartExam: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "منهاج الصف الثالث الثانوي (علمي علوم)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "مستوحى من كتب الوزارة، الامتحان، التفوق، والأضواء بنظام التابلت الحديث ٥٠٪ اختياري و ٥٠٪ مقالي",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Subject Selection (6 subjects)
        item {
            Text("اختر المادة الدراسية:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThanaweyaDataProvider.subjects.chunked(2).forEach { rowSubjects ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSubjects.forEach { subject ->
                            val isSelected = selectedSubject.id == subject.id
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                ),
                                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectSubject(subject) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = subject.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = subject.description,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chapter / Unit Selector
        item {
            Text("اختر الوحدة أو الفصل:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val allChapters = listOf("الكل") + selectedSubject.chapters
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allChapters) { chapter ->
                    val isSelected = selectedChapter == chapter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectChapter(chapter) },
                        label = { Text(chapter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Difficulty & Count Config
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("إعدادات الاختبار:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Difficulty
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مستوى الصعوبة:", fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("متوسط", "صعب").forEach { diff ->
                                val isSelected = selectedDifficulty == diff
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectDifficulty(diff) },
                                    label = { Text(diff, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Question Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("عدد الأسئلة (٥٠٪ اختياري + ٥٠٪ مقالي):", fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(4, 6, 10, 20).forEach { count ->
                                val isSelected = selectedQuestionCount == count
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectQuestionCount(count) },
                                    label = { Text("$count", fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Start Button
        item {
            Button(
                onClick = onStartExam,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ابدأ اختبار ${selectedSubject.name} الآن", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// 2. Active Exam View
@Composable
private fun ActiveExamView(
    questions: List<ThanaweyaQuestion>,
    currentIndex: Int,
    onSelectIndex: (Int) -> Unit,
    userSelectedOptions: MutableMap<Int, Int>,
    userEssayAnswers: MutableMap<Int, String>,
    essayAiGradings: MutableMap<Int, String>,
    essayAiScores: MutableMap<Int, Double>,
    isAiGradingLoading: Boolean,
    onGradeEssayWithAi: (Int, String, ThanaweyaQuestion) -> Unit,
    onFinishExam: () -> Unit
) {
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد أسئلة مطابقة للخيارات المحددة.")
        }
        return
    }

    val currentQ = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Exam Progress Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "السؤال ${currentIndex + 1} من ${questions.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        if (currentQ.type == QuestionType.MULTIPLE_CHOICE) "اختياري من متعدد (MCQ)" else "مقالي تحليلي (Essay)",
                        fontSize = 11.sp
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (currentQ.type == QuestionType.MULTIPLE_CHOICE) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Question Navigator numbers row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(questions.size) { idx ->
                val isAnswered = if (questions[idx].type == QuestionType.MULTIPLE_CHOICE) {
                    userSelectedOptions.containsKey(idx)
                } else {
                    !userEssayAnswers[idx].isNullOrBlank()
                }
                val isSelected = idx == currentIndex

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else if (isAnswered) Color(0xFF10B981)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onSelectIndex(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${idx + 1}",
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected || isAnswered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    // Source and Chapter Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currentQ.source} • ${currentQ.lessonName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentQ.questionText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Multiple Choice rendering
                if (currentQ.type == QuestionType.MULTIPLE_CHOICE) {
                    items(currentQ.options.size) { optIdx ->
                        val optionText = currentQ.options[optIdx]
                        val isSelected = userSelectedOptions[currentIndex] == optIdx

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    userSelectedOptions[currentIndex] = optIdx
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { userSelectedOptions[currentIndex] = optIdx }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = optionText,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                } else {
                    // Essay Question rendering
                    item {
                        var essayText by remember(currentIndex) {
                            mutableStateOf(userEssayAnswers[currentIndex] ?: "")
                        }

                        Text(
                            text = "اكتب إجابتك النموذجية وخطوات الحل بالتفصيل:",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = essayText,
                            onValueChange = {
                                essayText = it
                                userEssayAnswers[currentIndex] = it
                            },
                            placeholder = { Text("اكتب القوانين والتعليلات والخطوات بالتفصيل هنا...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // AI Instant Evaluation Button
                        Button(
                            onClick = {
                                onGradeEssayWithAi(currentIndex, essayText, currentQ)
                            },
                            enabled = essayText.trim().length >= 5 && !isAiGradingLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAiGradingLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري فحص الإجابة بالذكاء الاصطناعي...")
                            } else {
                                Icon(Icons.Default.Psychology, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تصحيح إجابتي فورياً بالذكاء الاصطناعي")
                            }
                        }

                        // Display AI evaluation if available
                        val aiFeedback = essayAiGradings[currentIndex]
                        if (!aiFeedback.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4F46E5))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تقييم مصحح الذكاء الاصطناعي:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = aiFeedback,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Next / Prev / Finish Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (currentIndex > 0) onSelectIndex(currentIndex - 1)
                },
                enabled = currentIndex > 0,
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text("السابق")
            }

            if (currentIndex < questions.size - 1) {
                Button(
                    onClick = { onSelectIndex(currentIndex + 1) }
                ) {
                    Text("التالي")
                }
            } else {
                Button(
                    onClick = onFinishExam,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إنهاء وتصحيح الاختبار")
                }
            }
        }
    }
}

// 3. Quiz Results & History View
@Composable
private fun QuizResultsAndHistoryView(
    lastResult: QuizResultItem?,
    history: List<QuizResultItem>,
    onRetake: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // If there's a last completed quiz result
        if (lastResult != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("نتيجة الاختبار الأخير", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${String.format("%.1f", lastResult.scorePercentage)}%",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${lastResult.totalScore} / ${lastResult.maxPossibleScore} درجات",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("المادة", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text(lastResult.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("أسئلة اختيارية", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text("${lastResult.mcqCount} أسئلة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("أسئلة مقالية", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text("${lastResult.essayCount} أسئلة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onRetake,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("بدء اختبار جديد في مادة أخرى")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "سجل الاختبارات السابقة (${history.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (history.isEmpty() && lastResult == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("لم تقم بإجراء أي اختبارات بعد. اختر مادة وابدأ الآن!", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            items(history) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${item.scorePercentage.toInt()}%",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "مستوى ${item.difficulty} • ${item.totalQuestions} أسئلة (٥٠٪ اختياري و ٥٠٪ مقالي)",
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
