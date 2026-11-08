package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.repository.StudyRepository
import com.example.ui.components.GoogleSignInButton
import com.example.ui.components.QuickCalculatorDialog
import com.example.util.PrayerTimesCalculator
import com.example.util.StudyTimerForegroundService
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

data class HomeToolCard(
    val route: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    studyRepository: StudyRepository,
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val isOwner = studyRepository.isAppOwner
    var showQuickCalculator by remember { mutableStateOf(false) }

    val nextPrayerInfo = remember {
        val cairo = PrayerTimesCalculator.egyptianGovernorates[0]
        PrayerTimesCalculator.calculateTimes(cairo)
    }

    if (showQuickCalculator) {
        QuickCalculatorDialog(onDismissRequest = { showQuickCalculator = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.rafiqi_mascot),
                            contentDescription = "شعار رفيقي الدراسي",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "رفيقي الدراسي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "منصتك المتكاملة للتفوق والنجاح",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    // Quick Calculator Button
                    FilledTonalIconButton(onClick = { showQuickCalculator = true }) {
                        Icon(Icons.Default.Calculate, contentDescription = "آلة حاسبة سريعة", tint = MaterialTheme.colorScheme.primary)
                    }
                    // Telegram Support Direct Action
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Y_lI11"))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = "الدعم الفني", tint = Color(0xFF0284C7))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Mascot Welcome Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cute Mascot Avatar
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.rafiqi_mascot),
                                    contentDescription = "رفيقي الدراسي",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().padding(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Greeting Speech Bubble
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "أهلاً بك، ${currentUser?.displayName?.split(" ")?.firstOrNull() ?: "يا بطل"}! 🎓",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "أنا رفيقك الدراسي الذكي، جاهز للمذاكرة وحل المسائل وتنظيم يومك!",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Account status / Login button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentUser != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("نشاطك وتقدمك محفوظ سحابياً", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                }
                            } else {
                                Text("سجل بحساب Google لحفظ تقدمك الدراسي", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                GoogleSignInButton(
                                    onAuthSuccess = {},
                                    onAuthError = {}
                                )
                            }
                        }
                    }
                }
            }

            // 2. Smart Status Banner: Next Prayer & Active Timer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Next Prayer Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRoute("islamic") }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("صلاة ${nextPrayerInfo.nextPrayerName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(nextPrayerInfo.nextPrayerTime, fontSize = 13.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Study Timer Card
                    val isTimerActive = StudyTimerForegroundService.isRunningState
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTimerActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRoute("timer") }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(if (isTimerActive) "المؤقت يعمل ⏱️" else "مؤقت المذاكرة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("ابدأ جلسة بومودورو", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            // 3. Section: منظومة الثانوية العامة والتقييم الذكي
            item {
                SectionHeader("منظومة الثانوية العامة والتقييم الذكي", "بنك أسئلة علمي علوم وتصحيح ذكي للمقالي وتقييم أسبوعي")
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LargeToolBannerCard(
                        title = "بنك أسئلة ٣ ثانوي (علمي علوم)",
                        description = "٥٠٪ اختياري و ٥٠٪ مقالي بتصحيح الذكاء الاصطناعي لكافة المواد (عربي، إنجليزي، فيزياء، كيمياء، أحياء، جيولوجيا) من كتب الوزارة والامتحان والتفوق",
                        icon = Icons.Default.Quiz,
                        color = Color(0xFF4F46E5),
                        badge = "٥٠٪ مقالي + اختياري",
                        onClick = { onNavigateToRoute("thanaweya") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MediumToolCard(
                            title = "التقييم الأسبوعي الذكي",
                            description = "مؤشرات ساعات المذاكرة وتحليل الأداء والدرجات",
                            icon = Icons.Default.Insights,
                            color = Color(0xFF0D9488),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("evaluation") }
                        )
                        MediumToolCard(
                            title = "المساعد الذكي (Turbo AI)",
                            description = "حل مسائل معقدة وشرح المناهج وتصحيح فوري",
                            icon = Icons.Default.Bolt,
                            color = Color(0xFF9333EA),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("ai") }
                        )
                    }
                }
            }

            // 4. Section: المنصة القرآنية والإسلامية المتكاملة
            item {
                SectionHeader("المنصة القرآنية والإسلامية", "رسم عثماني، تفسير ميسر، تلاوة بالخلفية للشيخ ياسر الدوسري، ومواقيت الصلاة")
            }

            item {
                LargeToolBannerCard(
                    title = "المصحف الشريف والتلاوة بالخلفية",
                    description = "قراءة بالرسم العثماني وتفسير الآيات، تلاوة الشيخ ياسر الدوسري والحصري في شريط الإشعارات، ختمة القرآن، والأذكار",
                    icon = Icons.Default.AutoStories,
                    color = Color(0xFF15803D),
                    badge = "رسم عثماني وتفسير",
                    onClick = { onNavigateToRoute("islamic") }
                )
            }

            // 5. Section: القاموس الأكاديمي والمحررات الذكية
            item {
                SectionHeader("المعجم العلمي والمحررات الدراسية", "أدوات متطورة لحل التمارين وقراءة الملفات والمصطلحات العلمية")
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MediumToolCard(
                            title = "القاموس الأكاديمي",
                            description = "مصطلحات علمية مع مفضلة سحابية ونطق صوتي",
                            icon = Icons.Default.Translate,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("dictionary") }
                        )
                        MediumToolCard(
                            title = "قارئ ومحرر PDF",
                            description = "أقلام كتابة حرة وتظليل وممحاة دقيقة بلا حدود",
                            icon = Icons.Default.PictureAsPdf,
                            color = Color(0xFFE11D48),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("pdf") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MediumToolCard(
                            title = "السبورة الذكية",
                            description = "سبورة بيضاء وسوداء ومسطرة ورسم بياني",
                            icon = Icons.Default.Draw,
                            color = Color(0xFF0D9488),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("whiteboard") }
                        )
                        MediumToolCard(
                            title = "يوتيوب الدراسي",
                            description = "مشاهدة الشروحات بدون تشتت مع دفتر ملاحظات",
                            icon = Icons.Default.SmartDisplay,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("youtube") }
                        )
                    }
                }
            }

            // 6. Section: التنظيم والإنجاز اليومي والحسابات
            item {
                SectionHeader("التنظيم والإنجاز والحسابات", "نظم وقتك وواجباتك لتحقيق أعلى الدرجات")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MediumToolCard(
                        title = "المهام اليومية",
                        description = "متابعة الواجبات ونسبة الإنجاز",
                        icon = Icons.Default.CheckCircleOutline,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("tasks") }
                    )
                    MediumToolCard(
                        title = "الملاحظات والملخصات",
                        description = "دفتر التلخيص والبحث السريع",
                        icon = Icons.Default.NoteAlt,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("notes") }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MediumToolCard(
                        title = "المؤقت الدراسي",
                        description = "بومودورو مع إشعارات الحالة",
                        icon = Icons.Default.HourglassTop,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("timer") }
                    )
                    MediumToolCard(
                        title = "الجدول الأسبوعي",
                        description = "جدول الحصص والمحاضرات",
                        icon = Icons.Default.DateRange,
                        color = Color(0xFF059669),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("schedule") }
                    )
                }
            }

            item {
                LargeToolBannerCard(
                    title = "الآلة الحاسبة العلمية المتطورة",
                    description = "حساب الدوال المثلثية، اللوغاريتمات، الجذور والأسس، ونافذة عائمة سريعة",
                    icon = Icons.Default.Calculate,
                    color = Color(0xFF4F46E5),
                    badge = "علمية متطورة",
                    onClick = { onNavigateToRoute("calculator") }
                )
            }

            // 7. Section: المجتمع والدعم الفني
            item {
                SectionHeader("المجتمع والدعم الفني", "قناة الملازم والتواصل مع المالك والدعم")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MediumToolCard(
                        title = "قناة رفيقي الدراسي",
                        description = "ملازم وإعلانات وتفاعل الطلاب",
                        icon = Icons.Default.Campaign,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("channel") }
                    )
                    MediumToolCard(
                        title = "الدعم الفني المباشر",
                        description = "تواصل عبر تليجرام t.me/Y_lI11",
                        icon = Icons.Default.SupportAgent,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Y_lI11"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Owner Dashboard button if owner
            if (isOwner) {
                item {
                    LargeToolBannerCard(
                        title = "لوحة تحكم مالك التطبيق",
                        description = "إحصائيات المنصة وإجمالي عدد الطلاب المسجلين وإدارة القناة",
                        icon = Icons.Default.AdminPanelSettings,
                        color = Color(0xFFB45309),
                        badge = "المالك",
                        onClick = { onNavigateToRoute("dashboard") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun LargeToolBannerCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(color.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(badge, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 17.sp,
                    maxLines = 2
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun MediumToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
            .height(136.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
