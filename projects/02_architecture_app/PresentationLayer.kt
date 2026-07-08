class DashboardViewModel(
    private val syncTopicsUseCase: SyncTopicsUseCase,
    private val completeTopicUseCase: CompleteTopicUseCase,
    private val buildDashboardUseCase: BuildDashboardUseCase
) {
    var state: DashboardState = DashboardState()
        private set

    fun dispatch(action: DashboardAction) {
        state = state.copy(isLoading = true, statusMessage = "Working...")

        state = when (action) {
            DashboardAction.Refresh -> {
                syncTopicsUseCase.execute()
                buildDashboardUseCase.execute().copy(statusMessage = "Topics synced")
            }
            is DashboardAction.CompleteTopic -> {
                val updated = completeTopicUseCase.execute(action.id)
                buildDashboardUseCase.execute().copy(
                    statusMessage = if (updated == null) {
                        "Topic not found"
                    } else {
                        "Completed ${updated.title}"
                    }
                )
            }
            is DashboardAction.FilterByDifficulty -> {
                buildDashboardUseCase.execute(maxDifficulty = action.maxDifficulty).copy(
                    statusMessage = "Filtered by difficulty <= ${action.maxDifficulty}"
                )
            }
        }
    }
}

object DashboardConsoleView {
    fun render(state: DashboardState) {
        println("Architecture App Dashboard")
        println("--------------------------")
        println("Status: ${state.statusMessage}")
        println("Pending topics: ${state.pendingCount}")
        println("Remaining minutes: ${state.totalMinutes}")
        println()

        state.cards.forEachIndexed { index, card ->
            println("${index + 1}. ${card.title}")
            println("   ${card.subtitle}")
            println("   ${card.highlight}")
        }
    }
}
