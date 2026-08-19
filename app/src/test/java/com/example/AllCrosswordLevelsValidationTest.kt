package com.example

import com.example.data.model.CrosswordDirection
import com.example.data.model.CrosswordWord
import com.example.data.model.WordEntity
import com.example.data.repository.CrosswordGenerationResult
import com.example.data.repository.CrosswordLayoutEngine
import com.example.data.repository.DictionaryData
import com.example.data.repository.LevelsData
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AllCrosswordLevelsValidationTest {

    @Test
    fun testAllPlayableLevelsGenerateCrosswordLayouts() {
        val levels = LevelsData.getLevels()
        val dictionary = DictionaryData.getPreloadedWords()
            .associateBy { it.id.lowercase(Locale.ENGLISH) }
        val errors = mutableListOf<String>()

        levels.filterNot { it.isChest }.forEach { level ->
            val result = CrosswordLayoutEngine.generateLayoutResult(
                levelId = level.id,
                wordIds = level.wordIds,
                dictionary = dictionary
            )

            when (result) {
                is CrosswordGenerationResult.Success -> {
                    if (result.words.isEmpty()) {
                        errors.add("Level ${level.id}: generated empty layout")
                    }
                    val maxDimension = if (level.id <= 20) 10 else if (level.id <= 60) 11 else 12
                    if (result.rows > maxDimension || result.columns > maxDimension) {
                        errors.add("Level ${level.id}: layout too large ${result.rows}x${result.columns}")
                    }
                    result.words.forEach { word ->
                        val dictionaryWord = dictionary[word.wordId]
                        if (dictionaryWord != null && clueRevealsAnswer(word, dictionaryWord)) {
                            errors.add("Level ${level.id}: clue reveals '${word.wordId}' translation '${dictionaryWord.turkish}'")
                        }
                    }
                    if (!isConnected(result.words)) {
                        errors.add("Level ${level.id}: generated layout is disconnected")
                    }
                }
                is CrosswordGenerationResult.Failure -> {
                    errors.add("Level ${level.id}: ${result.reason}; unplaced=${result.unplacedWordIds}")
                }
            }
        }

        assertTrue(
            "Crossword generation errors: $errors",
            errors.isEmpty()
        )
    }

    @Test
    fun testPlayableLevelWordsAreGloballyUnique() {
        val wordOwners = mutableMapOf<String, Int>()
        val duplicates = mutableListOf<String>()

        LevelsData.getLevels().filterNot { it.isChest }.forEach { level ->
            level.wordIds.forEach { wordId ->
                val previousOwner = wordOwners.putIfAbsent(wordId, level.id)
                if (previousOwner != null) {
                    duplicates.add("$wordId in levels $previousOwner and ${level.id}")
                }
            }
        }

        assertTrue(
            "Duplicate level words found: $duplicates",
            duplicates.isEmpty()
        )
    }

    private fun clueRevealsAnswer(word: CrosswordWord, dictionaryWord: WordEntity): Boolean {
        val clue = word.clueTurkish.normalizeForClueCheck()
        return dictionaryWord.turkish
            .split("/", ",", ";")
            .map { it.normalizeForClueCheck() }
            .filter { it.length >= 3 }
            .any { clue.contains(it) }
    }

    private fun isConnected(words: List<CrosswordWord>): Boolean {
        if (words.size <= 1) return true
        return words.all { word ->
            words.any { other -> other != word && cells(word).any { it in cells(other) } }
        }
    }

    private fun cells(word: CrosswordWord): Set<Pair<Int, Int>> {
        return word.answerEnglish.indices.map { index ->
            if (word.direction == CrosswordDirection.HORIZONTAL) {
                word.row to word.col + index
            } else {
                word.row + index to word.col
            }
        }.toSet()
    }

    private fun String.normalizeForClueCheck(): String {
        return lowercase(Locale("tr", "TR"))
            .replace(Regex("[^a-zçğıöşü0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
