import java.io.File

fun newLearningHubSnapshot(prefix: String): File {
    return File.createTempFile(prefix, ".json")
}

fun testLearningHubBootstrapLoadsRemoteCatalog() {
    val snapshot = newLearningHubSnapshot("learning-hub-bootstrap")
    snapshot.delete()
    val store = buildLearningHubStore(snapshot)

    store.dispatch(LearningHubIntent.Bootstrap)

    assertEquals(5, store.state.summary.totalLessons, "bootstrap lesson count")
    assertEquals(SyncStage.IDLE, store.state.syncStage, "bootstrap sync stage")
    assertContains(store.state.statusMessage, "Bootstrapped", "bootstrap status message")
    printTestSuccess("testLearningHubBootstrapLoadsRemoteCatalog")
}

fun testLearningHubBootstrapShowsIntermediateProgress() {
    val snapshot = newLearningHubSnapshot("learning-hub-progress")
    snapshot.delete()
    val runner = ControlledLearningHubTaskRunner()
    val store = buildLearningHubStore(snapshot, runner)

    store.dispatch(LearningHubIntent.Bootstrap)

    assertEquals(SyncStage.BOOTSTRAPPING, store.state.syncStage, "progress sync stage")
    assertEquals(1, runner.pendingCount(), "progress pending tasks")

    runner.runAll()

    assertEquals(SyncStage.IDLE, store.state.syncStage, "progress final sync stage")
    assertEquals(5, store.state.summary.totalLessons, "progress final lessons")
    printTestSuccess("testLearningHubBootstrapShowsIntermediateProgress")
}

fun testLearningHubRestoresCachedSnapshot() {
    val snapshot = newLearningHubSnapshot("learning-hub-cache")
    snapshot.delete()

    val firstStore = buildLearningHubStore(snapshot)
    firstStore.dispatch(LearningHubIntent.Bootstrap)
    firstStore.dispatch(LearningHubIntent.CompleteLesson("lesson-2"))

    val secondStore = buildLearningHubStore(snapshot)
    secondStore.dispatch(LearningHubIntent.Bootstrap)

    assertTrue(secondStore.state.lessons.any { it.id == "lesson-2" && it.completed }, "cached completion restored")
    assertContains(secondStore.state.statusMessage, "Restored", "cache restore status")
    printTestSuccess("testLearningHubRestoresCachedSnapshot")
}

fun testLearningHubCanNavigateToDetailAndBack() {
    val snapshot = newLearningHubSnapshot("learning-hub-route")
    snapshot.delete()
    val store = buildLearningHubStore(snapshot)

    store.dispatch(LearningHubIntent.Bootstrap)
    store.dispatch(LearningHubIntent.OpenLesson("lesson-5"))

    assertEquals(HubRoute.LessonDetail("lesson-5"), store.state.route, "detail route")
    assertEquals("lesson-5", store.state.selectedLesson?.id, "selected lesson id")

    store.dispatch(LearningHubIntent.BackToDashboard)

    assertEquals(HubRoute.Dashboard, store.state.route, "dashboard route")
    printTestSuccess("testLearningHubCanNavigateToDetailAndBack")
}

fun testLearningHubCanFilterTrack() {
    val snapshot = newLearningHubSnapshot("learning-hub-filter")
    snapshot.delete()
    val store = buildLearningHubStore(snapshot)

    store.dispatch(LearningHubIntent.Bootstrap)
    store.dispatch(LearningHubIntent.FilterTrack(LessonTrack.ARCHITECTURE))

    assertEquals(2, store.state.visibleLessons.size, "architecture visible count")
    assertTrue(store.state.visibleLessons.all { it.track == LessonTrack.ARCHITECTURE }, "architecture filter match")
    printTestSuccess("testLearningHubCanFilterTrack")
}

fun testLearningHubCompletionCreatesNotice() {
    val snapshot = newLearningHubSnapshot("learning-hub-complete")
    snapshot.delete()
    val store = buildLearningHubStore(snapshot)

    store.dispatch(LearningHubIntent.Bootstrap)
    store.dispatch(LearningHubIntent.CompleteLesson("lesson-5"))

    assertTrue(store.state.lessons.any { it.id == "lesson-5" && it.completed }, "lesson 5 completed")
    assertContains(store.state.notice?.message ?: "", "completed", "completion notice")
    assertEquals(1, store.state.pendingSyncCount, "pending sync count after completion")
    printTestSuccess("testLearningHubCompletionCreatesNotice")
}

fun testLearningHubRefreshFailureMovesToFailedState() {
    val snapshot = newLearningHubSnapshot("learning-hub-failure")
    snapshot.delete()
    val store = buildLearningHubStore(
        persistenceFile = snapshot,
        remoteSource = FakeLearningHubRemoteSource(shouldFail = true)
    )

    store.dispatch(LearningHubIntent.Bootstrap)

    assertEquals(SyncStage.FAILED, store.state.syncStage, "failure sync stage")
    assertContains(store.state.errorMessage ?: "", "unavailable", "failure error message")
    printTestSuccess("testLearningHubRefreshFailureMovesToFailedState")
}

fun testLearningHubObserverReceivesInitialAndUpdatedStates() {
    val snapshot = newLearningHubSnapshot("learning-hub-observer")
    snapshot.delete()
    val runner = ControlledLearningHubTaskRunner()
    val store = buildLearningHubStore(snapshot, runner)
    val observedIntents = mutableListOf<String>()

    val subscription = store.observe(LearningHubStateObserver { state ->
        observedIntents += state.lastIntent
    })

    store.dispatch(LearningHubIntent.Bootstrap)
    runner.runAll()
    store.dispatch(LearningHubIntent.FilterTrack(LessonTrack.ARCHITECTURE))

    assertEquals("InitialState", observedIntents.first(), "first observed state")
    assertTrue(observedIntents.contains("Bootstrap"), "observer should receive bootstrap states")
    assertTrue(observedIntents.contains("FilterTrack"), "observer should receive filter state")

    subscription.cancel()
    store.dispatch(LearningHubIntent.BackToDashboard)
    assertEquals(4, observedIntents.size, "observer event count after cancel")
    printTestSuccess("testLearningHubObserverReceivesInitialAndUpdatedStates")
}

fun testLearningHubObserverSeesRefreshProgressAndCompletion() {
    val snapshot = newLearningHubSnapshot("learning-hub-refresh-observer")
    snapshot.delete()
    val runner = ControlledLearningHubTaskRunner()
    val store = buildLearningHubStore(snapshot, runner)
    val syncStages = mutableListOf<SyncStage>()

    store.observe(LearningHubStateObserver { state ->
        syncStages += state.syncStage
    })

    store.dispatch(LearningHubIntent.Bootstrap)
    runner.runAll()
    store.dispatch(LearningHubIntent.RefreshCatalog)

    assertEquals(SyncStage.REFRESHING, store.state.syncStage, "refresh observer intermediate state")
    runner.runAll()

    assertTrue(syncStages.contains(SyncStage.REFRESHING), "observer saw refreshing stage")
    assertEquals(SyncStage.IDLE, syncStages.last(), "observer saw final idle stage")
    printTestSuccess("testLearningHubObserverSeesRefreshProgressAndCompletion")
}

fun testLearningHubCanSwitchConflictStrategy() {
    val snapshot = newLearningHubSnapshot("learning-hub-strategy")
    snapshot.delete()
    val store = buildLearningHubStore(snapshot)

    store.dispatch(LearningHubIntent.SetConflictStrategy(ConflictStrategy.REMOTE_WINS))

    assertEquals(ConflictStrategy.REMOTE_WINS, store.state.conflictStrategy, "conflict strategy state")
    assertEquals("SetConflictStrategy", store.state.lastIntent, "last intent after strategy change")
    printTestSuccess("testLearningHubCanSwitchConflictStrategy")
}

fun testLearningHubRefreshClearsPendingChangesAfterMerge() {
    val snapshot = newLearningHubSnapshot("learning-hub-merge")
    snapshot.delete()
    val store = buildLearningHubStore(snapshot)

    store.dispatch(LearningHubIntent.Bootstrap)
    store.dispatch(LearningHubIntent.CompleteLesson("lesson-5"))
    store.dispatch(LearningHubIntent.ToggleBookmark("lesson-3"))

    assertEquals(2, store.state.pendingSyncCount, "pending changes before refresh")

    store.dispatch(LearningHubIntent.RefreshCatalog)

    assertEquals(0, store.state.pendingSyncCount, "pending changes after refresh")
    assertTrue(store.state.lessons.any { it.id == "lesson-5" && it.completed }, "completion should survive merge")
    assertTrue(store.state.lessons.any { it.id == "lesson-3" && it.bookmarked }, "bookmark should survive merge")
    printTestSuccess("testLearningHubRefreshClearsPendingChangesAfterMerge")
}

fun testLearningHubEventChannelReceivesOneShotEvents() {
    val snapshot = newLearningHubSnapshot("learning-hub-events")
    snapshot.delete()
    val store = buildLearningHubStore(snapshot)
    val eventNames = mutableListOf<String>()

    store.observeEvents(LearningHubEventObserver { event ->
        eventNames += event.name
    })

    store.dispatch(LearningHubIntent.Bootstrap)
    store.dispatch(LearningHubIntent.CompleteLesson("lesson-5"))
    store.dispatch(LearningHubIntent.RefreshCatalog)

    assertTrue(eventNames.contains("bootstrap_complete"), "bootstrap event should be emitted")
    assertTrue(eventNames.contains("lesson_completed"), "completion event should be emitted")
    assertTrue(eventNames.contains("refresh_complete"), "refresh event should be emitted")
    printTestSuccess("testLearningHubEventChannelReceivesOneShotEvents")
}

fun main() {
    testLearningHubBootstrapLoadsRemoteCatalog()
    testLearningHubBootstrapShowsIntermediateProgress()
    testLearningHubRestoresCachedSnapshot()
    testLearningHubCanNavigateToDetailAndBack()
    testLearningHubCanFilterTrack()
    testLearningHubCompletionCreatesNotice()
    testLearningHubRefreshFailureMovesToFailedState()
    testLearningHubObserverReceivesInitialAndUpdatedStates()
    testLearningHubObserverSeesRefreshProgressAndCompletion()
    testLearningHubCanSwitchConflictStrategy()
    testLearningHubRefreshClearsPendingChangesAfterMerge()
    testLearningHubEventChannelReceivesOneShotEvents()
    println("All learning hub store tests passed.")
}
