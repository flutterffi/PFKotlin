class SyncCoursesUseCase(
    private val repository: CourseRepository,
    private val remoteSource: CourseRemoteSource
) {
    fun execute() {
        repository.replaceCourses(remoteSource.fetchCourses())
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
        filter: CourseStatusFilter
    ): CourseTrackerState {
        val allCourses = repository.getCourses()
        val visibleCourses = allCourses
            .filter { course ->
                val matchesQuery = query.isBlank() ||
                    course.title.contains(query, ignoreCase = true) ||
                    course.category.contains(query, ignoreCase = true)
                val matchesFilter = when (filter) {
                    CourseStatusFilter.ALL -> true
                    CourseStatusFilter.PLANNED -> course.status == CourseStatus.PLANNED
                    CourseStatusFilter.IN_PROGRESS -> course.status == CourseStatus.IN_PROGRESS
                    CourseStatusFilter.COMPLETED -> course.status == CourseStatus.COMPLETED
                }
                matchesQuery && matchesFilter
            }
            .sortedWith(
                compareByDescending<LearningCourse> { it.bookmarked }
                    .thenByDescending { it.status == CourseStatus.IN_PROGRESS }
                    .thenBy { it.estimatedMinutes }
            )

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
            filter = filter,
            statusMessage = "Loaded ${visibleCourses.size} courses",
            persistencePath = localStore.path()
        )
    }
}
