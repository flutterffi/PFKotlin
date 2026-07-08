enum class CourseLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class CourseStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED
}

data class LearningCourse(
    val id: String,
    val title: String,
    val category: String,
    val level: CourseLevel,
    val estimatedMinutes: Int,
    val status: CourseStatus,
    val bookmarked: Boolean
)

enum class CourseStatusFilter {
    ALL,
    PLANNED,
    IN_PROGRESS,
    COMPLETED
}

enum class CourseLevelFilter {
    ALL,
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

data class CourseSummary(
    val totalCourses: Int,
    val visibleCourses: Int,
    val completedCourses: Int,
    val bookmarkedCourses: Int,
    val remainingMinutes: Int
)

data class CourseTrackerState(
    val courses: List<LearningCourse>,
    val summary: CourseSummary,
    val query: String,
    val statusFilter: CourseStatusFilter,
    val levelFilter: CourseLevelFilter,
    val statusMessage: String,
    val persistencePath: String?,
    val lastIntent: String
)

sealed class CourseTrackerIntent {
    data object Load : CourseTrackerIntent()
    data class ImportCatalog(val path: String) : CourseTrackerIntent()
    data object SaveProgress : CourseTrackerIntent()
    data class Search(val query: String) : CourseTrackerIntent()
    data class FilterByStatus(val filter: CourseStatusFilter) : CourseTrackerIntent()
    data class FilterByLevel(val filter: CourseLevelFilter) : CourseTrackerIntent()
    data class StartCourse(val id: String) : CourseTrackerIntent()
    data class CompleteCourse(val id: String) : CourseTrackerIntent()
    data class ToggleBookmark(val id: String) : CourseTrackerIntent()
}

sealed class CourseTrackerMutation {
    data class Content(
        val query: String,
        val statusFilter: CourseStatusFilter,
        val levelFilter: CourseLevelFilter,
        val statusMessage: String,
        val lastIntent: String
    ) : CourseTrackerMutation()
}
