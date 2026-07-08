fun testArchitectureAppRefreshBuildsDashboard() {
    val repository = InMemoryTopicRepository()
    val remoteSource = FakeTopicRemoteSource()
    val viewModel = DashboardViewModel(
        syncTopicsUseCase = SyncTopicsUseCase(remoteSource, repository),
        completeTopicUseCase = CompleteTopicUseCase(repository),
        buildDashboardUseCase = BuildDashboardUseCase(repository)
    )

    viewModel.dispatch(DashboardAction.Refresh)

    assertEquals(4, viewModel.state.cards.size, "dashboard card count")
    assertEquals(4, viewModel.state.pendingCount, "pending count after refresh")
    assertContains(viewModel.state.statusMessage, "synced", "refresh status")
    printTestSuccess("testArchitectureAppRefreshBuildsDashboard")
}

fun testArchitectureAppCompletionReducesPendingCount() {
    val repository = InMemoryTopicRepository()
    val remoteSource = FakeTopicRemoteSource()
    val viewModel = DashboardViewModel(
        syncTopicsUseCase = SyncTopicsUseCase(remoteSource, repository),
        completeTopicUseCase = CompleteTopicUseCase(repository),
        buildDashboardUseCase = BuildDashboardUseCase(repository)
    )

    viewModel.dispatch(DashboardAction.Refresh)
    viewModel.dispatch(DashboardAction.CompleteTopic("topic-2"))

    assertEquals(3, viewModel.state.pendingCount, "pending count after completion")
    assertContains(viewModel.state.statusMessage, "Completed Collections practice", "completion status")
    printTestSuccess("testArchitectureAppCompletionReducesPendingCount")
}

fun testArchitectureAppFilterByDifficultyLimitsCards() {
    val repository = InMemoryTopicRepository()
    val remoteSource = FakeTopicRemoteSource()
    val viewModel = DashboardViewModel(
        syncTopicsUseCase = SyncTopicsUseCase(remoteSource, repository),
        completeTopicUseCase = CompleteTopicUseCase(repository),
        buildDashboardUseCase = BuildDashboardUseCase(repository)
    )

    viewModel.dispatch(DashboardAction.Refresh)
    viewModel.dispatch(DashboardAction.FilterByDifficulty(3))

    assertEquals(2, viewModel.state.cards.size, "filtered dashboard size")
    assertContains(viewModel.state.statusMessage, "<= 3", "filter status")
    printTestSuccess("testArchitectureAppFilterByDifficultyLimitsCards")
}

fun main() {
    testArchitectureAppRefreshBuildsDashboard()
    testArchitectureAppCompletionReducesPendingCount()
    testArchitectureAppFilterByDifficultyLimitsCards()
    println("All architecture app tests passed.")
}
