package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StudyNote
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    studyRepository: StudyRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val notes by studyRepository.observeNotes().collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog form state
    var editNoteId by remember { mutableStateOf<String?>(null) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteSubject by remember { mutableStateOf("عام") }

    val filteredNotes = remember(notes, searchQuery) {
        val q = searchQuery.trim().lowercase()
        val list = if (q.isBlank()) notes else notes.filter {
            it.title.lowercase().contains(q) ||
            it.content.lowercase().contains(q) ||
            it.subject.lowercase().contains(q)
        }
        list.sortedByDescending { it.isPinned }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editNoteId = null
            },
            title = { Text(if (editNoteId != null) "تعديل الملاحظة" else "ملاحظة دراسية جديدة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("عنوان الملاحظة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = noteSubject,
                        onValueChange = { noteSubject = it },
                        label = { Text("المادة الدراسية") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("محتوى الملاحظة أو التلخيص") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    if (editNoteId != null) {
                                        studyRepository.updateNote(
                                            noteId = editNoteId!!,
                                            title = noteTitle,
                                            content = noteContent,
                                            subject = noteSubject.ifBlank { "عام" },
                                            isPinned = false
                                        )
                                    } else {
                                        studyRepository.addNote(
                                            title = noteTitle,
                                            content = noteContent,
                                            subject = noteSubject.ifBlank { "عام" }
                                        )
                                    }
                                    showAddDialog = false
                                    editNoteId = null
                                    noteTitle = ""
                                    noteContent = ""
                                } catch (e: Exception) {
                                    // Handle
                                }
                            }
                        }
                    },
                    enabled = noteTitle.isNotBlank() && noteContent.isNotBlank()
                ) {
                    Text(if (editNoteId != null) "حفظ التعديل" else "إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editNoteId = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الملاحظات والملخصات الدراسية",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editNoteId = null
                    noteTitle = ""
                    noteContent = ""
                    noteSubject = "عام"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة ملاحظة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث في عناوين ومحتوى الملاحظات...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NoteAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "لا توجد ملاحظات تطابق البحث" else "لا توجد ملاحظات بعد. اضغط + لإضافة أول ملاحظة",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onPinToggle = {
                                coroutineScope.launch {
                                    studyRepository.updateNote(
                                        noteId = note.id,
                                        title = note.title,
                                        content = note.content,
                                        subject = note.subject,
                                        isPinned = !note.isPinned
                                    )
                                }
                            },
                            onEdit = {
                                editNoteId = note.id
                                noteTitle = note.title
                                noteContent = note.content
                                noteSubject = note.subject
                                showAddDialog = true
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    studyRepository.deleteNote(note.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: StudyNote,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onPinToggle) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "تثبيت الملاحظة",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            SuggestionChip(
                onClick = {},
                label = { Text(note.subject, fontSize = 11.sp) },
                modifier = Modifier.height(26.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
