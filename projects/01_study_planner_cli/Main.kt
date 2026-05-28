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
    val jsonFilePath: String?,
    val savePath: String?,
    val exportJsonPath: String?,
    val topicFilter: TopicType?,
    val topCount: Int?
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

fun applyPlannerOptions(
    tasks: List<StudyTask>,
    options: PlannerOptions
): Result<List<StudyTask>> = runCatching {
    val filtered = if (options.topicFilter == null) {
        tasks
    } else {
        tasks.filter { it.topic == options.topicFilter }
    }

    require(filtered.isNotEmpty()) {
        if (options.topicFilter == null) {
            "No tasks are available"
        } else {
            "No tasks matched topic ${options.topicFilter}"
        }
    }

    if (options.topCount == null) {
        filtered
    } else {
        filtered.take(options.topCount)
    }
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

fun jsonEscape(value: String): String {
    return buildString {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
}

fun jsonString(key: String, value: String): String {
    return "\"$key\":\"${jsonEscape(value)}\""
}

fun jsonNumber(key: String, value: Int): String {
    return "\"$key\":$value"
}

fun jsonNullableNumber(key: String, value: Int?): String {
    return if (value == null) "\"$key\":null" else "\"$key\":$value"
}

fun taskToJson(task: StudyTask): String {
    return listOf(
        jsonString("title", task.title),
        jsonString("topic", task.topic.name),
        jsonNumber("difficulty", task.difficulty),
        jsonNumber("estimatedMinutes", task.estimatedMinutes),
        jsonNullableNumber("deadlineDays", task.deadlineDays),
        jsonString("energyNeeded", task.energyNeeded.name)
    ).joinToString(prefix = "{", postfix = "}")
}

fun planToJson(plan: List<PlannedSession>, currentEnergy: EnergyLevel): String {
    val planItems = plan.joinToString(",\n    ") { session ->
        val task = session.task
        listOf(
            jsonString("title", task.title),
            jsonString("topic", task.topic.name),
            jsonNumber("difficulty", task.difficulty),
            jsonNumber("estimatedMinutes", task.estimatedMinutes),
            jsonNullableNumber("deadlineDays", task.deadlineDays),
            jsonString("energyNeeded", task.energyNeeded.name),
            jsonNumber("score", session.score),
            jsonString("block", session.block)
        ).joinToString(prefix = "{", postfix = "}")
    }

    return """
{
  "energyLevel": "${currentEnergy.name}",
  "count": ${plan.size},
  "plan": [
    $planItems
  ]
}
""".trimIndent()
}

fun saveJson(path: String, content: String): Result<Unit> = runCatching {
    val file = File(path)
    file.parentFile?.mkdirs()
    file.writeText(content)
}

fun parseJsonObject(rawObject: String): Map<String, String> {
    val cleaned = rawObject.removePrefix("{").removeSuffix("}")
    val parts = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var escapeNext = false

    cleaned.forEach { char ->
        when {
            escapeNext -> {
                current.append(char)
                escapeNext = false
            }
            char == '\\' -> {
                current.append(char)
                escapeNext = true
            }
            char == '"' -> {
                current.append(char)
                inQuotes = !inQuotes
            }
            char == ',' && !inQuotes -> {
                parts += current.toString()
                current.clear()
            }
            else -> current.append(char)
        }
    }

    if (current.isNotBlank()) {
        parts += current.toString()
    }

    return parts.associate { entry ->
        val pair = entry.split(":", limit = 2)
        require(pair.size == 2) { "Invalid JSON field: $entry" }
        val key = pair[0].trim().removeSurrounding("\"")
        val value = pair[1].trim()
        key to value
    }
}

fun parseJsonString(raw: String): String {
    return raw.removeSurrounding("\"")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
}

fun parseTaskJsonObject(rawObject: String, index: Int): Result<StudyTask> = runCatching {
    val fields = parseJsonObject(rawObject)

    val titleField = fields["title"] ?: error("Task $index is missing title")
    val topicField = fields["topic"] ?: error("Task $index is missing topic")
    val difficultyField = fields["difficulty"] ?: error("Task $index is missing difficulty")
    val estimatedMinutesField = fields["estimatedMinutes"] ?: error("Task $index is missing estimatedMinutes")
    val deadlineField = fields["deadlineDays"] ?: error("Task $index is missing deadlineDays")
    val energyField = fields["energyNeeded"] ?: error("Task $index is missing energyNeeded")

    StudyTask(
        title = parseJsonString(titleField),
        topic = parseTopicType(parseJsonString(topicField)).getOrElse {
            throw IllegalArgumentException("Task $index has an invalid topic")
        },
        difficulty = parsePositiveInt(difficultyField, "difficulty").getOrElse {
            throw IllegalArgumentException("Task $index has an invalid difficulty")
        },
        estimatedMinutes = parsePositiveInt(estimatedMinutesField, "estimatedMinutes").getOrElse {
            throw IllegalArgumentException("Task $index has an invalid estimatedMinutes")
        },
        deadlineDays = if (deadlineField == "null") {
            null
        } else {
            deadlineField.toInt().also {
                require(it >= 0) { "Task $index has a negative deadlineDays" }
            }
        },
        energyNeeded = parseEnergyLevel(parseJsonString(energyField)).getOrElse {
            throw IllegalArgumentException("Task $index has an invalid energyNeeded")
        }
    )
}

fun loadTasksFromJsonFile(filePath: String): Result<List<StudyTask>> = runCatching {
    val file = File(filePath)
    require(file.exists()) { "JSON task file does not exist: $filePath" }

    val content = file.readText().trim()
    require(content.startsWith("[") && content.endsWith("]")) {
        "JSON task file must contain an array of task objects"
    }

    val body = content.removePrefix("[").removeSuffix("]").trim()
    if (body.isBlank()) {
        return@runCatching emptyList()
    }

    val objects = mutableListOf<String>()
    val current = StringBuilder()
    var depth = 0
    var inQuotes = false
    var escapeNext = false

    body.forEach { char ->
        when {
            escapeNext -> {
                current.append(char)
                escapeNext = false
            }
            char == '\\' -> {
                current.append(char)
                escapeNext = true
            }
            char == '"' -> {
                current.append(char)
                inQuotes = !inQuotes
            }
            char == '{' && !inQuotes -> {
                depth += 1
                current.append(char)
            }
            char == '}' && !inQuotes -> {
                depth -= 1
                current.append(char)
                if (depth == 0) {
                    objects += current.toString().trim()
                    current.clear()
                }
            }
            char == ',' && !inQuotes && depth == 0 -> Unit
            else -> {
                if (!(current.isEmpty() && char.isWhitespace())) {
                    current.append(char)
                }
            }
        }
    }

    val tasks = objects.mapIndexed { index, rawObject ->
        parseTaskJsonObject(rawObject, index + 1).getOrElse { throw it }
    }

    require(tasks.isNotEmpty()) { "No tasks were found in $filePath" }
    tasks
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
    println("  --import-json <path>")
    println("  --save <path>")
    println("  --export-json <path>")
    println("  --topic <SYNTAX|OOP|COLLECTIONS|CONCURRENCY|TESTING>")
    println("  --top <count>")
    println()
    println("Examples:")
    println("  java -jar study-planner.jar")
    println("  java -jar study-planner.jar --energy HIGH")
    println("  java -jar study-planner.jar --energy LOW --file data/study_tasks.txt")
    println("  java -jar study-planner.jar --import-json data/study_tasks.json")
    println("  java -jar study-planner.jar --file data/study_tasks.txt --save reports/today.txt")
    println("  java -jar study-planner.jar --export-json reports/today.json")
    println("  java -jar study-planner.jar --topic COLLECTIONS --top 1")
}

fun parseArgs(args: Array<String>): Result<PlannerOptions> = runCatching {
    var energy = EnergyLevel.MEDIUM
    var filePath: String? = null
    var jsonFilePath: String? = null
    var savePath: String? = null
    var exportJsonPath: String? = null
    var topicFilter: TopicType? = null
    var topCount: Int? = null
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
            "--import-json" -> {
                require(index + 1 < args.size) { "Missing value after --import-json" }
                jsonFilePath = args[index + 1]
                index += 2
            }
            "--save" -> {
                require(index + 1 < args.size) { "Missing value after --save" }
                savePath = args[index + 1]
                index += 2
            }
            "--export-json" -> {
                require(index + 1 < args.size) { "Missing value after --export-json" }
                exportJsonPath = args[index + 1]
                index += 2
            }
            "--topic" -> {
                require(index + 1 < args.size) { "Missing value after --topic" }
                topicFilter = parseTopicType(args[index + 1]).getOrElse {
                    throw IllegalArgumentException("Invalid topic: ${args[index + 1]}")
                }
                index += 2
            }
            "--top" -> {
                require(index + 1 < args.size) { "Missing value after --top" }
                topCount = parsePositiveInt(args[index + 1], "top").getOrElse {
                    throw IllegalArgumentException("Invalid top count: ${args[index + 1]}")
                }
                index += 2
            }
            else -> {
                throw IllegalArgumentException("Unknown argument: ${args[index]}")
            }
        }
    }

    require(!(filePath != null && jsonFilePath != null)) {
        "Use either --file or --import-json, not both"
    }

    PlannerOptions(
        currentEnergy = energy,
        filePath = filePath,
        jsonFilePath = jsonFilePath,
        savePath = savePath,
        exportJsonPath = exportJsonPath,
        topicFilter = topicFilter,
        topCount = topCount
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
    val loadedTasks = when {
        options.filePath != null -> loadTasksFromFile(options.filePath)
        options.jsonFilePath != null -> loadTasksFromJsonFile(options.jsonFilePath)
        else -> {
            Result.success(sampleTasks())
        }
    }

    return loadedTasks.fold(
        onSuccess = { applyPlannerOptions(it, options) },
        onFailure = { Result.failure(it) }
    )
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

    if (options.exportJsonPath != null) {
        val json = planToJson(plan, options.currentEnergy)
        saveJson(options.exportJsonPath, json).getOrElse { error ->
            System.err.println("Export error: ${error.message}")
            return
        }
        println("Saved JSON to ${options.exportJsonPath}")
    }
}
