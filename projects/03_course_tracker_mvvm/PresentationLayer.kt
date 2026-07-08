interface CourseTaskRunner {
    fun submit(task: () -> Unit)
}

object ImmediateCourseTaskRunner : CourseTaskRunner {
    override fun submit(task: () -> Unit) {
        task()
    }
}

class ControlledCourseTaskRunner : CourseTaskRunner {
    private val pendingTasks = ArrayDeque<() -> Unit>()

    override fun submit(task: () -> Unit) {
        pendingTasks.addLast(task)
    }

    fun pendingCount(): Int = pendingTasks.size

    fun runNext() {
        pendingTasks.removeFirstOrNull()?.invoke()
    }

    fun runAll() {
        while (pendingTasks.isNotEmpty()) {
            runNext()
        }
    }
}

object CourseTrackerReducer {
    fun reduce(
        currentState: CourseTrackerState,
        mutation: CourseTrackerMutation,
        buildState: (String, CourseStatusFilter, CourseLevelFilter, CourseSortOption) -> CourseTrackerState
    ): CourseTrackerState {
        return when (mutation) {
            is CourseTrackerMutation.Progress -> {
                currentState.copy(
                    isLoading = mutation.isLoading,
                    isRefreshing = mutation.isRefreshing,
                    statusMessage = mutation.statusMessage,
                    errorMessage = null,
                    lastIntent = mutation.lastIntent.ifBlank { currentState.lastIntent }
                )
            }

            is CourseTrackerMutation.Content -> {
                buildState(
                    mutation.query,
                    mutation.statusFilter,
                    mutation.levelFilter,
                    mutation.sortOption
                ).copy(
                    isLoading = mutation.isLoading,
                    isRefreshing = mutation.isRefreshing,
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
    private val buildStateUseCase: BuildCourseTrackerStateUseCase,
    private val taskRunner: CourseTaskRunner
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
                    reduce(
                        CourseTrackerMutation.Progress(
                            isLoading = true,
                            isRefreshing = false,
                            statusMessage = "Loading courses",
                            lastIntent = "Load"
                        )
                    )
                    taskRunner.submit {
                        completeLoad()
                    }
                }

                CourseTrackerIntent.RefreshFromRemote -> {
                    reduce(
                        CourseTrackerMutation.Progress(
                            isLoading = false,
                            isRefreshing = true,
                            statusMessage = "Refreshing catalog from remote",
                            lastIntent = "RefreshFromRemote"
                        )
                    )
                    taskRunner.submit {
                        completeRefresh()
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
                            isLoading = false,
                            isRefreshing = false,
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
                            isLoading = false,
                            isRefreshing = false,
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
                            isLoading = false,
                            isRefreshing = false,
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
                            isLoading = false,
                            isRefreshing = false,
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
                            isLoading = false,
                            isRefreshing = false,
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
                            isLoading = false,
                            isRefreshing = false,
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
                            isLoading = false,
                            isRefreshing = false,
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
                            isLoading = false,
                            isRefreshing = false,
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
                            isLoading = false,
                            isRefreshing = false,
                            statusMessage = "Bookmark toggled for ${intent.id}",
                            errorMessage = null,
                            lastIntent = "ToggleBookmark"
                        )
                    )
                }
            }
        } catch (error: IllegalStateException) {
            state = state.copy(
                isLoading = false,
                isRefreshing = false,
                statusMessage = "Operation failed",
                errorMessage = error.message ?: "Unknown operation error",
                lastIntent = intent::class.simpleName ?: "UnknownIntent"
            )
        }
    }

    private fun completeLoad() {
        try {
            val restored = loadCoursesFromLocalUseCase.execute()
            if (restored) {
                reduce(
                    CourseTrackerMutation.Content(
                        query = query,
                        statusFilter = statusFilter,
                        levelFilter = levelFilter,
                        sortOption = sortOption,
                        isLoading = false,
                        isRefreshing = false,
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
                        isLoading = false,
                        isRefreshing = false,
                        statusMessage = "Catalog synced from remote and cached",
                        errorMessage = null,
                        lastIntent = "Load"
                    )
                )
            }
        } catch (error: IllegalStateException) {
            state = state.copy(
                isLoading = false,
                isRefreshing = false,
                statusMessage = "Operation failed",
                errorMessage = error.message ?: "Unknown load error",
                lastIntent = "Load"
            )
        }
    }

    private fun completeRefresh() {
        try {
            syncCoursesUseCase.execute()
            saveCoursesUseCase.execute()
            reduce(
                CourseTrackerMutation.Content(
                    query = query,
                    statusFilter = statusFilter,
                    levelFilter = levelFilter,
                    sortOption = sortOption,
                    isLoading = false,
                    isRefreshing = false,
                    statusMessage = "Catalog refreshed from remote",
                    errorMessage = null,
                    lastIntent = "RefreshFromRemote"
                )
            )
        } catch (error: IllegalStateException) {
            state = state.copy(
                isLoading = false,
                isRefreshing = false,
                statusMessage = "Operation failed",
                errorMessage = error.message ?: "Unknown refresh error",
                lastIntent = "RefreshFromRemote"
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
        lines += "loading: ${state.isLoading}"
        lines += "refreshing: ${state.isRefreshing}"
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
