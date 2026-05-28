import java.io.File

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
            jsonString("block", session.block),
            "\"breakdown\":{" +
                jsonNumber("urgency", session.breakdown.urgency) + "," +
                jsonNumber("energyFit", session.breakdown.energyFit) + "," +
                jsonNumber("topicBonus", session.breakdown.topicBonus) + "," +
                jsonNumber("lengthPenalty", session.breakdown.lengthPenalty) + "," +
                jsonNumber("difficultyPenalty", session.breakdown.difficultyPenalty) +
                "}"
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
