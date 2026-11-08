package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ChannelCommentItem
import com.example.data.model.ChannelPostItem
import com.example.data.repository.ChannelRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    channelRepository: ChannelRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val posts by channelRepository.observePosts().collectAsStateWithLifecycle(initialValue = emptyList())
    val isOwner = channelRepository.isAppOwner

    var showNewPostDialog by remember { mutableStateOf(false) }
    var activeCommentsPostId by remember { mutableStateOf<String?>(null) }

    // New Post Form
    var postTitle by remember { mutableStateOf("") }
    var postContent by remember { mutableStateOf("") }
    var postMediaType by remember { mutableStateOf("text") } // text, image, file
    var postMediaUrl by remember { mutableStateOf("") }
    var postFileName by remember { mutableStateOf("") }

    if (showNewPostDialog) {
        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            title = { Text("نشر إعلان / ملف جديد بالقناة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("عنوان المنشور") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text("نص الرسالة أو محتوى الإعلان") },
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("نوع المرفق:", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = postMediaType == "text",
                                onClick = { postMediaType = "text" },
                                label = { Text("نص فقط", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = postMediaType == "image",
                                onClick = { postMediaType = "image" },
                                label = { Text("صورة", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = postMediaType == "file",
                                onClick = { postMediaType = "file" },
                                label = { Text("ملف PDF", fontSize = 11.sp) }
                            )
                        }
                    }
                    if (postMediaType != "text") {
                        OutlinedTextField(
                            value = postMediaUrl,
                            onValueChange = { postMediaUrl = it },
                            label = { Text("رابط المرفق (URL مباشر أو تليجرام/Drive)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = postFileName,
                            onValueChange = { postFileName = it },
                            label = { Text("اسم الملف الظاهر للطلاب") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isNotBlank() && postContent.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    channelRepository.createPost(
                                        title = postTitle,
                                        content = postContent,
                                        mediaType = postMediaType,
                                        mediaUrl = postMediaUrl.ifBlank { null },
                                        fileName = postFileName.ifBlank { null }
                                    )
                                    showNewPostDialog = false
                                    postTitle = ""
                                    postContent = ""
                                    postMediaUrl = ""
                                    postFileName = ""
                                } catch (e: Exception) {
                                    // Handle
                                }
                            }
                        }
                    },
                    enabled = postTitle.isNotBlank() && postContent.isNotBlank()
                ) {
                    Text("نشر الآن بالقناة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPostDialog = false }) { Text("إلغاء") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("قناة التطبيق المدرسية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "موثقة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Text("المالك: mohamed373h1@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isOwner) {
                ExtendedFloatingActionButton(
                    onClick = { showNewPostDialog = true },
                    icon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                    text = { Text("نشر في القناة") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp)
        ) {
            // Telegram Support Banner
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Y_lI11"))
                    context.startActivity(intent)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0284C7))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("الدعم الفني وتواصل الطلاب على تليجرام", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("t.me/Y_lI11 • اضغط لفتح المحادثة المباشرة", fontSize = 11.sp, color = Color(0xFF0284C7))
                    }
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Posts Feed
            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد منشورات في القناة بعد", color = MaterialTheme.colorScheme.outline)
                        if (isOwner) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("أنت مالك القناة! اضغط زر النشر بالأسفل لمشاركة أول إعلان أو ملف", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(posts, key = { it.id }) { post ->
                        ChannelPostCard(
                            post = post,
                            isOwner = isOwner,
                            onReact = { reaction ->
                                coroutineScope.launch {
                                    channelRepository.toggleReaction(post.id, reaction)
                                }
                            },
                            onOpenComments = {
                                activeCommentsPostId = post.id
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    channelRepository.deletePost(post.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Comments Sheet
    if (activeCommentsPostId != null) {
        val postId = activeCommentsPostId!!
        val comments by channelRepository.observeComments(postId).collectAsStateWithLifecycle(initialValue = emptyList())
        var newCommentText by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { activeCommentsPostId = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "تعليقات ومناقشات الطلاب (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Text("كن أول من يعلق على هذا المنشور!", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        items(comments) { comment ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(comment.text, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("اكتب تعليقك أو استفسارك...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        channelRepository.addComment(postId, newCommentText)
                                        newCommentText = ""
                                    } catch (e: Exception) {
                                        // Handle
                                    }
                                }
                            }
                        },
                        enabled = newCommentText.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelPostCard(
    post: ChannelPostItem,
    isOwner: Boolean,
    onReact: (String) -> Unit,
    onOpenComments: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1).ifBlank { "م" },
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(post.authorEmail, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                if (isOwner) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المنشور", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Content
            Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)

            // Optional Image Attachment
            if (post.mediaType == "image" && !post.mediaUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "صورة مرفقة",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Optional File Attachment & Download Button
            if ((post.mediaType == "file" || post.mediaType == "image") && !post.mediaUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.mediaUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "تنزيل أو فتح المرفق: ${post.fileName ?: "الملف المرفق"}", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Interactions Row (Reactions & Comments)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Reactions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val reactions = listOf("👍", "❤️", "👏", "🔥")
                    for (emoji in reactions) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { onReact(emoji) }
                        ) {
                            Text(
                                text = emoji,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                    if (post.likesCount > 0) {
                        Text(
                            text = "${post.likesCount} تفاعل",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp)
                        )
                    }
                }

                // Comments Button
                TextButton(onClick = onOpenComments) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("التعليقات (${post.commentsCount})", fontSize = 12.sp)
                }
            }
        }
    }
}
