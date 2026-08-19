package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.WordEntity
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReviewSystemTest {

    @Test
    fun testWordReviewLogic() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repository = GameRepository(app)
        repository.checkAndSeedDatabase()

        val db = AppDatabase.getDatabase(app)
        val wordDao = db.wordDao()

        // 1. Mark a word as learned (reviewLevel = 1)
        val wordId = "hello"
        repository.markLearned(wordId)
        
        var word = wordDao.getWordById(wordId)!!
        assertEquals(1, word.reviewLevel)
        assertEquals(true, word.isLearned)

        // 2. Correct review (reviewLevel: 1 -> 2)
        repository.updateWordReview(wordId, isCorrect = true)
        word = wordDao.getWordById(wordId)!!
        assertEquals(2, word.reviewLevel)

        // 3. Incorrect review (reviewLevel: 2 -> 1 reset)
        repository.updateWordReview(wordId, isCorrect = false)
        word = wordDao.getWordById(wordId)!!
        assertEquals(1, word.reviewLevel)
        
        // 4. Multiple correct reviews (reviewLevel: 1 -> 5 max)
        repository.updateWordReview(wordId, isCorrect = true)
        repository.updateWordReview(wordId, isCorrect = true)
        repository.updateWordReview(wordId, isCorrect = true)
        repository.updateWordReview(wordId, isCorrect = true)
        repository.updateWordReview(wordId, isCorrect = true)
        
        word = wordDao.getWordById(wordId)!!
        assertEquals(5, word.reviewLevel)
    }
}
