package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 150,
    val hearts: Int = 5,
    val lastHeartRestoreTime: Long = 0L,
    val streak: Int = 0,
    val lastActiveDate: String = "",
    val avatar: String = "avatar_1",
    val username: String = "Çaprazca Yolcusu",
    val lastClaimedLoginDay: Int = 0,
    val highestUnlockedLevelId: Int = 1,
    val lastDailyRewardClaimDate: String = "",
    val dailyRewardCycleDay: Int = 0
)

@Entity(tableName = "level_state")
data class LevelStateEntity(
    @PrimaryKey val levelId: Int,
    val completed: Boolean = false,
    val stars: Int = 0,
    val bestTime: Int = 9999,
    val timesPlayed: Int = 0
)

@Entity(tableName = "word_state")
data class WordEntity(
    @PrimaryKey val id: String, // English word as ID (lowercase)
    val english: String,
    val turkish: String,
    val partOfSpeech: String,
    val cefrLevel: String,
    val englishDefinition: String,
    val turkishDefinition: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val phonetic: String,
    val synonyms: String = "", // Comma-separated
    val antonyms: String = "", // Comma-separated
    val themes: String = "", // Comma-separated
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val isFavorite: Boolean = false,
    val isLearned: Boolean = false,
    val reviewLevel: Int = 0, // Aralıklı tekrar seviyesi
    val lastReviewTime: Long = 0L
)

@Entity(tableName = "achievement_state")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val rarity: String, // "COMMON", "RARE", "EPIC", "LEGENDARY"
    val targetValue: Int,
    val currentValue: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val isSecret: Boolean = false,
    val rewardCoins: Int = 20,
    val rewardXp: Int = 50
)

@Entity(tableName = "task_state")
data class UserTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: String, // "DAILY", "WEEKLY"
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardXp: Int = 30,
    val rewardCoins: Int = 10,
    val lastUpdatedDate: String = "" // "YYYY-MM-DD" to track daily resets
)

@Entity(tableName = "notification_log")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// Statistics holder non-entity class
data class PlayerStatistics(
    val totalTimePlayedSeconds: Long = 0,
    val totalPuzzlesSolved: Int = 0,
    val totalPuzzlesFailed: Int = 0,
    val totalCorrectLetters: Int = 0,
    val totalWrongLetters: Int = 0,
    val totalWordsSolved: Int = 0,
    val hintsUsed: Int = 0,
    val perfectPuzzles: Int = 0,
    val streakRecord: Int = 0
)

enum class CefrLevel {
    A1,
    A2,
    B1,
    B2,
    C1,
    C2
}

enum class WordCategory(val value: String) {
    INTRO("Giriş"),
    FAMILY("Aile"),
    HOUSE("Ev"),
    SCHOOL("Okul"),
    FOOD("Yemek"),
    TIME("Zaman"),
    CITY("Şehir"),
    TRANSPORT("Ulaşım"),
    SHOPPING("Mağaza"),
    JOBS("Meslekler"),
    PLACES("Yerler"),
    ANIMALS("Hayvanlar"),
    NATURE("Doğa"),
    TRAVEL("Yolculuk")
}

enum class CrosswordDirection {
    HORIZONTAL,
    VERTICAL
}

// Represents a level word layout for the crossword
data class CrosswordWord(
    val wordId: String,
    val answerEnglish: String,
    val clueTurkish: String,
    val clueDescriptionTurkish: String,
    val row: Int,
    val col: Int,
    val direction: CrosswordDirection,
    val number: Int
)

data class PuzzleLevel(
    val id: Int,
    val worldId: Int,
    val levelNumber: Int,
    val title: String,
    val theme: String,
    val cefrLevel: String,
    val isBoss: Boolean = false,
    val isChest: Boolean = false,
    val words: List<CrosswordWord> = emptyList(),
    val unlockStars: Int = 0,
    val xpReward: Int = 50,
    val coinsReward: Int = 20
)

enum class WordSolveMethod {
    PLAYER,
    LETTER_HINT,
    WORD_HINT
}

data class DailyReward(val day: Int, val coins: Int)

object DailyRewardCatalog {
    val rewards = listOf(
        DailyReward(day = 1, coins = 50),
        DailyReward(day = 2, coins = 100),
        DailyReward(day = 3, coins = 150),
        DailyReward(day = 4, coins = 200),
        DailyReward(day = 5, coins = 250),
        DailyReward(day = 6, coins = 300),
        DailyReward(day = 7, coins = 500)
    )
}

sealed interface DailyRewardResult {
    data class Claimed(
        val day: Int,
        val coins: Int,
        val nextCycleDay: Int
    ) : DailyRewardResult

    data object AlreadyClaimedToday : DailyRewardResult
}

data class ChestClaimResult(
    val levelId: Int,
    val heartsEarned: Int,
    val coinsEarned: Int,
    val xpEarned: Int,
    val didLevelUp: Boolean,
    val newLevel: Int
)
