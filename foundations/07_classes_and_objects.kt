class StudySession(
    val topic: String,
    var minutes: Int
) {
    fun extend(extraMinutes: Int) {
        minutes += extraMinutes
    }

    fun summary(): String {
        return "$topic for $minutes minutes"
    }
}

object StudyTracker {
    var totalSessions: Int = 0

    fun registerSession() {
        totalSessions += 1
    }
}

fun main() {
    val session = StudySession("Kotlin classes", 25)
    StudyTracker.registerSession()
    session.extend(10)

    println(session.summary())
    println("Tracked sessions: ${StudyTracker.totalSessions}")
}
