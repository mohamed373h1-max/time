package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QuickCalculatorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

enum class PdfToolMode {
    PAN_ZOOM,
    PEN,
    HIGHLIGHTER,
    ERASER
}

data class PdfStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(1) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Tools state
    var selectedTool by remember { mutableStateOf(PdfToolMode.PEN) }
    var selectedColor by remember { mutableStateOf(Color(0xFF1976D2)) }
    var strokeWidth by remember { mutableFloatStateOf(4f) }
    var showCalculator by remember { mutableStateOf(false) }

    // Annotations mapped per page
    val pageStrokes = remember { mutableStateMapOf<Int, MutableList<PdfStroke>>() }
    var currentDrawingPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    // Zoom and pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pdfUri = uri
            currentPageIndex = 0
            pageStrokes.clear()
        }
    }

    // Load initial sample PDF if none chosen
    LaunchedEffect(pdfUri) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val fileDescriptor: ParcelFileDescriptor? = if (pdfUri != null) {
                    context.contentResolver.openFileDescriptor(pdfUri!!, "r")
                } else {
                    // Create a sample educational cheat-sheet PDF
                    createSampleCheatSheetPdf(context)
                }

                if (fileDescriptor != null) {
                    val renderer = PdfRenderer(fileDescriptor)
                    totalPages = renderer.pageCount
                    val safePage = currentPageIndex.coerceIn(0, totalPages - 1)
                    val page = renderer.openPage(safePage)

                    // Render with high resolution
                    val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderer.close()
                    fileDescriptor.close()

                    withContext(Dispatchers.Main) {
                        currentBitmap = bmp
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    // Reload page when page index changes
    LaunchedEffect(currentPageIndex) {
        if (pdfUri != null || currentBitmap != null) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val fileDescriptor = if (pdfUri != null) {
                        context.contentResolver.openFileDescriptor(pdfUri!!, "r")
                    } else {
                        createSampleCheatSheetPdf(context)
                    }

                    fileDescriptor?.use { fd ->
                        PdfRenderer(fd).use { renderer ->
                            totalPages = renderer.pageCount
                            val safePage = currentPageIndex.coerceIn(0, totalPages - 1)
                            renderer.openPage(safePage).use { page ->
                                val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                withContext(Dispatchers.Main) {
                                    currentBitmap = bmp
                                    isLoading = false
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        }
    }

    if (showCalculator) {
        QuickCalculatorDialog(onDismissRequest = { showCalculator = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (pdfUri != null) "قارئ ومحرر PDF" else "ملخص دراسي نموذجي (PDF)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الصفحة ${currentPageIndex + 1} من $totalPages",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    // Open File Button (unlimited size)
                    FilledTonalButton(
                        onClick = { filePickerLauncher.launch(arrayOf("application/pdf")) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فتح PDF", fontSize = 12.sp)
                    }

                    // Quick Calculator Button
                    IconButton(onClick = { showCalculator = true }) {
                        Icon(Icons.Default.Calculate, contentDescription = "آلة حاسبة سريعة")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                    // Tools Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pan/Zoom Mode
                        FilterChip(
                            selected = selectedTool == PdfToolMode.PAN_ZOOM,
                            onClick = { selectedTool = PdfToolMode.PAN_ZOOM },
                            label = { Text("تحريك وتكبير", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.PanTool, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )

                        // Pen Mode
                        FilterChip(
                            selected = selectedTool == PdfToolMode.PEN,
                            onClick = { selectedTool = PdfToolMode.PEN },
                            label = { Text("قلم للكتابة", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )

                        // Highlighter Mode
                        FilterChip(
                            selected = selectedTool == PdfToolMode.HIGHLIGHTER,
                            onClick = { selectedTool = PdfToolMode.HIGHLIGHTER },
                            label = { Text("قلم تظليل شفاف", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.BorderColor, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )

                        // Eraser Mode
                        FilterChip(
                            selected = selectedTool == PdfToolMode.ERASER,
                            onClick = { selectedTool = PdfToolMode.ERASER },
                            label = { Text("ممحاة", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.AutoFixNormal, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    // Colors & Actions Row
                    if (selectedTool == PdfToolMode.PEN || selectedTool == PdfToolMode.HIGHLIGHTER) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("اللون:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            val palette = if (selectedTool == PdfToolMode.HIGHLIGHTER) {
                                listOf(
                                    Color(0xFFFFEB3B), // Yellow
                                    Color(0xFF69F0AE), // Mint Green
                                    Color(0xFFFF80AB), // Pink
                                    Color(0xFF82B1FF), // Blue
                                    Color(0xFFFFD180)  // Orange
                                )
                            } else {
                                listOf(
                                    Color(0xFF1976D2), // Blue
                                    Color(0xFFD32F2F), // Red
                                    Color(0xFF388E3C), // Green
                                    Color(0xFF212121), // Black
                                    Color(0xFF7B1FA2)  // Purple
                                )
                            }

                            for (col in palette) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .pointerInput(col) {
                                            selectedColor = col
                                        }
                                        .padding(2.dp)
                                ) {
                                    if (selectedColor == col) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            // Undo
                            IconButton(
                                onClick = {
                                    val list = pageStrokes[currentPageIndex]
                                    if (!list.isNullOrEmpty()) {
                                        list.removeAt(list.size - 1)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "تراجع", modifier = Modifier.size(18.dp))
                            }

                            // Clear Page
                            IconButton(
                                onClick = { pageStrokes[currentPageIndex]?.clear() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "مسح التعديلات", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Page Navigation Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الصفحة السابقة")
                        }

                        Text(
                            text = "${currentPageIndex + 1} / $totalPages",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        IconButton(
                            onClick = { if (currentPageIndex < totalPages - 1) currentPageIndex++ },
                            enabled = currentPageIndex < totalPages - 1
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الصفحة التالية")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (currentBitmap != null) {
                val bmp = currentBitmap!!

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedTool) {
                            if (selectedTool == PdfToolMode.PAN_ZOOM) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.8f, 5f)
                                    offset += pan
                                }
                            }
                        }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedTool, currentPageIndex, selectedColor, strokeWidth) {
                                if (selectedTool != PdfToolMode.PAN_ZOOM) {
                                    detectDragGestures(
                                        onDragStart = { startOffset ->
                                            if (selectedTool == PdfToolMode.ERASER) {
                                                eraseNear(pageStrokes, currentPageIndex, startOffset)
                                            } else {
                                                currentDrawingPoints = listOf(startOffset)
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val pt = change.position
                                            if (selectedTool == PdfToolMode.ERASER) {
                                                eraseNear(pageStrokes, currentPageIndex, pt)
                                            } else {
                                                currentDrawingPoints = currentDrawingPoints + pt
                                            }
                                        },
                                        onDragEnd = {
                                            if (currentDrawingPoints.isNotEmpty() && selectedTool != PdfToolMode.ERASER) {
                                                val list = pageStrokes.getOrPut(currentPageIndex) { mutableListOf() }
                                                val isHigh = selectedTool == PdfToolMode.HIGHLIGHTER
                                                val col = if (isHigh) selectedColor.copy(alpha = 0.35f) else selectedColor
                                                val width = if (isHigh) 24f else strokeWidth * 2
                                                list.add(
                                                    PdfStroke(
                                                        points = currentDrawingPoints,
                                                        color = col,
                                                        strokeWidth = width,
                                                        isHighlighter = isHigh
                                                    )
                                                )
                                                currentDrawingPoints = emptyList()
                                            }
                                        }
                                    )
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Calculate aspect fit for PDF bitmap
                        val bmpRatio = bmp.width.toFloat() / bmp.height.toFloat()
                        val canvasRatio = canvasWidth / canvasHeight

                        val renderWidth: Float
                        val renderHeight: Float
                        if (canvasRatio > bmpRatio) {
                            renderHeight = canvasHeight
                            renderWidth = canvasHeight * bmpRatio
                        } else {
                            renderWidth = canvasWidth
                            renderHeight = canvasWidth / bmpRatio
                        }

                        val left = (canvasWidth - renderWidth) / 2f
                        val top = (canvasHeight - renderHeight) / 2f

                        // Draw PDF Page
                        drawImage(
                            image = bmp.asImageBitmap(),
                            dstOffset = androidx.compose.ui.unit.IntOffset(left.toInt(), top.toInt()),
                            dstSize = androidx.compose.ui.unit.IntSize(renderWidth.toInt(), renderHeight.toInt())
                        )

                        // Draw Existing Annotation Strokes for current page
                        val strokes = pageStrokes[currentPageIndex] ?: emptyList()
                        for (stroke in strokes) {
                            if (stroke.points.size > 1) {
                                val path = Path().apply {
                                    moveTo(stroke.points[0].x, stroke.points[0].y)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = stroke.color,
                                    style = Stroke(
                                        width = stroke.strokeWidth,
                                        cap = if (stroke.isHighlighter) StrokeCap.Square else StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Draw live active stroke
                        if (currentDrawingPoints.size > 1) {
                            val isHigh = selectedTool == PdfToolMode.HIGHLIGHTER
                            val col = if (isHigh) selectedColor.copy(alpha = 0.35f) else selectedColor
                            val width = if (isHigh) 24f else strokeWidth * 2

                            val path = Path().apply {
                                moveTo(currentDrawingPoints[0].x, currentDrawingPoints[0].y)
                                for (i in 1 until currentDrawingPoints.size) {
                                    lineTo(currentDrawingPoints[i].x, currentDrawingPoints[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = col,
                                style = Stroke(
                                    width = width,
                                    cap = if (isHigh) StrokeCap.Square else StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            } else {
                Text("تعذر عرض ملف الـ PDF. يرجى اختيار ملف آخر.")
            }
        }
    }
}

private fun eraseNear(
    pageStrokes: MutableMap<Int, MutableList<PdfStroke>>,
    pageIndex: Int,
    target: Offset
) {
    val strokes = pageStrokes[pageIndex] ?: return
    val radius = 30f
    strokes.removeAll { stroke ->
        stroke.points.any { pt ->
            val dx = pt.x - target.x
            val dy = pt.y - target.y
            sqrt(dx * dx + dy * dy) < radius
        }
    }
}

// Generates a helpful Study Cheat-Sheet PDF into cache directory
private fun createSampleCheatSheetPdf(context: Context): ParcelFileDescriptor? {
    val file = File(context.cacheDir, "sample_cheat_sheet.pdf")
    if (!file.exists()) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(30, 41, 59)
            textSize = 24f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        canvas.drawText("ملخص المذاكرة وقوانين الرياضيات والعلوم", 595f / 2f, 80f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.rgb(71, 85, 105)
        canvas.drawText("أدواتي المدرسية - تجربة تظليل النصوص والكتابة بالقلم على الـ PDF", 595f / 2f, 115f, paint)

        // Draw boxes & formulas
        val boxPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(241, 245, 249)
            style = android.graphics.Paint.Style.FILL
        }
        val borderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(203, 213, 225)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f
        }

        // Section 1
        canvas.drawRoundRect(40f, 150f, 555f, 320f, 16f, 16f, boxPaint)
        canvas.drawRoundRect(40f, 150f, 555f, 320f, 16f, 16f, borderPaint)

        paint.color = android.graphics.Color.rgb(15, 23, 42)
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText("1. قوانين الجبر وحساب المثلثات الأساسية:", 530f, 185f, paint)

        paint.textSize = 13f
        paint.isFakeBoldText = false
        paint.textAlign = android.graphics.Paint.Align.LEFT
        canvas.drawText("• Quadratic Formula: x = (-b ± √(b² - 4ac)) / (2a)", 60f, 220f, paint)
        canvas.drawText("• Pythagorean Theorem: a² + b² = c²", 60f, 250f, paint)
        canvas.drawText("• Trigonometric Identity: sin²(θ) + cos²(θ) = 1", 60f, 280f, paint)

        // Section 2
        canvas.drawRoundRect(40f, 350f, 555f, 520f, 16f, 16f, boxPaint)
        canvas.drawRoundRect(40f, 350f, 555f, 520f, 16f, 16f, borderPaint)

        paint.color = android.graphics.Color.rgb(15, 23, 42)
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText("2. قوانين الفيزياء الكلاسيكية:", 530f, 385f, paint)

        paint.textSize = 13f
        paint.isFakeBoldText = false
        paint.textAlign = android.graphics.Paint.Align.LEFT
        canvas.drawText("• Newton's Second Law: F = m · a  (القوة = الكتلة × التسارع)", 60f, 420f, paint)
        canvas.drawText("• Kinetic Energy: KE = ½ · m · v²  (طاقة الحركة)", 60f, 450f, paint)
        canvas.drawText("• Work & Power: W = F · d ,  P = W / t  (الشغل والقدرة)", 60f, 480f, paint)

        // Section 3
        canvas.drawRoundRect(40f, 550f, 555f, 720f, 16f, 16f, boxPaint)
        canvas.drawRoundRect(40f, 550f, 555f, 720f, 16f, 16f, borderPaint)

        paint.color = android.graphics.Color.rgb(15, 23, 42)
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText("3. نصائح المذاكرة الذكية والتحصيل السريع:", 530f, 585f, paint)

        paint.textSize = 13f
        paint.isFakeBoldText = false
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText("• استخدم قلم التظليل الأصفر لتمييز المصطلحات والمفاهيم الجوهرية.", 530f, 620f, paint)
        canvas.drawText("• استخدم القلم الأزرق لكتابة ملاحظاتك وتوضيحاتك في الهوامش.", 530f, 650f, paint)
        canvas.drawText("• قسّم جلسات المذاكرة بمؤقت بومودورو (25 دقيقة تركيز + 5 راحة).", 530f, 680f, paint)

        document.finishPage(page)
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        fos.close()
        document.close()
    }
    return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
}
