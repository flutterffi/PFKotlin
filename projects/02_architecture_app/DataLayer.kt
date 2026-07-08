interface TopicRepository {
    fun listTopics(): List<LearningTopic>
    fun replaceTopics(topics: List<LearningTopic>)
    fun markCompleted(id: String): LearningTopic?
}

class InMemoryTopicRepository(
    private var topics: List<LearningTopic> = emptyList()
) : TopicRepository {
    override fun listTopics(): List<LearningTopic> {
        return topics
    }

    override fun replaceTopics(topics: List<LearningTopic>) {
        this.topics = topics
    }

    override fun markCompleted(id: String): LearningTopic? {
        var updated: LearningTopic? = null

        topics = topics.map { topic ->
            if (topic.id == id) {
                updated = topic.copy(completed = true)
                updated!!
            } else {
                topic
            }
        }

        return updated
    }
}

interface TopicRemoteSource {
    fun fetchTopics(): List<LearningTopic>
}

class FakeTopicRemoteSource : TopicRemoteSource {
    override fun fetchTopics(): List<LearningTopic> {
        return listOf(
            LearningTopic("topic-1", "Kotlin functions", 2, 20, false),
            LearningTopic("topic-2", "Collections practice", 3, 30, false),
            LearningTopic("topic-3", "Sealed classes", 4, 35, false),
            LearningTopic("topic-4", "Architecture layers", 5, 45, false)
        )
    }
}
