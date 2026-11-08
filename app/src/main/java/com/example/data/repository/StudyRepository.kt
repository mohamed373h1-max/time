package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.KhatmaData
import com.example.data.model.ScheduleDayItem
import com.example.data.model.StudyNote
import com.example.data.model.TaskItem
import com.example.data.model.UserProfileData
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class StudyRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    val isAppOwner: Boolean
        get() = currentUserEmail.equals("mohamed373h1@gmail.com", ignoreCase = true)

    // User Profile
    suspend fun syncUserProfile() {
        val user = auth.currentUser ?: return
        val docRef = db.collection("users").document(user.uid)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            val profile = mapOf(
                "userId" to user.uid,
                "email" to (user.email ?: ""),
                "displayName" to (user.displayName ?: "طالب متميز"),
                "photoUrl" to user.photoUrl?.toString(),
                "totalStudyMinutes" to 0,
                "tasksCompleted" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "lastActive" to FieldValue.serverTimestamp()
            )
            docRef.set(profile).await()
        } else {
            docRef.update("lastActive", FieldValue.serverTimestamp()).await()
        }
    }

    suspend fun addStudyMinutes(minutes: Int) {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).update(
            "totalStudyMinutes", FieldValue.increment(minutes.toLong()),
            "lastActive", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun incrementTasksCompleted() {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).update(
            "tasksCompleted", FieldValue.increment(1),
            "lastActive", FieldValue.serverTimestamp()
        ).await()
    }

    // Tasks
    fun observeTasks(): Flow<List<TaskItem>> {
        val uid = currentUserId ?: return flowOf(emptyList())
        return db.collection("users").document(uid).collection("tasks")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(TaskItem::class.java) }
    }

    suspend fun addTask(title: String, subject: String, priority: String, dueDate: String): String {
        val uid = currentUserId ?: throw IllegalStateException("يجب تسجيل الدخول لحفظ المهمة")
        val docRef = db.collection("users").document(uid).collection("tasks").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "subject" to subject,
            "priority" to priority,
            "dueDate" to dueDate,
            "isCompleted" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun toggleTaskCompleted(taskId: String, isCompleted: Boolean) {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).collection("tasks").document(taskId).update(
            "isCompleted", isCompleted,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
        if (isCompleted) {
            incrementTasksCompleted()
        }
    }

    suspend fun deleteTask(taskId: String) {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).collection("tasks").document(taskId).delete().await()
    }

    // Notes
    fun observeNotes(): Flow<List<StudyNote>> {
        val uid = currentUserId ?: return flowOf(emptyList())
        return db.collection("users").document(uid).collection("notes")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(StudyNote::class.java) }
    }

    suspend fun addNote(title: String, content: String, subject: String, isPinned: Boolean = false): String {
        val uid = currentUserId ?: throw IllegalStateException("يجب تسجيل الدخول لحفظ الملاحظة")
        val docRef = db.collection("users").document(uid).collection("notes").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "content" to content,
            "subject" to subject,
            "isPinned" to isPinned,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun updateNote(noteId: String, title: String, content: String, subject: String, isPinned: Boolean) {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).collection("notes").document(noteId).update(
            "title", title,
            "content", content,
            "subject", subject,
            "isPinned", isPinned,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun deleteNote(noteId: String) {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).collection("notes").document(noteId).delete().await()
    }

    // Schedule
    fun observeSchedule(): Flow<List<ScheduleDayItem>> {
        val uid = currentUserId ?: return flowOf(emptyList())
        return db.collection("users").document(uid).collection("schedule")
            .snapshots()
            .map { snap -> snap.toObjects(ScheduleDayItem::class.java) }
    }

    suspend fun saveScheduleDay(dayId: String, dayName: String, periodsJson: String) {
        val uid = currentUserId ?: return
        val docRef = db.collection("users").document(uid).collection("schedule").document(dayId)
        val payload = mapOf(
            "dayId" to dayId,
            "dayName" to dayName,
            "periodsJson" to periodsJson,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    // Khatma Tracking
    fun observeKhatma(): Flow<KhatmaData?> {
        val uid = currentUserId ?: return flowOf(null)
        return db.collection("users").document(uid).collection("khatma").document("default")
            .snapshots()
            .map { snap -> snap.toObject(KhatmaData::class.java) }
    }

    suspend fun updateKhatma(currentPage: Int, currentJuz: Int, pagesReadToday: Int, dailyTarget: Int) {
        val uid = currentUserId ?: return
        val docRef = db.collection("users").document(uid).collection("khatma").document("default")
        val payload = mapOf(
            "userId" to uid,
            "currentPage" to currentPage,
            "currentJuz" to currentJuz,
            "dailyPagesTarget" to dailyTarget,
            "pagesReadToday" to pagesReadToday,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    // Dictionary Favorites
    fun observeDictionaryFavorites(): Flow<List<com.example.data.model.DictionaryFavoriteItem>> {
        val uid = currentUserId ?: return flowOf(emptyList())
        return db.collection("users").document(uid).collection("dictionary_favorites")
            .snapshots()
            .map { snap -> snap.toObjects(com.example.data.model.DictionaryFavoriteItem::class.java) }
    }

    suspend fun addDictionaryFavorite(entry: com.example.data.DictionaryEntry) {
        val uid = currentUserId ?: return
        val docId = entry.wordEn.replace("[^a-zA-Z0-9]".toRegex(), "_").lowercase().ifEmpty { "item_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("dictionary_favorites").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "wordEn" to entry.wordEn,
            "wordAr" to entry.wordAr,
            "category" to entry.category,
            "definitionAr" to entry.definitionAr,
            "exampleEn" to entry.exampleEn,
            "exampleAr" to entry.exampleAr,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun removeDictionaryFavorite(wordEn: String) {
        val uid = currentUserId ?: return
        val docId = wordEn.replace("[^a-zA-Z0-9]".toRegex(), "_").lowercase().ifEmpty { wordEn }
        db.collection("users").document(uid).collection("dictionary_favorites").document(docId).delete().await()
    }

    // 3rd Secondary Quiz Results
    fun observeQuizResults(): Flow<List<com.example.data.model.QuizResultItem>> {
        val uid = currentUserId ?: return flowOf(emptyList())
        return db.collection("users").document(uid).collection("quiz_results")
            .snapshots()
            .map { snap -> snap.toObjects(com.example.data.model.QuizResultItem::class.java) }
    }

    suspend fun saveQuizResult(result: com.example.data.model.QuizResultItem) {
        val uid = currentUserId ?: return
        val docRef = db.collection("users").document(uid).collection("quiz_results").document()
        val payload = mapOf(
            "userId" to uid,
            "subject" to result.subject,
            "chapter" to result.chapter,
            "difficulty" to result.difficulty,
            "totalQuestions" to result.totalQuestions,
            "mcqCount" to result.mcqCount,
            "essayCount" to result.essayCount,
            "correctAnswers" to result.correctAnswers,
            "totalScore" to result.totalScore,
            "maxPossibleScore" to result.maxPossibleScore,
            "scorePercentage" to result.scorePercentage,
            "aiEvaluationFeedback" to result.aiEvaluationFeedback,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    // Weekly Evaluation
    fun observeWeeklyEvaluation(weekId: String): Flow<com.example.data.model.WeeklyEvaluationItem?> {
        val uid = currentUserId ?: return flowOf(null)
        val safeDocId = weekId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        return db.collection("users").document(uid).collection("weekly_evaluations").document(safeDocId)
            .snapshots()
            .map { snap -> snap.toObject(com.example.data.model.WeeklyEvaluationItem::class.java) }
    }

    suspend fun saveWeeklyEvaluation(item: com.example.data.model.WeeklyEvaluationItem) {
        val uid = currentUserId ?: return
        val safeDocId = item.weekIdentifier.replace("[^a-zA-Z0-9_-]".toRegex(), "_").ifEmpty { "current_week" }
        val docRef = db.collection("users").document(uid).collection("weekly_evaluations").document(safeDocId)
        val payload = mapOf(
            "userId" to uid,
            "weekIdentifier" to item.weekIdentifier,
            "studyHoursCompleted" to item.studyHoursCompleted,
            "studyHoursTarget" to item.studyHoursTarget,
            "tasksFinished" to item.tasksFinished,
            "tasksTotal" to item.tasksTotal,
            "quizzesTaken" to item.quizzesTaken,
            "averageQuizScore" to item.averageQuizScore,
            "khatmaPagesRead" to item.khatmaPagesRead,
            "selfRating" to item.selfRating,
            "reflectionNotes" to item.reflectionNotes,
            "aiAdvice" to item.aiAdvice,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
    }
}
