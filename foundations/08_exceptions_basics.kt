fun parseTargetMinutes(raw: String): Int {
    val value = raw.toIntOrNull() ?: throw IllegalArgumentException("Minutes must be a number")
    require(value > 0) { "Minutes must be positive" }
    return value
}

fun main() {
    val inputs = listOf("30", "-5", "oops")

    inputs.forEach { raw ->
        try {
            val minutes = parseTargetMinutes(raw)
            println("Accepted: $minutes")
        } catch (error: IllegalArgumentException) {
            println("Rejected '$raw' -> ${error.message}")
        } finally {
            println("Checked input: $raw")
        }
    }
}
