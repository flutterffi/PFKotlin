class SyncCoursesUseCase(
    private val repository: CourseRepository,
    private val remoteSource: CourseRemoteSource
) {
    fun execute() {
        repository.replaceCourses(remoteSource.fetchCourses())
    }
}

class ImportCatalogUseCase(
    private val repository: CourseRepository,
    private val catalogSource: CourseCatalogSource
) {
    fun execute(path: String): Int {
        val courses = catalogSource.fetchCatalog(path)
        repository.replaceCourses(courses)
        return courses.size
    }
}

class LoadCoursesFromLocalUseCase(
    private val repository: CourseRepository,
    private val localStore: CourseLocalStore
) {
    fun execute(): Boolean {
        if (!localStore.exists()) {
            return false
        }

        val courses = localStore.loadCourses()
        if (courses.isEmpty()) {
            return false
        }

        repository.replaceCourses(courses)
        return true
    }
}

class UpdateCourseStatusUseCase(
    private val repository: CourseRepository
) {
    fun startCourse(id: String) {
        val course = repository.getCourses().firstOrNull { it.id == id } ?: return
        repository.updateCourse(course.copy(status = CourseStatus.IN_PROGRESS))
    }

    fun completeCourse(id: String) {
        val course = repository.getCourses().firstOrNull { it.id == id } ?: return
        repository.updateCourse(course.copy(status = CourseStatus.COMPLETED))
    }
}

class SaveCoursesUseCase(
    private val repository: CourseRepository,
    private val localStore: CourseLocalStore
) {
    fun execute() {
        localStore.saveCourses(repository.getCourses())
    }
}

class ToggleBookmarkUseCase(
    private val repository: CourseRepository
) {
    fun execute(id: String) {
        val course = repository.getCourses().firstOrNull { it.id == id } ?: return
        repository.updateCourse(course.copy(bookmarked = !course.bookmarked))
    }
}

class BuildCourseTrackerStateUseCase(
    private val repository: CourseRepository,
    private val localStore: CourseLocalStore
) {
    fun execute(
        query: String,
        statusFilter: CourseStatusFilter,
        levelFilter: CourseLevelFilter,
        sortOption: CourseSortOption
    ): CourseTrackerState {
        val allCourses = repository.getCourses()
        val filteredCourses = allCourses
            .filter { course ->
                val matchesQuery = query.isBlank() ||
                    course.title.contains(query, ignoreCase = true) ||
                    course.category.contains(query, ignoreCase = true)
                val matchesStatusFilter = when (statusFilter) {
                    CourseStatusFilter.ALL -> true
                    CourseStatusFilter.PLANNED -> course.status == CourseStatus.PLANNED
                    CourseStatusFilter.IN_PROGRESS -> course.status == CourseStatus.IN_PROGRESS
                    CourseStatusFilter.COMPLETED -> course.status == CourseStatus.COMPLETED
                }
                val matchesLevelFilter = when (levelFilter) {
                    CourseLevelFilter.ALL -> true
                    CourseLevelFilter.BEGINNER -> course.level == CourseLevel.BEGINNER
                    CourseLevelFilter.INTERMEDIATE -> course.level == CourseLevel.INTERMEDIATE
                    CourseLevelFilter.ADVANCED -> course.level == CourseLevel.ADVANCED
                }
                matchesQuery && matchesStatusFilter && matchesLevelFilter
            }
        val visibleCourses = sortCourses(filteredCourses, sortOption)

        val summary = CourseSummary(
            totalCourses = allCourses.size,
            visibleCourses = visibleCourses.size,
            completedCourses = allCourses.count { it.status == CourseStatus.COMPLETED },
            bookmarkedCourses = allCourses.count { it.bookmarked },
            remainingMinutes = allCourses
                .filter { it.status != CourseStatus.COMPLETED }
                .sumOf { it.estimatedMinutes }
        )

        return CourseTrackerState(
            courses = visibleCourses,
            summary = summary,
            query = query,
            statusFilter = statusFilter,
            levelFilter = levelFilter,
            sortOption = sortOption,
            statusMessage = "Loaded ${visibleCourses.size} courses",
            errorMessage = null,
            persistencePath = localStore.path(),
            lastIntent = "InitialState"
        )
    }

    private fun sortCourses(
        courses: List<LearningCourse>,
        sortOption: CourseSortOption
    ): List<LearningCourse> {
        return when (sortOption) {
            CourseSortOption.SMART -> courses.sortedWith(
                compareByDescending<LearningCourse> { it.bookmarked }
                    .thenByDescending { it.status == CourseStatus.IN_PROGRESS }
                    .thenBy { it.estimatedMinutes }
            )

            CourseSortOption.TITLE_ASC -> courses.sortedBy { it.title }
            CourseSortOption.MINUTES_ASC -> courses.sortedBy { it.estimatedMinutes }
            CourseSortOption.LEVEL_DESC -> courses.sortedWith(
                compareByDescending<LearningCourse> { it.level.ordinal }
                    .thenBy { it.title }
            )
        }
    }
}
