package com.example.data.repository

object EconomyConfig {
    const val STARTER_COINS = 240
    const val LETTER_HINT_COST = 20
    const val WORD_HINT_COST = 75
    const val HEART_COST = 120
    const val HEART_RESTORE_MINUTES = 15L
    const val MAX_HEARTS = 5

    const val THREE_STAR_BONUS_XP = 25
    const val THREE_STAR_BONUS_COINS = 8
    const val PERFECT_BONUS_XP = 35
    const val PERFECT_BONUS_COINS = 15
    const val REPLAY_XP = 10
    const val REPLAY_COINS = 3

    const val PRACTICE_XP_PER_CORRECT = 5
    const val PRACTICE_COINS_PER_CORRECT = 1

    fun levelXpReward(levelId: Int, isBoss: Boolean): Int {
        val base = when {
            levelId <= 20 -> 45
            levelId <= 60 -> 55
            else -> 70
        }
        return if (isBoss) base + 45 else base
    }

    fun levelCoinReward(levelId: Int, isBoss: Boolean): Int {
        val base = when {
            levelId <= 20 -> 18
            levelId <= 60 -> 24
            else -> 32
        }
        return if (isBoss) base + 18 else base
    }

    fun chestCoins(levelId: Int): Int = when {
        levelId <= 40 -> 90
        levelId <= 75 -> 150
        else -> 240
    }

    fun chestXp(levelId: Int): Int = when {
        levelId <= 40 -> 130
        levelId <= 75 -> 230
        else -> 360
    }
}
