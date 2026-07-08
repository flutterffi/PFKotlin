import java.io.File

fun buildCourseTrackerViewModel(
    persistenceFile: File = File("data/course_tracker_state.json"),
    taskRunner: CourseTaskRunner = ImmediateCourseTaskRunner
): CourseTrackerViewModel {
    val repository = InMemoryCourseRepository()
    val remoteSource = FakeCourseRemoteSource()
    val catalogSource = FileCourseCatalogSource()
    val localStore = FileCourseLocalStore(persistenceFile)
    val syncCoursesUseCase = SyncCoursesUseCase(repository, remoteSource)
    val importCatalogUseCase = ImportCatalogUseCase(repository, catalogSource)
    val loadCoursesFromLocalUseCase = LoadCoursesFromLocalUseCase(repository, localStore)
    val updateCourseStatusUseCase = UpdateCourseStatusUseCase(repository)
    val toggleBookmarkUseCase = ToggleBookmarkUseCase(repository)
    val saveCoursesUseCase = SaveCoursesUseCase(repository, localStore)
    val buildStateUseCase = BuildCourseTrackerStateUseCase(repository, localStore)

    return CourseTrackerViewModel(
        syncCoursesUseCase = syncCoursesUseCase,
        importCatalogUseCase = importCatalogUseCase,
        loadCoursesFromLocalUseCase = loadCoursesFromLocalUseCase,
        updateCourseStatusUseCase = updateCourseStatusUseCase,
        toggleBookmarkUseCase = toggleBookmarkUseCase,
        saveCoursesUseCase = saveCoursesUseCase,
        buildStateUseCase = buildStateUseCase,
        taskRunner = taskRunner
    )
}

fun main() {
    val viewModel = buildCourseTrackerViewModel()

    viewModel.dispatch(CourseTrackerIntent.Load)
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerIntent.RefreshFromRemote)
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerIntent.ImportCatalog("data/course_catalog.json"))
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerIntent.Search("Architecture"))
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerIntent.FilterByStatus(CourseStatusFilter.COMPLETED))
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerIntent.FilterByLevel(CourseLevelFilter.ADVANCED))
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerIntent.SortBy(CourseSortOption.TITLE_ASC))
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerIntent.ToggleBookmark("course-3"))
    viewModel.dispatch(CourseTrackerIntent.Search(""))
    viewModel.dispatch(CourseTrackerIntent.FilterByStatus(CourseStatusFilter.ALL))
    viewModel.dispatch(CourseTrackerIntent.FilterByLevel(CourseLevelFilter.ALL))
    viewModel.dispatch(CourseTrackerIntent.SortBy(CourseSortOption.SMART))
    viewModel.dispatch(CourseTrackerIntent.StartCourse("course-1"))
    viewModel.dispatch(CourseTrackerIntent.CompleteCourse("course-2"))
    viewModel.dispatch(CourseTrackerIntent.SaveProgress)
    println(CourseTrackerConsoleView.render(viewModel.state))
}
