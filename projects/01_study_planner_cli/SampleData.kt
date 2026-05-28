fun sampleTasks(): List<StudyTask> = listOf(
    StudyTask(
        title = "Review null safety operators",
        topic = TopicType.SYNTAX,
        difficulty = 2,
        estimatedMinutes = 20,
        deadlineDays = 1,
        energyNeeded = EnergyLevel.LOW
    ),
    StudyTask(
        title = "Implement a sealed result model",
        topic = TopicType.OOP,
        difficulty = 4,
        estimatedMinutes = 35,
        deadlineDays = 3,
        energyNeeded = EnergyLevel.MEDIUM
    ),
    StudyTask(
        title = "Solve collection transformation drills",
        topic = TopicType.COLLECTIONS,
        difficulty = 3,
        estimatedMinutes = 30,
        deadlineDays = 0,
        energyNeeded = EnergyLevel.MEDIUM
    ),
    StudyTask(
        title = "Read coroutine cancellation notes",
        topic = TopicType.CONCURRENCY,
        difficulty = 5,
        estimatedMinutes = 50,
        deadlineDays = null,
        energyNeeded = EnergyLevel.HIGH
    ),
    StudyTask(
        title = "Write tests for planner scoring",
        topic = TopicType.TESTING,
        difficulty = 3,
        estimatedMinutes = 25,
        deadlineDays = 2,
        energyNeeded = EnergyLevel.MEDIUM
    )
)
