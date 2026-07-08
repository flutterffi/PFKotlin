fun main() {
    val previewState = CourseTrackerState(
        courses = listOf(
            LearningCourse(
                id = "preview-1",
                title = "Preview Kotlin Flow",
                category = "Concurrency",
                level = CourseLevel.ADVANCED,
                estimatedMinutes = 70,
                status = CourseStatus.IN_PROGRESS,
                bookmarked = true
            ),
            LearningCourse(
                id = "preview-2",
                title = "Preview Collections Refresh",
                category = "Collections",
                level = CourseLevel.INTERMEDIATE,
                estimatedMinutes = 35,
                status = CourseStatus.PLANNED,
                bookmarked = false
            )
        ),
        summary = CourseSummary(
            totalCourses = 2,
            visibleCourses = 2,
            completedCourses = 0,
            bookmarkedCourses = 1,
            remainingMinutes = 105
        ),
        query = "",
        filter = CourseStatusFilter.ALL,
        statusMessage = "Preview state"
    )

    println(CourseTrackerConsoleView.render(previewState))
}
