fun describeScore(score: Int?): String {
    val safeScore = score ?: return "Score is missing"

    return when {
        safeScore >= 90 -> "Excellent"
        safeScore >= 75 -> "Strong"
        safeScore >= 60 -> "Improving"
        else -> "Needs review"
    }
}

fun main() {
    val scores = listOf(95, 78, null, 55)

    scores.forEach { score ->
        val label = describeScore(score)
        println("Input=$score -> $label")
    }

    val bonusMessage = scores.filterNotNull()
        .firstOrNull { it > 80 }
        ?.let { "First high score found: $it" }
        ?: "No high score yet"

    println(bonusMessage)
}
