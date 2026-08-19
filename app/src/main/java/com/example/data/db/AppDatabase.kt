package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getProgressDirect(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: UserProgressEntity)
}

@Dao
interface LevelStateDao {
    @Query("SELECT * FROM level_state")
    fun getAllLevelStates(): Flow<List<LevelStateEntity>>

    @Query("SELECT * FROM level_state")
    suspend fun getAllLevelStatesSync(): List<LevelStateEntity>

    @Query("SELECT * FROM level_state WHERE levelId = :id")
    suspend fun getLevelState(id: Int): LevelStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(levelState: LevelStateEntity)

    @Query("SELECT SUM(stars) FROM level_state")
    fun getTotalStarsFlow(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM level_state WHERE completed = 1")
    fun getCompletedCountFlow(): Flow<Int?>
}

@Dao
interface WordDao {
    @Query("SELECT * FROM word_state")
    fun getAllWordsFlow(): Flow<List<WordEntity>>

    @Query("SELECT * FROM word_state WHERE id = :id")
    suspend fun getWordById(id: String): WordEntity?

    @Query("SELECT * FROM word_state")
    suspend fun getAllWords(): List<WordEntity>

    @Query("SELECT * FROM word_state WHERE isFavorite = 1")
    fun getFavoriteWordsFlow(): Flow<List<WordEntity>>

    @Query("SELECT * FROM word_state WHERE isLearned = 1")
    fun getLearnedWordsFlow(): Flow<List<WordEntity>>

    @Query("SELECT * FROM word_state WHERE reviewLevel > 0")
    fun getWordsToReviewFlow(): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Query("SELECT COUNT(*) FROM word_state WHERE isLearned = 1")
    fun getLearnedCountFlow(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM word_state")
    suspend fun getTotalWordsCount(): Int
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement_state")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievement_state")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query("SELECT * FROM achievement_state WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("SELECT COUNT(*) FROM achievement_state WHERE isUnlocked = 1")
    fun getUnlockedCountFlow(): Flow<Int?>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_state")
    fun getAllTasksFlow(): Flow<List<UserTaskEntity>>

    @Query("SELECT * FROM task_state")
    suspend fun getAllTasks(): List<UserTaskEntity>

    @Query("SELECT * FROM task_state WHERE id = :id")
    suspend fun getTaskById(id: String): UserTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<UserTaskEntity>)

    @Update
    suspend fun updateTask(task: UserTaskEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_log ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notification_log SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notification_log")
    suspend fun clearAllNotifications()
}

@Database(
    entities = [
        UserProgressEntity::class,
        LevelStateEntity::class,
        WordEntity::class,
        AchievementEntity::class,
        UserTaskEntity::class,
        NotificationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun levelStateDao(): LevelStateDao
    abstract fun wordDao(): WordDao
    abstract fun achievementDao(): AchievementDao
    abstract fun taskDao(): TaskDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_progress ADD COLUMN highestUnlockedLevelId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN lastDailyRewardClaimDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN dailyRewardCycleDay INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_progress ADD COLUMN dinoStage TEXT NOT NULL DEFAULT 'EGG'")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN dinoNickname TEXT NOT NULL DEFAULT 'Mino'")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN unlockedDinoStages TEXT NOT NULL DEFAULT 'EGG'")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN selectedDinoColor TEXT NOT NULL DEFAULT 'default'")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN selectedAccessory TEXT NOT NULL DEFAULT 'none'")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN lastEvolutionSeen TEXT NOT NULL DEFAULT 'NONE'")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_progress ADD COLUMN lastFreeHeartClaimDate TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "caprazca_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
