import java.io.File

fun newTempStateFile(prefix: String): File {
    return File.createTempFile(prefix, ".json")
}

fun testCourseTrackerLoadBuildsSummary() {
    val viewModel = buildCourseTrackerViewModel(newTempStateFile("course-tracker-load"))

    viewModel.dispatch(CourseTrackerAction.Load)

    assertEquals(4, viewModel.state.summary.totalCourses, "total course count")
    assertEquals(1, viewModel.state.summary.completedCourses, "completed course count")
    assertEquals(155, viewModel.state.summary.remainingMinutes, "remaining minutes")
    printTestSuccess("testCourseTrackerLoadBuildsSummary")
}

fun testCourseTrackerSearchNarrowsVisibleCourses() {
    val viewModel = buildCourseTrackerViewModel(newTempStateFile("course-tracker-search"))

    viewModel.dispatch(CourseTrackerAction.Load)
    viewModel.dispatch(CourseTrackerAction.Search("Architecture"))

    assertEquals(1, viewModel.state.courses.size, "architecture query count")
    assertEquals("course-4", viewModel.state.courses.first().id, "architecture query id")
    printTestSuccess("testCourseTrackerSearchNarrowsVisibleCourses")
}

fun testCourseTrackerStatusUpdatesAffectSummary() {
    val viewModel = buildCourseTrackerViewModel(newTempStateFile("course-tracker-status"))

    viewModel.dispatch(CourseTrackerAction.Load)
    viewModel.dispatch(CourseTrackerAction.StartCourse("course-1"))
    viewModel.dispatch(CourseTrackerAction.CompleteCourse("course-2"))

    assertEquals(2, viewModel.state.summary.completedCourses, "completed count after updates")
    assertEquals(95, viewModel.state.summary.remainingMinutes, "remaining minutes after updates")
    printTestSuccess("testCourseTrackerStatusUpdatesAffectSummary")
}

fun testCourseTrackerBookmarkToggleChangesOrderingSignal() {
    val viewModel = buildCourseTrackerViewModel(newTempStateFile("course-tracker-bookmark"))

    viewModel.dispatch(CourseTrackerAction.Load)
    viewModel.dispatch(CourseTrackerAction.ToggleBookmark("course-3"))

    assertTrue(viewModel.state.courses.any { it.id == "course-3" && it.bookmarked }, "course-3 should be bookmarked")
    assertEquals(3, viewModel.state.summary.bookmarkedCourses, "bookmarked course count")
    printTestSuccess("testCourseTrackerBookmarkToggleChangesOrderingSignal")
}

fun testCourseTrackerPersistsAndRestoresLocalProgress() {
    val persistenceFile = newTempStateFile("course-tracker-persist")
    persistenceFile.delete()

    val firstViewModel = buildCourseTrackerViewModel(persistenceFile)
    firstViewModel.dispatch(CourseTrackerAction.Load)
    firstViewModel.dispatch(CourseTrackerAction.ToggleBookmark("course-3"))
    firstViewModel.dispatch(CourseTrackerAction.CompleteCourse("course-2"))
    firstViewModel.dispatch(CourseTrackerAction.SaveProgress)

    assertTrue(persistenceFile.exists(), "persistence file should exist")

    val secondViewModel = buildCourseTrackerViewModel(persistenceFile)
    secondViewModel.dispatch(CourseTrackerAction.Load)

    assertContains(secondViewModel.state.statusMessage, "restored", "restored status message")
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

fun main() {
    testCourseTrackerLoadBuildsSummary()
    testCourseTrackerSearchNarrowsVisibleCourses()
    testCourseTrackerStatusUpdatesAffectSummary()
    testCourseTrackerBookmarkToggleChangesOrderingSignal()
    testCourseTrackerPersistsAndRestoresLocalProgress()
    println("All course tracker MVVM tests passed.")
}
