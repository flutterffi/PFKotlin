class BootstrapLearningHubUseCase(
    private val repository: LearningHubRepository,
    private val localStore: LearningHubLocalStore,
    private val remoteSource: LearningHubRemoteSource
) {
    fun execute(): String {
        if (localStore.exists()) {
            val cachedLessons = localStore.loadLessons()
            if (cachedLessons.isNotEmpty()) {
                repository.replaceLessons(cachedLessons)
                return "Restored cached learning hub"
            }
        }

        val remoteLessons = remoteSource.fetchLessons()
        repository.replaceLessons(remoteLessons)
        localStore.saveLessons(remoteLessons)
        return "Bootstrapped from remote catalog"
    }
}

class RefreshLearningHubUseCase(
    private val repository: LearningHubRepository,
    private val localStore: LearningHubLocalStore,
    private val remoteSource: LearningHubRemoteSource
) {
    fun execute(): String {
        val remoteLessons = remoteSource.fetchLessons()
        repository.replaceLessons(remoteLessons)
        localStore.saveLessons(remoteLessons)
        return "Refreshed remote catalog"
    }
}

class MergeRemoteLearningHubUseCase(
    private val repository: LearningHubRepository,
    private val localStore: LearningHubLocalStore,
    private val remoteSource: LearningHubRemoteSource
) {
    fun execute(
        pendingChanges: List<PendingSyncChange>,
        strategy: ConflictStrategy
    ): String {
        val remoteLessons = remoteSource.fetchLessons()
        val localLessons = repository.allLessons()
        val localLessonMap = localLessons.associateBy { it.id }
        val mergedLessons = remoteLessons.map { remoteLesson ->
            val localLesson = localLessonMap[remoteLesson.id] ?: return@map remoteLesson
            when (strategy) {
                ConflictStrategy.LOCAL_WINS -> applyPendingChanges(remoteLesson, localLesson, pendingChanges)
                ConflictStrategy.REMOTE_WINS -> remoteLesson
            }
        }

        repository.replaceLessons(mergedLessons)
        localStore.saveLessons(mergedLessons)
        return when (strategy) {
            ConflictStrategy.LOCAL_WINS -> "Merged remote catalog with local changes"
            ConflictStrategy.REMOTE_WINS -> "Replaced local snapshot with remote catalog"
        }
    }

    private fun applyPendingChanges(
        remoteLesson: HubLesson,
        localLesson: HubLesson,
        pendingChanges: List<PendingSyncChange>
    ): HubLesson {
        val changesForLesson = pendingChanges.filter { it.lessonId == remoteLesson.id }
        if (changesForLesson.isEmpty()) {
            return remoteLesson
        }

        var mergedLesson = remoteLesson
        changesForLesson.forEach { change ->
            mergedLesson = when (change.type) {
                PendingChangeType.TOGGLE_BOOKMARK -> mergedLesson.copy(bookmarked = localLesson.bookmarked)
                PendingChangeType.COMPLETE_LESSON -> mergedLesson.copy(completed = localLesson.completed)
            }
        }
        return mergedLesson
    }
}

class SaveLearningHubSnapshotUseCase(
    private val repository: LearningHubRepository,
    private val localStore: LearningHubLocalStore
) {
    fun execute(): String {
        localStore.saveLessons(repository.allLessons())
        return "Saved offline snapshot"
    }
}

class ToggleHubBookmarkUseCase(
    private val repository: LearningHubRepository
) {
    fun execute(lessonId: String): HubLesson {
        val lesson = repository.allLessons().firstOrNull { it.id == lessonId }
            ?: throw IllegalStateException("Lesson not found: $lessonId")
        val updatedLesson = lesson.copy(bookmarked = !lesson.bookmarked)
        repository.updateLesson(updatedLesson)
        return updatedLesson
    }
}

class CompleteHubLessonUseCase(
    private val repository: LearningHubRepository
) {
    fun execute(lessonId: String): HubLesson {
        val lesson = repository.allLessons().firstOrNull { it.id == lessonId }
            ?: throw IllegalStateException("Lesson not found: $lessonId")
        val updatedLesson = lesson.copy(completed = true)
        repository.updateLesson(updatedLesson)
        return updatedLesson
    }
}

class BuildLearningHubStateUseCase(
    private val repository: LearningHubRepository,
    private val localStore: LearningHubLocalStore
) {
    fun execute(
        route: HubRoute,
        activeTrack: LessonTrack?,
        syncStage: SyncStage,
        conflictStrategy: ConflictStrategy,
        pendingSyncCount: Int,
        statusMessage: String,
        errorMessage: String?,
        notice: HubNotice?,
        lastIntent: String
    ): LearningHubState {
        val lessons = repository.allLessons()
        val visibleLessons = lessons
            .filter { lesson -> activeTrack == null || lesson.track == activeTrack }
            .sortedWith(
                compareByDescending<HubLesson> { it.bookmarked }
                    .thenByDescending { !it.completed }
                    .thenByDescending { it.difficulty }
                    .thenBy { it.estimatedMinutes }
            )
        val selectedLesson = when (route) {
            HubRoute.Dashboard -> null
            is HubRoute.LessonDetail -> lessons.firstOrNull { it.id == route.lessonId }
        }

        val summary = LearningHubSummary(
            totalLessons = lessons.size,
            visibleLessons = visibleLessons.size,
            completedLessons = lessons.count { it.completed },
            bookmarkedLessons = lessons.count { it.bookmarked },
            remainingMinutes = lessons.filter { !it.completed }.sumOf { it.estimatedMinutes }
        )

        return LearningHubState(
            lessons = lessons,
            visibleLessons = visibleLessons,
            summary = summary,
            route = route,
            selectedLesson = selectedLesson,
            activeTrack = activeTrack,
            syncStage = syncStage,
            isOfflineReady = localStore.exists(),
            conflictStrategy = conflictStrategy,
            pendingSyncCount = pendingSyncCount,
            statusMessage = statusMessage,
            errorMessage = errorMessage,
            notice = notice,
            persistencePath = localStore.path(),
            lastIntent = lastIntent
        )
    }
}
