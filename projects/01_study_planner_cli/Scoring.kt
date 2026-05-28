fun StudyTask.urgencyScore(): Int = when (deadlineDays) {
    null -> 5
    0 -> 40
    1 -> 30
    in 2..3 -> 20
    else -> 10
}

fun StudyTask.energyScore(currentEnergy: EnergyLevel): Int = when {
    currentEnergy == EnergyLevel.HIGH && energyNeeded == EnergyLevel.HIGH -> 20
    currentEnergy == EnergyLevel.LOW && energyNeeded == EnergyLevel.HIGH -> 4
    currentEnergy == energyNeeded -> 16
    else -> 10
}

fun StudyTask.topicBonus(): Int = when (topic) {
    TopicType.SYNTAX -> 12
    TopicType.COLLECTIONS -> 14
    TopicType.OOP -> 10
    TopicType.CONCURRENCY -> 18
    TopicType.TESTING -> 16
}

fun StudyTask.lengthPenalty(): Int = estimatedMinutes / 10

fun StudyTask.planBlock(): String = when {
    estimatedMinutes <= 20 -> "Quick Win"
    estimatedMinutes <= 40 -> "Core Focus"
    else -> "Deep Work"
}

fun StudyTask.scoreBreakdown(currentEnergy: EnergyLevel): ScoreBreakdown {
    return ScoreBreakdown(
        urgency = urgencyScore(),
        energyFit = energyScore(currentEnergy),
        topicBonus = topicBonus(),
        lengthPenalty = lengthPenalty(),
        difficultyPenalty = difficulty
    )
}

fun buildPlan(tasks: List<StudyTask>, currentEnergy: EnergyLevel): List<PlannedSession> {
    return tasks
        .map { task ->
            val breakdown = task.scoreBreakdown(currentEnergy)

            PlannedSession(
                task = task,
                score = breakdown.totalScore(),
                block = task.planBlock(),
                breakdown = breakdown
            )
        }
        .sortedWith(
            compareByDescending<PlannedSession> { it.score }
                .thenBy { it.task.estimatedMinutes }
                .thenBy { it.task.title }
        )
}

fun applyPlannerOptions(
    tasks: List<StudyTask>,
    options: PlannerOptions
): Result<List<StudyTask>> = runCatching {
    val filtered = if (options.topicFilter == null) {
        tasks
    } else {
        tasks.filter { it.topic == options.topicFilter }
    }

    require(filtered.isNotEmpty()) {
        if (options.topicFilter == null) {
            "No tasks are available"
        } else {
            "No tasks matched topic ${options.topicFilter}"
        }
    }

    if (options.topCount == null) {
        filtered
    } else {
        filtered.take(options.topCount)
    }
}
