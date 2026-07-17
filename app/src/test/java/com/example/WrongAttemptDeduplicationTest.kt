package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CrosswordDirection
import com.example.data.model.PuzzleLevel
import com.example.data.model.CrosswordWord
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WrongAttemptDeduplicationTest {

    @Test
    fun testWrongAttemptDeduplication() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GameViewModel(app)

        // Mock an active level with 1 word
        val mockWord = CrosswordWord(
            wordId = "test_word",
            answerEnglish = "CAT",
            clueTurkish = "Kedi",
            clueDescriptionTurkish = "Evcil kedi",
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
        // Overwrite activeLevel manually for testing
        val activeLevelField = viewModel.javaClass.getDeclaredField("_activeLevel")
        activeLevelField.isAccessible = true
        (activeLevelField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<PuzzleLevel?>).value = mockLevel

        // First select the word and cell
        viewModel.selectCell(0, 0)

        // Input incorrect letter 'D'
        viewModel.inputLetter('D')
        viewModel.selectCell(0, 1)
        viewModel.inputLetter('O')
        viewModel.selectCell(0, 2)
        viewModel.inputLetter('G') // Word filled with 'DOG', which is wrong!

        // Error count should be 1
        assertEquals(1, viewModel.errorsCount.value)

        // Enter 'DOG' again (re-input 'G' at cell 0,2)
        viewModel.selectCell(0, 2)
        viewModel.inputLetter('G')

        // Error count should STILL be 1 (deduplicated!)
        assertEquals(1, viewModel.errorsCount.value)

        // Enter a different wrong word 'COT'
        viewModel.selectCell(0, 1)
        viewModel.inputLetter('O')
        viewModel.selectCell(0, 2)
        viewModel.inputLetter('T') // Word filled with 'COT', wrong!

        // Error count should increment to 2
        assertEquals(2, viewModel.errorsCount.value)
    }
}
