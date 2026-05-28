enum class TopicType {
    SYNTAX,
    OOP,
    COLLECTIONS,
    CONCURRENCY,
    TESTING
}

enum class EnergyLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class StudyTask(
    val title: String,
    val topic: TopicType,
    val difficulty: Int,
    val estimatedMinutes: Int,
    val deadlineDays: Int?,
    val energyNeeded: EnergyLevel
)

data class PlannedSession(
    val task: StudyTask,
    val score: Int,
    val block: String
)

fun StudyTask.urgencyScore(): Int = when (deadlineDays) {
    null -> 5
    0 -> 40
    1 -> 30
    in 2..3 -> 20
    else -> 10
}

fun StudyTask.energyScore(currentEnergy: EnergyLevel): Int = when {
    currentEnergy == EnergyLevel.HIGH && energyNeeded == EnergyLevel.HIGH -> 20
    currentEnergy == EnergyLevel.LOW && energyNeeded == EnergyLevel.HIGH -> 4
    currentEnergy == energyNeeded -> 16
    else -> 10
}

fun StudyTask.topicBonus(): Int = when (topic) {
    TopicType.SYNTAX -> 12
    TopicType.COLLECTIONS -> 14
    TopicType.OOP -> 10
    TopicType.CONCURRENCY -> 18
    TopicType.TESTING -> 16
}

fun StudyTask.lengthPenalty(): Int = estimatedMinutes / 10

fun StudyTask.planBlock(): String = when {
    estimatedMinutes <= 20 -> "Quick Win"
    estimatedMinutes <= 40 -> "Core Focus"
    else -> "Deep Work"
}

fun buildPlan(tasks: List<StudyTask>, currentEnergy: EnergyLevel): List<PlannedSession> {
    return tasks
        .map { task ->
            val score = task.urgencyScore() +
                task.energyScore(currentEnergy) +
                task.topicBonus() -
                task.lengthPenalty() -
                task.difficulty

            PlannedSession(
                task = task,
                score = score,
                block = task.planBlock()
            )
        }
        .sortedWith(
            compareByDescending<PlannedSession> { it.score }
                .thenBy { it.task.estimatedMinutes }
                .thenBy { it.task.title }
        )
}

fun printPlan(plan: List<PlannedSession>) {
    println("Study Planner Result")
    println("--------------------")

    plan.forEachIndexed { index, session ->
        val task = session.task
        val deadlineText = task.deadlineDays?.let { "$it day(s)" } ?: "flexible"

        println(
            "${index + 1}. ${task.title} | " +
                "topic=${task.topic} | " +
                "score=${session.score} | " +
                "block=${session.block} | " +
                "time=${task.estimatedMinutes}m | " +
                "deadline=$deadlineText"
        )
    }

    val grouped = plan.groupBy { it.block }
    println()
    println("Block summary")
    grouped.forEach { (block, sessions) ->
        val totalMinutes = sessions.sumOf { it.task.estimatedMinutes }
        println("- $block: ${sessions.size} task(s), $totalMinutes minutes")
    }
}

fun sampleTasks(): List<StudyTask> = listOf(
    StudyTask(
        title = "Review null safety operators",
        topic = TopicType.SYNTAX,
        difficulty = 2,
        estimatedMinutes = 20,
        deadlineDays = 1,
        energyNeeded = EnergyLevel.LOW
    ),
    StudyTask(
        title = "Implement a sealed result model",
        topic = TopicType.OOP,
        difficulty = 4,
        estimatedMinutes = 35,
        deadlineDays = 3,
        energyNeeded = EnergyLevel.MEDIUM
    ),
    StudyTask(
        title = "Solve collection transformation drills",
        topic = TopicType.COLLECTIONS,
        difficulty = 3,
        estimatedMinutes = 30,
        deadlineDays = 0,
        energyNeeded = EnergyLevel.MEDIUM
    ),
    StudyTask(
        title = "Read coroutine cancellation notes",
        topic = TopicType.CONCURRENCY,
        difficulty = 5,
        estimatedMinutes = 50,
        deadlineDays = null,
        energyNeeded = EnergyLevel.HIGH
    ),
    StudyTask(
        title = "Write tests for planner scoring",
        topic = TopicType.TESTING,
        difficulty = 3,
        estimatedMinutes = 25,
        deadlineDays = 2,
        energyNeeded = EnergyLevel.MEDIUM
    )
)

fun main() {
    val currentEnergy = EnergyLevel.MEDIUM
    val plan = buildPlan(sampleTasks(), currentEnergy)
    printPlan(plan)
}
