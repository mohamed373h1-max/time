package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfileData(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val totalStudyMinutes: Int = 0,
    val tasksCompleted: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val lastActive: Timestamp? = null
)

@IgnoreExtraProperties
data class TaskItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val dueDate: String = "",
    val priority: String = "medium", // low, medium, high
    val isCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudyNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val subject: String = "",
    val isPinned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class SchedulePeriod(
    val periodNumber: Int = 1,
    val subject: String = "",
    val teacher: String = "",
    val time: String = "",
    val room: String = ""
)

@IgnoreExtraProperties
data class ScheduleDayItem(
    @DocumentId val id: String = "",
    val dayId: String = "",
    val dayName: String = "",
    val periodsJson: String = "[]",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class KhatmaData(
    @DocumentId val id: String = "",
    val userId: String = "",
    val currentPage: Int = 1,
    val currentJuz: Int = 1,
    val dailyPagesTarget: Int = 4,
    val pagesReadToday: Int = 0,
    val lastReadDate: String = "",
    val completedCount: Int = 0,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChannelPostItem(
    @DocumentId val id: String = "",
    val authorEmail: String = "",
    val authorName: String = "",
    val title: String = "",
    val content: String = "",
    val mediaType: String = "text", // text, image, file
    val mediaUrl: String? = null,
    val fileName: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChannelCommentItem(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String? = null,
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChannelReactionItem(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val reactionType: String = "like", // like, heart, clap, fire
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class AzkarItem(
    val id: String,
    val category: String, // morning, evening, sleep, study
    val text: String,
    val repeatCount: Int = 1,
    val virtue: String = ""
)

data class QuranSurah(
    val number: Int,
    val nameAr: String,
    val englishName: String,
    val revelationType: String, // Meccan / Medinan
    val ayahsCount: Int,
    val startPage: Int
)

data class AyahModel(
    val number: Int,
    val textUthmani: String,
    val tafsir: String
)

@IgnoreExtraProperties
data class DictionaryFavoriteItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val wordEn: String = "",
    val wordAr: String = "",
    val category: String = "",
    val definitionAr: String = "",
    val exampleEn: String = "",
    val exampleAr: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class QuestionType {
    MULTIPLE_CHOICE, // اختياري من متعدد
    ESSAY            // مقالي تحليلي
}

data class ThanaweyaQuestion(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val chapterName: String,
    val lessonName: String,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val difficulty: String = "متوسط", // متوسط، صعب
    val source: String = "كتاب الامتحان والوزارة", // كتاب الامتحان، التفوق، الأضواء، امتحانات استرشادية
    val questionText: String,
    val options: List<String> = emptyList(), // For Multiple Choice (4 choices)
    val correctOptionIndex: Int = 0,        // For Multiple Choice
    val modelAnswer: String,                // Model answer for essay or explanation for MCQ
    val gradingRubric: String = "",         // Rubric criteria for AI grading of essay questions
    val maxScore: Int = 2
)

@IgnoreExtraProperties
data class QuizResultItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val chapter: String = "",
    val difficulty: String = "متوسط",
    val totalQuestions: Int = 0,
    val mcqCount: Int = 0,
    val essayCount: Int = 0,
    val correctAnswers: Int = 0,
    val totalScore: Double = 0.0,
    val maxPossibleScore: Double = 0.0,
    val scorePercentage: Double = 0.0,
    val aiEvaluationFeedback: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WeeklyEvaluationItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val weekIdentifier: String = "", // e.g. "2026-W45"
    val studyHoursCompleted: Double = 0.0,
    val studyHoursTarget: Double = 25.0,
    val tasksFinished: Int = 0,
    val tasksTotal: Int = 0,
    val quizzesTaken: Int = 0,
    val averageQuizScore: Double = 0.0,
    val khatmaPagesRead: Int = 0,
    val selfRating: Int = 4, // 1 to 5 stars
    val reflectionNotes: String = "",
    val aiAdvice: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

