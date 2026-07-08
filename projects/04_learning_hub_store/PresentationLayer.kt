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
        buildState: (HubRoute, LessonTrack?, SyncStage, ConflictStrategy, Int, String, String?, HubNotice?, String) -> LearningHubState
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
    private val observers = linkedMapOf<Int, LearningHubStateObserver>()
    private val eventObservers = linkedMapOf<Int, LearningHubEventObserver>()
    private var nextObserverId: Int = 1

    var state: LearningHubState = buildStateUseCase.execute(
        route = route,
        activeTrack = activeTrack,
        syncStage = syncStage,
        conflictStrategy = conflictStrategy,
        pendingSyncCount = pendingChanges.size,
        statusMessage = statusMessage,
        errorMessage = errorMessage,
        notice = notice,
        lastIntent = "InitialState"
    )
        private set

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
            LearningHubIntent.Bootstrap -> {
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
                conflictStrategy = intent.strategy
                state = state.copy(
                    conflictStrategy = conflictStrategy,
                    statusMessage = "Conflict strategy changed to ${intent.strategy}",
                    errorMessage = null,
                    lastIntent = "SetConflictStrategy"
                )
                notifyObservers()
                emitEvent(HubEvent("strategy_changed", "Conflict strategy set to ${intent.strategy}"))
            }

            is LearningHubIntent.OpenLesson -> {
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
        state = state.copy(
            conflictStrategy = conflictStrategy,
            pendingSyncCount = pendingChanges.size
        )
        notifyObservers()
    }

    private fun rememberPendingChange(lessonId: String, type: PendingChangeType) {
        pendingChanges.removeAll { it.lessonId == lessonId && it.type == type }
        pendingChanges += PendingSyncChange(lessonId, type)
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
