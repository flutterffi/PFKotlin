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

enum class CourseSortOption {
    SMART,
    TITLE_ASC,
    MINUTES_ASC,
    LEVEL_DESC
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
    val sortOption: CourseSortOption,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val statusMessage: String,
    val errorMessage: String?,
    val persistencePath: String?,
    val lastIntent: String
)

sealed class CourseTrackerIntent {
    data object Load : CourseTrackerIntent()
    data object RefreshFromRemote : CourseTrackerIntent()
    data class ImportCatalog(val path: String) : CourseTrackerIntent()
    data object SaveProgress : CourseTrackerIntent()
    data class Search(val query: String) : CourseTrackerIntent()
    data class FilterByStatus(val filter: CourseStatusFilter) : CourseTrackerIntent()
    data class FilterByLevel(val filter: CourseLevelFilter) : CourseTrackerIntent()
    data class SortBy(val option: CourseSortOption) : CourseTrackerIntent()
    data class StartCourse(val id: String) : CourseTrackerIntent()
    data class CompleteCourse(val id: String) : CourseTrackerIntent()
    data class ToggleBookmark(val id: String) : CourseTrackerIntent()
}

sealed class CourseTrackerMutation {
    data class Progress(
        val isLoading: Boolean,
        val isRefreshing: Boolean,
        val statusMessage: String,
        val lastIntent: String
    ) : CourseTrackerMutation()

    data class Content(
        val query: String,
        val statusFilter: CourseStatusFilter,
        val levelFilter: CourseLevelFilter,
        val sortOption: CourseSortOption,
        val isLoading: Boolean,
        val isRefreshing: Boolean,
        val statusMessage: String,
        val errorMessage: String?,
        val lastIntent: String
    ) : CourseTrackerMutation()
}
