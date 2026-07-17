package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.GameRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LevelProgressionTest {

    @Test
    fun testLevelProgressionLockingAndHearts() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repository = GameRepository(app)
        repository.checkAndSeedDatabase()

        // Level 2 should be locked initially (highestUnlockedLevelId is 1)
        val startLevel2Success = repository.startPlayLevel(2)
        assertFalse("Locked level 2 should fail to start", startLevel2Success)

        // Level 1 should be playable and start successfully (consumes a heart)
        val startLevel1Success = repository.startPlayLevel(1)
        assertTrue("Unlocked level 1 should start successfully", startLevel1Success)
    }
}
