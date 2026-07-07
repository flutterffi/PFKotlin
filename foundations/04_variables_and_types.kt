fun main() {
    val learnerName: String = "Plato"
    var completedLessons = 3
    val dailyPracticeMinutes = 45
    val confidenceScore = 7.5
    val isReviewDay = false

    println("Learner: $learnerName")
    println("Completed lessons: $completedLessons")
    println("Practice minutes today: $dailyPracticeMinutes")
    println("Confidence score: $confidenceScore")
    println("Review day: $isReviewDay")

    completedLessons += 1
    val projectedHours = dailyPracticeMinutes / 60.0

    println("After one more lesson: $completedLessons")
    println("Projected hours today: $projectedHours")
}
