package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StudyRepository
import com.example.util.StudyTimerForegroundService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TimerMode(val title: String, val defaultMinutes: Int) {
    POMODORO("جلسة تركيز", 25),
    SHORT_BREAK("استراحة قصيرة", 5),
    LONG_BREAK("استراحة طويلة", 15),
    CUSTOM("مخصص", 45)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTimerScreen(
    studyRepository: StudyRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedMode by remember { mutableStateOf(TimerMode.POMODORO) }
    var customMinutes by remember { mutableIntStateOf(45) }
    var totalSeconds by remember { mutableIntStateOf(25 * 60) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var completedSessions by remember { mutableIntStateOf(0) }

    // Smart Notifications Preferences State
    var notifyStudyReminders by remember { mutableStateOf(true) }
    var notifyPrayerReminders by remember { mutableStateOf(true) }
    var notifyTaskReminders by remember { mutableStateOf(true) }

    // Notification Permission Launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission handled
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        // Sync with foreground service state if already running
        if (StudyTimerForegroundService.isRunningState) {
            isRunning = true
            remainingSeconds = StudyTimerForegroundService.remainingSecondsState
        }
    }

    // Timer countdown loop
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            completedSessions++

            // Haptic vibration
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(800)
                }
            } catch (e: Exception) {
                // Ignore vibration failure
            }

            // Record study minutes
            if (selectedMode == TimerMode.POMODORO || selectedMode == TimerMode.CUSTOM) {
                val mins = totalSeconds / 60
                coroutineScope.launch {
                    studyRepository.addStudyMinutes(mins)
                }
            }
        }
    }

    fun setMode(mode: TimerMode) {
        if (isRunning) {
            StudyTimerForegroundService.stopTimer(context)
        }
        selectedMode = mode
        val mins = if (mode == TimerMode.CUSTOM) customMinutes else mode.defaultMinutes
        totalSeconds = mins * 60
        remainingSeconds = totalSeconds
        isRunning = false
    }

    val progress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f,
        label = "TimerProgress"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "المؤقت الدراسي وجلسات المذاكرة",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modes selection chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (mode in TimerMode.entries) {
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { setMode(mode) },
                        label = { Text(mode.title, fontSize = 12.sp) }
                    )
                }
            }

            if (selectedMode == TimerMode.CUSTOM) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("المدة: $customMinutes دقيقة", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Slider(
                        value = customMinutes.toFloat(),
                        onValueChange = {
                            customMinutes = it.toInt()
                            if (!isRunning) {
                                totalSeconds = customMinutes * 60
                                remainingSeconds = totalSeconds
                            }
                        },
                        valueRange = 5f..120f,
                        steps = 22,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Circular Countdown Dial
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )
                // Active Progress Ring
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (selectedMode == TimerMode.POMODORO || selectedMode == TimerMode.CUSTOM) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color(0xFF10B981) // Green for break
                    },
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )

                // Timer Digital Time & Status
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedMode.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    if (isRunning) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ظاهر في شريط الإشعارات ✓",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Control Buttons (Start / Pause / Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = totalSeconds
                        StudyTimerForegroundService.stopTimer(context)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط")
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Play / Pause Main Button
                Button(
                    onClick = {
                        if (isRunning) {
                            isRunning = false
                            StudyTimerForegroundService.pauseTimer(context)
                        } else {
                            isRunning = true
                            StudyTimerForegroundService.startTimer(context, remainingSeconds, selectedMode.title)
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(76.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "إيقاف مؤقت" else "بدء المذاكرة",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Fast Forward 5 min
                OutlinedButton(
                    onClick = {
                        remainingSeconds = (remainingSeconds + 5 * 60).coerceAtMost(180 * 60)
                        totalSeconds = maxOf(totalSeconds, remainingSeconds)
                        if (isRunning) {
                            StudyTimerForegroundService.startTimer(context, remainingSeconds, selectedMode.title)
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+5 د", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sessions Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الجلسات المكتملة", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text("$completedSessions", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("إجمالي ساعات التركيز", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        val totalMins = completedSessions * 25
                        val h = totalMins / 60
                        val m = totalMins % 60
                        Text("${h}س ${m}د", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Notifications Settings Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نظام الإشعارات والتنبيهات الذكية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تنبيهات استمرار مؤقت المذاكرة في شريط الحالة", fontSize = 13.sp)
                        Switch(
                            checked = notifyStudyReminders,
                            onCheckedChange = { notifyStudyReminders = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تذكير أوقات الصلاة في مصر", fontSize = 13.sp)
                        Switch(
                            checked = notifyPrayerReminders,
                            onCheckedChange = { notifyPrayerReminders = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تذكير المهام اليومية المتأخرة", fontSize = 13.sp)
                        Switch(
                            checked = notifyTaskReminders,
                            onCheckedChange = { notifyTaskReminders = it }
                        )
                    }
                }
            }
        }
    }
}
