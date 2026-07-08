interface CourseRepository {
    fun getCourses(): List<LearningCourse>
    fun replaceCourses(courses: List<LearningCourse>)
    fun updateCourse(updatedCourse: LearningCourse)
}

interface CourseRemoteSource {
    fun fetchCourses(): List<LearningCourse>
}

class InMemoryCourseRepository(
    initialCourses: List<LearningCourse> = emptyList()
) : CourseRepository {
    private val courses = initialCourses.toMutableList()

    override fun getCourses(): List<LearningCourse> = courses.toList()

    override fun replaceCourses(courses: List<LearningCourse>) {
        this.courses.clear()
        this.courses.addAll(courses)
    }

    override fun updateCourse(updatedCourse: LearningCourse) {
        val index = courses.indexOfFirst { it.id == updatedCourse.id }
        if (index >= 0) {
            courses[index] = updatedCourse
        }
    }
}

class FakeCourseRemoteSource : CourseRemoteSource {
    override fun fetchCourses(): List<LearningCourse> = listOf(
        LearningCourse(
            id = "course-1",
            title = "Kotlin Basics Sprint",
            category = "Syntax",
            level = CourseLevel.BEGINNER,
            estimatedMinutes = 45,
            status = CourseStatus.PLANNED,
            bookmarked = true
        ),
        LearningCourse(
            id = "course-2",
            title = "Collections Workout",
            category = "Collections",
            level = CourseLevel.INTERMEDIATE,
            estimatedMinutes = 60,
            status = CourseStatus.IN_PROGRESS,
            bookmarked = false
        ),
        LearningCourse(
            id = "course-3",
            title = "Sealed Result Modeling",
            category = "Error Handling",
            level = CourseLevel.ADVANCED,
            estimatedMinutes = 50,
            status = CourseStatus.PLANNED,
            bookmarked = false
        ),
        LearningCourse(
            id = "course-4",
            title = "MVVM Architecture Practice",
            category = "Architecture",
            level = CourseLevel.ADVANCED,
            estimatedMinutes = 90,
            status = CourseStatus.COMPLETED,
            bookmarked = true
        )
    )
}
