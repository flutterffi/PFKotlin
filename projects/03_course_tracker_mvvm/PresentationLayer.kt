object CourseTrackerReducer {
    fun reduce(
        currentState: CourseTrackerState,
        mutation: CourseTrackerMutation,
        buildState: (String, CourseStatusFilter, CourseLevelFilter) -> CourseTrackerState
    ): CourseTrackerState {
        return when (mutation) {
            is CourseTrackerMutation.Content -> {
                buildState(mutation.query, mutation.statusFilter, mutation.levelFilter).copy(
                    statusMessage = mutation.statusMessage,
                    lastIntent = mutation.lastIntent.ifBlank { currentState.lastIntent }
                )
            }
        }
    }
}

class CourseTrackerViewModel(
    private val syncCoursesUseCase: SyncCoursesUseCase,
    private val importCatalogUseCase: ImportCatalogUseCase,
    private val loadCoursesFromLocalUseCase: LoadCoursesFromLocalUseCase,
    private val updateCourseStatusUseCase: UpdateCourseStatusUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val saveCoursesUseCase: SaveCoursesUseCase,
    private val buildStateUseCase: BuildCourseTrackerStateUseCase
) {
    private var query: String = ""
    private var statusFilter: CourseStatusFilter = CourseStatusFilter.ALL
    private var levelFilter: CourseLevelFilter = CourseLevelFilter.ALL

    var state: CourseTrackerState = buildStateUseCase.execute(query, statusFilter, levelFilter)
        private set

    fun dispatch(intent: CourseTrackerIntent) {
        when (intent) {
            CourseTrackerIntent.Load -> {
                val restored = loadCoursesFromLocalUseCase.execute()
                if (restored) {
                    reduce(
                        CourseTrackerMutation.Content(
                            query = query,
                            statusFilter = statusFilter,
                            levelFilter = levelFilter,
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
                            statusFilter = statusFilter,
                            levelFilter = levelFilter,
                            statusMessage = "Catalog synced from remote and cached",
                            lastIntent = "Load"
                        )
                    )
                }
            }

            is CourseTrackerIntent.ImportCatalog -> {
                val importedCount = importCatalogUseCase.execute(intent.path)
                saveCoursesUseCase.execute()
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
                        statusMessage = "Imported $importedCount courses from catalog",
                        lastIntent = "ImportCatalog"
                    )
                )
            }

            CourseTrackerIntent.SaveProgress -> {
                saveCoursesUseCase.execute()
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
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
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
                        statusMessage = "Search updated",
                        lastIntent = "Search"
                    )
                )
            }

            is CourseTrackerIntent.FilterByStatus -> {
                statusFilter = intent.filter
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
                        statusMessage = "Status filter changed to ${intent.filter}",
                        lastIntent = "FilterByStatus"
                    )
                )
            }

            is CourseTrackerIntent.FilterByLevel -> {
                levelFilter = intent.filter
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
                        statusMessage = "Level filter changed to ${intent.filter}",
                        lastIntent = "FilterByLevel"
                    )
                )
            }

            is CourseTrackerIntent.StartCourse -> {
                updateCourseStatusUseCase.startCourse(intent.id)
                saveCoursesUseCase.execute()
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
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
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
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
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
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
        lines += "status-filter: ${state.statusFilter}"
        lines += "level-filter: ${state.levelFilter}"
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
