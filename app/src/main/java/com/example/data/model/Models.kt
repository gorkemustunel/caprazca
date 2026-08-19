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
    val dailyRewardCycleDay: Int = 0,

    // Dino Evolution System Fields
    val dinoStage: String = "EGG",
    val dinoNickname: String = "Mino",
    val unlockedDinoStages: String = "EGG",
    val selectedDinoColor: String = "default",
    val selectedAccessory: String = "none",
    val lastEvolutionSeen: String = "NONE",
    val lastFreeHeartClaimDate: String = ""
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
    val wordIds: List<String> = emptyList(), // Added to store raw IDs for lazy generation
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
        DailyReward(day = 1, coins = 40),
        DailyReward(day = 2, coins = 55),
        DailyReward(day = 3, coins = 70),
        DailyReward(day = 4, coins = 85),
        DailyReward(day = 5, coins = 105),
        DailyReward(day = 6, coins = 130),
        DailyReward(day = 7, coins = 180)
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

// ================= DINO EVOLUTION SYSTEM MODELS =================

enum class DinoStage(val displayName: String, val description: String) {
    EGG("Gizemli Yumurta", "İlk bulmacanı bekliyor. Çözdükçe çatlayacak ve içindeki küçük dost ortaya çıkacak."),
    TINY_BABY("Minik Yavru", "Dünyaya merhaba de! Henüz çok küçük ama öğrenmeye hazır."),
    GROWING_BABY("Gelişen Yavru", "Biraz daha büyüdü ve kendine güveni geldi."),
    EXPLORER("Kaşif", "Sırt çantasıyla yeni diyarlar keşfeden cesur bir gezgin."),
    SCHOLAR("Bilgin", "Okuyan, araştıran ve kelimelerin gücünü bilen bir bilge."),
    MASTER("Usta", "Dilin ve bilginin zirvesine ulaşmış efsanevi bir usta.");

    companion object {
        fun safeValueOf(name: String): DinoStage {
            return when (name.uppercase()) {
                "EGG" -> EGG
                "TINY_BABY" -> TINY_BABY
                "BABY", "GROWING_BABY" -> GROWING_BABY
                "EXPLORER" -> EXPLORER
                "SCHOLAR" -> SCHOLAR
                "MASTER" -> MASTER
                else -> EGG
            }
        }
    }
}

enum class DinoMood {
    IDLE, HAPPY, CURIOUS, CELEBRATING, SHOCKED, THINKING
}

enum class DinoAnimationState {
    IDLE, BLINK, HAPPY, JUMP, THINKING, CELEBRATING, HATCHING, EVOLVING
}

enum class DinoAccessory(val id: String, val displayName: String) {
    NONE("none", "Yok"),
    BACKPACK("backpack", "Sırt Çantası"),
    GLASSES("glasses", "Gözlük"),
    HAT("hat", "Kaşif Şapkası"),
    BOOK("book", "Bilgi Kitabı"),
    BADGE("badge", "Altın Rozet")
}

data class DinoMilestone(
    val completedLevels: Int,
    val title: String,
    val description: String,
    val stage: DinoStage? = null,
    val accessory: DinoAccessory? = null
) {
    val pendingKey: String
        get() = when {
            completedLevels == 1 && stage == DinoStage.TINY_BABY -> "PENDING_HATCHING"
            stage != null -> "PENDING_STAGE_${stage.name}"
            accessory != null -> "PENDING_ACCESSORY_${accessory.name}"
            else -> "PENDING_DINO_$completedLevels"
        }

    val seenKey: String
        get() = pendingKey.removePrefix("PENDING_")
}

object DinoEvolutionPlan {
    val milestones = listOf(
        DinoMilestone(1, "Yumurta Çatladı!", "İlk bulmacadan sonra minik dostun dünyaya geldi.", stage = DinoStage.TINY_BABY),
        DinoMilestone(4, "Dinozorun Büyüdü!", "Dikenleri belirginleşti ve daha özgüvenli duruyor.", stage = DinoStage.GROWING_BABY),
        DinoMilestone(7, "Macera Rozeti!", "İlk rozetini taktı. Artık yolculuk daha ciddi.", accessory = DinoAccessory.BADGE),
        DinoMilestone(10, "Kaşif Formu!", "Sırt çantasıyla haritayı keşfetmeye hazır.", stage = DinoStage.EXPLORER, accessory = DinoAccessory.BACKPACK),
        DinoMilestone(14, "Kaşif Şapkası!", "Yeni şapkasıyla her bölümde daha hazırlıklı görünüyor.", accessory = DinoAccessory.HAT),
        DinoMilestone(18, "Gözlük Kazandı!", "Kelimelere daha dikkatli bakıyor.", accessory = DinoAccessory.GLASSES),
        DinoMilestone(22, "Bilgin Formu!", "Artık kitaplarıyla çalışan küçük bir bilgin.", stage = DinoStage.SCHOLAR, accessory = DinoAccessory.BOOK),
        DinoMilestone(26, "Parlak Dikenler!", "Sırtındaki dikenler daha canlı parlıyor."),
        DinoMilestone(30, "Altın Parıltı!", "Başarılarını gösterecek altın detayları parlamaya başladı."),
        DinoMilestone(35, "Usta Formu!", "Efsanevi forma ulaştı ve artık çok daha güçlü görünüyor.", stage = DinoStage.MASTER)
    )

    fun stageForCompletedLevels(completedLevels: Int): DinoStage {
        return milestones
            .filter { it.completedLevels <= completedLevels && it.stage != null }
            .maxByOrNull { it.completedLevels }
            ?.stage
            ?: DinoStage.EGG
    }

    fun achievedMilestone(completedLevels: Int): DinoMilestone? {
        return milestones
            .filter { it.completedLevels <= completedLevels }
            .maxByOrNull { it.completedLevels }
    }

    fun nextMilestone(completedLevels: Int): DinoMilestone? {
        return milestones.firstOrNull { it.completedLevels > completedLevels }
    }
}

data class DinoEvolutionRequirement(
    val stage: DinoStage,
    val requiredXp: Int = 0,
    val requiredCompletedLevels: Int = 0,
    val requiredStars: Int = 0,
    val requiredWorldId: Int = 0
)

data class DinoProgress(
    val currentStage: DinoStage,
    val selectedVisualStage: DinoStage,
    val nickname: String,
    val unlockedStages: Set<DinoStage>,
    val unlockedAccessories: Set<DinoAccessory>,
    val selectedAccessory: DinoAccessory?,
    val pendingEvolution: DinoStage? = null
)

sealed interface DinoReactionEvent {
    data object CorrectAnswer : DinoReactionEvent
    data object WrongAnswer : DinoReactionEvent
    data class StreakReached(val streak: Int) : DinoReactionEvent
    data class LevelCompleted(
        val stars: Int,
        val earnedXp: Int
    ) : DinoReactionEvent
}
