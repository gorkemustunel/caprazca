package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameSessionTest {

    @Test
    fun testGameSessionLifecycle() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GameViewModel(app)

        // Start level 1 session
        viewModel.startGameSession(1)
        assertNotNull(viewModel.activeLevel.value)
        assertEquals(1, viewModel.selectedLevelId.value)
        assertEquals(0, viewModel.errorsCount.value)

        // Quit session
        viewModel.quitGameSession()
        assertNull(viewModel.activeLevel.value)
        assertNull(viewModel.selectedLevelId.value)
    }
}
