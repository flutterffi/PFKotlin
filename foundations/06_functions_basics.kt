fun greet(name: String, punctuation: String = "!"): String {
    return "Hello, $name$punctuation"
}

fun totalMinutes(vararg blocks: Int): Int {
    return blocks.sum()
}

fun isStrongStudyDay(minutes: Int, focusScore: Int): Boolean {
    return minutes >= 60 && focusScore >= 7
}

fun main() {
    println(greet("Plato"))
    println(greet(name = "Kotlin learner", punctuation = " :)"))

    val minutes = totalMinutes(20, 15, 30)
    println("Total minutes: $minutes")
    println("Strong study day: ${isStrongStudyDay(minutes, focusScore = 8)}")
}
