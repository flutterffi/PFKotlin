import java.io.File

fun buildLearningHubStore(
    persistenceFile: File = File("data/learning_hub_snapshot.json"),
    taskRunner: LearningHubTaskRunner = ImmediateLearningHubTaskRunner,
    remoteSource: LearningHubRemoteSource = FakeLearningHubRemoteSource()
): LearningHubStore {
    val repository = InMemoryLearningHubRepository()
    val localStore = FileLearningHubLocalStore(persistenceFile)
    val bootstrapUseCase = BootstrapLearningHubUseCase(repository, localStore, remoteSource)
    val refreshUseCase = RefreshLearningHubUseCase(repository, localStore, remoteSource)
    val saveSnapshotUseCase = SaveLearningHubSnapshotUseCase(repository, localStore)
    val toggleBookmarkUseCase = ToggleHubBookmarkUseCase(repository)
    val completeLessonUseCase = CompleteHubLessonUseCase(repository)
    val buildStateUseCase = BuildLearningHubStateUseCase(repository, localStore)

    return LearningHubStore(
        bootstrapUseCase = bootstrapUseCase,
        refreshUseCase = refreshUseCase,
        saveSnapshotUseCase = saveSnapshotUseCase,
        toggleBookmarkUseCase = toggleBookmarkUseCase,
        completeLessonUseCase = completeLessonUseCase,
        buildStateUseCase = buildStateUseCase,
        taskRunner = taskRunner
    )
}

fun main() {
    val store = buildLearningHubStore()

    store.dispatch(LearningHubIntent.Bootstrap)
    println(LearningHubConsoleView.render(store.state))
    println()

    store.dispatch(LearningHubIntent.FilterTrack(LessonTrack.ARCHITECTURE))
    println(LearningHubConsoleView.render(store.state))
    println()

    store.dispatch(LearningHubIntent.OpenLesson("lesson-5"))
    println(LearningHubConsoleView.render(store.state))
    println()

    store.dispatch(LearningHubIntent.CompleteLesson("lesson-5"))
    println(LearningHubConsoleView.render(store.state))
    println()

    store.dispatch(LearningHubIntent.BackToDashboard)
    store.dispatch(LearningHubIntent.RefreshCatalog)
    println(LearningHubConsoleView.render(store.state))
}
