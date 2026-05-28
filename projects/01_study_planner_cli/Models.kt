enum class TopicType {
    SYNTAX,
    OOP,
    COLLECTIONS,
    CONCURRENCY,
    TESTING
}

enum class EnergyLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class StudyTask(
    val title: String,
    val topic: TopicType,
    val difficulty: Int,
    val estimatedMinutes: Int,
    val deadlineDays: Int?,
    val energyNeeded: EnergyLevel
)

data class PlannerOptions(
    val currentEnergy: EnergyLevel,
    val filePath: String?,
    val jsonFilePath: String?,
    val savePath: String?,
    val exportJsonPath: String?,
    val explain: Boolean,
    val topicFilter: TopicType?,
    val topCount: Int?
)

data class ScoreBreakdown(
    val urgency: Int,
    val energyFit: Int,
    val topicBonus: Int,
    val lengthPenalty: Int,
    val difficultyPenalty: Int
) {
    fun totalScore(): Int {
        return urgency + energyFit + topicBonus - lengthPenalty - difficultyPenalty
    }

    fun asLine(): String {
        return "urgency=+$urgency, energy=+$energyFit, topic=+$topicBonus, length=-$lengthPenalty, difficulty=-$difficultyPenalty"
    }
}

data class PlannedSession(
    val task: StudyTask,
    val score: Int,
    val block: String,
    val breakdown: ScoreBreakdown
)
