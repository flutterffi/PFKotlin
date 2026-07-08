class CourseTrackerViewModel(
    private val syncCoursesUseCase: SyncCoursesUseCase,
    private val updateCourseStatusUseCase: UpdateCourseStatusUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val buildStateUseCase: BuildCourseTrackerStateUseCase
) {
    private var query: String = ""
    private var filter: CourseStatusFilter = CourseStatusFilter.ALL

    var state: CourseTrackerState = buildStateUseCase.execute(query, filter)
        private set

    fun dispatch(action: CourseTrackerAction) {
        when (action) {
            CourseTrackerAction.Load -> {
                syncCoursesUseCase.execute()
                refresh("Catalog synced")
            }

            is CourseTrackerAction.Search -> {
                query = action.query.trim()
                refresh("Search updated")
            }

            is CourseTrackerAction.Filter -> {
                filter = action.filter
                refresh("Filter changed to ${action.filter}")
            }

            is CourseTrackerAction.StartCourse -> {
                updateCourseStatusUseCase.startCourse(action.id)
                refresh("Started ${action.id}")
            }

            is CourseTrackerAction.CompleteCourse -> {
                updateCourseStatusUseCase.completeCourse(action.id)
                refresh("Completed ${action.id}")
            }

            is CourseTrackerAction.ToggleBookmark -> {
                toggleBookmarkUseCase.execute(action.id)
                refresh("Bookmark toggled for ${action.id}")
            }
        }
    }

    private fun refresh(message: String) {
        state = buildStateUseCase.execute(query, filter).copy(statusMessage = message)
    }
}

object CourseTrackerConsoleView {
    fun render(state: CourseTrackerState): String {
        val lines = mutableListOf<String>()
        lines += "== Course Tracker =="
        lines += "status: ${state.statusMessage}"
        lines += "query: ${state.query.ifBlank { "<none>" }}"
        lines += "filter: ${state.filter}"
        lines += "summary: total=${state.summary.totalCourses}, visible=${state.summary.visibleCourses}, completed=${state.summary.completedCourses}, bookmarked=${state.summary.bookmarkedCourses}, remaining=${state.summary.remainingMinutes}m"

        if (state.courses.isEmpty()) {
            lines += "No courses match the current search."
        } else {
            state.courses.forEachIndexed { index, course ->
                val bookmarkMark = if (course.bookmarked) "*" else "-"
                lines += "${index + 1}. [$bookmarkMark] ${course.title} | ${course.category} | ${course.level} | ${course.status} | ${course.estimatedMinutes}m"
            }
        }

        return lines.joinToString("\n")
    }
}
