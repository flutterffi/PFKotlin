data class LearningTopic(
    val id: String,
    val title: String,
    val difficulty: Int,
    val recommendedMinutes: Int,
    val completed: Boolean
)

data class DashboardCard(
    val title: String,
    val subtitle: String,
    val highlight: String
)

data class DashboardState(
    val cards: List<DashboardCard> = emptyList(),
    val pendingCount: Int = 0,
    val totalMinutes: Int = 0,
    val isLoading: Boolean = false,
    val statusMessage: String = "Idle"
)

sealed class DashboardAction {
    data object Refresh : DashboardAction()
    data class CompleteTopic(val id: String) : DashboardAction()
    data class FilterByDifficulty(val maxDifficulty: Int) : DashboardAction()
}
