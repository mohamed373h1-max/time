package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChannelCommentItem
import com.example.data.model.ChannelPostItem
import com.example.data.model.ChannelReactionItem
import com.example.data.model.UserProfileData
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ChannelRepository(private val db: FirebaseFirestore) {

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

    // Observe channel broadcast posts
    fun observePosts(): Flow<List<ChannelPostItem>> {
        return db.collection("channel_posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(ChannelPostItem::class.java) }
    }

    // Owner creates a post
    suspend fun createPost(
        title: String,
        content: String,
        mediaType: String = "text",
        mediaUrl: String? = null,
        fileName: String? = null
    ): String {
        val email = currentUserEmail ?: throw IllegalStateException("يجب تسجيل الدخول بالبريد المعتمد")
        if (!isAppOwner) {
            throw IllegalStateException("فقط مالك التطبيق ($email) يمكنه النشر في القناة")
        }

        val docRef = db.collection("channel_posts").document()
        val payload = mutableMapOf<String, Any>(
            "authorEmail" to email,
            "authorName" to (auth.currentUser?.displayName ?: "إدارة التطبيق"),
            "title" to title,
            "content" to content,
            "mediaType" to mediaType,
            "likesCount" to 0,
            "commentsCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!mediaUrl.isNullOrBlank()) payload["mediaUrl"] = mediaUrl
        if (!fileName.isNullOrBlank()) payload["fileName"] = fileName

        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun deletePost(postId: String) {
        if (!isAppOwner) throw IllegalStateException("فقط مالك التطبيق يمكنه الحذف")
        db.collection("channel_posts").document(postId).delete().await()
    }

    // Reactions
    suspend fun toggleReaction(postId: String, reactionType: String) {
        val uid = currentUserId ?: return
        val reactionRef = db.collection("channel_posts").document(postId)
            .collection("reactions").document(uid)

        val existing = reactionRef.get().await()
        if (existing.exists()) {
            val prevType = existing.getString("reactionType")
            if (prevType == reactionType) {
                // Remove reaction
                reactionRef.delete().await()
                db.collection("channel_posts").document(postId).update(
                    "likesCount", FieldValue.increment(-1)
                ).await()
                return
            }
        } else {
            db.collection("channel_posts").document(postId).update(
                "likesCount", FieldValue.increment(1)
            ).await()
        }

        val payload = mapOf(
            "postId" to postId,
            "userId" to uid,
            "reactionType" to reactionType,
            "createdAt" to FieldValue.serverTimestamp()
        )
        reactionRef.set(payload).await()
    }

    fun observeComments(postId: String): Flow<List<ChannelCommentItem>> {
        return db.collection("channel_posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(ChannelCommentItem::class.java) }
    }

    suspend fun addComment(postId: String, text: String) {
        val user = auth.currentUser ?: throw IllegalStateException("يجب تسجيل الدخول للتعليق")
        val commentRef = db.collection("channel_posts").document(postId).collection("comments").document()
        val payload = mutableMapOf<String, Any>(
            "postId" to postId,
            "userId" to user.uid,
            "userName" to (user.displayName ?: "طالب"),
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp()
        )
        user.photoUrl?.let { payload["userPhoto"] = it.toString() }

        commentRef.set(payload).await()
        db.collection("channel_posts").document(postId).update(
            "commentsCount", FieldValue.increment(1)
        ).await()
    }

    // Owner statistics
    suspend fun getAppUsersCount(): Int {
        if (!isAppOwner) return 0
        return try {
            val countSnapshot = db.collection("users").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            countSnapshot.count.toInt()
        } catch (e: Exception) {
            val list = db.collection("users").get().await()
            list.size()
        }
    }

    suspend fun getAppTotalStudyHours(): Long {
        if (!isAppOwner) return 0
        return try {
            val users = db.collection("users").get().await().toObjects(UserProfileData::class.java)
            val totalMinutes = users.sumOf { it.totalStudyMinutes.toLong() }
            totalMinutes / 60
        } catch (e: Exception) {
            0
        }
    }
}
