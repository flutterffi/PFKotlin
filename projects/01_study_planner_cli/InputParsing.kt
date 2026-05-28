import java.io.File

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
