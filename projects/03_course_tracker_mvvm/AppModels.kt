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
    val filter: CourseStatusFilter,
    val statusMessage: String
)

sealed class CourseTrackerAction {
    data object Load : CourseTrackerAction()
    data class Search(val query: String) : CourseTrackerAction()
    data class Filter(val filter: CourseStatusFilter) : CourseTrackerAction()
    data class StartCourse(val id: String) : CourseTrackerAction()
    data class CompleteCourse(val id: String) : CourseTrackerAction()
    data class ToggleBookmark(val id: String) : CourseTrackerAction()
}
