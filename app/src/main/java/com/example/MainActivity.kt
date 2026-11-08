package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.credentials.CredentialManager
import com.example.data.repository.ChannelRepository
import com.example.data.repository.StudyRepository
import com.example.ui.components.attemptAutoSignIn
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App Check must be configured before any UI or Firebase operations
        configureAppCheck(this, intent)

        // Create timer notification channel
        NotificationHelper.createNotificationChannel(this)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppRoot()
                    }
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val studyRepository = remember { StudyRepository(context) }
    val channelRepository = remember { ChannelRepository(context) }

    var currentScreen by remember { mutableStateOf("home") }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    // Observe auth state changes
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                coroutineScope.launch {
                    studyRepository.syncUserProfile()
                }
            }
        }
        Firebase.auth.addAuthStateListener(listener)

        // Silent auto sign-in attempt on startup
        val credentialManager = CredentialManager.create(context)
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            },
            onUnauthenticated = {},
            scope = coroutineScope
        )

        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Handle back button on sub-screens
    if (currentScreen != "home") {
        BackHandler {
            currentScreen = "home"
        }
    }

    when (currentScreen) {
        "home" -> HomeScreen(
            studyRepository = studyRepository,
            onNavigateToRoute = { route -> currentScreen = route }
        )
        "pdf" -> PdfViewerScreen(
            onNavigateBack = { currentScreen = "home" }
        )
        "whiteboard" -> WhiteboardScreen(
            onNavigateBack = { currentScreen = "home" }
        )
        "youtube" -> StudyYouTubeScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "tasks" -> TasksScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "notes" -> NotesScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "timer" -> StudyTimerScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "schedule" -> ScheduleScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "calculator" -> CalculatorScreen(
            onNavigateBack = { currentScreen = "home" }
        )
        "islamic" -> IslamicScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "ai" -> AiStudyScreen(
            onNavigateBack = { currentScreen = "home" }
        )
        "dictionary" -> DictionaryScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "thanaweya" -> ThanaweyaScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "evaluation" -> WeeklyEvaluationScreen(
            studyRepository = studyRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "channel" -> ChannelScreen(
            channelRepository = channelRepository,
            onNavigateBack = { currentScreen = "home" }
        )
        "dashboard" -> OwnerDashboardScreen(
            channelRepository = channelRepository,
            onNavigateBack = { currentScreen = "home" },
            onNavigateToChannel = { currentScreen = "channel" }
        )
    }
}
