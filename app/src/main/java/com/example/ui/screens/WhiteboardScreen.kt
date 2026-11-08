package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QuickCalculatorDialog
import kotlin.math.sqrt

enum class BoardBackground {
    WHITE,
    BLACKBOARD,
    GRAPH_PAPER,
    LINED_PAPER
}

data class BoardStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteboardScreen(
    onNavigateBack: () -> Unit
) {
    var boardBackground by remember { mutableStateOf(BoardBackground.WHITE) }
    var selectedColor by remember { mutableStateOf(Color(0xFF1E293B)) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    var isEraser by remember { mutableStateOf(false) }
    var isHighlighter by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    val strokes = remember { mutableStateListOf<BoardStroke>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    // Auto update default pen color when switching to blackboard
    LaunchedEffect(boardBackground) {
        if (boardBackground == BoardBackground.BLACKBOARD && selectedColor == Color(0xFF1E293B)) {
            selectedColor = Color.White
        } else if (boardBackground != BoardBackground.BLACKBOARD && selectedColor == Color.White) {
            selectedColor = Color(0xFF1E293B)
        }
    }

    if (showCalculator) {
        QuickCalculatorDialog(onDismissRequest = { showCalculator = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "السبورة الذكية للدراسة والحل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    // Calculator
                    IconButton(onClick = { showCalculator = true }) {
                        Icon(Icons.Default.Calculate, contentDescription = "آلة حاسبة سريعة")
                    }
                    // Undo
                    IconButton(
                        onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.size - 1) },
                        enabled = strokes.isNotEmpty()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "تراجع")
                    }
                    // Clear All
                    IconButton(onClick = { strokes.clear() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "مسح الكل")
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Background & Tool Style Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الخلفية:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        FilterChip(
                            selected = boardBackground == BoardBackground.WHITE,
                            onClick = { boardBackground = BoardBackground.WHITE },
                            label = { Text("بيضاء") }
                        )
                        FilterChip(
                            selected = boardBackground == BoardBackground.BLACKBOARD,
                            onClick = { boardBackground = BoardBackground.BLACKBOARD },
                            label = { Text("سبورة كلاسيكية") }
                        )
                        FilterChip(
                            selected = boardBackground == BoardBackground.GRAPH_PAPER,
                            onClick = { boardBackground = BoardBackground.GRAPH_PAPER },
                            label = { Text("رسم بياني") }
                        )
                        FilterChip(
                            selected = boardBackground == BoardBackground.LINED_PAPER,
                            onClick = { boardBackground = BoardBackground.LINED_PAPER },
                            label = { Text("مسطرة") }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Modes: Pen / Highlighter / Eraser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    isEraser = false
                                    isHighlighter = false
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (!isEraser && !isHighlighter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!isEraser && !isHighlighter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("قلم", fontSize = 12.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    isEraser = false
                                    isHighlighter = true
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isHighlighter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isHighlighter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.BorderColor, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تظليل", fontSize = 12.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    isEraser = true
                                    isHighlighter = false
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isEraser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isEraser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AutoFixNormal, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ممحاة", fontSize = 12.sp)
                            }
                        }

                        // Thickness slider / selector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("السُمك: ${strokeWidth.toInt()}", fontSize = 11.sp)
                            Slider(
                                value = strokeWidth,
                                onValueChange = { strokeWidth = it },
                                valueRange = 2f..24f,
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }

                    // Colors Palette
                    if (!isEraser) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val colors = if (boardBackground == BoardBackground.BLACKBOARD) {
                                listOf(
                                    Color.White,
                                    Color(0xFFFFEB3B),
                                    Color(0xFF69F0AE),
                                    Color(0xFF82B1FF),
                                    Color(0xFFFF80AB),
                                    Color(0xFFFFAB40)
                                )
                            } else {
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF1976D2),
                                    Color(0xFFD32F2F),
                                    Color(0xFF388E3C),
                                    Color(0xFF7B1FA2),
                                    Color(0xFFFF8F00)
                                )
                            }

                            for (col in colors) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            width = if (selectedColor == col) 3.dp else 1.dp,
                                            color = if (selectedColor == col) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = col }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val bgColor = when (boardBackground) {
            BoardBackground.WHITE -> Color(0xFFFCFDFD)
            BoardBackground.BLACKBOARD -> Color(0xFF1B2E24) // Classic Chalkboard dark green
            BoardBackground.GRAPH_PAPER -> Color(0xFFF8FAFC)
            BoardBackground.LINED_PAPER -> Color(0xFFFDFBF7)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isEraser, isHighlighter, selectedColor, strokeWidth) {
                        detectDragGestures(
                            onDragStart = { startPt ->
                                if (isEraser) {
                                    eraseAtPoint(strokes, startPt)
                                } else {
                                    currentPoints = listOf(startPt)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val pt = change.position
                                if (isEraser) {
                                    eraseAtPoint(strokes, pt)
                                } else {
                                    currentPoints = currentPoints + pt
                                }
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty() && !isEraser) {
                                    val col = if (isHighlighter) selectedColor.copy(alpha = 0.35f) else selectedColor
                                    val width = if (isHighlighter) (strokeWidth * 3).coerceAtLeast(20f) else strokeWidth
                                    strokes.add(
                                        BoardStroke(
                                            points = currentPoints,
                                            color = col,
                                            strokeWidth = width,
                                            isHighlighter = isHighlighter
                                        )
                                    )
                                    currentPoints = emptyList()
                                }
                            }
                        )
                    }
            ) {
                // Background grid or lines pattern
                if (boardBackground == BoardBackground.GRAPH_PAPER) {
                    val gridSize = 40f
                    val gridColor = Color(0xFFCBD5E1).copy(alpha = 0.6f)
                    var x = 0f
                    while (x < size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += gridSize
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += gridSize
                    }
                } else if (boardBackground == BoardBackground.LINED_PAPER) {
                    val lineSpacing = 50f
                    val lineColor = Color(0xFF94A3B8).copy(alpha = 0.4f)
                    var y = lineSpacing
                    while (y < size.height) {
                        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += lineSpacing
                    }
                    // Red vertical margin
                    drawLine(Color(0xFFEF4444).copy(alpha = 0.4f), Offset(60f, 0f), Offset(60f, size.height), strokeWidth = 1.5f)
                }

                // Render completed strokes
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

                // Render active current stroke
                if (currentPoints.size > 1) {
                    val col = if (isHighlighter) selectedColor.copy(alpha = 0.35f) else selectedColor
                    val width = if (isHighlighter) (strokeWidth * 3).coerceAtLeast(20f) else strokeWidth
                    val path = Path().apply {
                        moveTo(currentPoints[0].x, currentPoints[0].y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = col,
                        style = Stroke(
                            width = width,
                            cap = if (isHighlighter) StrokeCap.Square else StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}

private fun eraseAtPoint(strokes: MutableList<BoardStroke>, point: Offset) {
    val radius = 35f
    strokes.removeAll { stroke ->
        stroke.points.any { pt ->
            val dx = pt.x - point.x
            val dy = pt.y - point.y
            sqrt(dx * dx + dy * dy) < radius
        }
    }
}
