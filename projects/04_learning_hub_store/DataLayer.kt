import java.io.File

interface LearningHubRepository {
    fun allLessons(): List<HubLesson>
    fun replaceLessons(lessons: List<HubLesson>)
    fun updateLesson(updatedLesson: HubLesson)
}

interface LearningHubLocalStore {
    fun exists(): Boolean
    fun loadLessons(): List<HubLesson>
    fun saveLessons(lessons: List<HubLesson>)
    fun path(): String
}

interface LearningHubRemoteSource {
    fun fetchLessons(): List<HubLesson>
}

class InMemoryLearningHubRepository(
    initialLessons: List<HubLesson> = emptyList()
) : LearningHubRepository {
    private val lessons = initialLessons.toMutableList()

    override fun allLessons(): List<HubLesson> = lessons.toList()

    override fun replaceLessons(lessons: List<HubLesson>) {
        this.lessons.clear()
        this.lessons.addAll(lessons)
    }

    override fun updateLesson(updatedLesson: HubLesson) {
        val index = lessons.indexOfFirst { it.id == updatedLesson.id }
        if (index >= 0) {
            lessons[index] = updatedLesson
        }
    }
}

class FileLearningHubLocalStore(
    private val file: File
) : LearningHubLocalStore {
    override fun exists(): Boolean = file.exists()

    override fun loadLessons(): List<HubLesson> {
        if (!file.exists()) {
            return emptyList()
        }
        return LearningHubJsonCodec.decodeLessons(file.readText())
    }

    override fun saveLessons(lessons: List<HubLesson>) {
        file.parentFile?.mkdirs()
        file.writeText(LearningHubJsonCodec.encodeLessons(lessons))
    }

    override fun path(): String = file.absolutePath
}

class FakeLearningHubRemoteSource(
    private val shouldFail: Boolean = false
) : LearningHubRemoteSource {
    override fun fetchLessons(): List<HubLesson> {
        if (shouldFail) {
            throw IllegalStateException("Remote catalog is unavailable")
        }

        return listOf(
            HubLesson("lesson-1", "Kotlin Types Drill", LessonTrack.FOUNDATIONS, 1, 20, false, true),
            HubLesson("lesson-2", "Android State Holder", LessonTrack.ANDROID, 4, 55, false, false),
            HubLesson("lesson-3", "Ktor Routing Practice", LessonTrack.BACKEND, 3, 45, false, false),
            HubLesson("lesson-4", "Reducer Design Lab", LessonTrack.ARCHITECTURE, 5, 65, true, true),
            HubLesson("lesson-5", "Offline First Repository", LessonTrack.ARCHITECTURE, 5, 60, false, false)
        )
    }
}

object LearningHubJsonCodec {
    fun encodeLessons(lessons: List<HubLesson>): String {
        return lessons.joinToString(
            prefix = "[\n",
            postfix = "\n]",
            separator = ",\n"
        ) { lesson ->
            """
            |  {
            |    "id": "${escape(lesson.id)}",
            |    "title": "${escape(lesson.title)}",
            |    "track": "${lesson.track}",
            |    "difficulty": ${lesson.difficulty},
            |    "estimatedMinutes": ${lesson.estimatedMinutes},
            |    "completed": ${lesson.completed},
            |    "bookmarked": ${lesson.bookmarked}
            |  }
            """.trimMargin()
        }
    }

    fun decodeLessons(json: String): List<HubLesson> {
        val objectMatches = Regex("\\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL).findAll(json)
        return objectMatches.map { match ->
            val block = match.value
            HubLesson(
                id = readString(block, "id"),
                title = readString(block, "title"),
                track = LessonTrack.valueOf(readString(block, "track")),
                difficulty = readInt(block, "difficulty"),
                estimatedMinutes = readInt(block, "estimatedMinutes"),
                completed = readBoolean(block, "completed"),
                bookmarked = readBoolean(block, "bookmarked")
            )
        }.toList()
    }

    private fun readString(block: String, key: String): String {
        val pattern = Regex("\"$key\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")
        val value = pattern.find(block)?.groupValues?.get(1)
            ?: throw IllegalStateException("Missing string field: $key")
        return unescape(value)
    }

    private fun readInt(block: String, key: String): Int {
        val pattern = Regex("\"$key\"\\s*:\\s*(\\d+)")
        val value = pattern.find(block)?.groupValues?.get(1)
            ?: throw IllegalStateException("Missing int field: $key")
        return value.toInt()
    }

    private fun readBoolean(block: String, key: String): Boolean {
        val pattern = Regex("\"$key\"\\s*:\\s*(true|false)")
        val value = pattern.find(block)?.groupValues?.get(1)
            ?: throw IllegalStateException("Missing boolean field: $key")
        return value.toBoolean()
    }

    private fun escape(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    private fun unescape(text: String): String {
        return text
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }
}
