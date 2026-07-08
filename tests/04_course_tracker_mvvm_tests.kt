import java.io.File

fun newTempStateFile(prefix: String): File {
    return File.createTempFile(prefix, ".json")
}

fun testCourseTrackerLoadBuildsSummary() {
    val viewModel = buildCourseTrackerViewModel(newTempStateFile("course-tracker-load"))

    viewModel.dispatch(CourseTrackerIntent.Load)

    assertEquals(4, viewModel.state.summary.totalCourses, "total course count")
    assertEquals(1, viewModel.state.summary.completedCourses, "completed course count")
    assertEquals(155, viewModel.state.summary.remainingMinutes, "remaining minutes")
    assertEquals("Load", viewModel.state.lastIntent, "last intent after load")
    printTestSuccess("testCourseTrackerLoadBuildsSummary")
}

fun testCourseTrackerSearchNarrowsVisibleCourses() {
    val viewModel = buildCourseTrackerViewModel(newTempStateFile("course-tracker-search"))

    viewModel.dispatch(CourseTrackerIntent.Load)
    viewModel.dispatch(CourseTrackerIntent.Search("Architecture"))

    assertEquals(1, viewModel.state.courses.size, "architecture query count")
    assertEquals("course-4", viewModel.state.courses.first().id, "architecture query id")
    assertEquals("Search", viewModel.state.lastIntent, "last intent after search")
    printTestSuccess("testCourseTrackerSearchNarrowsVisibleCourses")
}

fun testCourseTrackerStatusUpdatesAffectSummary() {
    val viewModel = buildCourseTrackerViewModel(newTempStateFile("course-tracker-status"))

    viewModel.dispatch(CourseTrackerIntent.Load)
    viewModel.dispatch(CourseTrackerIntent.StartCourse("course-1"))
    viewModel.dispatch(CourseTrackerIntent.CompleteCourse("course-2"))

    assertEquals(2, viewModel.state.summary.completedCourses, "completed count after updates")
    assertEquals(95, viewModel.state.summary.remainingMinutes, "remaining minutes after updates")
    assertEquals("CompleteCourse", viewModel.state.lastIntent, "last intent after completion")
    printTestSuccess("testCourseTrackerStatusUpdatesAffectSummary")
}

fun testCourseTrackerBookmarkToggleChangesOrderingSignal() {
    val viewModel = buildCourseTrackerViewModel(newTempStateFile("course-tracker-bookmark"))

    viewModel.dispatch(CourseTrackerIntent.Load)
    viewModel.dispatch(CourseTrackerIntent.ToggleBookmark("course-3"))

    assertTrue(viewModel.state.courses.any { it.id == "course-3" && it.bookmarked }, "course-3 should be bookmarked")
    assertEquals(3, viewModel.state.summary.bookmarkedCourses, "bookmarked course count")
    assertEquals("ToggleBookmark", viewModel.state.lastIntent, "last intent after bookmark")
    printTestSuccess("testCourseTrackerBookmarkToggleChangesOrderingSignal")
}

fun testCourseTrackerPersistsAndRestoresLocalProgress() {
    val persistenceFile = newTempStateFile("course-tracker-persist")
    persistenceFile.delete()

    val firstViewModel = buildCourseTrackerViewModel(persistenceFile)
    firstViewModel.dispatch(CourseTrackerIntent.Load)
    firstViewModel.dispatch(CourseTrackerIntent.ToggleBookmark("course-3"))
    firstViewModel.dispatch(CourseTrackerIntent.CompleteCourse("course-2"))
    firstViewModel.dispatch(CourseTrackerIntent.SaveProgress)

    assertTrue(persistenceFile.exists(), "persistence file should exist")

    val secondViewModel = buildCourseTrackerViewModel(persistenceFile)
    secondViewModel.dispatch(CourseTrackerIntent.Load)

    assertContains(secondViewModel.state.statusMessage, "restored", "restored status message")
    assertEquals("Load", secondViewModel.state.lastIntent, "last intent after restore")
    assertTrue(
        secondViewModel.state.courses.any { it.id == "course-3" && it.bookmarked },
        "bookmarked course should be restored"
    )
    assertTrue(
        secondViewModel.state.courses.any { it.id == "course-2" && it.status == CourseStatus.COMPLETED },
        "completed course should be restored"
    )
    printTestSuccess("testCourseTrackerPersistsAndRestoresLocalProgress")
}

fun testCourseTrackerReducerBuildsStateFromMutation() {
    val initialState = CourseTrackerState(
        courses = emptyList(),
        summary = CourseSummary(
            totalCourses = 0,
            visibleCourses = 0,
            completedCourses = 0,
            bookmarkedCourses = 0,
            remainingMinutes = 0
        ),
        query = "",
        filter = CourseStatusFilter.ALL,
        statusMessage = "Initial",
        persistencePath = "/tmp/test.json",
        lastIntent = "InitialState"
    )

    val reducedState = CourseTrackerReducer.reduce(
        currentState = initialState,
        mutation = CourseTrackerMutation.Content(
            query = "Kotlin",
            filter = CourseStatusFilter.IN_PROGRESS,
            statusMessage = "Reducer applied",
            lastIntent = "Search"
        )
    ) { query, filter ->
        CourseTrackerState(
            courses = listOf(
                LearningCourse(
                    id = "reduced-1",
                    title = "Kotlin Reducer",
                    category = "Architecture",
                    level = CourseLevel.INTERMEDIATE,
                    estimatedMinutes = 40,
                    status = CourseStatus.IN_PROGRESS,
                    bookmarked = false
                )
            ),
            summary = CourseSummary(
                totalCourses = 1,
                visibleCourses = 1,
                completedCourses = 0,
                bookmarkedCourses = 0,
                remainingMinutes = 40
            ),
            query = query,
            filter = filter,
            statusMessage = "Generated",
            persistencePath = "/tmp/test.json",
            lastIntent = "Generated"
        )
    }

    assertEquals("Kotlin", reducedState.query, "reduced query")
    assertEquals(CourseStatusFilter.IN_PROGRESS, reducedState.filter, "reduced filter")
    assertEquals("Reducer applied", reducedState.statusMessage, "reduced status message")
    assertEquals("Search", reducedState.lastIntent, "reduced last intent")
    assertEquals(1, reducedState.courses.size, "reduced course count")
    printTestSuccess("testCourseTrackerReducerBuildsStateFromMutation")
}

fun main() {
    testCourseTrackerLoadBuildsSummary()
    testCourseTrackerSearchNarrowsVisibleCourses()
    testCourseTrackerStatusUpdatesAffectSummary()
    testCourseTrackerBookmarkToggleChangesOrderingSignal()
    testCourseTrackerPersistsAndRestoresLocalProgress()
    testCourseTrackerReducerBuildsStateFromMutation()
    println("All course tracker MVVM tests passed.")
}
