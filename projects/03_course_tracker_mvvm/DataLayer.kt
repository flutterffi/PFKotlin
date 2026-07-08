import java.io.File

interface CourseRepository {
    fun getCourses(): List<LearningCourse>
    fun replaceCourses(courses: List<LearningCourse>)
    fun updateCourse(updatedCourse: LearningCourse)
}

interface CourseRemoteSource {
    fun fetchCourses(): List<LearningCourse>
}

interface CourseLocalStore {
    fun exists(): Boolean
    fun loadCourses(): List<LearningCourse>
    fun saveCourses(courses: List<LearningCourse>)
    fun path(): String
}

class InMemoryCourseRepository(
    initialCourses: List<LearningCourse> = emptyList()
) : CourseRepository {
    private val courses = initialCourses.toMutableList()

    override fun getCourses(): List<LearningCourse> = courses.toList()

    override fun replaceCourses(courses: List<LearningCourse>) {
        this.courses.clear()
        this.courses.addAll(courses)
    }

    override fun updateCourse(updatedCourse: LearningCourse) {
        val index = courses.indexOfFirst { it.id == updatedCourse.id }
        if (index >= 0) {
            courses[index] = updatedCourse
        }
    }
}

class FakeCourseRemoteSource : CourseRemoteSource {
    override fun fetchCourses(): List<LearningCourse> = listOf(
        LearningCourse(
            id = "course-1",
            title = "Kotlin Basics Sprint",
            category = "Syntax",
            level = CourseLevel.BEGINNER,
            estimatedMinutes = 45,
            status = CourseStatus.PLANNED,
            bookmarked = true
        ),
        LearningCourse(
            id = "course-2",
            title = "Collections Workout",
            category = "Collections",
            level = CourseLevel.INTERMEDIATE,
            estimatedMinutes = 60,
            status = CourseStatus.IN_PROGRESS,
            bookmarked = false
        ),
        LearningCourse(
            id = "course-3",
            title = "Sealed Result Modeling",
            category = "Error Handling",
            level = CourseLevel.ADVANCED,
            estimatedMinutes = 50,
            status = CourseStatus.PLANNED,
            bookmarked = false
        ),
        LearningCourse(
            id = "course-4",
            title = "MVVM Architecture Practice",
            category = "Architecture",
            level = CourseLevel.ADVANCED,
            estimatedMinutes = 90,
            status = CourseStatus.COMPLETED,
            bookmarked = true
        )
    )
}

class FileCourseLocalStore(
    private val file: File
) : CourseLocalStore {
    override fun exists(): Boolean = file.exists()

    override fun loadCourses(): List<LearningCourse> {
        if (!file.exists()) {
            return emptyList()
        }

        return CourseJsonCodec.decodeCourses(file.readText())
    }

    override fun saveCourses(courses: List<LearningCourse>) {
        file.parentFile?.mkdirs()
        file.writeText(CourseJsonCodec.encodeCourses(courses))
    }

    override fun path(): String = file.absolutePath
}

object CourseJsonCodec {
    fun encodeCourses(courses: List<LearningCourse>): String {
        return courses.joinToString(
            prefix = "[\n",
            postfix = "\n]",
            separator = ",\n"
        ) { course ->
            """
            |  {
            |    "id": "${escape(course.id)}",
            |    "title": "${escape(course.title)}",
            |    "category": "${escape(course.category)}",
            |    "level": "${course.level}",
            |    "estimatedMinutes": ${course.estimatedMinutes},
            |    "status": "${course.status}",
            |    "bookmarked": ${course.bookmarked}
            |  }
            """.trimMargin()
        }
    }

    fun decodeCourses(json: String): List<LearningCourse> {
        val objectMatches = Regex("\\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL).findAll(json)
        return objectMatches.map { match ->
            val block = match.value
            LearningCourse(
                id = readString(block, "id"),
                title = readString(block, "title"),
                category = readString(block, "category"),
                level = CourseLevel.valueOf(readString(block, "level")),
                estimatedMinutes = readInt(block, "estimatedMinutes"),
                status = CourseStatus.valueOf(readString(block, "status")),
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
