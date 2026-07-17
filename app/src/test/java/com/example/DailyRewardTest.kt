package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DailyRewardResult
import com.example.data.repository.GameRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyRewardTest {

    @Test
    fun testDailyRewardClaimAndDoubleClaimProtection() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repository = GameRepository(app)
        repository.checkAndSeedDatabase()

        // Claim daily reward first time
        val firstClaim = repository.claimDailyLoginReward()
        assertTrue("First claim should be successful", firstClaim is DailyRewardResult.Claimed)
        val claimedResult = firstClaim as DailyRewardResult.Claimed
        assertEquals("First claim should be day 1", 1, claimedResult.day)

        // Try double claim
        val secondClaim = repository.claimDailyLoginReward()
        assertTrue("Second claim on same day should be rejected", secondClaim is DailyRewardResult.AlreadyClaimedToday)
    }
}
