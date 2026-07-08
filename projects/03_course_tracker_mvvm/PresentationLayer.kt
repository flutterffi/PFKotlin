object CourseTrackerReducer {
    fun reduce(
        currentState: CourseTrackerState,
        mutation: CourseTrackerMutation,
        buildState: (String, CourseStatusFilter, CourseLevelFilter, CourseSortOption) -> CourseTrackerState
    ): CourseTrackerState {
        return when (mutation) {
            is CourseTrackerMutation.Content -> {
                buildState(
                    mutation.query,
                    mutation.statusFilter,
                    mutation.levelFilter,
                    mutation.sortOption
                ).copy(
                    statusMessage = mutation.statusMessage,
                    errorMessage = mutation.errorMessage,
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
    private var sortOption: CourseSortOption = CourseSortOption.SMART

    var state: CourseTrackerState = buildStateUseCase.execute(query, statusFilter, levelFilter, sortOption)
        private set

    fun dispatch(intent: CourseTrackerIntent) {
        try {
            when (intent) {
                CourseTrackerIntent.Load -> {
                    val restored = loadCoursesFromLocalUseCase.execute()
                    if (restored) {
                        reduce(
                            CourseTrackerMutation.Content(
                                query = query,
                                statusFilter = statusFilter,
                                levelFilter = levelFilter,
                                sortOption = sortOption,
                                statusMessage = "Progress restored from disk",
                                errorMessage = null,
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
                                sortOption = sortOption,
                                statusMessage = "Catalog synced from remote and cached",
                                errorMessage = null,
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
                            sortOption = sortOption,
                            statusMessage = "Imported $importedCount courses from catalog",
                            errorMessage = null,
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
                            sortOption = sortOption,
                            statusMessage = "Progress saved to disk",
                            errorMessage = null,
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
                            sortOption = sortOption,
                            statusMessage = "Search updated",
                            errorMessage = null,
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
                            sortOption = sortOption,
                            statusMessage = "Status filter changed to ${intent.filter}",
                            errorMessage = null,
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
                            sortOption = sortOption,
                            statusMessage = "Level filter changed to ${intent.filter}",
                            errorMessage = null,
                            lastIntent = "FilterByLevel"
                        )
                    )
                }

                is CourseTrackerIntent.SortBy -> {
                    sortOption = intent.option
                    reduce(
                        CourseTrackerMutation.Content(
                            query = query,
                            statusFilter = statusFilter,
                            levelFilter = levelFilter,
                            sortOption = sortOption,
                            statusMessage = "Sort changed to ${intent.option}",
                            errorMessage = null,
                            lastIntent = "SortBy"
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
                            sortOption = sortOption,
                            statusMessage = "Started ${intent.id}",
                            errorMessage = null,
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
                            sortOption = sortOption,
                            statusMessage = "Completed ${intent.id}",
                            errorMessage = null,
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
                            sortOption = sortOption,
                            statusMessage = "Bookmark toggled for ${intent.id}",
                            errorMessage = null,
                            lastIntent = "ToggleBookmark"
                        )
                    )
                }
            }
        } catch (error: IllegalStateException) {
            state = state.copy(
                statusMessage = "Operation failed",
                errorMessage = error.message ?: "Unknown operation error",
                lastIntent = intent::class.simpleName ?: "UnknownIntent"
            )
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
        lines += "sort: ${state.sortOption}"
        lines += "last-intent: ${state.lastIntent}"
        lines += "cache: ${state.persistencePath ?: "<none>"}"
        if (state.errorMessage != null) {
            lines += "error: ${state.errorMessage}"
        }
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
