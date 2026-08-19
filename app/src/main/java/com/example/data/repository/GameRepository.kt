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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.work.*
import com.example.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class GameRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val progressDao = db.userProgressDao()
    private val levelStateDao = db.levelStateDao()
    private val wordDao = db.wordDao()
    private val achievementDao = db.achievementDao()
    private val taskDao = db.taskDao()
    private val notificationDao = db.notificationDao()
    private val workManager = WorkManager.getInstance(context)

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

    val wordsToReviewFlow: Flow<List<WordEntity>> = wordDao.getAllWordsFlow()
        .map { words: List<WordEntity> ->
            val now = System.currentTimeMillis()
            words.filter { word ->
                if (word.reviewLevel <= 0) return@filter false
                val intervalDays = when (word.reviewLevel) {
                    1 -> 1; 2 -> 3; 3 -> 7; 4 -> 14; 5 -> 30; else -> 0
                }
                word.lastReviewTime + (intervalDays * 24 * 60 * 60 * 1000L) <= now
            }
        }

    // Initialize database with seed data if empty
    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        // 1. Seed user progress if not present
        val currentProgress = progressDao.getProgressDirect()
        if (currentProgress == null) {
            val initialProgress = UserProgressEntity(
                id = 1,
                level = 1,
                xp = 0,
                coins = EconomyConfig.STARTER_COINS,
                hearts = EconomyConfig.MAX_HEARTS,
                lastHeartRestoreTime = System.currentTimeMillis(),
                streak = 1,
                lastActiveDate = getTodayDateString(),
                avatar = "avatar_1",
                username = "Çaprazca Gezgini",
                dinoStage = "EGG",
                dinoNickname = "Mino",
                unlockedDinoStages = "EGG",
                lastEvolutionSeen = "NONE"
            )
            progressDao.insertOrUpdate(initialProgress)
            Log.d("GameRepository", "Seeded user progress.")
        }

        // 2. Seed/update dictionary without overwriting user word progress.
        seedMissingDictionaryWords()

        // 3. Seed achievements if empty
        val currentAchievements = achievementDao.getAllAchievements()
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
                AchievementEntity("early_bird", "Erken Kalkan", "Sabah 06:00'dan önce bir bölüm tamamla.", "Secret", "COMMON", 1, isSecret = true, rewardCoins = 50, rewardXp = 100),
                AchievementEntity("hints_used_stat", "Yardımsever", "İpucu kullanmaya başladın.", "Skill", "COMMON", 10000, isSecret = true)
            )
            achievementDao.insertAchievements(seedAchievements)
            Log.d("GameRepository", "Seeded achievements.")
        }

        // 4. Seed daily/weekly tasks and add newly introduced tasks without resetting progress.
        val currentTasks = taskDao.getAllTasks()
        val seedTasks = seedTasks(getTodayDateString())
        val missingTasks = seedTasks.filter { seed ->
            currentTasks.none { it.id == seed.id }
        }
        if (missingTasks.isNotEmpty()) {
            taskDao.insertTasks(missingTasks)
            Log.d("GameRepository", "Seeded ${missingTasks.size} missing tasks.")
        }
        seedTasks.forEach { seed ->
            val existing = currentTasks.firstOrNull { it.id == seed.id } ?: return@forEach
            val metadataChanged = existing.title != seed.title ||
                existing.description != seed.description ||
                existing.targetValue != seed.targetValue ||
                existing.rewardXp != seed.rewardXp ||
                existing.rewardCoins != seed.rewardCoins
            if (metadataChanged) {
                taskDao.updateTask(existing.copy(
                    title = seed.title,
                    description = seed.description,
                    targetValue = seed.targetValue,
                    rewardXp = seed.rewardXp,
                    rewardCoins = seed.rewardCoins
                ))
            }
        }

        // 5. Update streak & check heart regeneration on app launch
        updateStreakAndHearts()

        // 6. Schedule reminders
        scheduleDailyReminders()

        // Room Migration / Safety Normalization of Progress
        normalizeProgressAfterMigration()
    }

    private suspend fun seedMissingDictionaryWords() {
        val existingWords = wordDao.getAllWords()
        val existingById = existingWords.associateBy { it.id.lowercase(Locale.ENGLISH) }
        val seedWords = DictionaryData.getPreloadedWords()
        val missingWords = seedWords.filter { word ->
            word.id.lowercase(Locale.ENGLISH) !in existingById
        }
        if (missingWords.isNotEmpty()) {
            wordDao.insertWords(missingWords)
            Log.d("GameRepository", "Seeded ${missingWords.size} missing dictionary words.")
        }

        seedWords.forEach { seed ->
            val existing = existingById[seed.id.lowercase(Locale.ENGLISH)] ?: return@forEach
            val metadataChanged =
                existing.english != seed.english ||
                    existing.turkish != seed.turkish ||
                    existing.partOfSpeech != seed.partOfSpeech ||
                    existing.cefrLevel != seed.cefrLevel ||
                    existing.englishDefinition != seed.englishDefinition ||
                    existing.turkishDefinition != seed.turkishDefinition ||
                    existing.exampleSentence != seed.exampleSentence ||
                    existing.exampleTranslation != seed.exampleTranslation ||
                    existing.phonetic != seed.phonetic

            if (metadataChanged) {
                wordDao.updateWord(
                    existing.copy(
                        english = seed.english,
                        turkish = seed.turkish,
                        partOfSpeech = seed.partOfSpeech,
                        cefrLevel = seed.cefrLevel,
                        englishDefinition = seed.englishDefinition,
                        turkishDefinition = seed.turkishDefinition,
                        exampleSentence = seed.exampleSentence,
                        exampleTranslation = seed.exampleTranslation,
                        phonetic = seed.phonetic,
                        synonyms = seed.synonyms,
                        antonyms = seed.antonyms,
                        themes = seed.themes
                    )
                )
            }
        }
    }

    private fun seedTasks(today: String): List<UserTaskEntity> = listOf(
        UserTaskEntity("daily_1", "Bulmaca Fatihi", "2 bulmaca tamamla.", "DAILY", 2, rewardXp = 30, rewardCoins = 10, lastUpdatedDate = today),
        UserTaskEntity("daily_2", "Kelime Avcısı", "10 kelime çöz.", "DAILY", 10, rewardXp = 28, rewardCoins = 8, lastUpdatedDate = today),
        UserTaskEntity("daily_3", "Ezberci", "Sözlükte 3 kelimeyi favoriye ekle veya öğrenildi yap.", "DAILY", 3, rewardXp = 34, rewardCoins = 10, lastUpdatedDate = today),
        UserTaskEntity("daily_4", "Tertemiz Tur", "1 bölümü hatasız ve ipucusuz tamamla.", "DAILY", 1, rewardXp = 45, rewardCoins = 16, lastUpdatedDate = today),
        UserTaskEntity("daily_5", "İpucu Denemesi", "1 ipucu kullan.", "DAILY", 1, rewardXp = 18, rewardCoins = 4, lastUpdatedDate = today),
        UserTaskEntity("daily_6", "Pratik Zamanı", "Sözlükte 1 pratik kartı tamamla.", "DAILY", 1, rewardXp = 26, rewardCoins = 6, lastUpdatedDate = today),
        UserTaskEntity("daily_7", "Hızlı Koşu", "1 bölümü 3 dakikanın altında bitir.", "DAILY", 1, rewardXp = 34, rewardCoins = 10, lastUpdatedDate = today),
        UserTaskEntity("daily_8", "Yardımsız Çözüm", "1 bölümü ipucu kullanmadan bitir.", "DAILY", 1, rewardXp = 34, rewardCoins = 10, lastUpdatedDate = today),
        UserTaskEntity("daily_9", "Günlük Sandık", "Günlük giriş ödülünü topla.", "DAILY", 1, rewardXp = 18, rewardCoins = 4, lastUpdatedDate = today),
        UserTaskEntity("daily_10", "Favori Kelime", "Sözlükte 1 kelimeyi favoriye ekle.", "DAILY", 1, rewardXp = 18, rewardCoins = 5, lastUpdatedDate = today),
        UserTaskEntity("daily_11", "Yeni Kelime", "Sözlükte 1 kelimeyi öğrendim olarak işaretle.", "DAILY", 1, rewardXp = 22, rewardCoins = 6, lastUpdatedDate = today),
        UserTaskEntity("daily_12", "Az Hatalı Tur", "1 bölümü en fazla 1 hatayla tamamla.", "DAILY", 1, rewardXp = 30, rewardCoins = 8, lastUpdatedDate = today),
        UserTaskEntity("weekly_1", "Haftalık Usta", "15 bulmaca tamamla.", "WEEKLY", 15, rewardXp = 150, rewardCoins = 42, lastUpdatedDate = today),
        UserTaskEntity("weekly_2", "Kelime Koleksiyoneri", "80 kelime çöz.", "WEEKLY", 80, rewardXp = 140, rewardCoins = 38, lastUpdatedDate = today),
        UserTaskEntity("weekly_3", "Kusursuz Hafta", "5 bölümü hiç hata yapmadan tamamla.", "WEEKLY", 5, rewardXp = 190, rewardCoins = 58, lastUpdatedDate = today),
        UserTaskEntity("weekly_4", "Mükemmel Seri", "3 bölümü hatasız ve ipucusuz tamamla.", "WEEKLY", 3, rewardXp = 175, rewardCoins = 52, lastUpdatedDate = today),
        UserTaskEntity("weekly_5", "Sözlük Alışkanlığı", "20 sözlük çalışması veya işaretlemesi yap.", "WEEKLY", 20, rewardXp = 140, rewardCoins = 36, lastUpdatedDate = today),
        UserTaskEntity("weekly_6", "Yardımcı Araçlar", "5 ipucu kullan.", "WEEKLY", 5, rewardXp = 85, rewardCoins = 20, lastUpdatedDate = today),
        UserTaskEntity("weekly_7", "Hız Ustası", "5 bölümü 3 dakikanın altında bitir.", "WEEKLY", 5, rewardXp = 145, rewardCoins = 40, lastUpdatedDate = today),
        UserTaskEntity("weekly_8", "İpucusuz Hafta", "7 bölümü ipucu kullanmadan bitir.", "WEEKLY", 7, rewardXp = 165, rewardCoins = 46, lastUpdatedDate = today),
        UserTaskEntity("weekly_9", "Sadık Ziyaretçi", "5 günlük giriş ödülü topla.", "WEEKLY", 5, rewardXp = 110, rewardCoins = 30, lastUpdatedDate = today),
        UserTaskEntity("weekly_10", "Pratik Maratonu", "10 sözlük pratiği tamamla.", "WEEKLY", 10, rewardXp = 125, rewardCoins = 30, lastUpdatedDate = today),
        UserTaskEntity("weekly_11", "Favori Defteri", "10 kelimeyi favoriye ekle veya öğren.", "WEEKLY", 10, rewardXp = 120, rewardCoins = 30, lastUpdatedDate = today),
        UserTaskEntity("weekly_12", "Dikkatli Çözücü", "8 bölümü en fazla 1 hatayla tamamla.", "WEEKLY", 8, rewardXp = 150, rewardCoins = 42, lastUpdatedDate = today)
    )

    private suspend fun normalizeProgressAfterMigration() = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext

        // 1. Calculate highest unlocked level based on completed level states
        val completedStates = levelStateDao.getAllLevelStatesSync()
        val highestCompletedLevel = completedStates.filter { it.completed }.maxOfOrNull { it.levelId } ?: 0

        // highestUnlockedLevelId = min(N + 1, totalLevels)
        val totalLevels = LevelsData.getLevels().size
        val calculatedHighestUnlocked = (highestCompletedLevel + 1).coerceAtMost(totalLevels).coerceAtLeast(1)

        // 2. Migrate lastClaimedLoginDay to dailyRewardCycleDay if needed
        var updatedCycleDay = progress.dailyRewardCycleDay
        if (updatedCycleDay == 0 && progress.lastClaimedLoginDay > 0) {
            updatedCycleDay = progress.lastClaimedLoginDay
        }

        // 3. Dino stage normalization. Keep brand-new players in the egg state.
        val completedCount = completedStates.count { it.completed }
        val mappedStage = when(progress.dinoStage) {
            "TINY_BABY" -> if (completedCount == 0 && progress.lastEvolutionSeen == "PENDING_HATCHING") "EGG" else "TINY_BABY"
            "BABY" -> "GROWING_BABY"
            else -> progress.dinoStage
        }
        val mappedUnlocked = progress.unlockedDinoStages.split(",")
            .map { if (it == "BABY") "GROWING_BABY" else it }
            .distinct()
            .joinToString(",")
        val mappedEvolutionSeen = if (mappedStage == "EGG" && progress.lastEvolutionSeen == "PENDING_HATCHING") {
            "NONE"
        } else {
            progress.lastEvolutionSeen
        }
        val mappedUsername = when {
            progress.username.contains("Ã") ||
                progress.username.contains("Ä") ||
                progress.username.contains("Å") -> "Çaprazca Gezgini"
            else -> progress.username
        }

        if (progress.highestUnlockedLevelId < calculatedHighestUnlocked ||
            progress.dailyRewardCycleDay != updatedCycleDay ||
            progress.dinoStage != mappedStage ||
            progress.lastEvolutionSeen != mappedEvolutionSeen ||
            progress.username != mappedUsername) {

            progressDao.insertOrUpdate(progress.copy(
                highestUnlockedLevelId = maxOf(progress.highestUnlockedLevelId, calculatedHighestUnlocked),
                dailyRewardCycleDay = updatedCycleDay,
                dinoStage = mappedStage,
                unlockedDinoStages = mappedUnlocked,
                lastEvolutionSeen = mappedEvolutionSeen,
                username = mappedUsername
            ))
            Log.d("GameRepository", "Normalized progress fields after database check.")
        }
    }

    // Heart regeneration logic (5 hearts max, 1 heart per 15 mins)
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
            val heartRestoreMs = EconomyConfig.HEART_RESTORE_MINUTES * 60 * 1000L
            val elapsed = now - updatedRestoreTime
            if (elapsed >= heartRestoreMs) {
                val heartsToAdd = (elapsed / heartRestoreMs).toInt()
                updatedHearts = (updatedHearts + heartsToAdd).coerceAtMost(EconomyConfig.MAX_HEARTS)
                updatedRestoreTime = if (updatedHearts == EconomyConfig.MAX_HEARTS) {
                    now
                } else {
                    updatedRestoreTime + (heartsToAdd * heartRestoreMs)
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

        // 3. Reset tasks if needed
        resetTasksIfNeeded(today)
    }

    // Check if tasks need a reset
    private suspend fun resetTasksIfNeeded(today: String) {
        val tasks = taskDao.getAllTasks()

        // Reset DAILY tasks
        val dailyTasks = tasks.filter { it.type == "DAILY" }
        if (dailyTasks.isNotEmpty() && dailyTasks.first().lastUpdatedDate != today) {
            dailyTasks.forEach { task ->
                taskDao.updateTask(task.copy(
                    currentValue = 0,
                    isCompleted = false,
                    isClaimed = false,
                    lastUpdatedDate = today
                ))
            }
            addNotification("Günlük Görevler Yenilendi!", "Bugünkü görevlerini tamamla, ödülleri kap!")
        }

        // Reset WEEKLY tasks once per calendar week, even if the player skips Monday.
        val weeklyTasks = tasks.filter { it.type == "WEEKLY" }
        if (weeklyTasks.isNotEmpty() && weekKey(weeklyTasks.first().lastUpdatedDate) != weekKey(today)) {
             weeklyTasks.forEach { task ->
                taskDao.updateTask(task.copy(
                    currentValue = 0,
                    isCompleted = false,
                    isClaimed = false,
                    lastUpdatedDate = today
                ))
            }
            addNotification("Haftalık Görevler Yenilendi!", "Yeni haftanın görevleri hazır!")
        }
    }

    private fun weekKey(dateText: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateText) ?: return ""
            SimpleDateFormat("YYYY-ww", Locale.US).format(date)
        } catch (_: Exception) {
            ""
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
            incrementAchievementProgress("daily_5", 1)
            incrementAchievementProgress("weekly_6", 1)
            true
        } else {
            false
        }
    }

    // Buy heart with coins
    suspend fun buyHeart(): Boolean = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext false
        if (progress.coins >= EconomyConfig.HEART_COST && progress.hearts < EconomyConfig.MAX_HEARTS) {
            progressDao.insertOrUpdate(progress.copy(
                coins = progress.coins - EconomyConfig.HEART_COST,
                hearts = progress.hearts + 1
            ))
            addNotification("Can Satın Alındı!", "${EconomyConfig.HEART_COST} jeton karşılığında 1 can aldın.")
            true
        } else {
            false
        }
    }

    suspend fun claimFreeDailyHeart(): Boolean = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext false
        val today = getTodayDateString()
        if (progress.lastFreeHeartClaimDate == today || progress.hearts >= EconomyConfig.MAX_HEARTS) {
            return@withContext false
        }
        progressDao.insertOrUpdate(progress.copy(
            hearts = (progress.hearts + 1).coerceAtMost(EconomyConfig.MAX_HEARTS),
            lastFreeHeartClaimDate = today,
            lastHeartRestoreTime = System.currentTimeMillis()
        ))
        addNotification("Bonus Can!", "Günlük bonus olarak 1 can kazandın.")
        true
    }

    // Spend dynamic heart on game start
    suspend fun useHeart(): Boolean = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext false
        if (progress.hearts > 0) {
            val now = System.currentTimeMillis()
            val newRestoreTime = if (progress.hearts == 5) now else progress.lastHeartRestoreTime
            val updatedHearts = progress.hearts - 1
            progressDao.insertOrUpdate(progress.copy(
                hearts = updatedHearts,
                lastHeartRestoreTime = newRestoreTime
            ))
            scheduleHeartRefillReminder(updatedHearts)
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

    // Force restore hearts (explicit rewards such as chests or admin reset)
    suspend fun restoreAllHearts() = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext
        progressDao.insertOrUpdate(progress.copy(hearts = EconomyConfig.MAX_HEARTS, lastHeartRestoreTime = System.currentTimeMillis()))
    }

    private fun scheduleHeartRefillReminder(currentHearts: Int) {
        if (currentHearts >= EconomyConfig.MAX_HEARTS) {
            workManager.cancelAllWorkByTag("hearts_full")
            return
        }
        val delayMinutes = (EconomyConfig.MAX_HEARTS - currentHearts) * EconomyConfig.HEART_RESTORE_MINUTES
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf("type" to "hearts_full"))
            .addTag("hearts_full")
            .build()
        workManager.enqueueUniqueWork("hearts_full", ExistingWorkPolicy.REPLACE, workRequest)
    }

    fun scheduleDailyReminders() {
        // Daily reward reminder at 12:00
        val rewardRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(12), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("type" to "daily_reward"))
            .addTag("daily_reward")
            .build()
        workManager.enqueueUniquePeriodicWork("daily_reward", ExistingPeriodicWorkPolicy.KEEP, rewardRequest)

        // Streak reminder at 19:00
        val streakRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(19), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("type" to "streak_reminder"))
            .addTag("streak_reminder")
            .build()
        workManager.enqueueUniquePeriodicWork("streak_reminder", ExistingPeriodicWorkPolicy.KEEP, streakRequest)
    }

    private fun calculateInitialDelay(hour: Int): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis - now
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
                    xpEarned += EconomyConfig.THREE_STAR_BONUS_XP
                    coinsEarned += EconomyConfig.THREE_STAR_BONUS_COINS
                }
                // Perfect completion (no errors, no hints)
                if (errorsCount == 0 && hintsUsed == 0) {
                    xpEarned += EconomyConfig.PERFECT_BONUS_XP
                    coinsEarned += EconomyConfig.PERFECT_BONUS_COINS
                    // Perfect achievements
                    evaluateAchievement("perfect_clean", 1)
                    incrementAchievementProgress("perfect_streak", 1)
                    incrementAchievementProgress("weekly_3", 1) // Weekly task
                    incrementAchievementProgress("daily_4", 1)
                    incrementAchievementProgress("weekly_4", 1)
                } else {
                    // Break perfect streak
                    resetAchievementProgress("perfect_streak")
                }

                // No hint achievement
                if (hintsUsed == 0) {
                    evaluateAchievement("no_hints", 1)
                    incrementAchievementProgress("own_master", 1)
                    incrementAchievementProgress("daily_8", 1)
                    incrementAchievementProgress("weekly_8", 1)
                }
            } else {
                // Replay reward (smaller)
                xpEarned += EconomyConfig.REPLAY_XP
                coinsEarned += EconomyConfig.REPLAY_COINS
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
                progress.highestUnlockedLevelId.coerceAtLeast(nextLevelId).coerceAtMost(100)
            } else {
                progress.highestUnlockedLevelId
            }

            // Save progress back
            val updatedProgress = progress.copy(
                level = currentLevel,
                xp = currentXp,
                coins = progress.coins + coinsEarned,
                hearts = if (didLevelUp) EconomyConfig.MAX_HEARTS else progress.hearts,
                highestUnlockedLevelId = newHighestUnlockedLevelId
            )
            progressDao.insertOrUpdate(updatedProgress)

            if (didLevelUp) {
                notificationDao.insertNotification(NotificationEntity(
                    title = "Seviye Atladın!",
                    message = "Tebrikler! Seviye $currentLevel oldun. Yeni ödüller ve daha güçlü bir dino seni bekliyor."
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

            // Check for Dino Evolution
            checkDinoEvolution()

            // Time limits achievements
            if (timeTakenSeconds <= 180) {
                evaluateAchievement("fast_thinker", 1)
                incrementAchievementProgress("time_lord", 1)
                incrementAchievementProgress("daily_7", 1)
                incrementAchievementProgress("weekly_7", 1)
            }

            if (errorsCount <= 1) {
                incrementAchievementProgress("daily_12", 1)
                incrementAchievementProgress("weekly_12", 1)
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

        val world1Levels = (1..40).toList()
        val world2Levels = (41..75).toList()
        val world3Levels = (76..100).toList()

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
            val rewardCoins = EconomyConfig.chestCoins(levelId)
            val rewardXp = EconomyConfig.chestXp(levelId)

            var currentXp = progress.xp + rewardXp
            var currentLevel = progress.level
            var didLevelUp = false
            while (currentXp >= currentLevel * 200) {
                currentXp -= (currentLevel * 200)
                currentLevel += 1
                didLevelUp = true
            }

            val nextLevelId = levelId + 1
            val newHighestLevelId = progress.highestUnlockedLevelId.coerceAtLeast(nextLevelId).coerceAtMost(100)

            val updatedProgress = progress.copy(
                coins = progress.coins + rewardCoins,
                level = currentLevel,
                xp = currentXp,
                hearts = EconomyConfig.MAX_HEARTS, // Fully refill
                highestUnlockedLevelId = newHighestLevelId
            )
            progressDao.insertOrUpdate(updatedProgress)

            notificationDao.insertNotification(NotificationEntity(
                title = "Hazine Sandığı Açıldı!",
                message = "$rewardCoins jeton ve $rewardXp XP kazandın!"
            ))

            // Check for Dino Evolution
            checkDinoEvolution()

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
            incrementAchievementProgress("daily_10", 1)
            incrementAchievementProgress("weekly_5", 1)
            incrementAchievementProgress("weekly_11", 1)
        }
    }

    suspend fun markLearned(wordId: String) = withContext(Dispatchers.IO) {
        val word = wordDao.getWordById(wordId) ?: return@withContext
        wordDao.updateWord(word.copy(isLearned = true, reviewLevel = 1, lastReviewTime = System.currentTimeMillis()))
        incrementAchievementProgress("daily_3", 1) // Task progress
        incrementAchievementProgress("daily_11", 1)
        incrementAchievementProgress("weekly_5", 1)
        incrementAchievementProgress("weekly_11", 1)

        val totalLearned = wordDao.getLearnedCountFlow().firstOrNull() ?: 0
        evaluateAchievement("learned_50", totalLearned)
        evaluateAchievement("learned_100", totalLearned)
    }

    suspend fun updateWordReview(wordId: String, isCorrect: Boolean) = withContext(Dispatchers.IO) {
        val word = wordDao.getWordById(wordId) ?: return@withContext
        val newLevel = if (isCorrect) (word.reviewLevel + 1).coerceAtMost(5) else 1
        val newTimesCorrect = word.timesCorrect + if (isCorrect) 1 else 0
        val newTimesWrong = word.timesWrong + if (isCorrect) 0 else 1
        val isLearned = word.isLearned || newTimesCorrect >= 2 || newLevel >= 3
        wordDao.updateWord(word.copy(
            timesCorrect = newTimesCorrect,
            timesWrong = newTimesWrong,
            isLearned = isLearned,
            reviewLevel = newLevel,
            lastReviewTime = System.currentTimeMillis()
        ))

        if (isCorrect) {
            val progress = progressDao.getProgressDirect() ?: return@withContext
            var currentXp = progress.xp + 10 // 10 XP per correct review
            var currentLevel = progress.level
            while (currentXp >= currentLevel * 200) {
                currentXp -= (currentLevel * 200)
                currentLevel += 1
            }
            progressDao.insertOrUpdate(progress.copy(xp = currentXp, level = currentLevel))
            incrementAchievementProgress("daily_2", 1) // Counts as word solved
            incrementAchievementProgress("daily_3", 1)
            incrementAchievementProgress("weekly_5", 1)
            incrementAchievementProgress("weekly_11", 1)

            if (isLearned && !word.isLearned) {
                addNotification("Yeni Kelime Öğrenildi!", "\"${word.english.uppercase()}\" kelimesini artık biliyorsun!")
                val totalLearned = wordDao.getLearnedCountFlow().firstOrNull() ?: 0
                evaluateAchievement("learned_50", totalLearned)
                evaluateAchievement("learned_100", totalLearned)
            }
        }
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
        var didLevelUp = false
        while (currentXp >= currentLevel * 200) {
            currentXp -= (currentLevel * 200)
            currentLevel += 1
            didLevelUp = true
        }
        progressDao.insertOrUpdate(progress.copy(
            coins = progress.coins + ach.rewardCoins,
            level = currentLevel,
            xp = currentXp,
            hearts = if (didLevelUp) EconomyConfig.MAX_HEARTS else progress.hearts
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
            var didLevelUp = false
            while (currentXp >= currentLevel * 200) {
                currentXp -= (currentLevel * 200)
                currentLevel += 1
                didLevelUp = true
            }
            progressDao.insertOrUpdate(progress.copy(
                coins = progress.coins + task.rewardCoins,
                level = currentLevel,
                xp = currentXp,
                hearts = if (didLevelUp) EconomyConfig.MAX_HEARTS else progress.hearts
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

            val cycleDay = getNextDailyRewardDay(progress)

            val reward = DailyRewardCatalog.rewards.find { it.day == cycleDay } ?: DailyReward(cycleDay, 40)
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

            incrementAchievementProgress("daily_9", 1)
            incrementAchievementProgress("weekly_9", 1)

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
            val xpEarned = correctAnswers * EconomyConfig.PRACTICE_XP_PER_CORRECT
            val coinsEarned = correctAnswers * EconomyConfig.PRACTICE_COINS_PER_CORRECT

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
                hearts = if (didLevelUp) EconomyConfig.MAX_HEARTS else progress.hearts
            )
            progressDao.insertOrUpdate(updatedProgress)
            incrementAchievementProgress("daily_6", 1)
            incrementAchievementProgress("weekly_5", correctAnswers)
            incrementAchievementProgress("weekly_10", 1)

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
            id = 1, level = 1, xp = 0, coins = EconomyConfig.STARTER_COINS, hearts = EconomyConfig.MAX_HEARTS,
            lastHeartRestoreTime = System.currentTimeMillis(), streak = 1,
            lastActiveDate = getTodayDateString(), avatar = "avatar_1", username = "Çaprazca Gezgini",
            lastClaimedLoginDay = 0,
            highestUnlockedLevelId = 1,
            lastDailyRewardClaimDate = "",
            dailyRewardCycleDay = 0,
            dinoStage = "EGG",
            dinoNickname = "Mino",
            unlockedDinoStages = "EGG",
            lastEvolutionSeen = "NONE"
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

        taskDao.insertTasks(seedTasks(getTodayDateString()))

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

    // ================= DINO EVOLUTION LOGIC =================

    suspend fun checkDinoEvolution() = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext

        val completedLevels = levelStateDao.getCompletedCountFlow().firstOrNull() ?: 0
        val milestone = DinoEvolutionPlan.achievedMilestone(completedLevels) ?: return@withContext
        if (progress.lastEvolutionSeen == milestone.seenKey || progress.lastEvolutionSeen == milestone.pendingKey) {
            return@withContext
        }

        val targetStage = DinoEvolutionPlan.stageForCompletedLevels(completedLevels)
        val selectedAccessory = milestone.accessory?.id ?: progress.selectedAccessory
        if (milestone.stage != null) {
            val unlockedStages = progress.unlockedDinoStages.split(",").toMutableSet()
            unlockedStages.add(targetStage.name)

            val updatedProgress = progress.copy(
                dinoStage = targetStage.name,
                unlockedDinoStages = unlockedStages.joinToString(","),
                selectedAccessory = selectedAccessory,
                lastEvolutionSeen = milestone.pendingKey
            )
            progressDao.insertOrUpdate(updatedProgress)
        } else {
            progressDao.insertOrUpdate(
                progress.copy(
                    dinoStage = targetStage.name,
                    selectedAccessory = selectedAccessory,
                    lastEvolutionSeen = milestone.pendingKey
                )
            )
        }
        addNotification(milestone.title, milestone.description)
    }

    suspend fun renameDino(newName: String) = withContext(Dispatchers.IO) {
        if (newName.length in 2..14) {
            val progress = progressDao.getProgressDirect() ?: return@withContext
            progressDao.insertOrUpdate(progress.copy(dinoNickname = newName))
        }
    }

    suspend fun markEvolutionAsSeen() = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressDirect() ?: return@withContext
        if (progress.lastEvolutionSeen.startsWith("PENDING_")) {
            val stage = progress.lastEvolutionSeen.removePrefix("PENDING_")
            progressDao.insertOrUpdate(progress.copy(lastEvolutionSeen = stage))
        }
    }

    // Helper functions for date arithmetic
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun getNextDailyRewardDay(progress: UserProgressEntity): Int {
        val today = getTodayDateString()
        if (progress.lastDailyRewardClaimDate == today) return -1

        val lastCycleDay = when {
            progress.dailyRewardCycleDay in 1..7 -> progress.dailyRewardCycleDay
            progress.lastClaimedLoginDay in 1..7 -> progress.lastClaimedLoginDay
            else -> 0
        }

        return when (progress.lastDailyRewardClaimDate) {
            getYesterdayDateString() -> (lastCycleDay % 7) + 1
            "" -> if (lastCycleDay > 0) (lastCycleDay % 7) + 1 else 1
            else -> 1
        }
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
