class SyncTopicsUseCase(
    private val remoteSource: TopicRemoteSource,
    private val repository: TopicRepository
) {
    fun execute(): List<LearningTopic> {
        val topics = remoteSource.fetchTopics()
        repository.replaceTopics(topics)
        return topics
    }
}

class CompleteTopicUseCase(
    private val repository: TopicRepository
) {
    fun execute(id: String): LearningTopic? {
        return repository.markCompleted(id)
    }
}

class BuildDashboardUseCase(
    private val repository: TopicRepository
) {
    fun execute(maxDifficulty: Int? = null): DashboardState {
        val topics = repository.listTopics()
            .filter { maxDifficulty == null || it.difficulty <= maxDifficulty }

        val cards = topics.map { topic ->
            DashboardCard(
                title = topic.title,
                subtitle = "Difficulty ${topic.difficulty} • ${topic.recommendedMinutes} min",
                highlight = if (topic.completed) "Completed" else "Pending"
            )
        }

        return DashboardState(
            cards = cards,
            pendingCount = topics.count { !it.completed },
            totalMinutes = topics.filter { !it.completed }.sumOf { it.recommendedMinutes },
            isLoading = false,
            statusMessage = if (topics.isEmpty()) "No topics available" else "Dashboard ready"
        )
    }
}
