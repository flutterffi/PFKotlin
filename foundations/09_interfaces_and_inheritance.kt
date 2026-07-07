interface PracticeItem {
    fun description(): String
}

open class Lesson(
    private val title: String
) : PracticeItem {
    override fun description(): String {
        return "Lesson: $title"
    }
}

class TimedLesson(
    title: String,
    private val minutes: Int
) : Lesson(title) {
    override fun description(): String {
        return "${super.description()} for $minutes minutes"
    }
}

fun main() {
    val items: List<PracticeItem> = listOf(
        Lesson("Null safety review"),
        TimedLesson("Collection transformations", 30)
    )

    items.forEach { item ->
        println(item.description())
    }
}
