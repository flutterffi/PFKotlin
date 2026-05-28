data class PracticeTask(
    val title: String,
    val minutes: Int,
    val done: Boolean
)

fun PracticeTask.statusLabel(): String = if (done) "done" else "pending"

fun List<PracticeTask>.totalMinutes(): Int = sumOf { it.minutes }

fun main() {
    val tasks = listOf(
        PracticeTask("Read about data classes", 20, true),
        PracticeTask("Write extension functions", 30, false),
        PracticeTask("Refactor a when expression", 15, false)
    )

    tasks.forEach { task ->
        println("${task.title} -> ${task.statusLabel()}")
    }

    println("Total practice time: ${tasks.totalMinutes()} minutes")
}
