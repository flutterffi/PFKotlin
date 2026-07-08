interface LearningHubTaskRunner {
    fun submit(task: () -> Unit)
}

fun interface LearningHubStateObserver {
    fun onState(state: LearningHubState)
}

fun interface LearningHubEventObserver {
    fun onEvent(event: HubEvent)
}

fun interface LearningHubSubscription {
    fun cancel()
}

object ImmediateLearningHubTaskRunner : LearningHubTaskRunner {
    override fun submit(task: () -> Unit) {
        task()
    }
}

class ControlledLearningHubTaskRunner : LearningHubTaskRunner {
    private val tasks = ArrayDeque<() -> Unit>()

    override fun submit(task: () -> Unit) {
        tasks.addLast(task)
    }

    fun pendingCount(): Int = tasks.size

    fun runNext() {
        tasks.removeFirstOrNull()?.invoke()
    }

    fun runAll() {
        while (tasks.isNotEmpty()) {
            runNext()
        }
    }
}

object LearningHubReducer {
    fun reduce(
        currentState: LearningHubState,
        mutation: LearningHubMutation,
        buildState: (HubRoute, LessonTrack?, SyncStage, ConflictStrategy, Int, Int, Int, Boolean, String, String?, HubNotice?, String) -> LearningHubState
    ): LearningHubState {
        return when (mutation) {
            is LearningHubMutation.Progress -> {
                currentState.copy(
                    route = mutation.route,
                    activeTrack = mutation.activeTrack,
                    syncStage = mutation.syncStage,
                    statusMessage = mutation.statusMessage,
                    errorMessage = mutation.errorMessage,
                    notice = mutation.notice,
                    lastIntent = mutation.lastIntent
                )
            }

            is LearningHubMutation.Content -> {
                buildState(
                    mutation.route,
                    mutation.activeTrack,
                    mutation.syncStage,
                    currentState.conflictStrategy,
                    currentState.pendingSyncCount,
                    currentState.historySize,
                    currentState.historyIndex,
                    currentState.isTimeTraveling,
                    mutation.statusMessage,
                    mutation.errorMessage,
                    mutation.notice,
                    mutation.lastIntent
                )
            }
        }
    }
}

class LearningHubStore(
    private val bootstrapUseCase: BootstrapLearningHubUseCase,
    private val refreshUseCase: RefreshLearningHubUseCase,
    private val mergeRemoteUseCase: MergeRemoteLearningHubUseCase,
    private val saveSnapshotUseCase: SaveLearningHubSnapshotUseCase,
    private val toggleBookmarkUseCase: ToggleHubBookmarkUseCase,
    private val completeLessonUseCase: CompleteHubLessonUseCase,
    private val buildStateUseCase: BuildLearningHubStateUseCase,
    private val taskRunner: LearningHubTaskRunner
) {
    private var route: HubRoute = HubRoute.Dashboard
    private var activeTrack: LessonTrack? = null
    private var syncStage: SyncStage = SyncStage.IDLE
    private var conflictStrategy: ConflictStrategy = ConflictStrategy.LOCAL_WINS
    private var statusMessage: String = "Ready"
    private var errorMessage: String? = null
    private var notice: HubNotice? = null
    private val pendingChanges = mutableListOf<PendingSyncChange>()
    private val history = mutableListOf<LearningHubState>()
    private val observers = linkedMapOf<Int, LearningHubStateObserver>()
    private val eventObservers = linkedMapOf<Int, LearningHubEventObserver>()
    private var nextObserverId: Int = 1

    var state: LearningHubState = buildStateUseCase.execute(
        route = route,
        activeTrack = activeTrack,
        syncStage = syncStage,
        conflictStrategy = conflictStrategy,
        pendingSyncCount = pendingChanges.size,
        historySize = 0,
        historyIndex = 0,
        isTimeTraveling = false,
        statusMessage = statusMessage,
        errorMessage = errorMessage,
        notice = notice,
        lastIntent = "InitialState"
    )
        private set
    private var liveState: LearningHubState = state

    init {
        appendHistory(state)
    }

    fun observe(observer: LearningHubStateObserver): LearningHubSubscription {
        val observerId = nextObserverId++
        observers[observerId] = observer
        observer.onState(state)
        return LearningHubSubscription {
            observers.remove(observerId)
        }
    }

    fun observeEvents(observer: LearningHubEventObserver): LearningHubSubscription {
        val observerId = nextObserverId++
        eventObservers[observerId] = observer
        return LearningHubSubscription {
            eventObservers.remove(observerId)
        }
    }

    fun dispatch(intent: LearningHubIntent) {
        when (intent) {
            is LearningHubIntent.JumpToHistory -> {
                val historyState = history.getOrNull(intent.index)
                    ?: throw IllegalStateException("History index out of range: ${intent.index}")
                state = historyState.copy(
                    historySize = history.size,
                    historyIndex = intent.index,
                    isTimeTraveling = true,
                    notice = null,
                    lastIntent = "JumpToHistory"
                )
                notifyObservers()
                emitEvent(HubEvent("history_jump", "Jumped to history index ${intent.index}"))
            }

            LearningHubIntent.ReturnToLive -> {
                state = liveState.copy(
                    historySize = history.size,
                    historyIndex = history.lastIndex.coerceAtLeast(0),
                    isTimeTraveling = false,
                    lastIntent = "ReturnToLive"
                )
                notifyObservers()
                emitEvent(HubEvent("history_live", "Returned to live state"))
            }

            LearningHubIntent.Bootstrap -> {
                returnToLiveIfNeeded()
                reduce(
                    LearningHubMutation.Progress(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = SyncStage.BOOTSTRAPPING,
                        statusMessage = "Bootstrapping learning hub",
                        errorMessage = null,
                        notice = null,
                        lastIntent = "Bootstrap"
                    )
                )
                taskRunner.submit { completeBootstrap() }
            }

            LearningHubIntent.RefreshCatalog,
            LearningHubIntent.RetrySync -> {
                returnToLiveIfNeeded()
                reduce(
                    LearningHubMutation.Progress(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = SyncStage.REFRESHING,
                        statusMessage = "Refreshing learning catalog",
                        errorMessage = null,
                        notice = null,
                        lastIntent = if (intent == LearningHubIntent.RetrySync) "RetrySync" else "RefreshCatalog"
                    )
                )
                taskRunner.submit { completeRefresh(intent) }
            }

            LearningHubIntent.SaveSnapshot -> {
                returnToLiveIfNeeded()
                val message = saveSnapshotUseCase.execute()
                reduce(
                    LearningHubMutation.Content(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = SyncStage.IDLE,
                        statusMessage = message,
                        errorMessage = null,
                        notice = HubNotice("snapshot", message),
                        lastIntent = "SaveSnapshot"
                    )
                )
                emitEvent(HubEvent("snapshot_saved", message))
            }

            is LearningHubIntent.SetConflictStrategy -> {
                returnToLiveIfNeeded()
                conflictStrategy = intent.strategy
                liveState = liveState.copy(
                    conflictStrategy = conflictStrategy,
                    historySize = history.size,
                    historyIndex = history.lastIndex.coerceAtLeast(0),
                    isTimeTraveling = false,
                    statusMessage = "Conflict strategy changed to ${intent.strategy}",
                    errorMessage = null,
                    lastIntent = "SetConflictStrategy"
                )
                appendHistory(liveState)
                notifyObservers()
                emitEvent(HubEvent("strategy_changed", "Conflict strategy set to ${intent.strategy}"))
            }

            is LearningHubIntent.OpenLesson -> {
                returnToLiveIfNeeded()
                route = HubRoute.LessonDetail(intent.lessonId)
                reduce(
                    LearningHubMutation.Content(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = syncStage,
                        statusMessage = "Opened lesson ${intent.lessonId}",
                        errorMessage = null,
                        notice = null,
                        lastIntent = "OpenLesson"
                    )
                )
            }

            LearningHubIntent.BackToDashboard -> {
                returnToLiveIfNeeded()
                route = HubRoute.Dashboard
                reduce(
                    LearningHubMutation.Content(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = syncStage,
                        statusMessage = "Returned to dashboard",
                        errorMessage = null,
                        notice = null,
                        lastIntent = "BackToDashboard"
                    )
                )
            }

            is LearningHubIntent.FilterTrack -> {
                returnToLiveIfNeeded()
                activeTrack = intent.track
                route = HubRoute.Dashboard
                reduce(
                    LearningHubMutation.Content(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = syncStage,
                        statusMessage = "Track filter updated",
                        errorMessage = null,
                        notice = null,
                        lastIntent = "FilterTrack"
                    )
                )
            }

            is LearningHubIntent.ToggleBookmark -> {
                returnToLiveIfNeeded()
                val updatedLesson = toggleBookmarkUseCase.execute(intent.lessonId)
                rememberPendingChange(intent.lessonId, PendingChangeType.TOGGLE_BOOKMARK)
                saveSnapshotUseCase.execute()
                reduce(
                    LearningHubMutation.Content(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = SyncStage.IDLE,
                        statusMessage = "Bookmark updated for ${updatedLesson.title}",
                        errorMessage = null,
                        notice = HubNotice("bookmark-${updatedLesson.id}", "Bookmark updated"),
                        lastIntent = "ToggleBookmark"
                    )
                )
                emitEvent(HubEvent("bookmark_updated", updatedLesson.title))
            }

            is LearningHubIntent.CompleteLesson -> {
                returnToLiveIfNeeded()
                val updatedLesson = completeLessonUseCase.execute(intent.lessonId)
                rememberPendingChange(intent.lessonId, PendingChangeType.COMPLETE_LESSON)
                saveSnapshotUseCase.execute()
                reduce(
                    LearningHubMutation.Content(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = SyncStage.IDLE,
                        statusMessage = "Completed ${updatedLesson.title}",
                        errorMessage = null,
                        notice = HubNotice("complete-${updatedLesson.id}", "Lesson completed"),
                        lastIntent = "CompleteLesson"
                    )
                )
                emitEvent(HubEvent("lesson_completed", updatedLesson.title))
            }

            LearningHubIntent.ClearNotice -> {
                returnToLiveIfNeeded()
                reduce(
                    LearningHubMutation.Content(
                        route = route,
                        activeTrack = activeTrack,
                        syncStage = syncStage,
                        statusMessage = statusMessage,
                        errorMessage = errorMessage,
                        notice = null,
                        lastIntent = "ClearNotice"
                    )
                )
            }
        }
    }

    private fun completeBootstrap() {
        try {
            val message = bootstrapUseCase.execute()
            reduce(
                LearningHubMutation.Content(
                    route = route,
                    activeTrack = activeTrack,
                    syncStage = SyncStage.IDLE,
                    statusMessage = message,
                    errorMessage = null,
                    notice = null,
                    lastIntent = "Bootstrap"
                )
            )
            emitEvent(HubEvent("bootstrap_complete", message))
        } catch (error: IllegalStateException) {
            syncStage = SyncStage.FAILED
            reduce(
                LearningHubMutation.Content(
                    route = route,
                    activeTrack = activeTrack,
                    syncStage = SyncStage.FAILED,
                    statusMessage = "Bootstrap failed",
                    errorMessage = error.message ?: "Unknown bootstrap error",
                    notice = null,
                    lastIntent = "Bootstrap"
                )
            )
            emitEvent(HubEvent("bootstrap_failed", error.message ?: "Unknown bootstrap error"))
        }
    }

    private fun completeRefresh(intent: LearningHubIntent) {
        try {
            val message = if (pendingChanges.isEmpty()) {
                refreshUseCase.execute()
            } else {
                mergeRemoteUseCase.execute(pendingChanges.toList(), conflictStrategy)
            }
            pendingChanges.clear()
            reduce(
                LearningHubMutation.Content(
                    route = route,
                    activeTrack = activeTrack,
                    syncStage = SyncStage.IDLE,
                    statusMessage = message,
                    errorMessage = null,
                    notice = HubNotice("refresh", "Catalog refreshed"),
                    lastIntent = if (intent == LearningHubIntent.RetrySync) "RetrySync" else "RefreshCatalog"
                )
            )
            emitEvent(HubEvent("refresh_complete", message))
        } catch (error: IllegalStateException) {
            reduce(
                LearningHubMutation.Content(
                    route = route,
                    activeTrack = activeTrack,
                    syncStage = SyncStage.FAILED,
                    statusMessage = "Refresh failed",
                    errorMessage = error.message ?: "Unknown refresh error",
                    notice = null,
                    lastIntent = if (intent == LearningHubIntent.RetrySync) "RetrySync" else "RefreshCatalog"
                )
            )
            emitEvent(HubEvent("refresh_failed", error.message ?: "Unknown refresh error"))
        }
    }

    private fun reduce(mutation: LearningHubMutation) {
        syncStage = when (mutation) {
            is LearningHubMutation.Progress -> mutation.syncStage
            is LearningHubMutation.Content -> mutation.syncStage
        }
        statusMessage = when (mutation) {
            is LearningHubMutation.Progress -> mutation.statusMessage
            is LearningHubMutation.Content -> mutation.statusMessage
        }
        errorMessage = when (mutation) {
            is LearningHubMutation.Progress -> mutation.errorMessage
            is LearningHubMutation.Content -> mutation.errorMessage
        }
        notice = when (mutation) {
            is LearningHubMutation.Progress -> mutation.notice
            is LearningHubMutation.Content -> mutation.notice
        }
        state = LearningHubReducer.reduce(state, mutation, buildStateUseCase::execute)
        liveState = state.copy(
            conflictStrategy = conflictStrategy,
            pendingSyncCount = pendingChanges.size,
            historySize = history.size,
            historyIndex = history.lastIndex.coerceAtLeast(0),
            isTimeTraveling = false
        )
        state = liveState
        appendHistory(liveState)
        notifyObservers()
    }

    fun historyEntries(): List<HubHistoryEntry> {
        return history.mapIndexed { index, item ->
            HubHistoryEntry(
                index = index,
                lastIntent = item.lastIntent,
                syncStage = item.syncStage,
                route = item.route,
                statusMessage = item.statusMessage
            )
        }
    }

    private fun rememberPendingChange(lessonId: String, type: PendingChangeType) {
        pendingChanges.removeAll { it.lessonId == lessonId && it.type == type }
        pendingChanges += PendingSyncChange(lessonId, type)
    }

    private fun appendHistory(newState: LearningHubState) {
        history += newState.copy(
            historySize = history.size + 1,
            historyIndex = history.size,
            isTimeTraveling = false
        )
        liveState = history.last()
        state = liveState
    }

    private fun returnToLiveIfNeeded() {
        if (!state.isTimeTraveling) {
            return
        }
        state = liveState.copy(
            historySize = history.size,
            historyIndex = history.lastIndex.coerceAtLeast(0),
            isTimeTraveling = false,
            lastIntent = "ReturnToLive"
        )
        notifyObservers()
    }

    private fun notifyObservers() {
        observers.values.forEach { observer ->
            observer.onState(state)
        }
    }

    private fun emitEvent(event: HubEvent) {
        eventObservers.values.forEach { observer ->
            observer.onEvent(event)
        }
    }
}

object LearningHubConsoleView {
    fun render(state: LearningHubState): String {
        val lines = mutableListOf<String>()
        lines += "== Learning Hub =="
        lines += "status: ${state.statusMessage}"
        lines += "sync: ${state.syncStage}"
        lines += "route: ${state.route}"
        lines += "track-filter: ${state.activeTrack ?: "ALL"}"
        lines += "offline-ready: ${state.isOfflineReady}"
        lines += "conflict-strategy: ${state.conflictStrategy}"
        lines += "pending-sync: ${state.pendingSyncCount}"
        lines += "history: ${state.historyIndex + 1}/${state.historySize}"
        lines += "time-traveling: ${state.isTimeTraveling}"
        lines += "last-intent: ${state.lastIntent}"
        lines += "snapshot: ${state.persistencePath ?: "<none>"}"
        if (state.errorMessage != null) {
            lines += "error: ${state.errorMessage}"
        }
        if (state.notice != null) {
            lines += "notice: ${state.notice.message}"
        }
        lines += "summary: total=${state.summary.totalLessons}, visible=${state.summary.visibleLessons}, completed=${state.summary.completedLessons}, bookmarked=${state.summary.bookmarkedLessons}, remaining=${state.summary.remainingMinutes}m"

        when (state.route) {
            HubRoute.Dashboard -> {
                if (state.visibleLessons.isEmpty()) {
                    lines += "No lessons available."
                } else {
                    state.visibleLessons.forEachIndexed { index, lesson ->
                        val bookmarkMark = if (lesson.bookmarked) "*" else "-"
                        val completionMark = if (lesson.completed) "done" else "todo"
                        lines += "${index + 1}. [$bookmarkMark/$completionMark] ${lesson.title} | ${lesson.track} | difficulty=${lesson.difficulty} | ${lesson.estimatedMinutes}m"
                    }
                }
            }

            is HubRoute.LessonDetail -> {
                val lesson = state.selectedLesson
                if (lesson == null) {
                    lines += "Selected lesson is missing."
                } else {
                    lines += "detail: ${lesson.title}"
                    lines += "track: ${lesson.track}"
                    lines += "difficulty: ${lesson.difficulty}"
                    lines += "duration: ${lesson.estimatedMinutes}m"
                    lines += "completed: ${lesson.completed}"
                    lines += "bookmarked: ${lesson.bookmarked}"
                }
            }
        }

        return lines.joinToString("\n")
    }
}
