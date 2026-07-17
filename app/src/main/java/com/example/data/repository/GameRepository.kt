package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GameRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val progressDao = db.userProgressDao()
    private val levelStateDao = db.levelStateDao()
    private val wordDao = db.wordDao()
    private val achievementDao = db.achievementDao()
    private val taskDao = db.taskDao()
    private val notificationDao = db.notificationDao()

    // Flow Getters
    val progressFlow: Flow<UserProgressEntity?> = progressDao.getProgress()
    val levelStatesFlow: Flow<List<LevelStateEntity>> = levelStateDao.getAllLevelStates()
    val dictionaryFlow: Flow<List<WordEntity>> = wordDao.getAllWordsFlow()
    val achievementsFlow: Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()
    val tasksFlow: Flow<List<UserTaskEntity>> = taskDao.getAllTasksFlow()
    val notificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    val totalStarsFlow: Flow<Int?> = levelStateDao.getTotalStarsFlow()
    val completedCountFlow: Flow<Int?> = levelStateDao.getCompletedCountFlow()
    val learnedCountFlow: Flow<Int?> = wordDao.getLearnedCountFlow()

    // Initialize database with seed data if empty
    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        // 1. Seed user progress if not present
        val currentProgress = progressDao.getProgressDirect()
        if (currentProgress == null) {
            val initialProgress = UserProgressEntity(
                id = 1,
                level = 1,
                xp = 0,
                coins = 300, // Generous starter coins
                hearts = 5,
                lastHeartRestoreTime = System.currentTimeMillis(),
                streak = 1,
                lastActiveDate = getTodayDateString(),
                avatar = "avatar_1",
                username = "Çaprazca Gezgini"
            )
            progressDao.insertOrUpdate(initialProgress)
            Log.d("GameRepository", "Seeded user progress.")
        }

        // 2. Seed dictionary
        val totalWords = wordDao.getTotalWordsCountFlow().firstOrNull() ?: 0
        if (totalWords == 0) {
            val wordsList = DictionaryData.getPreloadedWords()
            wordDao.insertWords(wordsList)
            Log.d("GameRepository", "Seeded ${wordsList.size} dictionary words.")
        }

        // 3. Seed achievements if empty
        val currentAchievements = achievementDao.getAllAchievementsFlow().firstOrNull() ?: emptyList()
        if (currentAchievements.isEmpty()) {
            val seedAchievements = listOf(
                AchievementEntity("first_step", "İlk Adım", "İlk bulmacanı başarıyla tamamla.", "Progress", "COMMON", 1, rewardCoins = 20, rewardXp = 50),
                AchievementEntity("warm_up", "Isınıyorum", "5 bulmaca tamamla.", "Progress", "COMMON", 5, rewardCoins = 30, rewardXp = 100),
                AchievementEntity("puzzle_lover", "Bulmaca Sever", "25 bulmaca tamamla.", "Progress", "RARE", 25, rewardCoins = 100, rewardXp = 250),
                AchievementEntity("crossword_master", "Kare Ustası", "40 bulmaca tamamla.", "Progress", "EPIC", 40, rewardCoins = 200, rewardXp = 500),
                AchievementEntity("first_word", "İlk Kelime", "İlk kelimeni doğru çöz.", "Vocabulary", "COMMON", 1, rewardCoins = 10, rewardXp = 20),
                AchievementEntity("word_hunter", "Kelime Avcısı", "100 kelime çöz.", "Vocabulary", "RARE", 100, rewardCoins = 50, rewardXp = 150),
                AchievementEntity("word_hoarder", "Kelime Hazinesi", "250 kelime çöz.", "Vocabulary", "EPIC", 250, rewardCoins = 150, rewardXp = 400),
                AchievementEntity("perfect_clean", "Tertemiz", "Bir bölümü hiç hata yapmadan tamamla.", "Skill", "COMMON", 1, rewardCoins = 50, rewardXp = 100),
                AchievementEntity("perfect_streak", "Kusursuz Seri", "Art arda 3 bölümü hatasız tamamla.", "Skill", "EPIC", 3, rewardCoins = 150, rewardXp = 300),
                AchievementEntity("no_hints", "Yardımsız", "Bir bölümü hiç ipucu kullanmadan bitir.", "Skill", "COMMON", 1, rewardCoins = 30, rewardXp = 80),
                AchievementEntity("own_master", "Kendi Başına", "10 bölümü hiç ipucu kullanmadan bitir.", "Skill", "RARE", 10, rewardCoins = 100, rewardXp = 250),
                AchievementEntity("fast_thinker", "Hızlı Düşün", "Bir bölümü 3 dakikanın (180s) altında bitir.", "Time", "COMMON", 1, rewardCoins = 30, rewardXp = 60),
                AchievementEntity("time_lord", "Zamanın Efendisi", "10 bölümü 3 dakikanın altında bitir.", "Time", "RARE", 10, rewardCoins = 120, rewardXp = 200),
                AchievementEntity("first_fav", "İlk Favori", "Sözlükte bir kelimeyi favorilerine ekle.", "Dictionary", "COMMON", 1, rewardCoins = 10, rewardXp = 30),
                AchievementEntity("fav_collector", "Koleksiyoncu", "Sözlükte 10 kelimeyi favorilerine ekle.", "Dictionary", "COMMON", 10, rewardCoins = 30, rewardXp = 100),
                AchievementEntity("learned_50", "Dil Meraklısı", "Sözlükte 50 kelimeyi öğrendim olarak işaretle.", "Dictionary", "RARE", 50, rewardCoins = 100, rewardXp = 250),
                AchievementEntity("learned_100", "Dil Kaşifi", "Sözlükte 100 kelimeyi öğrendim olarak işaretle.", "Dictionary", "EPIC", 100, rewardCoins = 250, rewardXp = 500),
                AchievementEntity("three_days", "3 Gün Üst Üste", "3 günlük giriş serisi oluştur.", "Streak", "COMMON", 3, rewardCoins = 50, rewardXp = 100),
                AchievementEntity("seven_days", "Bir Haftalık Seri", "7 günlük giriş serisi oluştur.", "Streak", "RARE", 7, rewardCoins = 150, rewardXp = 300),
                AchievementEntity("world_1_clear", "A1 Mezunu", "1. Dünya'daki tüm bölümleri tamamla.", "Theme", "COMMON", 15, rewardCoins = 100, rewardXp = 200),
                AchievementEntity("world_2_clear", "A2 Kaşifi", "2. Dünya'daki tüm bölümleri tamamla.", "Theme", "RARE", 15, rewardCoins = 200, rewardXp = 400),
                AchievementEntity("world_3_clear", "B1 Yolcusu", "3. Dünya'daki tüm bölümleri tamamla.", "Theme", "EPIC", 15, rewardCoins = 300, rewardXp = 600),
                AchievementEntity("night_owl", "Gece Kuşu", "Gece yarısından sonra (00:00-04:00) bir bölüm tamamla.", "Secret", "COMMON", 1, isSecret = true, rewardCoins = 50, rewardXp = 100),
                AchievementEntity("early_bird", "Erken Kalkan", "Sabah 06:00'dan önce bir bölüm tamamla.", "Secret", "COMMON", 1, isSecret = true, rewardCoins = 50, rewardXp = 100)
            )
            achievementDao.insertAchievements(seedAchievements)
            Log.d("GameRepository", "Seeded achievements.")
        }

        // 4. Seed daily/weekly tasks if empty
        val currentTasks = taskDao.getAllTasksFlow().firstOrNull() ?: emptyList()
        if (currentTasks.isEmpty()) {
            val seedTasks = listOf(
                UserTaskEntity("daily_1", "Bulmaca Fatihi", "2 bulmaca tamamla.", "DAILY", 2, rewardXp = 30, rewardCoins = 10, lastUpdatedDate = getTodayDateString()),
                UserTaskEntity("daily_2", "Kelime Avcısı", "10 kelime çöz.", "DAILY", 10, rewardXp = 30, rewardCoins = 10, lastUpdatedDate = getTodayDateString()),
                UserTaskEntity("daily_3", "Ezberci", "Sözlükte 3 kelimeyi favoriye ekle veya öğrenildi yap.", "DAILY", 3, rewardXp = 40, rewardCoins = 15, lastUpdatedDate = getTodayDateString()),
                UserTaskEntity("weekly_1", "Haftalık Usta", "15 bulmaca tamamla.", "WEEKLY", 15, rewardXp = 150, rewardCoins = 50, lastUpdatedDate = getTodayDateString()),
                UserTaskEntity("weekly_2", "Kelime Koleksiyoneri", "80 kelime çöz.", "WEEKLY", 80, rewardXp = 150, rewardCoins = 50, lastUpdatedDate = getTodayDateString()),
                UserTaskEntity("weekly_3", "Kusursuz Hafta", "5 bölümü hiç hata yapmadan tamamla.", "WEEKLY", 5, rewardXp = 200, rewardCoins = 75, lastUpdatedDate = getTodayDateString())
            )
            taskDao.insertTasks(seedTasks)
            Log.d("GameRepository", "Seeded tasks.")
        }

        // 5. Update streak & check heart regeneration on app launch
        updateStreakAndHearts()

        // Room Migration / Safety Normalization of Progress
        normalizeProgressAfterMigration()
    }

    private suspend fun normalizeProgressAfterMigration() = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext
        
        // 1. Calculate highest unlocked level based on completed level states
        val completedStates = levelStateDao.getAllLevelStates().firstOrNull() ?: emptyList()
        val highestCompletedLevel = completedStates.filter { it.completed }.maxOfOrNull { it.levelId } ?: 0
        
        // highestUnlockedLevelId = min(N + 1, 45)
        val calculatedHighestUnlocked = (highestCompletedLevel + 1).coerceAtMost(45).coerceAtLeast(1)
        
        // 2. Migrate lastClaimedLoginDay to dailyRewardCycleDay if needed
        var updatedCycleDay = progress.dailyRewardCycleDay
        if (updatedCycleDay == 0 && progress.lastClaimedLoginDay > 0) {
            updatedCycleDay = progress.lastClaimedLoginDay
        }
        
        if (progress.highestUnlockedLevelId < calculatedHighestUnlocked || progress.dailyRewardCycleDay != updatedCycleDay) {
            progressDao.insertOrUpdate(progress.copy(
                highestUnlockedLevelId = maxOf(progress.highestUnlockedLevelId, calculatedHighestUnlocked),
                dailyRewardCycleDay = updatedCycleDay
            ))
            Log.d("GameRepository", "Normalized progress fields after database check.")
        }
    }

    // Heart regeneration logic (5 hearts max, 1 heart per 30 mins)
    suspend fun updateStreakAndHearts() = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext
        val now = System.currentTimeMillis()
        val today = getTodayDateString()

        // 1. Streak and login reward check
        var updatedStreak = progress.streak
        var updatedLastActive = progress.lastActiveDate
        if (progress.lastActiveDate != today) {
            val yesterday = getYesterdayDateString()
            if (progress.lastActiveDate == yesterday) {
                // Consecutive day
                updatedStreak += 1
            } else if (progress.lastActiveDate.isNotEmpty()) {
                // Streak broken
                updatedStreak = 1
            }
            updatedLastActive = today
            // Add a welcome notification
            addNotification("Günlük Giriş!", "Serin devam ediyor: $updatedStreak gün! Günlük ödülünü almayı unutma.")
            // Also evaluate streak achievements
            evaluateAchievement("three_days", updatedStreak)
            evaluateAchievement("seven_days", updatedStreak)
        }

        // 2. Heart regeneration check
        var updatedHearts = progress.hearts
        var updatedRestoreTime = progress.lastHeartRestoreTime

        if (updatedHearts < 5) {
            val thirtyMinsMs = 30 * 60 * 1000L
            val elapsed = now - updatedRestoreTime
            if (elapsed >= thirtyMinsMs) {
                val heartsToAdd = (elapsed / thirtyMinsMs).toInt()
                updatedHearts = (updatedHearts + heartsToAdd).coerceAtMost(5)
                updatedRestoreTime = if (updatedHearts == 5) {
                    now
                } else {
                    updatedRestoreTime + (heartsToAdd * thirtyMinsMs)
                }
                if (heartsToAdd > 0) {
                    addNotification("Canların Yenilendi!", "Canların doldu. Şu anki canın: $updatedHearts/5.")
                }
            }
        } else {
            updatedRestoreTime = now
        }

        val updatedProgress = progress.copy(
            hearts = updatedHearts,
            lastHeartRestoreTime = updatedRestoreTime,
            streak = updatedStreak,
            lastActiveDate = updatedLastActive
        )
        progressDao.insertOrUpdate(updatedProgress)

        // 3. Reset daily tasks if date changed
        resetDailyTasksIfNeeded(today)
    }

    // Check if daily tasks need a reset
    private suspend fun resetDailyTasksIfNeeded(today: String) {
        val tasks = taskDao.getAllTasksFlow().firstOrNull() ?: return
        val dailyTasks = tasks.filter { it.type == "DAILY" }
        if (dailyTasks.isNotEmpty() && dailyTasks.first().lastUpdatedDate != today) {
            val updatedTasks = dailyTasks.map {
                it.copy(
                    currentValue = 0,
                    isCompleted = false,
                    isClaimed = false,
                    lastUpdatedDate = today
                )
            }
            updatedTasks.forEach { taskDao.updateTask(it) }
            addNotification("Günlük Görevler Yenilendi!", "Bugünkü görevlerini tamamla, ödülleri kap!")
        }
    }

    // Add notification log
    suspend fun addNotification(title: String, message: String) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(NotificationEntity(title = title, message = message))
    }

    // Buy hint / use hint
    suspend fun spendCoins(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext false
        if (progress.coins >= amount) {
            progressDao.insertOrUpdate(progress.copy(coins = progress.coins - amount))
            // Update stats
            incrementAchievementProgress("hints_used_stat", 1) // Hidden tracking stat
            true
        } else {
            false
        }
    }

    // Buy heart with coins
    suspend fun buyHeart(): Boolean = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext false
        if (progress.coins >= 100 && progress.hearts < 5) {
            progressDao.insertOrUpdate(progress.copy(
                coins = progress.coins - 100,
                hearts = progress.hearts + 1
            ))
            addNotification("Can Satın Alındı!", "100 jeton karşılığında 1 can aldın.")
            true
        } else {
            false
        }
    }

    // Spend dynamic heart on game start
    suspend fun useHeart(): Boolean = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext false
        if (progress.hearts > 0) {
            val now = System.currentTimeMillis()
            val newRestoreTime = if (progress.hearts == 5) now else progress.lastHeartRestoreTime
            progressDao.insertOrUpdate(progress.copy(
                hearts = progress.hearts - 1,
                lastHeartRestoreTime = newRestoreTime
            ))
            true
        } else {
            false
        }
    }

    private var isStartingLevelProgress = false

    suspend fun startPlayLevel(levelId: Int): Boolean = withContext(Dispatchers.IO) {
        if (isStartingLevelProgress) return@withContext false
        isStartingLevelProgress = true
        try {
            val level = LevelsData.getLevels().find { it.id == levelId } ?: return@withContext false
            if (level.isChest) return@withContext false

            val progress = progressDao.getProgressDirect() ?: return@withContext false
            if (levelId > progress.highestUnlockedLevelId) return@withContext false
            if (progress.hearts <= 0) return@withContext false

            // Consume heart
            val success = useHeart()
            success
        } finally {
            isStartingLevelProgress = false
        }
    }

    // Force restore hearts (level up reward etc)
    suspend fun restoreAllHearts() = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext
        progressDao.insertOrUpdate(progress.copy(hearts = 5, lastHeartRestoreTime = System.currentTimeMillis()))
    }

    // Handle level completion
    suspend fun completeLevel(
        levelId: Int,
        timeTakenSeconds: Int,
        starsEarned: Int,
        hintsUsed: Int,
        errorsCount: Int,
        isBoss: Boolean
    ): PuzzleResult = withContext(Dispatchers.IO) {
        db.withTransaction {
            // Fetch current progress
            val progress = progressDao.getProgressDirect() ?: throw IllegalStateException("Progress not initialized")

            // Fetch level info to calculate rewards
            val level = LevelsData.getLevels().find { it.id == levelId } ?: throw IllegalArgumentException("Level not found")

            // Read previous state to check if already completed
            val previousState = levelStateDao.getLevelState(levelId)
            val isFirstTime = previousState == null || !previousState.completed

            // Update Level State
            val newStars = previousState?.stars?.coerceAtLeast(starsEarned) ?: starsEarned
            val newBestTime = previousState?.bestTime?.coerceAtMost(timeTakenSeconds) ?: timeTakenSeconds
            val updatedLevelState = LevelStateEntity(
                levelId = levelId,
                completed = true,
                stars = newStars,
                bestTime = newBestTime,
                timesPlayed = (previousState?.timesPlayed ?: 0) + 1
            )
            levelStateDao.insertOrUpdate(updatedLevelState)

            // Calculate XP and Coins rewards
            var xpEarned = 0
            var coinsEarned = 0

            if (isFirstTime) {
                xpEarned += level.xpReward
                coinsEarned += level.coinsReward

                // Additional stars rewards
                if (starsEarned == 3) {
                    xpEarned += 25
                    coinsEarned += 10
                }
                // Perfect completion (no errors, no hints)
                if (errorsCount == 0 && hintsUsed == 0) {
                    xpEarned += 30
                    coinsEarned += 20
                    // Perfect achievements
                    evaluateAchievement("perfect_clean", 1)
                    incrementAchievementProgress("perfect_streak", 1)
                    incrementAchievementProgress("weekly_3", 1) // Weekly task
                } else {
                    // Break perfect streak
                    resetAchievementProgress("perfect_streak")
                }

                // No hint achievement
                if (hintsUsed == 0) {
                    evaluateAchievement("no_hints", 1)
                    incrementAchievementProgress("own_master", 1)
                }
            } else {
                // Replay reward (smaller)
                xpEarned += 15
                coinsEarned += 5
            }

            // Apply XP and Level Up checks
            var currentXp = progress.xp + xpEarned
            var currentLevel = progress.level
            var didLevelUp = false

            while (currentXp >= currentLevel * 200) {
                currentXp -= (currentLevel * 200)
                currentLevel += 1
                didLevelUp = true
            }

            val nextLevelId = levelId + 1
            val newHighestUnlockedLevelId = if (isFirstTime) {
                progress.highestUnlockedLevelId.coerceAtLeast(nextLevelId).coerceAtMost(45)
            } else {
                progress.highestUnlockedLevelId
            }

            // Save progress back
            val updatedProgress = progress.copy(
                level = currentLevel,
                xp = currentXp,
                coins = progress.coins + coinsEarned,
                hearts = if (didLevelUp) 5 else progress.hearts, // Fully restore hearts on level up!
                highestUnlockedLevelId = newHighestUnlockedLevelId
            )
            progressDao.insertOrUpdate(updatedProgress)

            if (didLevelUp) {
                notificationDao.insertNotification(NotificationEntity(
                    title = "Seviye Atladın!",
                    message = "Tebrikler! Seviye $currentLevel oldun. 5 can ve ödül jetonları tanımlandı."
                ))
            }

            // Event-driven Task Progress increments
            incrementAchievementProgress("daily_1", 1) // Daily task: complete 2 levels
            incrementAchievementProgress("weekly_1", 1) // Weekly task: complete 15 levels
            incrementAchievementProgress("warm_up", 1) // Achievement: 5 levels
            incrementAchievementProgress("puzzle_lover", 1) // Achievement: 25 levels
            incrementAchievementProgress("crossword_master", 1) // Achievement: 40 levels

            // World clear achievement checks
            checkWorldClears()

            // Time limits achievements
            if (timeTakenSeconds <= 180) {
                evaluateAchievement("fast_thinker", 1)
                incrementAchievementProgress("time_lord", 1)
            }

            // Secret achievements
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour in 0..4) {
                evaluateAchievement("night_owl", 1)
            } else if (hour in 5..6) {
                evaluateAchievement("early_bird", 1)
            }

            // Save stats and return result
            PuzzleResult(
                xpEarned = xpEarned,
                coinsEarned = coinsEarned,
                starsEarned = starsEarned,
                didLevelUp = didLevelUp,
                isFirstTime = isFirstTime
            )
        }
    }

    // Check if entire worlds have been completed to unlock achievements
    private suspend fun checkWorldClears() {
        val completedLevels = levelStateDao.getAllLevelStates().firstOrNull()?.filter { it.completed }?.map { it.levelId } ?: emptyList()

        val world1Levels = (1..15).toList()
        val world2Levels = (16..30).toList()
        val world3Levels = (31..45).toList()

        if (completedLevels.containsAll(world1Levels)) {
            evaluateAchievement("world_1_clear", 15)
        }
        if (completedLevels.containsAll(world2Levels)) {
            evaluateAchievement("world_2_clear", 15)
        }
        if (completedLevels.containsAll(world3Levels)) {
            evaluateAchievement("world_3_clear", 15)
        }
    }

    // Complete a chest node and gain rewards using an atomic transaction
    suspend fun claimChest(levelId: Int): ChestClaimResult = withContext(Dispatchers.IO) {
        db.withTransaction {
            val progress = progressDao.getProgressDirect() ?: throw IllegalStateException("Progress not found")
            if (levelId > progress.highestUnlockedLevelId) {
                throw IllegalStateException("Chest level is locked")
            }

            val level = LevelsData.getLevels().find { it.id == levelId } ?: throw IllegalArgumentException("Level not found")
            if (!level.isChest) {
                throw IllegalArgumentException("Level is not a chest node")
            }

            val previousState = levelStateDao.getLevelState(levelId)
            if (previousState != null && previousState.completed) {
                throw IllegalStateException("Chest has already been claimed")
            }

            // Mark completed so it can't be claimed again
            levelStateDao.insertOrUpdate(LevelStateEntity(levelId = levelId, completed = true, stars = 3))

            // Chest rewards based on world
            val rewardCoins = when {
                levelId <= 15 -> 100
                levelId <= 30 -> 200
                else -> 350
            }
            val rewardXp = when {
                levelId <= 15 -> 150
                levelId <= 30 -> 300
                else -> 500
            }

            var currentXp = progress.xp + rewardXp
            var currentLevel = progress.level
            var didLevelUp = false
            while (currentXp >= currentLevel * 200) {
                currentXp -= (currentLevel * 200)
                currentLevel += 1
                didLevelUp = true
            }

            val nextLevelId = levelId + 1
            val newHighestLevelId = progress.highestUnlockedLevelId.coerceAtLeast(nextLevelId).coerceAtMost(45)

            val updatedProgress = progress.copy(
                coins = progress.coins + rewardCoins,
                level = currentLevel,
                xp = currentXp,
                hearts = 5, // Fully refill
                highestUnlockedLevelId = newHighestLevelId
            )
            progressDao.insertOrUpdate(updatedProgress)

            notificationDao.insertNotification(NotificationEntity(
                title = "Hazine Sandığı Açıldı!",
                message = "$rewardCoins jeton ve $rewardXp XP kazandın!"
            ))

            ChestClaimResult(
                levelId = levelId,
                heartsEarned = (5 - progress.hearts).coerceAtLeast(0),
                coinsEarned = rewardCoins,
                xpEarned = rewardXp,
                didLevelUp = didLevelUp,
                newLevel = currentLevel
            )
        }
    }

    // Word solved event
    suspend fun registerWordSolved(
        wordId: String,
        wasCorrect: Boolean,
        solveMethod: WordSolveMethod = WordSolveMethod.PLAYER
    ) = withContext(Dispatchers.IO) {
        val word = wordDao.getWordById(wordId) ?: return@withContext
        val timesCorrect = word.timesCorrect + (if (wasCorrect) 1 else 0)
        val timesWrong = word.timesWrong + (if (wasCorrect) 0 else 1)
        val isLearned = timesCorrect >= 2 // Mark learned after 2 correct solves

        // Review level increase ONLY if solved by PLAYER!
        val newReviewLevel = if (wasCorrect) {
            if (solveMethod == WordSolveMethod.PLAYER) {
                (word.reviewLevel + 1).coerceAtMost(5)
            } else {
                word.reviewLevel // No review level increase for hint solves
            }
        } else {
            (word.reviewLevel - 1).coerceAtLeast(0)
        }

        val updatedWord = word.copy(
            timesCorrect = timesCorrect,
            timesWrong = timesWrong,
            isLearned = word.isLearned || isLearned,
            reviewLevel = newReviewLevel,
            lastReviewTime = System.currentTimeMillis()
        )
        wordDao.updateWord(updatedWord)

        if (wasCorrect) {
            // Stats & tasks progress ONLY if solved by PLAYER!
            if (solveMethod == WordSolveMethod.PLAYER) {
                incrementAchievementProgress("first_word", 1)
                incrementAchievementProgress("word_hunter", 1)
                incrementAchievementProgress("word_hoarder", 1)
                incrementAchievementProgress("daily_2", 1) // Daily task: 10 words solved
                incrementAchievementProgress("weekly_2", 1) // Weekly task: 80 words solved
            }
        }

        if (isLearned && !word.isLearned) {
            addNotification("Yeni Kelime Öğrenildi!", "\"${word.english.uppercase()}\" kelimesini artık biliyorsun!")
            // Check learned achievements
            val totalLearned = wordDao.getLearnedCountFlow().firstOrNull() ?: 0
            evaluateAchievement("learned_50", totalLearned)
            evaluateAchievement("learned_100", totalLearned)
        }
    }

    // Words space repetition list
    suspend fun toggleFavorite(wordId: String) = withContext(Dispatchers.IO) {
        val word = wordDao.getWordById(wordId) ?: return@withContext
        val newFav = !word.isFavorite
        wordDao.updateWord(word.copy(isFavorite = newFav))

        if (newFav) {
            incrementAchievementProgress("first_fav", 1)
            incrementAchievementProgress("fav_collector", 1)
            incrementAchievementProgress("daily_3", 1) // Task progress
        }
    }

    suspend fun markLearned(wordId: String) = withContext(Dispatchers.IO) {
        val word = wordDao.getWordById(wordId) ?: return@withContext
        wordDao.updateWord(word.copy(isLearned = true))
        incrementAchievementProgress("daily_3", 1) // Task progress

        val totalLearned = wordDao.getLearnedCountFlow().firstOrNull() ?: 0
        evaluateAchievement("learned_50", totalLearned)
        evaluateAchievement("learned_100", totalLearned)
    }

    // Evaluate single value target achievement
    suspend fun evaluateAchievement(id: String, value: Int) = withContext(Dispatchers.IO) {
        val ach = achievementDao.getAchievementById(id) ?: return@withContext
        if (ach.isUnlocked) return@withContext

        val updatedVal = value.coerceAtMost(ach.targetValue)
        val isNowUnlocked = updatedVal >= ach.targetValue
        val unlockedAt = if (isNowUnlocked) System.currentTimeMillis() else 0L

        achievementDao.updateAchievement(ach.copy(
            currentValue = updatedVal,
            isUnlocked = isNowUnlocked,
            unlockedAt = unlockedAt
        ))

        if (isNowUnlocked) {
            claimAchievementReward(ach)
        }
    }

    // Progress increments for achievements/tasks
    suspend fun incrementAchievementProgress(id: String, increment: Int) = withContext(Dispatchers.IO) {
        // First check achievements
        achievementDao.getAchievementById(id)?.let { ach ->
            if (!ach.isUnlocked) {
                val newVal = (ach.currentValue + increment).coerceAtMost(ach.targetValue)
                val isUnlocked = newVal >= ach.targetValue
                val unlockedAt = if (isUnlocked) System.currentTimeMillis() else 0L
                achievementDao.updateAchievement(ach.copy(
                    currentValue = newVal,
                    isUnlocked = isUnlocked,
                    unlockedAt = unlockedAt
                ))
                if (isUnlocked) {
                    claimAchievementReward(ach)
                }
            }
        }

        // Also check tasks
        taskDao.getTaskById(id)?.let { task ->
            if (!task.isCompleted) {
                val newVal = (task.currentValue + increment).coerceAtMost(task.targetValue)
                val isCompleted = newVal >= task.targetValue
                taskDao.updateTask(task.copy(
                    currentValue = newVal,
                    isCompleted = isCompleted
                ))
                if (isCompleted) {
                    addNotification("Görev Tamamlandı!", "\"${task.title}\" görevi bitti. Ödülünü topla!")
                }
            }
        }
    }

    private suspend fun resetAchievementProgress(id: String) {
        achievementDao.getAchievementById(id)?.let { ach ->
            if (!ach.isUnlocked) {
                achievementDao.updateAchievement(ach.copy(currentValue = 0))
            }
        }
    }

    private suspend fun claimAchievementReward(ach: AchievementEntity) {
        val progress = progressDao.getProgressDirect() ?: return
        var currentXp = progress.xp + ach.rewardXp
        var currentLevel = progress.level
        while (currentXp >= currentLevel * 200) {
            currentXp -= (currentLevel * 200)
            currentLevel += 1
        }
        progressDao.insertOrUpdate(progress.copy(
            coins = progress.coins + ach.rewardCoins,
            level = currentLevel,
            xp = currentXp
        ))
        addNotification("Başarım Açıldı!", "\"${ach.title}\" başarımı tamamlandı! +${ach.rewardCoins} Jeton ve +${ach.rewardXp} XP kazandın!")
    }

    // Task rewards claiming
    suspend fun claimTaskReward(taskId: String): Boolean = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext false
        if (task.isCompleted && !task.isClaimed) {
            val progress = progressDao.getProgressDirect() ?: return@withContext false
            var currentXp = progress.xp + task.rewardXp
            var currentLevel = progress.level
            while (currentXp >= currentLevel * 200) {
                currentXp -= (currentLevel * 200)
                currentLevel += 1
            }
            progressDao.insertOrUpdate(progress.copy(
                coins = progress.coins + task.rewardCoins,
                level = currentLevel,
                xp = currentXp
            ))
            taskDao.updateTask(task.copy(isClaimed = true))
            addNotification("Ödül Alındı!", "\"${task.title}\" görevinin ödülleri toplandı.")
            true
        } else {
            false
        }
    }

    // Daily Login Rewards claim (1 to 7 days)
    suspend fun claimDailyLoginReward(): DailyRewardResult = withContext(Dispatchers.IO) {
        db.withTransaction {
            val progress = progressDao.getProgressDirect() ?: return@withTransaction DailyRewardResult.AlreadyClaimedToday
            val todayStr = getTodayDateString()
            if (progress.lastDailyRewardClaimDate == todayStr) {
                return@withTransaction DailyRewardResult.AlreadyClaimedToday
            }

            val lastClaim = progress.lastDailyRewardClaimDate
            val yesterdayStr = getYesterdayDateString()

            val cycleDay = when {
                lastClaim.isEmpty() -> 1
                lastClaim == yesterdayStr -> {
                    val lastCycle = progress.dailyRewardCycleDay
                    if (lastCycle >= 7) 1 else lastCycle + 1
                }
                else -> 1 // Streak broken - reset to day 1
            }

            val reward = DailyRewardCatalog.rewards.find { it.day == cycleDay } ?: DailyReward(cycleDay, 50)
            val nextCycleDay = if (cycleDay >= 7) 1 else cycleDay + 1

            val updatedProgress = progress.copy(
                coins = progress.coins + reward.coins,
                lastDailyRewardClaimDate = todayStr,
                dailyRewardCycleDay = cycleDay,
                lastClaimedLoginDay = cycleDay
            )
            progressDao.insertOrUpdate(updatedProgress)

            notificationDao.insertNotification(NotificationEntity(
                title = "Giriş Ödülü!",
                message = "$cycleDay. Gün giriş ödülü olarak ${reward.coins} jeton kazandın!"
            ))

            DailyRewardResult.Claimed(
                day = cycleDay,
                coins = reward.coins,
                nextCycleDay = nextCycleDay
            )
        }
    }

    // Complete vocabulary practice and earn rewards (5 XP and 2 coins per correct answer)
    suspend fun completePracticeSession(wordsSolved: Int, correctAnswers: Int) = withContext(Dispatchers.IO) {
        if (correctAnswers <= 0) return@withContext
        db.withTransaction {
            val progress = progressDao.getProgressDirect() ?: return@withTransaction
            val xpEarned = correctAnswers * 5
            val coinsEarned = correctAnswers * 2

            var currentXp = progress.xp + xpEarned
            var currentLevel = progress.level
            var didLevelUp = false

            while (currentXp >= currentLevel * 200) {
                currentXp -= (currentLevel * 200)
                currentLevel += 1
                didLevelUp = true
            }

            val updatedProgress = progress.copy(
                level = currentLevel,
                xp = currentXp,
                coins = progress.coins + coinsEarned,
                hearts = if (didLevelUp) 5 else progress.hearts
            )
            progressDao.insertOrUpdate(updatedProgress)

            if (didLevelUp) {
                notificationDao.insertNotification(NotificationEntity(
                    title = "Seviye Atladın!",
                    message = "Sözlük pratiğiyle seviye atladın! Yeni seviyen: $currentLevel."
                ))
            } else {
                notificationDao.insertNotification(NotificationEntity(
                    title = "Sözlük Pratiği Tamamlandı!",
                    message = "Pratik testi sonucunda $coinsEarned jeton ve $xpEarned XP kazandın!"
                ))
            }
        }
    }

    // Reset progress completely with confirmation
    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        // Clear tables or drop DB
        db.clearAllTables()

        // Re-seed database
        val initialProgress = UserProgressEntity(
            id = 1, level = 1, xp = 0, coins = 300, hearts = 5,
            lastHeartRestoreTime = System.currentTimeMillis(), streak = 1,
            lastActiveDate = getTodayDateString(), avatar = "avatar_1", username = "Çaprazca Gezgini",
            lastClaimedLoginDay = 0,
            highestUnlockedLevelId = 1,
            lastDailyRewardClaimDate = "",
            dailyRewardCycleDay = 0
        )
        progressDao.insertOrUpdate(initialProgress)

        val wordsList = DictionaryData.getPreloadedWords()
        wordDao.insertWords(wordsList)

        val seedAchievements = listOf(
            AchievementEntity("first_step", "İlk Adım", "İlk bulmacanı başarıyla tamamla.", "Progress", "COMMON", 1, rewardCoins = 20, rewardXp = 50),
            AchievementEntity("warm_up", "Isınıyorum", "5 bulmaca tamamla.", "Progress", "COMMON", 5, rewardCoins = 30, rewardXp = 100),
            AchievementEntity("puzzle_lover", "Bulmaca Sever", "25 bulmaca tamamla.", "Progress", "RARE", 25, rewardCoins = 100, rewardXp = 250),
            AchievementEntity("crossword_master", "Kare Ustası", "40 bulmaca tamamla.", "Progress", "EPIC", 40, rewardCoins = 200, rewardXp = 500),
            AchievementEntity("first_word", "İlk Kelime", "İlk kelimeni doğru çöz.", "Vocabulary", "COMMON", 1, rewardCoins = 10, rewardXp = 20),
            AchievementEntity("word_hunter", "Kelime Avcısı", "100 kelime çöz.", "Vocabulary", "RARE", 100, rewardCoins = 50, rewardXp = 150),
            AchievementEntity("word_hoarder", "Kelime Hazinesi", "250 kelime çöz.", "Vocabulary", "EPIC", 250, rewardCoins = 150, rewardXp = 400),
            AchievementEntity("perfect_clean", "Tertemiz", "Bir bölümü hiç hata yapmadan tamamla.", "Skill", "COMMON", 1, rewardCoins = 50, rewardXp = 100),
            AchievementEntity("perfect_streak", "Kusursuz Seri", "Art arda 3 bölümü hatasız tamamla.", "Skill", "EPIC", 3, rewardCoins = 150, rewardXp = 300),
            AchievementEntity("no_hints", "Yardımsız", "Bir bölümü hiç ipucu kullanmadan bitir.", "Skill", "COMMON", 1, rewardCoins = 30, rewardXp = 80),
            AchievementEntity("own_master", "Kendi Başına", "10 bölümü hiç ipucu kullanmadan bitir.", "Skill", "RARE", 10, rewardCoins = 100, rewardXp = 250),
            AchievementEntity("fast_thinker", "Hızlı Düşün", "Bir bölümü 3 dakikanın (180s) altında bitir.", "Time", "COMMON", 1, rewardCoins = 30, rewardXp = 60),
            AchievementEntity("time_lord", "Zamanın Efendisi", "10 bölümü 3 dakikanın altında bitir.", "Time", "RARE", 10, rewardCoins = 120, rewardXp = 200),
            AchievementEntity("first_fav", "İlk Favori", "Sözlükte bir kelimeyi favorilerine ekle.", "Dictionary", "COMMON", 1, rewardCoins = 10, rewardXp = 30),
            AchievementEntity("fav_collector", "Koleksiyoncu", "Sözlükte 10 kelimeyi favorilerine ekle.", "Dictionary", "COMMON", 10, rewardCoins = 30, rewardXp = 100),
            AchievementEntity("learned_50", "Dil Meraklısı", "Sözlükte 50 kelimeyi öğrendim olarak işaretle.", "Dictionary", "RARE", 50, rewardCoins = 100, rewardXp = 250),
            AchievementEntity("learned_100", "Dil Kaşifi", "Sözlükte 100 kelimeyi öğrendim olarak işaretle.", "Dictionary", "EPIC", 100, rewardCoins = 250, rewardXp = 500),
            AchievementEntity("three_days", "3 Gün Üst Üste", "3 günlük giriş serisi oluştur.", "Streak", "COMMON", 3, rewardCoins = 50, rewardXp = 100),
            AchievementEntity("seven_days", "Bir Haftalık Seri", "7 günlük giriş serisi oluştur.", "Streak", "RARE", 7, rewardCoins = 150, rewardXp = 300),
            AchievementEntity("world_1_clear", "A1 Mezunu", "1. Dünya'daki tüm bölümleri tamamla.", "Theme", "COMMON", 15, rewardCoins = 100, rewardXp = 200),
            AchievementEntity("world_2_clear", "A2 Kaşifi", "2. Dünya'daki tüm bölümleri tamamla.", "Theme", "RARE", 15, rewardCoins = 200, rewardXp = 400),
            AchievementEntity("world_3_clear", "B1 Yolcusu", "3. Dünya'daki tüm bölümleri tamamla.", "Theme", "EPIC", 15, rewardCoins = 300, rewardXp = 600),
            AchievementEntity("night_owl", "Gece Kuşu", "Gece yarısından sonra (00:00-04:00) bir bölüm tamamla.", "Secret", "COMMON", 1, isSecret = true, rewardCoins = 50, rewardXp = 100),
            AchievementEntity("early_bird", "Erken Kalkan", "Sabah 06:00'dan önce bir bölüm tamamla.", "Secret", "COMMON", 1, isSecret = true, rewardCoins = 50, rewardXp = 100)
        )
        achievementDao.insertAchievements(seedAchievements)

        val seedTasks = listOf(
            UserTaskEntity("daily_1", "Bulmaca Fatihi", "2 bulmaca tamamla.", "DAILY", 2, rewardXp = 30, rewardCoins = 10, lastUpdatedDate = getTodayDateString()),
            UserTaskEntity("daily_2", "Kelime Avcısı", "10 kelime çöz.", "DAILY", 10, rewardXp = 30, rewardCoins = 10, lastUpdatedDate = getTodayDateString()),
            UserTaskEntity("daily_3", "Ezberci", "Sözlükte 3 kelimeyi favoriye ekle veya öğrenildi yap.", "DAILY", 3, rewardXp = 40, rewardCoins = 15, lastUpdatedDate = getTodayDateString()),
            UserTaskEntity("weekly_1", "Haftalık Usta", "15 bulmaca tamamla.", "WEEKLY", 15, rewardXp = 150, rewardCoins = 50, lastUpdatedDate = getTodayDateString()),
            UserTaskEntity("weekly_2", "Kelime Koleksiyoneri", "80 kelime çöz.", "WEEKLY", 80, rewardXp = 150, rewardCoins = 50, lastUpdatedDate = getTodayDateString()),
            UserTaskEntity("weekly_3", "Kusursuz Hafta", "5 bölümü hiç hata yapmadan tamamla.", "WEEKLY", 5, rewardXp = 200, rewardCoins = 75, lastUpdatedDate = getTodayDateString())
        )
        taskDao.insertTasks(seedTasks)

        addNotification("Sıfırlama Başarılı!", "Tüm oyun ilerlemen başarıyla sıfırlandı.")
    }

    // Update profile settings
    suspend fun updateProfile(username: String, avatar: String) = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext
        progressDao.insertOrUpdate(progress.copy(username = username, avatar = avatar))
    }

    // Clean notification logs
    suspend fun clearNotifications() = withContext(Dispatchers.IO) {
        notificationDao.clearAllNotifications()
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    // Helper functions for date arithmetic
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}

// Support classes
data class PuzzleResult(
    val xpEarned: Int,
    val coinsEarned: Int,
    val starsEarned: Int,
    val didLevelUp: Boolean,
    val isFirstTime: Boolean
)

data class RewardItem(
    val name: String,
    val amount: Int,
    val iconKey: String
)
