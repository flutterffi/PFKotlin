import java.io.File

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

data class PlannerOptions(
    val currentEnergy: EnergyLevel,
    val filePath: String?,
    val savePath: String?
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

fun parseEnergyLevel(raw: String): Result<EnergyLevel> = runCatching {
    EnergyLevel.valueOf(raw.uppercase())
}

fun parseTopicType(raw: String): Result<TopicType> = runCatching {
    TopicType.valueOf(raw.uppercase())
}

fun parsePositiveInt(raw: String, fieldName: String): Result<Int> = runCatching {
    raw.toInt().also {
        require(it > 0) { "$fieldName must be positive" }
    }
}

fun parseOptionalDeadline(raw: String): Result<Int?> = runCatching {
    if (raw.equals("none", ignoreCase = true)) {
        null
    } else {
        raw.toInt().also {
            require(it >= 0) { "deadlineDays must be zero or positive" }
        }
    }
}

fun parseTaskLine(line: String, lineNumber: Int): Result<StudyTask> {
    val parts = line.split("|").map { it.trim() }
    if (parts.size != 6) {
        return Result.failure(
            IllegalArgumentException(
                "Line $lineNumber must contain 6 pipe-separated fields"
            )
        )
    }

    val title = parts[0]
    if (title.isBlank()) {
        return Result.failure(IllegalArgumentException("Line $lineNumber has an empty title"))
    }

    return runCatching {
        StudyTask(
            title = title,
            topic = parseTopicType(parts[1]).getOrElse {
                throw IllegalArgumentException("Line $lineNumber has an invalid topic: ${parts[1]}")
            },
            difficulty = parsePositiveInt(parts[2], "difficulty").getOrElse {
                throw IllegalArgumentException("Line $lineNumber has an invalid difficulty: ${parts[2]}")
            },
            estimatedMinutes = parsePositiveInt(parts[3], "estimatedMinutes").getOrElse {
                throw IllegalArgumentException("Line $lineNumber has an invalid estimatedMinutes: ${parts[3]}")
            },
            deadlineDays = parseOptionalDeadline(parts[4]).getOrElse {
                throw IllegalArgumentException("Line $lineNumber has an invalid deadlineDays: ${parts[4]}")
            },
            energyNeeded = parseEnergyLevel(parts[5]).getOrElse {
                throw IllegalArgumentException("Line $lineNumber has an invalid energy level: ${parts[5]}")
            }
        )
    }
}

fun loadTasksFromFile(filePath: String): Result<List<StudyTask>> = runCatching {
    val file = File(filePath)
    require(file.exists()) { "Task file does not exist: $filePath" }

    val parsedTasks = file.readLines()
        .mapIndexedNotNull { index, line ->
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#")) {
                null
            } else {
                parseTaskLine(trimmed, index + 1).getOrElse { throw it }
            }
        }

    require(parsedTasks.isNotEmpty()) { "No tasks were found in $filePath" }
    parsedTasks
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

fun buildPlanReport(plan: List<PlannedSession>, currentEnergy: EnergyLevel): String {
    val lines = mutableListOf<String>()
    lines += "Study Planner Report"
    lines += "===================="
    lines += "Energy level: $currentEnergy"
    lines += "Total tasks: ${plan.size}"
    lines += ""

    plan.forEachIndexed { index, session ->
        val task = session.task
        val deadlineText = task.deadlineDays?.let { "$it day(s)" } ?: "flexible"
        lines += "${index + 1}. ${task.title}"
        lines += "   topic=${task.topic}, score=${session.score}, block=${session.block}, time=${task.estimatedMinutes}m, deadline=$deadlineText"
    }

    lines += ""
    lines += "Block summary"
    plan.groupBy { it.block }.forEach { (block, sessions) ->
        val totalMinutes = sessions.sumOf { it.task.estimatedMinutes }
        lines += "- $block: ${sessions.size} task(s), $totalMinutes minutes"
    }

    return lines.joinToString("\n")
}

fun savePlanReport(path: String, content: String): Result<Unit> = runCatching {
    val file = File(path)
    file.parentFile?.mkdirs()
    file.writeText(content)
}

fun printUsage() {
    println("Study Planner CLI")
    println("Usage:")
    println("  --help")
    println("  --energy <LOW|MEDIUM|HIGH>")
    println("  --file <path>")
    println("  --save <path>")
    println()
    println("Examples:")
    println("  java -jar study-planner.jar")
    println("  java -jar study-planner.jar --energy HIGH")
    println("  java -jar study-planner.jar --energy LOW --file data/study_tasks.txt")
    println("  java -jar study-planner.jar --file data/study_tasks.txt --save reports/today.txt")
}

fun parseArgs(args: Array<String>): Result<PlannerOptions> = runCatching {
    var energy = EnergyLevel.MEDIUM
    var filePath: String? = null
    var savePath: String? = null
    var index = 0

    while (index < args.size) {
        when (args[index]) {
            "--help" -> {
                printUsage()
                throw IllegalStateException("HELP_REQUESTED")
            }
            "--energy" -> {
                require(index + 1 < args.size) { "Missing value after --energy" }
                energy = parseEnergyLevel(args[index + 1]).getOrElse {
                    throw IllegalArgumentException("Invalid energy level: ${args[index + 1]}")
                }
                index += 2
            }
            "--file" -> {
                require(index + 1 < args.size) { "Missing value after --file" }
                filePath = args[index + 1]
                index += 2
            }
            "--save" -> {
                require(index + 1 < args.size) { "Missing value after --save" }
                savePath = args[index + 1]
                index += 2
            }
            else -> {
                throw IllegalArgumentException("Unknown argument: ${args[index]}")
            }
        }
    }

    PlannerOptions(
        currentEnergy = energy,
        filePath = filePath,
        savePath = savePath
    )
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

fun resolveTasks(options: PlannerOptions): Result<List<StudyTask>> {
    return if (options.filePath == null) {
        Result.success(sampleTasks())
    } else {
        loadTasksFromFile(options.filePath)
    }
}

fun main(args: Array<String>) {
    val options = parseArgs(args).getOrElse { error ->
        if (error.message == "HELP_REQUESTED") {
            return
        }
        System.err.println("Argument error: ${error.message}")
        printUsage()
        return
    }

    val tasks = resolveTasks(options).getOrElse { error ->
        System.err.println("Input error: ${error.message}")
        return
    }

    val plan = buildPlan(tasks, options.currentEnergy)
    printPlan(plan)

    if (options.savePath != null) {
        val report = buildPlanReport(plan, options.currentEnergy)
        savePlanReport(options.savePath, report).getOrElse { error ->
            System.err.println("Save error: ${error.message}")
            return
        }
        println()
        println("Saved report to ${options.savePath}")
    }
}
