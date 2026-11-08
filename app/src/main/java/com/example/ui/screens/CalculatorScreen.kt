package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CalculatorEngine

enum class CasioMode {
    COMP,    // General calculation
    EQN,     // Quadratic and Linear equations
    STAT,    // Statistics (mean, sum, SD)
    MATRIX   // Matrices
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateBack: () -> Unit
) {
    // Current Active Mode
    var activeMode by remember { mutableStateOf(CasioMode.COMP) }
    var showModeSelector by remember { mutableStateOf(false) }

    // Casio State
    var isShiftActive by remember { mutableStateOf(false) }
    var isAlphaActive by remember { mutableStateOf(false) }
    var isDegreeMode by remember { mutableStateOf(true) }
    var memoryValue by remember { mutableDoubleStateOf(0.0) }
    var lastAns by remember { mutableDoubleStateOf(0.0) }

    // Display
    var expression by remember { mutableStateOf("") }
    var resultDisplay by remember { mutableStateOf("0") }
    var isResultCalculated by remember { mutableStateOf(false) }
    var isDecimalFractionToggled by remember { mutableStateOf(false) }

    // EQN Mode inputs
    var eqnType by remember { mutableIntStateOf(1) } // 1: Quadratic (ax²+bx+c=0), 2: 2x2 Linear System
    var coeffA by remember { mutableStateOf("") }
    var coeffB by remember { mutableStateOf("") }
    var coeffC by remember { mutableStateOf("") }
    var coeffA2 by remember { mutableStateOf("") }
    var coeffB2 by remember { mutableStateOf("") }
    var coeffC2 by remember { mutableStateOf("") }
    var quadraticSolution by remember { mutableStateOf<CalculatorEngine.QuadraticResult?>(null) }
    var linearSolution by remember { mutableStateOf<CalculatorEngine.Linear2x2Result?>(null) }

    // STAT Mode inputs
    var statInput by remember { mutableStateOf("") }
    var statResult by remember { mutableStateOf<CalculatorEngine.StatsResult?>(null) }

    // Core calculator button click handler
    fun handleInput(symbol: String) {
        if (isResultCalculated && symbol !in listOf("+", "-", "×", "÷", "^")) {
            expression = ""
            isResultCalculated = false
        } else if (isResultCalculated) {
            expression = "Ans"
            isResultCalculated = false
        }
        expression += symbol
        isDecimalFractionToggled = false
    }

    fun handleClear() {
        expression = ""
        resultDisplay = "0"
        isResultCalculated = false
        isShiftActive = false
        isAlphaActive = false
    }

    fun handleBackspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }
    }

    fun handleEquals() {
        if (expression.isBlank()) return
        try {
            val exprWithAns = expression.replace("Ans", lastAns.toString())
            val res = CalculatorEngine.evaluate(exprWithAns, isDegreeMode)
            lastAns = res
            isResultCalculated = true
            isDecimalFractionToggled = false
            resultDisplay = if (res % 1.0 == 0.0 && !res.isInfinite() && !res.isNaN()) {
                res.toLong().toString()
            } else {
                "%.6f".format(res).trimEnd('0').trimEnd('.')
            }
        } catch (e: Exception) {
            resultDisplay = "Math ERROR"
        }
    }

    fun handleToggleFractionDecimal() {
        if (isResultCalculated && !lastAns.isNaN() && !lastAns.isInfinite()) {
            isDecimalFractionToggled = !isDecimalFractionToggled
            resultDisplay = if (isDecimalFractionToggled) {
                CalculatorEngine.decimalToFraction(lastAns)
            } else {
                if (lastAns % 1.0 == 0.0) lastAns.toLong().toString()
                else "%.6f".format(lastAns).trimEnd('0').trimEnd('.')
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Casio fx-991ES PLUS",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = { showModeSelector = true },
                            label = { Text(activeMode.name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    TextButton(onClick = { isDegreeMode = !isDegreeMode }) {
                        Text(
                            text = if (isDegreeMode) "[ DEG ]" else "[ RAD ]",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showModeSelector = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "تغيير النظام MODE")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1E242B)) // Casio fx-991 Body Color
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ================= 1. CASIO HEADER & SOLAR PANEL =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CASIO",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "fx-991ES PLUS • NATURAL-V.P.A.M.",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Solar Cell Simulation
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF3B1D11), Color(0xFF5E2B18), Color(0xFF3B1D11))
                            )
                        )
                        .border(1.dp, Color(0xFF78350F), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TWO WAY POWER",
                        fontSize = 8.sp,
                        color = Color(0xFFFDE68A).copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ================= 2. TWO-LINE NATURAL LCD DISPLAY =================
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC7D3C0)), // Iconic Casio greenish-grey LCD
                border = BorderStroke(2.dp, Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // LCD Status Flags Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (isShiftActive) Text("S", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF9A3412))
                            if (isAlphaActive) Text("A", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFFBE123C))
                            if (memoryValue != 0.0) Text("M", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Text(if (isDegreeMode) "D" else "R", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Text("Math ▲▼", fontSize = 10.sp, color = Color(0xFF334155))
                        }
                        Text(activeMode.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                    }

                    // Upper Line: Natural Expression Formula
                    Text(
                        text = expression.ifEmpty { " " },
                        color = Color(0xFF0F172A),
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bottom Line: High-contrast Result Aligned Right
                    Text(
                        text = resultDisplay,
                        color = Color(0xFF020617),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ================= 3. CASIO MODES CONTENT =================
            when (activeMode) {
                CasioMode.COMP -> {
                    // Standard Natural Casio Keypad
                    CasioKeypadView(
                        isShift = isShiftActive,
                        isAlpha = isAlphaActive,
                        onShiftToggle = { isShiftActive = !isShiftActive },
                        onAlphaToggle = { isAlphaActive = !isAlphaActive },
                        onInput = { symbol ->
                            handleInput(symbol)
                            isShiftActive = false
                            isAlphaActive = false
                        },
                        onClear = { handleClear() },
                        onBackspace = { handleBackspace() },
                        onEquals = { handleEquals() },
                        onToggleSD = { handleToggleFractionDecimal() },
                        onModeClick = { showModeSelector = true }
                    )
                }
                CasioMode.EQN -> {
                    CasioEquationSolverView(
                        eqnType = eqnType,
                        onSelectType = { eqnType = it },
                        coeffA = coeffA, onAChange = { coeffA = it },
                        coeffB = coeffB, onBChange = { coeffB = it },
                        coeffC = coeffC, onCChange = { coeffC = it },
                        coeffA2 = coeffA2, onA2Change = { coeffA2 = it },
                        coeffB2 = coeffB2, onB2Change = { coeffB2 = it },
                        coeffC2 = coeffC2, onC2Change = { coeffC2 = it },
                        quadraticResult = quadraticSolution,
                        linearResult = linearSolution,
                        onSolve = {
                            if (eqnType == 1) {
                                val a = coeffA.toDoubleOrNull() ?: 1.0
                                val b = coeffB.toDoubleOrNull() ?: 0.0
                                val c = coeffC.toDoubleOrNull() ?: 0.0
                                quadraticSolution = CalculatorEngine.solveQuadratic(a, b, c)
                            } else {
                                val a1 = coeffA.toDoubleOrNull() ?: 1.0
                                val b1 = coeffB.toDoubleOrNull() ?: 1.0
                                val c1 = coeffC.toDoubleOrNull() ?: 0.0
                                val a2 = coeffA2.toDoubleOrNull() ?: 1.0
                                val b2 = coeffB2.toDoubleOrNull() ?: -1.0
                                val c2 = coeffC2.toDoubleOrNull() ?: 0.0
                                linearSolution = CalculatorEngine.solveLinear2x2(a1, b1, c1, a2, b2, c2)
                            }
                        },
                        onBackToComp = { activeMode = CasioMode.COMP }
                    )
                }
                CasioMode.STAT -> {
                    CasioStatisticsView(
                        input = statInput,
                        onInputChange = { statInput = it },
                        result = statResult,
                        onCalculate = {
                            val nums = statInput.split(",", " ", "\n")
                                .mapNotNull { it.trim().toDoubleOrNull() }
                            statResult = CalculatorEngine.calculateStats(nums)
                        },
                        onBackToComp = { activeMode = CasioMode.COMP }
                    )
                }
                CasioMode.MATRIX -> {
                    CasioMatrixView(
                        onBackToComp = { activeMode = CasioMode.COMP }
                    )
                }
            }
        }
    }

    // Casio Mode Selector Dialog (Mode 1 to 8)
    if (showModeSelector) {
        AlertDialog(
            onDismissRequest = { showModeSelector = false },
            title = {
                Text(
                    text = "Casio fx-991ES PLUS • MODE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CasioModeOptionItem(1, "COMP", "الحسابات الرياضية والمعادلات العامة") {
                        activeMode = CasioMode.COMP
                        showModeSelector = false
                    }
                    CasioModeOptionItem(2, "EQN", "حل المعادلات التربيعية وأنظمة المعادلات (Quadratic & 2x2)") {
                        activeMode = CasioMode.EQN
                        showModeSelector = false
                    }
                    CasioModeOptionItem(3, "STAT", "الإحصاء والانحراف المعياري والوسط الحسابي") {
                        activeMode = CasioMode.STAT
                        showModeSelector = false
                    }
                    CasioModeOptionItem(4, "MATRIX", "المحددات والعمليات على المصفوفات") {
                        activeMode = CasioMode.MATRIX
                        showModeSelector = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModeSelector = false }) { Text("إغلاق") }
            }
        )
    }
}

// ---------------- CASIO KEYPAD COMPONENT ----------------
@Composable
private fun CasioKeypadView(
    isShift: Boolean,
    isAlpha: Boolean,
    onShiftToggle: () -> Unit,
    onAlphaToggle: () -> Unit,
    onInput: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onToggleSD: () -> Unit,
    onModeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: SHIFT, ALPHA, D-PAD REPLAY, MODE, ON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CasioPillKey(
                label = "SHIFT",
                bgColor = if (isShift) Color(0xFFD97706) else Color(0xFF475569),
                textColor = Color(0xFFFDE68A),
                modifier = Modifier.weight(1.1f),
                onClick = onShiftToggle
            )
            CasioPillKey(
                label = "ALPHA",
                bgColor = if (isAlpha) Color(0xFFE11D48) else Color(0xFF475569),
                textColor = Color(0xFFFECDD3),
                modifier = Modifier.weight(1.1f),
                onClick = onAlphaToggle
            )

            // Circular D-Pad REPLAY Center Key
            Box(
                modifier = Modifier
                    .weight(1.8f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0xFF334155))
                    .border(1.dp, Color(0xFF64748B), RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("REPLAY", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            }

            CasioPillKey(
                label = "MODE",
                bgColor = Color(0xFF475569),
                textColor = Color.White,
                modifier = Modifier.weight(1.1f),
                onClick = onModeClick
            )
            CasioPillKey(
                label = "ON",
                bgColor = Color(0xFF475569),
                textColor = Color.White,
                modifier = Modifier.weight(0.9f),
                onClick = onClear
            )
        }

        // Row 2: Fractions (a/b), Square root, x², x^y, log, ln
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CasioFuncKey(label = "□/□", shiftLabel = "d/c", isShift = isShift, modifier = Modifier.weight(1f)) { onInput("/") }
            CasioFuncKey(label = "√□", shiftLabel = "∛", isShift = isShift, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("cbrt(") else onInput("sqrt(")
            }
            CasioFuncKey(label = "x²", shiftLabel = "x³", isShift = isShift, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("^3") else onInput("^2")
            }
            CasioFuncKey(label = "x^■", shiftLabel = "ˣ√", isShift = isShift, modifier = Modifier.weight(1f)) { onInput("^") }
            CasioFuncKey(label = "log", shiftLabel = "10^x", isShift = isShift, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("10^(") else onInput("log(")
            }
            CasioFuncKey(label = "ln", shiftLabel = "e^x", isShift = isShift, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("e^(") else onInput("ln(")
            }
        }

        // Row 3: (-), °, hyp, sin, cos, tan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CasioFuncKey(label = "(-)", shiftLabel = "A", isShift = isShift, modifier = Modifier.weight(1f)) { onInput("-") }
            CasioFuncKey(label = "° ' \"", shiftLabel = "B", isShift = isShift, modifier = Modifier.weight(1f)) { onInput("°") }
            CasioFuncKey(label = "hyp", shiftLabel = "C", isShift = isShift, modifier = Modifier.weight(1f)) { onInput("sinh(") }
            CasioFuncKey(label = "sin", shiftLabel = "sin⁻¹", isShift = isShift, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("asin(") else onInput("sin(")
            }
            CasioFuncKey(label = "cos", shiftLabel = "cos⁻¹", isShift = isShift, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("acos(") else onInput("cos(")
            }
            CasioFuncKey(label = "tan", shiftLabel = "tan⁻¹", isShift = isShift, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("atan(") else onInput("tan(")
            }
        }

        // Row 4: RCL, ENG, (, ), S<=>D, M+
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CasioFuncKey(label = "x!", shiftLabel = "%", isShift = isShift, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("%") else onInput("!")
            }
            CasioFuncKey(label = "(", shiftLabel = "X", isShift = isShift, modifier = Modifier.weight(1f)) { onInput("(") }
            CasioFuncKey(label = ")", shiftLabel = "Y", isShift = isShift, modifier = Modifier.weight(1f)) { onInput(")") }
            CasioFuncKey(
                label = "S<=>D",
                shiftLabel = "a b/c",
                isShift = isShift,
                modifier = Modifier.weight(1.5f),
                onClick = onToggleSD
            )
            CasioFuncKey(label = "M+", shiftLabel = "M-", isShift = isShift, modifier = Modifier.weight(1f)) { onInput("+") }
        }

        // Number Pad Row 1: 7, 8, 9, DEL (Orange), AC (Orange)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CasioNumKey("7", modifier = Modifier.weight(1f)) { onInput("7") }
            CasioNumKey("8", modifier = Modifier.weight(1f)) { onInput("8") }
            CasioNumKey("9", modifier = Modifier.weight(1f)) { onInput("9") }
            CasioActionKey("DEL", Color(0xFFEA580C), modifier = Modifier.weight(1f), onClick = onBackspace)
            CasioActionKey("AC", Color(0xFFDC2626), modifier = Modifier.weight(1f), onClick = onClear)
        }

        // Number Pad Row 2: 4, 5, 6, ×, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CasioNumKey("4", modifier = Modifier.weight(1f)) { onInput("4") }
            CasioNumKey("5", modifier = Modifier.weight(1f)) { onInput("5") }
            CasioNumKey("6", modifier = Modifier.weight(1f)) { onInput("6") }
            CasioNumKey("×", bgColor = Color(0xFF334155), textColor = Color.White, modifier = Modifier.weight(1f)) { onInput("×") }
            CasioNumKey("÷", bgColor = Color(0xFF334155), textColor = Color.White, modifier = Modifier.weight(1f)) { onInput("÷") }
        }

        // Number Pad Row 3: 1, 2, 3, +, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CasioNumKey("1", modifier = Modifier.weight(1f)) { onInput("1") }
            CasioNumKey("2", modifier = Modifier.weight(1f)) { onInput("2") }
            CasioNumKey("3", modifier = Modifier.weight(1f)) { onInput("3") }
            CasioNumKey("+", bgColor = Color(0xFF334155), textColor = Color.White, modifier = Modifier.weight(1f)) { onInput("+") }
            CasioNumKey("-", bgColor = Color(0xFF334155), textColor = Color.White, modifier = Modifier.weight(1f)) { onInput("-") }
        }

        // Number Pad Row 4: 0, ., ×10^x, Ans, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CasioNumKey("0", modifier = Modifier.weight(1f)) { onInput("0") }
            CasioNumKey(".", modifier = Modifier.weight(1f)) { onInput(".") }
            CasioNumKey("π", shiftLabel = "e", isShift = isShift, bgColor = Color(0xFF334155), textColor = Color.White, modifier = Modifier.weight(1f)) {
                if (isShift) onInput("e") else onInput("π")
            }
            CasioNumKey("Ans", bgColor = Color(0xFF334155), textColor = Color.White, modifier = Modifier.weight(1f)) { onInput("Ans") }
            CasioNumKey("=", bgColor = Color(0xFF2563EB), textColor = Color.White, modifier = Modifier.weight(1f), onClick = onEquals)
        }
    }
}

// ---------------- CASIO EQUATION SOLVER (EQN MODE) ----------------
@Composable
private fun CasioEquationSolverView(
    eqnType: Int,
    onSelectType: (Int) -> Unit,
    coeffA: String, onAChange: (String) -> Unit,
    coeffB: String, onBChange: (String) -> Unit,
    coeffC: String, onCChange: (String) -> Unit,
    coeffA2: String, onA2Change: (String) -> Unit,
    coeffB2: String, onB2Change: (String) -> Unit,
    coeffC2: String, onC2Change: (String) -> Unit,
    quadraticResult: CalculatorEngine.QuadraticResult?,
    linearResult: CalculatorEngine.Linear2x2Result?,
    onSolve: () -> Unit,
    onBackToComp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("EQN: موديول حل المعادلات الرياضية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextButton(onClick = onBackToComp) {
                    Text("رجوع للحاسبة COMP", color = Color(0xFF38BDF8), fontSize = 12.sp)
                }
            }

            // Type selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = eqnType == 1,
                    onClick = { onSelectType(1) },
                    label = { Text("1: a x² + b x + c = 0", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = eqnType == 2,
                    onClick = { onSelectType(2) },
                    label = { Text("2: an X + bn Y = cn", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (eqnType == 1) {
                Text("أدخل معاملات المعادلة التربيعية:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = coeffA, onValueChange = onAChange,
                        label = { Text("a") }, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = coeffB, onValueChange = onBChange,
                        label = { Text("b") }, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = coeffC, onValueChange = onCChange,
                        label = { Text("c") }, modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = onSolve,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("احسب الجذور SOLVE (=)")
                }

                if (quadraticResult != null) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("جذور المعادلة (Roots):", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("X₁ = ${quadraticResult.x1}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("X₂ = ${quadraticResult.x2}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("نقطة رأس المنحنى: (${quadraticResult.vertexX}, ${quadraticResult.vertexY})", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }
            } else {
                // 2x2 linear
                Text("المعادلة الأولى: a₁ x + b₁ y = c₁", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = coeffA, onValueChange = onAChange, label = { Text("a₁") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = coeffB, onValueChange = onBChange, label = { Text("b₁") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = coeffC, onValueChange = onCChange, label = { Text("c₁") }, modifier = Modifier.weight(1f))
                }

                Text("المعادلة الثانية: a₂ x + b₂ y = c₂", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = coeffA2, onValueChange = onA2Change, label = { Text("a₂") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = coeffB2, onValueChange = onB2Change, label = { Text("b₂") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = coeffC2, onValueChange = onC2Change, label = { Text("c₂") }, modifier = Modifier.weight(1f))
                }

                Button(onClick = onSolve, modifier = Modifier.fillMaxWidth()) {
                    Text("حل النظام SOLVE (=)")
                }

                if (linearResult != null) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("حل النظام الخطي:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            Text("X = ${linearResult.x}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Y = ${linearResult.y}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CASIO STATISTICS (STAT MODE) ----------------
@Composable
private fun CasioStatisticsView(
    input: String,
    onInputChange: (String) -> Unit,
    result: CalculatorEngine.StatsResult?,
    onCalculate: () -> Unit,
    onBackToComp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("STAT: إحصاء وانحراف معياري", color = Color.White, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBackToComp) { Text("رجوع COMP", color = Color(0xFF38BDF8)) }
            }

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                label = { Text("أدخل البيانات (مفصولة بمسافة أو فاصلة)") },
                placeholder = { Text("مثال: 12, 15, 18, 22, 25, 30") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth()) {
                Text("احسب المؤشرات الإحصائية (=)")
            }

            if (result != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("العدد (n): ${result.count}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("الوسط الحسابي (x̄): ${"%.4f".format(result.mean)}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("المجموع (Σx): ${result.sum}", color = Color(0xFF94A3B8))
                        Text("انحراف العينة المعياري (sx): ${"%.4f".format(result.sampleStdDev)}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        Text("انحراف المجتمع المعياري (σx): ${"%.4f".format(result.populationStdDev)}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------- CASIO MATRIX MODE ----------------
@Composable
private fun CasioMatrixView(onBackToComp: () -> Unit) {
    var a by remember { mutableStateOf("1") }
    var b by remember { mutableStateOf("2") }
    var c by remember { mutableStateOf("3") }
    var d by remember { mutableStateOf("4") }
    var detResult by remember { mutableStateOf<Double?>(null) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MATRIX: محدد مصفوفة 2×2", color = Color.White, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBackToComp) { Text("رجوع COMP", color = Color(0xFF38BDF8)) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a₁₁") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("a₁₂") }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("a₂₁") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("a₂₂") }, modifier = Modifier.weight(1f))
            }

            Button(
                onClick = {
                    val m = listOf(a.toDoubleOrNull() ?: 0.0, b.toDoubleOrNull() ?: 0.0, c.toDoubleOrNull() ?: 0.0, d.toDoubleOrNull() ?: 0.0)
                    detResult = CalculatorEngine.matrix2x2Det(m)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("احسب المحدد det(MatA)")
            }

            if (detResult != null) {
                Text("قيمة المحدد det = $detResult", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

// ---------------- CASIO BUTTON STYLES ----------------
@Composable
private fun CasioPillKey(
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(34.dp)
    ) {
        Text(label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
private fun CasioFuncKey(
    label: String,
    shiftLabel: String = "",
    isShift: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (shiftLabel.isNotEmpty()) {
            Text(
                text = shiftLabel,
                color = Color(0xFFD97706), // Gold shift color
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF334155))
                .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CasioNumKey(
    label: String,
    shiftLabel: String = "",
    isShift: Boolean = false,
    bgColor: Color = Color(0xFFE2E8F0),
    textColor: Color = Color(0xFF0F172A),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (shiftLabel.isNotEmpty()) {
            Text(shiftLabel, color = Color(0xFFD97706), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = textColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CasioActionKey(
    label: String,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun CasioModeOptionItem(
    number: Int,
    name: String,
    desc: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$number:", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
