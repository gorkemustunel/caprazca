package com.example.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        sounds["correct_letter"] = soundPool.load(context, R.raw.correct_letter, 1)
        sounds["wrong_letter"] = soundPool.load(context, R.raw.wrong_letter, 1)
        sounds["word_complete"] = soundPool.load(context, R.raw.word_complete, 1)
        sounds["level_complete"] = soundPool.load(context, R.raw.level_complete, 1)
        sounds["star_earned"] = soundPool.load(context, R.raw.star_earned, 1)
        sounds["coin_earned"] = soundPool.load(context, R.raw.coin_earned, 1)
    }

    fun playSound(soundKey: String) {
        val soundId = sounds[soundKey]
        if (soundId != null && soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
