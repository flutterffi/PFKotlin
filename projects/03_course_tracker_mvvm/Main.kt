fun buildCourseTrackerViewModel(): CourseTrackerViewModel {
    val repository = InMemoryCourseRepository()
    val remoteSource = FakeCourseRemoteSource()
    val syncCoursesUseCase = SyncCoursesUseCase(repository, remoteSource)
    val updateCourseStatusUseCase = UpdateCourseStatusUseCase(repository)
    val toggleBookmarkUseCase = ToggleBookmarkUseCase(repository)
    val buildStateUseCase = BuildCourseTrackerStateUseCase(repository)

    return CourseTrackerViewModel(
        syncCoursesUseCase = syncCoursesUseCase,
        updateCourseStatusUseCase = updateCourseStatusUseCase,
        toggleBookmarkUseCase = toggleBookmarkUseCase,
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
    println(CourseTrackerConsoleView.render(viewModel.state))
}
