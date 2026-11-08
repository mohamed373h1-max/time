package com.example.util

import kotlin.math.*

object CalculatorEngine {

    // Evaluate basic to advanced algebraic expressions
    fun evaluate(expression: String, isDegreeMode: Boolean = true): Double {
        if (expression.isBlank()) return 0.0

        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", Math.PI.toString())
            .replace("Ans", "0.0") // Fallback if Ans not replaced
            .replace("e", Math.E.toString())

        val tokens = tokenize(sanitized)
        val rpn = toRpn(tokens)
        return evalRpn(rpn, isDegreeMode)
    }

    // Convert decimal to simplified fraction (for the iconic Casio S<=>D button)
    fun decimalToFraction(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return value.toString()
        if (value % 1.0 == 0.0) return value.toLong().toString()

        val sign = if (value < 0) "-" else ""
        val absVal = abs(value)

        val tolerance = 1.0E-6
        var h1 = 1L
        var h2 = 0L
        var k1 = 0L
        var k2 = 1L
        var b = absVal

        do {
            val a = b.toLong()
            var aux = h1
            h1 = a * h1 + h2
            h2 = aux
            aux = k1
            k1 = a * k1 + k2
            k2 = aux
            b = 1.0 / (b - a)
        } while (abs(absVal - h1.toDouble() / k1.toDouble()) > absVal * tolerance && k1 < 100000L)

        return if (k1 == 1L) {
            "$sign$h1"
        } else if (k1 in 2..99999) {
            "$sign$h1/$k1"
        } else {
            "%.6f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    // Factorial n!
    fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 170) return Double.NaN
        var res = 1.0
        for (i in 2..n.toInt()) {
            res *= i
        }
        return res
    }

    // Permutations nPr
    fun nPr(n: Int, r: Int): Double {
        if (n < 0 || r < 0 || r > n) return Double.NaN
        return factorial(n.toDouble()) / factorial((n - r).toDouble())
    }

    // Combinations nCr
    fun nCr(n: Int, r: Int): Double {
        if (n < 0 || r < 0 || r > n) return Double.NaN
        return factorial(n.toDouble()) / (factorial(r.toDouble()) * factorial((n - r).toDouble()))
    }

    // Solve Quadratic Equation: a x² + b x + c = 0
    data class QuadraticResult(
        val x1: String,
        val x2: String,
        val vertexX: Double,
        val vertexY: Double
    )

    fun solveQuadratic(a: Double, b: Double, c: Double): QuadraticResult? {
        if (a == 0.0) return null
        val delta = b * b - 4 * a * c
        val vx = -b / (2 * a)
        val vy = c - (b * b) / (4 * a)

        return if (delta >= 0) {
            val root1 = (-b + sqrt(delta)) / (2 * a)
            val root2 = (-b - sqrt(delta)) / (2 * a)
            QuadraticResult(
                x1 = decimalToFraction(root1),
                x2 = decimalToFraction(root2),
                vertexX = vx,
                vertexY = vy
            )
        } else {
            val realPart = -b / (2 * a)
            val imaginaryPart = sqrt(-delta) / (2 * a)
            val rStr = if (realPart != 0.0) "%.4f".format(realPart).trimEnd('0').trimEnd('.') else ""
            val iStr = "%.4f".format(abs(imaginaryPart)).trimEnd('0').trimEnd('.')
            QuadraticResult(
                x1 = "${if (rStr.isNotEmpty()) "$rStr + " else ""}${iStr}i",
                x2 = "${if (rStr.isNotEmpty()) "$rStr - " else ""}${iStr}i",
                vertexX = vx,
                vertexY = vy
            )
        }
    }

    // Solve 2x2 Linear System: a1 x + b1 y = c1, a2 x + b2 y = c2
    data class Linear2x2Result(val x: String, val y: String)

    fun solveLinear2x2(a1: Double, b1: Double, c1: Double, a2: Double, b2: Double, c2: Double): Linear2x2Result? {
        val det = a1 * b2 - a2 * b1
        if (abs(det) < 1e-12) return null // No unique solution
        val x = (c1 * b2 - c2 * b1) / det
        val y = (a1 * c2 - a2 * c1) / det
        return Linear2x2Result(
            x = decimalToFraction(x),
            y = decimalToFraction(y)
        )
    }

    // Statistics calculator
    data class StatsResult(
        val count: Int,
        val mean: Double,
        val sum: Double,
        val sumSquares: Double,
        val sampleStdDev: Double,
        val populationStdDev: Double
    )

    fun calculateStats(numbers: List<Double>): StatsResult? {
        if (numbers.isEmpty()) return null
        val n = numbers.size
        val sum = numbers.sum()
        val mean = sum / n
        val sumSquares = numbers.sumOf { it * it }
        val variancePop = numbers.sumOf { (it - mean).pow(2) } / n
        val popSd = sqrt(variancePop)
        val sampleSd = if (n > 1) sqrt(numbers.sumOf { (it - mean).pow(2) } / (n - 1)) else 0.0

        return StatsResult(
            count = n,
            mean = mean,
            sum = sum,
            sumSquares = sumSquares,
            sampleStdDev = sampleSd,
            populationStdDev = popSd
        )
    }

    // Matrix 2x2 and 3x3 Determinant
    fun matrix2x2Det(m: List<Double>): Double {
        if (m.size < 4) return 0.0
        return m[0] * m[3] - m[1] * m[2]
    }

    fun matrix3x3Det(m: List<Double>): Double {
        if (m.size < 9) return 0.0
        return m[0] * (m[4] * m[8] - m[5] * m[7]) -
                m[1] * (m[3] * m[8] - m[5] * m[6]) +
                m[2] * (m[3] * m[7] - m[4] * m[6])
    }

    // Internal tokenizer & RPN evaluator
    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c in "+-*/^()!%" -> {
                    if (c == '-' && (tokens.isEmpty() || tokens.last() in "(+-*/^")) {
                        tokens.add("neg")
                    } else {
                        tokens.add(c.toString())
                    }
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isLetter() || expr[i].isDigit())) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun precedence(op: String): Int = when (op) {
        "+", "-" -> 1
        "*", "/" -> 2
        "neg" -> 3
        "^" -> 4
        "!", "%" -> 5
        "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "sqrt", "cbrt", "log", "ln", "abs" -> 6
        else -> 0
    }

    private fun toRpn(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        val functions = listOf(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "sqrt", "cbrt", "log", "ln", "abs", "neg"
        )

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token in functions -> stack.addFirst(token)
                token == "!" || token == "%" -> output.add(token)
                token == "(" -> stack.addFirst(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.first() != "(") {
                        output.add(stack.removeFirst())
                    }
                    if (stack.isNotEmpty() && stack.first() == "(") {
                        stack.removeFirst()
                    }
                    if (stack.isNotEmpty() && stack.first() in functions) {
                        output.add(stack.removeFirst())
                    }
                }
                token in listOf("+", "-", "*", "/", "^") -> {
                    while (stack.isNotEmpty() && stack.first() != "(" && precedence(stack.first()) >= precedence(token)) {
                        output.add(stack.removeFirst())
                    }
                    stack.addFirst(token)
                }
            }
        }
        while (stack.isNotEmpty()) {
            output.add(stack.removeFirst())
        }
        return output
    }

    private fun evalRpn(rpn: List<String>, isDegreeMode: Boolean): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addFirst(num)
                continue
            }

            when (token) {
                "neg" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(-a)
                }
                "!" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(factorial(a))
                }
                "%" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(a / 100.0)
                }
                "sin" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    val rad = if (isDegreeMode) a * PI / 180.0 else a
                    stack.addFirst(sin(rad))
                }
                "cos" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    val rad = if (isDegreeMode) a * PI / 180.0 else a
                    stack.addFirst(cos(rad))
                }
                "tan" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    val rad = if (isDegreeMode) a * PI / 180.0 else a
                    stack.addFirst(tan(rad))
                }
                "asin" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    val res = asin(a)
                    stack.addFirst(if (isDegreeMode) res * 180.0 / PI else res)
                }
                "acos" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    val res = acos(a)
                    stack.addFirst(if (isDegreeMode) res * 180.0 / PI else res)
                }
                "atan" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    val res = atan(a)
                    stack.addFirst(if (isDegreeMode) res * 180.0 / PI else res)
                }
                "sinh" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(sinh(a))
                }
                "cosh" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(cosh(a))
                }
                "tanh" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(tanh(a))
                }
                "sqrt" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(sqrt(a))
                }
                "cbrt" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(cbrt(a))
                }
                "log" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(log10(a))
                }
                "ln" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(ln(a))
                }
                "abs" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(abs(a))
                }
                "+", "-", "*", "/", "^" -> {
                    val b = stack.removeFirstOrNull() ?: 0.0
                    val a = stack.removeFirstOrNull() ?: 0.0
                    val res = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> if (b != 0.0) a / b else Double.NaN
                        "^" -> a.pow(b)
                        else -> 0.0
                    }
                    stack.addFirst(res)
                }
            }
        }
        return stack.firstOrNull() ?: 0.0
    }
}
