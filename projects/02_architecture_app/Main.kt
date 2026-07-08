fun main() {
    val repository = InMemoryTopicRepository()
    val remoteSource = FakeTopicRemoteSource()

    val viewModel = DashboardViewModel(
        syncTopicsUseCase = SyncTopicsUseCase(remoteSource, repository),
        completeTopicUseCase = CompleteTopicUseCase(repository),
        buildDashboardUseCase = BuildDashboardUseCase(repository)
    )

    viewModel.dispatch(DashboardAction.Refresh)
    DashboardConsoleView.render(viewModel.state)

    println()
    viewModel.dispatch(DashboardAction.CompleteTopic("topic-2"))
    DashboardConsoleView.render(viewModel.state)

    println()
    viewModel.dispatch(DashboardAction.FilterByDifficulty(3))
    DashboardConsoleView.render(viewModel.state)
}
