fun main() {
    val lessons = listOf("syntax", "null safety", "functions", "classes")

    println("For loop with index:")
    for ((index, lesson) in lessons.withIndex()) {
        println("${index + 1}. $lesson")
    }

    println()
    println("Range loop:")
    for (day in 1..3) {
        println("Day $day practice block")
    }

    println()
    println("While loop:")
    var remaining = 3
    while (remaining > 0) {
        println("Remaining review tasks: $remaining")
        remaining--
    }
}
