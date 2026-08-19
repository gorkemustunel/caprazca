package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserTaskEntity
import com.example.data.repository.GameRepository
import com.example.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TaskResetTest {

    @Test
    fun testWeeklyTaskResetOnMonday() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repository = GameRepository(app)
        repository.checkAndSeedDatabase()

        val db = AppDatabase.getDatabase(app)
        val taskDao = db.taskDao()

        // 1. Set a weekly task as completed on a Sunday
        val sunday = Calendar.getInstance()
        sunday.set(2026, Calendar.JULY, 19, 10, 0) // Sunday
        ShadowSystemClock.setNanoTime(sunday.timeInMillis * 1_000_000)
        
        val todayStr = repository.getTodayDateString()
        val weeklyTask = UserTaskEntity(
            id = "weekly_test",
            title = "Test Weekly",
            description = "Desc",
            type = "WEEKLY",
            targetValue = 10,
            currentValue = 10,
            isCompleted = true,
            lastUpdatedDate = todayStr
        )
        taskDao.insertTasks(listOf(weeklyTask))

        // Verify it is completed
        var tasks = taskDao.getAllTasks()
        assertEquals(10, tasks.find { it.id == "weekly_test" }?.currentValue)
        assertEquals(true, tasks.find { it.id == "weekly_test" }?.isCompleted)

        // 2. Move to Monday
        val monday = Calendar.getInstance()
        monday.set(2026, Calendar.JULY, 20) // Monday
        ShadowSystemClock.setNanoTime(monday.timeInMillis * 1_000_000)
        
        val newTodayStr = repository.getTodayDateString()
        
        // 3. Trigger reset logic (simulated by updateStreakAndHearts which calls resetTasksIfNeeded)
        repository.updateStreakAndHearts()

        // 4. Verify weekly task is reset
        tasks = taskDao.getAllTasks()
        val resetTask = tasks.find { it.id == "weekly_test" }!!
        assertEquals(0, resetTask.currentValue)
        assertFalse(resetTask.isCompleted)
        assertEquals(newTodayStr, resetTask.lastUpdatedDate)
    }
}
