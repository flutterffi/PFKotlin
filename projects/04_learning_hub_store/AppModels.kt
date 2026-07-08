enum class LessonTrack {
    FOUNDATIONS,
    ANDROID,
    BACKEND,
    ARCHITECTURE
}

enum class SyncStage {
    IDLE,
    BOOTSTRAPPING,
    REFRESHING,
    FAILED
}

enum class ConflictStrategy {
    LOCAL_WINS,
    REMOTE_WINS
}

enum class PendingChangeType {
    TOGGLE_BOOKMARK,
    COMPLETE_LESSON
}

data class HubLesson(
    val id: String,
    val title: String,
    val track: LessonTrack,
    val difficulty: Int,
    val estimatedMinutes: Int,
    val completed: Boolean,
    val bookmarked: Boolean
)

sealed class HubRoute {
    data object Dashboard : HubRoute()
    data class LessonDetail(val lessonId: String) : HubRoute()
}

data class HubNotice(
    val id: String,
    val message: String
)

data class PendingSyncChange(
    val lessonId: String,
    val type: PendingChangeType
)

data class HubEvent(
    val name: String,
    val message: String
)

data class LearningHubSummary(
    val totalLessons: Int,
    val visibleLessons: Int,
    val completedLessons: Int,
    val bookmarkedLessons: Int,
    val remainingMinutes: Int
)

data class LearningHubState(
    val lessons: List<HubLesson>,
    val visibleLessons: List<HubLesson>,
    val summary: LearningHubSummary,
    val route: HubRoute,
    val selectedLesson: HubLesson?,
    val activeTrack: LessonTrack?,
    val syncStage: SyncStage,
    val isOfflineReady: Boolean,
    val conflictStrategy: ConflictStrategy,
    val pendingSyncCount: Int,
    val statusMessage: String,
    val errorMessage: String?,
    val notice: HubNotice?,
    val persistencePath: String?,
    val lastIntent: String
)

sealed class LearningHubIntent {
    data object Bootstrap : LearningHubIntent()
    data object RefreshCatalog : LearningHubIntent()
    data object RetrySync : LearningHubIntent()
    data object SaveSnapshot : LearningHubIntent()
    data class SetConflictStrategy(val strategy: ConflictStrategy) : LearningHubIntent()
    data class OpenLesson(val lessonId: String) : LearningHubIntent()
    data object BackToDashboard : LearningHubIntent()
    data class FilterTrack(val track: LessonTrack?) : LearningHubIntent()
    data class ToggleBookmark(val lessonId: String) : LearningHubIntent()
    data class CompleteLesson(val lessonId: String) : LearningHubIntent()
    data object ClearNotice : LearningHubIntent()
}

sealed class LearningHubMutation {
    data class Progress(
        val route: HubRoute,
        val activeTrack: LessonTrack?,
        val syncStage: SyncStage,
        val statusMessage: String,
        val errorMessage: String?,
        val notice: HubNotice?,
        val lastIntent: String
    ) : LearningHubMutation()

    data class Content(
        val route: HubRoute,
        val activeTrack: LessonTrack?,
        val syncStage: SyncStage,
        val statusMessage: String,
        val errorMessage: String?,
        val notice: HubNotice?,
        val lastIntent: String
    ) : LearningHubMutation()
}
