package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SchedulePeriod
import com.example.data.repository.StudyRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import java.util.Calendar

data class WeekDayInfo(val id: String, val nameAr: String, val dayOfWeekNumber: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    studyRepository: StudyRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scheduleItems by studyRepository.observeSchedule().collectAsStateWithLifecycle(initialValue = emptyList())

    val days = listOf(
        WeekDayInfo("sat", "السبت", Calendar.SATURDAY),
        WeekDayInfo("sun", "الأحد", Calendar.SUNDAY),
        WeekDayInfo("mon", "الإثنين", Calendar.MONDAY),
        WeekDayInfo("tue", "الثلاثاء", Calendar.TUESDAY),
        WeekDayInfo("wed", "الأربعاء", Calendar.WEDNESDAY),
        WeekDayInfo("thu", "الخميس", Calendar.THURSDAY),
        WeekDayInfo("fri", "الجمعة", Calendar.FRIDAY)
    )

    // Current day detection
    val todayCal = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val defaultDay = days.firstOrNull { it.dayOfWeekNumber == todayCal } ?: days[0]
    var selectedDay by remember { mutableStateOf(defaultDay) }

    val moshi = remember { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }
    val type = Types.newParameterizedType(List::class.java, SchedulePeriod::class.java)
    val adapter = moshi.adapter<List<SchedulePeriod>>(type)

    // Parse periods for currently selected day
    val dayItem = scheduleItems.firstOrNull { it.dayId == selectedDay.id }
    val periods = remember(dayItem) {
        if (dayItem != null && dayItem.periodsJson.isNotBlank()) {
            try {
                adapter.fromJson(dayItem.periodsJson) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Add period dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var periodSubject by remember { mutableStateOf("") }
    var periodTeacher by remember { mutableStateOf("") }
    var periodTime by remember { mutableStateOf("") }
    var periodRoom by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة حصة/محاضرة ليوم ${selectedDay.nameAr}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = periodSubject,
                        onValueChange = { periodSubject = it },
                        label = { Text("اسم المادة (مثال: رياضيات)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = periodTeacher,
                        onValueChange = { periodTeacher = it },
                        label = { Text("المعلم / الأستاذ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = periodTime,
                        onValueChange = { periodTime = it },
                        label = { Text("الوقت (مثال: 08:30 ص - 10:00 ص)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = periodRoom,
                        onValueChange = { periodRoom = it },
                        label = { Text("الفصل / القاعة (مثال: مدرج أ)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (periodSubject.isNotBlank()) {
                            val newPeriod = SchedulePeriod(
                                periodNumber = periods.size + 1,
                                subject = periodSubject,
                                teacher = periodTeacher,
                                time = periodTime,
                                room = periodRoom
                            )
                            val updatedList = periods + newPeriod
                            val json = adapter.toJson(updatedList)
                            coroutineScope.launch {
                                studyRepository.saveScheduleDay(selectedDay.id, selectedDay.nameAr, json)
                            }
                            periodSubject = ""
                            periodTeacher = ""
                            periodTime = ""
                            periodRoom = ""
                            showAddDialog = false
                        }
                    },
                    enabled = periodSubject.isNotBlank()
                ) {
                    Text("إضافة الحصة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
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
                        text = "الجدول الأسبوعي الدراسي",
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
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة حصة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Days of the week row selector
            ScrollableTabRow(
                selectedTabIndex = days.indexOf(selectedDay),
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                days.forEach { day ->
                    val isToday = day.dayOfWeekNumber == todayCal
                    Tab(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.nameAr,
                                    fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isToday) {
                                    Text(
                                        text = "اليوم",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day header card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "جدول حصص يوم ${selectedDay.nameAr}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${periods.size} حصص مجدولة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة حصة", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Periods List
            if (periods.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الجدول فارغ ليوم ${selectedDay.nameAr}. اضغط + لملء الحصص والمواعيد",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(periods) { period ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Period Number Badge
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${period.periodNumber}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = period.subject,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        if (period.time.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(period.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                        if (period.room.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Room, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(period.room, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                    }
                                    if (period.teacher.isNotBlank()) {
                                        Text(
                                            text = "المعلم: ${period.teacher}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val updated = periods.filter { it != period }.mapIndexed { index, item ->
                                            item.copy(periodNumber = index + 1)
                                        }
                                        val json = adapter.toJson(updated)
                                        coroutineScope.launch {
                                            studyRepository.saveScheduleDay(selectedDay.id, selectedDay.nameAr, json)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف الحصة", tint = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
