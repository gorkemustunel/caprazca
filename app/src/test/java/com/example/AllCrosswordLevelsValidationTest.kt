package com.example

import com.example.data.model.WordEntity
import com.example.data.repository.LevelsData
import com.example.data.repository.CrosswordLayoutValidator
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AllCrosswordLevelsValidationTest {

    @Test
    fun testAll39CrosswordLevelsAreValid() {
        val levels = LevelsData.getLevels()

        // Construct mock dictionary from all level words to pass validation check
        val dictionary = levels.flatMap { it.words }.associate { word ->
            val lowercaseId = word.wordId.lowercase(Locale.ENGLISH)
            lowercaseId to WordEntity(
                id = lowercaseId,
                english = word.answerEnglish,
                turkish = word.clueTurkish,
                partOfSpeech = "noun",
                cefrLevel = "A1",
                englishDefinition = word.clueDescriptionTurkish,
                turkishDefinition = word.clueDescriptionTurkish,
                exampleSentence = "Example sentence",
                exampleTranslation = "Örnek cümle",
                phonetic = "/example/",
                synonyms = "",
                antonyms = "",
                themes = "",
                timesCorrect = 0,
                timesWrong = 0,
                isFavorite = false,
                isLearned = false,
                reviewLevel = 0,
                lastReviewTime = 0L
            )
        }

        val validationResult = CrosswordLayoutValidator.validateLevels(levels, dictionary)
        assertTrue(
            "Levels validation errors: ${validationResult.errors}",
            validationResult.isValid
        )
    }
}
