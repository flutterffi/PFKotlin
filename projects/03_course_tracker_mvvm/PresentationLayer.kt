object CourseTrackerReducer {
    fun reduce(
        currentState: CourseTrackerState,
        mutation: CourseTrackerMutation,
        buildState: (String, CourseStatusFilter) -> CourseTrackerState
    ): CourseTrackerState {
        return when (mutation) {
            is CourseTrackerMutation.Content -> {
                buildState(mutation.query, mutation.filter).copy(
                    statusMessage = mutation.statusMessage,
                    lastIntent = mutation.lastIntent.ifBlank { currentState.lastIntent }
                )
            }
        }
    }
}

class CourseTrackerViewModel(
    private val syncCoursesUseCase: SyncCoursesUseCase,
    private val loadCoursesFromLocalUseCase: LoadCoursesFromLocalUseCase,
    private val updateCourseStatusUseCase: UpdateCourseStatusUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val saveCoursesUseCase: SaveCoursesUseCase,
    private val buildStateUseCase: BuildCourseTrackerStateUseCase
) {
    private var query: String = ""
    private var filter: CourseStatusFilter = CourseStatusFilter.ALL

    var state: CourseTrackerState = buildStateUseCase.execute(query, filter)
        private set

    fun dispatch(intent: CourseTrackerIntent) {
        when (intent) {
            CourseTrackerIntent.Load -> {
                val restored = loadCoursesFromLocalUseCase.execute()
                if (restored) {
                    reduce(
                        CourseTrackerMutation.Content(
                            query = query,
                            filter = filter,
                            statusMessage = "Progress restored from disk",
                            lastIntent = "Load"
                        )
                    )
                } else {
                    syncCoursesUseCase.execute()
                    saveCoursesUseCase.execute()
                    reduce(
                        CourseTrackerMutation.Content(
                            query = query,
                            filter = filter,
                            statusMessage = "Catalog synced from remote and cached",
                            lastIntent = "Load"
                        )
                    )
                }
            }

            CourseTrackerIntent.SaveProgress -> {
                saveCoursesUseCase.execute()
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        filter = filter,
                        statusMessage = "Progress saved to disk",
                        lastIntent = "SaveProgress"
                    )
                )
            }

            is CourseTrackerIntent.Search -> {
                query = intent.query.trim()
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        filter = filter,
                        statusMessage = "Search updated",
                        lastIntent = "Search"
                    )
                )
            }

            is CourseTrackerIntent.Filter -> {
                filter = intent.filter
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        filter = filter,
                        statusMessage = "Filter changed to ${intent.filter}",
                        lastIntent = "Filter"
                    )
                )
            }

            is CourseTrackerIntent.StartCourse -> {
                updateCourseStatusUseCase.startCourse(intent.id)
                saveCoursesUseCase.execute()
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        filter = filter,
                        statusMessage = "Started ${intent.id}",
                        lastIntent = "StartCourse"
                    )
                )
            }

            is CourseTrackerIntent.CompleteCourse -> {
                updateCourseStatusUseCase.completeCourse(intent.id)
                saveCoursesUseCase.execute()
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        filter = filter,
                        statusMessage = "Completed ${intent.id}",
                        lastIntent = "CompleteCourse"
                    )
                )
            }

            is CourseTrackerIntent.ToggleBookmark -> {
                toggleBookmarkUseCase.execute(intent.id)
                saveCoursesUseCase.execute()
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        filter = filter,
                        statusMessage = "Bookmark toggled for ${intent.id}",
                        lastIntent = "ToggleBookmark"
                    )
                )
            }
        }
    }

    private fun reduce(mutation: CourseTrackerMutation) {
        state = CourseTrackerReducer.reduce(state, mutation, buildStateUseCase::execute)
    }
}

object CourseTrackerConsoleView {
    fun render(state: CourseTrackerState): String {
        val lines = mutableListOf<String>()
        lines += "== Course Tracker =="
        lines += "status: ${state.statusMessage}"
        lines += "query: ${state.query.ifBlank { "<none>" }}"
        lines += "filter: ${state.filter}"
        lines += "last-intent: ${state.lastIntent}"
        lines += "cache: ${state.persistencePath ?: "<none>"}"
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
