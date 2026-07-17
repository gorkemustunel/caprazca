package com.example

import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.data.db.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RoomMigrationTest {

    @Test
    fun testMigration1To2() {
        val factory = FrameworkSQLiteOpenHelperFactory()
        val config = SupportSQLiteOpenHelper.Configuration.builder(RuntimeEnvironment.getApplication())
            .name("test_db")
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Create v1 user_progress table
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `user_progress` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `username` TEXT NOT NULL,
                            `avatar` TEXT NOT NULL,
                            `level` INTEGER NOT NULL,
                            `xp` INTEGER NOT NULL,
                            `coins` INTEGER NOT NULL,
                            `hearts` INTEGER NOT NULL,
                            `streak` INTEGER NOT NULL,
                            `lastClaimedLoginDay` INTEGER NOT NULL
                        )
                    """.trimIndent())
                    
                    // Insert a v1 row
                    db.execSQL("""
                        INSERT INTO `user_progress` (username, avatar, level, xp, coins, hearts, streak, lastClaimedLoginDay)
                        VALUES ('Alp', 'avatar_1', 2, 120, 100, 5, 3, 2)
                    """.trimIndent())
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = factory.create(config)
        val db = helper.writableDatabase

        // Apply MIGRATION_1_2
        AppDatabase.MIGRATION_1_2.migrate(db)

        // Read and verify columns in v2
        val cursor = db.query("SELECT * FROM user_progress")
        assert(cursor.moveToFirst())

        val highestUnlockedLevelIdCol = cursor.getColumnIndex("highestUnlockedLevelId")
        val lastDailyRewardClaimDateCol = cursor.getColumnIndex("lastDailyRewardClaimDate")
        val dailyRewardCycleDayCol = cursor.getColumnIndex("dailyRewardCycleDay")

        assert(highestUnlockedLevelIdCol != -1)
        assert(lastDailyRewardClaimDateCol != -1)
        assert(dailyRewardCycleDayCol != -1)

        assertEquals(1, cursor.getInt(highestUnlockedLevelIdCol))
        assertEquals("", cursor.getString(lastDailyRewardClaimDateCol))
        assertEquals(0, cursor.getInt(dailyRewardCycleDayCol))

        cursor.close()
        db.close()
    }
}
