fun testCourseTrackerLoadBuildsSummary() {
    val viewModel = buildCourseTrackerViewModel()

    viewModel.dispatch(CourseTrackerAction.Load)

    assertEquals(4, viewModel.state.summary.totalCourses, "total course count")
    assertEquals(1, viewModel.state.summary.completedCourses, "completed course count")
    assertEquals(155, viewModel.state.summary.remainingMinutes, "remaining minutes")
    printTestSuccess("testCourseTrackerLoadBuildsSummary")
}

fun testCourseTrackerSearchNarrowsVisibleCourses() {
    val viewModel = buildCourseTrackerViewModel()

    viewModel.dispatch(CourseTrackerAction.Load)
    viewModel.dispatch(CourseTrackerAction.Search("Architecture"))

    assertEquals(1, viewModel.state.courses.size, "architecture query count")
    assertEquals("course-4", viewModel.state.courses.first().id, "architecture query id")
    printTestSuccess("testCourseTrackerSearchNarrowsVisibleCourses")
}

fun testCourseTrackerStatusUpdatesAffectSummary() {
    val viewModel = buildCourseTrackerViewModel()

    viewModel.dispatch(CourseTrackerAction.Load)
    viewModel.dispatch(CourseTrackerAction.StartCourse("course-1"))
    viewModel.dispatch(CourseTrackerAction.CompleteCourse("course-2"))

    assertEquals(2, viewModel.state.summary.completedCourses, "completed count after updates")
    assertEquals(95, viewModel.state.summary.remainingMinutes, "remaining minutes after updates")
    printTestSuccess("testCourseTrackerStatusUpdatesAffectSummary")
}

fun testCourseTrackerBookmarkToggleChangesOrderingSignal() {
    val viewModel = buildCourseTrackerViewModel()

    viewModel.dispatch(CourseTrackerAction.Load)
    viewModel.dispatch(CourseTrackerAction.ToggleBookmark("course-3"))

    assertTrue(viewModel.state.courses.any { it.id == "course-3" && it.bookmarked }, "course-3 should be bookmarked")
    assertEquals(3, viewModel.state.summary.bookmarkedCourses, "bookmarked course count")
    printTestSuccess("testCourseTrackerBookmarkToggleChangesOrderingSignal")
}

fun main() {
    testCourseTrackerLoadBuildsSummary()
    testCourseTrackerSearchNarrowsVisibleCourses()
    testCourseTrackerStatusUpdatesAffectSummary()
    testCourseTrackerBookmarkToggleChangesOrderingSignal()
    println("All course tracker MVVM tests passed.")
}
