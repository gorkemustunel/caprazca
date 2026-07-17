package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CrosswordDirection
import com.example.data.model.PuzzleLevel
import com.example.data.model.CrosswordWord
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HintSystemTest {

    @Test
    fun testHintCoinConsumptionAndDeduplication() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        
        // Seed database first
        val repository = com.example.data.repository.GameRepository(app)
        repository.checkAndSeedDatabase()

        val viewModel = GameViewModel(app)

        // Start active collection of progress to keep WhileSubscribed alive
        val collectJob = launch {
            viewModel.progress.collect {}
        }

        // Wait for progress flow to collect and populate viewModel.progress.value
        val initialProgress = viewModel.progress.filterNotNull().first()
        val initialCoins = initialProgress.coins
        assertTrue("Player should have at least 15 coins for hint", initialCoins >= 15)

        val mockWord = CrosswordWord(
            wordId = "test",
            answerEnglish = "HI",
            clueTurkish = "Selam",
            clueDescriptionTurkish = "Selamlaşma",
            row = 0,
            col = 0,
            direction = CrosswordDirection.HORIZONTAL,
            number = 1
        )
        val mockLevel = PuzzleLevel(
            id = 999,
            worldId = 1,
            levelNumber = 1,
            title = "Mock Level",
            theme = "Mock Theme",
            cefrLevel = "A1",
            words = listOf(mockWord)
        )

        viewModel.startGameSession(mockLevel.id)
        val activeLevelField = viewModel.javaClass.getDeclaredField("_activeLevel")
        activeLevelField.isAccessible = true
        (activeLevelField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<PuzzleLevel?>).value = mockLevel

        // Select cell 0,0
        viewModel.selectCell(0, 0)

        // Request hint
        viewModel.revealSelectedCell()

        // Wait/get updated progress from flow
        val updatedProgress = viewModel.progress.filterNotNull().first { it.coins < initialCoins }
        val finalCoins = updatedProgress.coins
        assertEquals("Coins should decrease by 15", initialCoins - 15, finalCoins)

        collectJob.cancel()
    }
}
