data class TaskInput(
    val title: String,
    val difficulty: Int,
    val estimateMinutes: Int
)

sealed class PracticeLane {
    object Warmup : PracticeLane()
    object Focus : PracticeLane()
    object Stretch : PracticeLane()
}

data class PlannedTask(
    val title: String,
    val lane: PracticeLane,
    val estimateMinutes: Int
)

fun classify(task: TaskInput): PracticeLane = when {
    task.difficulty <= 2 -> PracticeLane.Warmup
    task.difficulty <= 4 -> PracticeLane.Focus
    else -> PracticeLane.Stretch
}

fun <T, R> List<T>.mapWith(transform: (T) -> R): List<R> = map(transform)

fun PracticeLane.label(): String = when (this) {
    PracticeLane.Warmup -> "Warmup"
    PracticeLane.Focus -> "Focus"
    PracticeLane.Stretch -> "Stretch"
}

fun main() {
    val rawTasks = listOf(
        TaskInput("Practice val and var", 1, 10),
        TaskInput("Write collection filters", 3, 25),
        TaskInput("Model a sealed hierarchy", 5, 35)
    )

    val planned = rawTasks.mapWith { task ->
        PlannedTask(
            title = task.title,
            lane = classify(task),
            estimateMinutes = task.estimateMinutes
        )
    }

    planned.forEach { task ->
        println("${task.lane.label()}: ${task.title} (${task.estimateMinutes} min)")
    }
}
