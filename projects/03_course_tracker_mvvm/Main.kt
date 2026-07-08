import java.io.File

fun buildCourseTrackerViewModel(
    persistenceFile: File = File("data/course_tracker_state.json")
): CourseTrackerViewModel {
    val repository = InMemoryCourseRepository()
    val remoteSource = FakeCourseRemoteSource()
    val localStore = FileCourseLocalStore(persistenceFile)
    val syncCoursesUseCase = SyncCoursesUseCase(repository, remoteSource)
    val loadCoursesFromLocalUseCase = LoadCoursesFromLocalUseCase(repository, localStore)
    val updateCourseStatusUseCase = UpdateCourseStatusUseCase(repository)
    val toggleBookmarkUseCase = ToggleBookmarkUseCase(repository)
    val saveCoursesUseCase = SaveCoursesUseCase(repository, localStore)
    val buildStateUseCase = BuildCourseTrackerStateUseCase(repository, localStore)

    return CourseTrackerViewModel(
        syncCoursesUseCase = syncCoursesUseCase,
        loadCoursesFromLocalUseCase = loadCoursesFromLocalUseCase,
        updateCourseStatusUseCase = updateCourseStatusUseCase,
        toggleBookmarkUseCase = toggleBookmarkUseCase,
        saveCoursesUseCase = saveCoursesUseCase,
        buildStateUseCase = buildStateUseCase
    )
}

fun main() {
    val viewModel = buildCourseTrackerViewModel()

    viewModel.dispatch(CourseTrackerAction.Load)
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerAction.Search("Architecture"))
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerAction.Filter(CourseStatusFilter.COMPLETED))
    println(CourseTrackerConsoleView.render(viewModel.state))
    println()

    viewModel.dispatch(CourseTrackerAction.ToggleBookmark("course-3"))
    viewModel.dispatch(CourseTrackerAction.Search(""))
    viewModel.dispatch(CourseTrackerAction.Filter(CourseStatusFilter.ALL))
    viewModel.dispatch(CourseTrackerAction.StartCourse("course-1"))
    viewModel.dispatch(CourseTrackerAction.CompleteCourse("course-2"))
    viewModel.dispatch(CourseTrackerAction.SaveProgress)
    println(CourseTrackerConsoleView.render(viewModel.state))
}
