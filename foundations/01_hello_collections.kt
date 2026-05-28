fun main() {
    val learner = "Plato"
    val topics = listOf("variables", "functions", "collections", "classes")
    val topicLengths = topics.associateWith { it.length }

    println("Hello, $learner. Welcome to Kotlin practice.")
    println("Topics: ${topics.joinToString()}")
    println("Longest topic: ${topics.maxByOrNull { it.length }}")
    println("Topic lengths: $topicLengths")
}
