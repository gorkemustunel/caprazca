package com.example.data.repository

import com.example.data.model.CrosswordDirection
import com.example.data.model.CrosswordWord
import com.example.data.model.PuzzleLevel
import com.example.data.model.WordEntity
import java.util.Locale

sealed interface CrosswordGenerationResult {
    data class Success(
        val words: List<CrosswordWord>,
        val rows: Int,
        val columns: Int
    ) : CrosswordGenerationResult

    data class Failure(
        val levelId: Int,
        val reason: String,
        val unplacedWordIds: List<String>
    ) : CrosswordGenerationResult
}

object CrosswordLayoutEngine {

    fun generateLayout(
        levelId: Int,
        wordIds: List<String>,
        dictionary: Map<String, WordEntity>
    ): List<CrosswordWord> {
        val result = generateLayoutResult(levelId, wordIds, dictionary)
        if (result is CrosswordGenerationResult.Success) {
            return result.words
        } else if (result is CrosswordGenerationResult.Failure) {
            throw IllegalStateException("Failed to generate a valid connected crossword layout for level $levelId: ${result.reason}")
        }
        throw IllegalStateException("Unknown crossword generation state for level $levelId")
    }

    fun generateLayoutResult(
        levelId: Int,
        wordIds: List<String>,
        dictionary: Map<String, WordEntity>
    ): CrosswordGenerationResult {
        if (wordIds.isEmpty()) {
            return CrosswordGenerationResult.Failure(levelId, "No words to place", emptyList())
        }

        try {
            val wordsToPlace = wordIds.map { id ->
                val dictWord = dictionary[id.lowercase(Locale.ENGLISH)]
                    ?: return CrosswordGenerationResult.Failure(levelId, "Word '$id' not found in dictionary", listOf(id))
                dictWord
            }

            val sortedDictWords = wordsToPlace.sortedWith(
                compareByDescending<WordEntity> { it.english.length }
                    .thenBy { it.id }
            )

            val firstWord = sortedDictWords.first()
            val remainingWords = sortedDictWords.drop(1)

            val firstAnswer = firstWord.english.uppercase(Locale.ENGLISH)
            val firstPlaced = PlacedWord(
                wordId = firstWord.id,
                answerEnglish = firstAnswer,
                clueTurkish = firstWord.turkish.uppercase(Locale.ENGLISH),
                clueDescriptionTurkish = firstWord.turkishDefinition,
                row = 0,
                col = 0,
                direction = CrosswordDirection.HORIZONTAL
            )

            val initialPlacedList = listOf(firstPlaced)

            val result = backtrack(remainingWords, initialPlacedList)
            if (result == null) {
                return CrosswordGenerationResult.Failure(
                    levelId,
                    "No valid crossword layout could be generated",
                    remainingWords.map { it.id }
                )
            }

            val minRow = result.minOf { it.row }
            val minCol = result.minOf { it.col }

            val finalWords = result.map { pw ->
                pw.copy(
                    row = pw.row - minRow,
                    col = pw.col - minCol
                )
            }.sortedWith(
                compareBy<PlacedWord> { it.row }
                    .thenBy { it.col }
            ).mapIndexed { index, pw ->
                CrosswordWord(
                    wordId = pw.wordId,
                    answerEnglish = pw.answerEnglish,
                    clueTurkish = pw.clueTurkish,
                    clueDescriptionTurkish = pw.clueDescriptionTurkish,
                    row = pw.row,
                    col = pw.col,
                    direction = pw.direction,
                    number = index + 1
                )
            }

            val maxRow = finalWords.maxOf { pw ->
                if (pw.direction == CrosswordDirection.VERTICAL) pw.row + pw.answerEnglish.length else pw.row + 1
            }
            val maxCol = finalWords.maxOf { pw ->
                if (pw.direction == CrosswordDirection.HORIZONTAL) pw.col + pw.answerEnglish.length else pw.col + 1
            }

            return CrosswordGenerationResult.Success(finalWords, maxRow, maxCol)
        } catch (e: Exception) {
            return CrosswordGenerationResult.Failure(levelId, e.message ?: "Unknown error", wordIds)
        }
    }

    private data class PlacedWord(
        val wordId: String,
        val answerEnglish: String,
        val clueTurkish: String,
        val clueDescriptionTurkish: String,
        val row: Int,
        val col: Int,
        val direction: CrosswordDirection
    )

    private fun backtrack(
        remaining: List<WordEntity>,
        placed: List<PlacedWord>
    ): List<PlacedWord>? {
        if (remaining.isEmpty()) {
            return if (isValidConnectedLayout(placed)) placed else null
        }

        // Try to place any of the remaining words at any possible intersection
        for (i in remaining.indices) {
            val candidateWord = remaining[i]
            val candidateAnswer = candidateWord.english.uppercase(Locale.ENGLISH)
            val newRemaining = remaining.filterIndexed { index, _ -> index != i }

            for (placedWord in placed) {
                for (charIndexInCandidate in candidateAnswer.indices) {
                    val candidateChar = candidateAnswer[charIndexInCandidate]
                    for (charIndexInPlaced in placedWord.answerEnglish.indices) {
                        val placedChar = placedWord.answerEnglish[charIndexInPlaced]

                        if (candidateChar == placedChar) {
                            val nextDir = if (placedWord.direction == CrosswordDirection.HORIZONTAL) {
                                CrosswordDirection.VERTICAL
                            } else {
                                CrosswordDirection.HORIZONTAL
                            }

                            val placedCharRow = if (placedWord.direction == CrosswordDirection.HORIZONTAL) {
                                placedWord.row
                            } else {
                                placedWord.row + charIndexInPlaced
                            }
                            val placedCharCol = if (placedWord.direction == CrosswordDirection.HORIZONTAL) {
                                placedWord.col + charIndexInPlaced
                            } else {
                                placedWord.col
                            }

                            val nextStartRow = if (nextDir == CrosswordDirection.HORIZONTAL) {
                                placedCharRow
                            } else {
                                placedCharRow - charIndexInCandidate
                            }
                            val nextStartCol = if (nextDir == CrosswordDirection.HORIZONTAL) {
                                placedCharCol - charIndexInCandidate
                            } else {
                                placedCharCol
                            }

                            val candidatePlaced = PlacedWord(
                                wordId = candidateWord.id,
                                answerEnglish = candidateAnswer,
                                clueTurkish = candidateWord.turkish.uppercase(Locale.ENGLISH),
                                clueDescriptionTurkish = candidateWord.turkishDefinition,
                                row = nextStartRow,
                                col = nextStartCol,
                                direction = nextDir
                            )

                            if (canPlace(candidatePlaced, placed)) {
                                val nextPlacedList = placed + candidatePlaced
                                val finalLayout = backtrack(newRemaining, nextPlacedList)
                                if (finalLayout != null) {
                                    return finalLayout
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun canPlace(candidate: PlacedWord, placed: List<PlacedWord>): Boolean {
        // Build maps of occupied cells
        val gridMap = mutableMapOf<Pair<Int, Int>, Char>()
        val cellDirections = mutableMapOf<Pair<Int, Int>, MutableSet<CrosswordDirection>>()

        for (w in placed) {
            // Check same position and direction
            if (candidate.row == w.row && candidate.col == w.col && candidate.direction == w.direction) {
                return false
            }
            for (k in w.answerEnglish.indices) {
                val r = if (w.direction == CrosswordDirection.HORIZONTAL) w.row else w.row + k
                val c = if (w.direction == CrosswordDirection.HORIZONTAL) w.col + k else w.col
                gridMap[Pair(r, c)] = w.answerEnglish[k]
                cellDirections.getOrPut(Pair(r, c)) { mutableSetOf() }.add(w.direction)
            }
        }

        // Candidate cells
        val candidateCells = mutableListOf<Pair<Int, Int>>()
        for (i in candidate.answerEnglish.indices) {
            val r = if (candidate.direction == CrosswordDirection.HORIZONTAL) candidate.row else candidate.row + i
            val c = if (candidate.direction == CrosswordDirection.HORIZONTAL) candidate.col + i else candidate.col
            candidateCells.add(Pair(r, c))
        }

        // Rule 7: Kelimenin başlangıcından önceki ve bitişinden sonraki hücre boş olmalıdır.
        if (candidate.direction == CrosswordDirection.HORIZONTAL) {
            if (gridMap.containsKey(Pair(candidate.row, candidate.col - 1))) return false
            if (gridMap.containsKey(Pair(candidate.row, candidate.col + candidate.answerEnglish.length))) return false
        } else {
            if (gridMap.containsKey(Pair(candidate.row - 1, candidate.col))) return false
            if (gridMap.containsKey(Pair(candidate.row + candidate.answerEnglish.length, candidate.col))) return false
        }

        // Check each letter of candidate
        for (i in candidateCells.indices) {
            val (r, c) = candidateCells[i]
            val char = candidate.answerEnglish[i]

            val existingChar = gridMap[Pair(r, c)]
            if (existingChar != null) {
                // Rule 2: Dolu bir hücre yalnızca aynı harfi içeriyorsa kesişim olarak kullanılabilir.
                if (existingChar != char) return false

                // Rule 3 & 4: Aynı yönde iki kelime üst üste bindirilemez.
                val dirs = cellDirections[Pair(r, c)] ?: emptySet()
                if (dirs.contains(candidate.direction)) return false
            } else {
                // Not an intersection cell, apply neighbor rules
                // Perpendicular adjacent touches (without intersection) are forbidden.
                // Parallel side-by-side touches are allowed.
                if (candidate.direction == CrosswordDirection.HORIZONTAL) {
                    val topDirs = cellDirections[Pair(r - 1, c)] ?: emptySet()
                    if (topDirs.contains(CrosswordDirection.VERTICAL)) return false

                    val bottomDirs = cellDirections[Pair(r + 1, c)] ?: emptySet()
                    if (bottomDirs.contains(CrosswordDirection.VERTICAL)) return false
                } else {
                    val leftDirs = cellDirections[Pair(r, c - 1)] ?: emptySet()
                    if (leftDirs.contains(CrosswordDirection.HORIZONTAL)) return false

                    val rightDirs = cellDirections[Pair(r, c + 1)] ?: emptySet()
                    if (rightDirs.contains(CrosswordDirection.HORIZONTAL)) return false
                }
            }
        }

        return true
    }

    private fun getPlacedWordCells(pw: PlacedWord): Set<Pair<Int, Int>> {
        val cells = mutableSetOf<Pair<Int, Int>>()
        for (k in pw.answerEnglish.indices) {
            val r = if (pw.direction == CrosswordDirection.HORIZONTAL) pw.row else pw.row + k
            val c = if (pw.direction == CrosswordDirection.HORIZONTAL) pw.col + k else pw.col
            cells.add(Pair(r, c))
        }
        return cells
    }

    private fun sharesPlacedWordCell(w1: PlacedWord, w2: PlacedWord): Boolean {
        val cells1 = getPlacedWordCells(w1)
        val cells2 = getPlacedWordCells(w2)
        return cells1.any { cells2.contains(it) }
    }

    private fun isValidConnectedLayout(placed: List<PlacedWord>): Boolean {
        if (placed.isEmpty()) return true
        if (placed.size > 1) {
            for (word in placed) {
                val intersects = placed.any { other ->
                    other != word && sharesPlacedWordCell(word, other)
                }
                if (!intersects) return false
            }
        }
        return true
    }
}

object CrosswordLayoutValidator {
    fun validateLevels(
        levels: List<PuzzleLevel>,
        dictionary: Map<String, WordEntity>
    ): CrosswordValidationResult {
        val seenIds = mutableSetOf<Int>()
        var playableCount = 0
        var chestCount = 0
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        for (level in levels) {
            val levelId = level.id
            if (levelId in seenIds) {
                errors.add("Duplicate Level ID: $levelId")
                continue
            }
            seenIds.add(levelId)

            if (level.isChest) {
                chestCount++
                if (level.words.isNotEmpty()) {
                    errors.add("Level $levelId: Chest level must not contain words.")
                }
                continue
            }

            playableCount++
            if (level.words.isEmpty()) {
                errors.add("Level $levelId: Playable level must contain words.")
                continue
            }

            val seenWordIds = mutableSetOf<String>()
            val allCells = mutableMapOf<Pair<Int, Int>, Char>()
            val cellToWords = mutableMapOf<Pair<Int, Int>, MutableList<CrosswordWord>>()
            val wordCellMap = mutableMapOf<String, List<Pair<Int, Int>>>()

            for (word in level.words) {
                val wId = word.wordId
                if (wId in seenWordIds) {
                    errors.add("Level $levelId: Duplicate Word ID '$wId'")
                    continue
                }
                seenWordIds.add(wId)

                val dictWord = dictionary[wId.lowercase(Locale.ENGLISH)]
                if (dictWord == null) {
                    errors.add("Level $levelId: Word '$wId' not found in dictionary.")
                    continue
                }

                if (word.answerEnglish.isEmpty()) {
                    errors.add("Level $levelId: Word '$wId' answerEnglish is empty.")
                    continue
                }

                if (!word.answerEnglish.all { it in 'A'..'Z' }) {
                    errors.add("Level $levelId: Word '$wId' has non-A-Z character: ${word.answerEnglish}")
                    continue
                }

                if (word.row < 0 || word.col < 0) {
                    errors.add("Level $levelId: Word '$wId' has negative coordinate: (${word.row}, ${word.col})")
                    continue
                }

                // Add to cells and check letter clashes at intersections
                val cells = mutableListOf<Pair<Int, Int>>()
                for (k in word.answerEnglish.indices) {
                    val r = if (word.direction == CrosswordDirection.HORIZONTAL) word.row else word.row + k
                    val c = if (word.direction == CrosswordDirection.HORIZONTAL) word.col + k else word.col
                    cells.add(Pair(r, c))

                    val existingChar = allCells[Pair(r, c)]
                    if (existingChar != null && existingChar != word.answerEnglish[k]) {
                        errors.add("Level $levelId: '${word.wordId}' ve diğer kelime (${r}, ${c}) hücresinde farklı harf gerektiriyor: $existingChar / ${word.answerEnglish[k]}")
                    }
                    allCells[Pair(r, c)] = word.answerEnglish[k]
                    cellToWords.getOrPut(Pair(r, c)) { mutableListOf() }.add(word)
                }
                wordCellMap[wId] = cells
            }

            // Check connectivity
            if (level.words.size > 1) {
                for (word in level.words) {
                    val intersects = level.words.any { other ->
                        other != word && sharesCell(word, other)
                    }
                    if (!intersects) {
                        errors.add("Level $levelId: Disconnected word '${word.wordId}'")
                    }
                }
            }

            // Check forbidden adjacency, start/end letters, and parallel overlaps/corridors
            val addedErrorSet = mutableSetOf<String>()
            for (w1 in level.words) {
                val cells1 = wordCellMap[w1.wordId] ?: emptyList()
                
                // 1. Check start and end of w1 (Kelimenin başlangıcından hemen önce ve bitişinden hemen sonra harf bulunmamalı)
                if (w1.direction == CrosswordDirection.HORIZONTAL) {
                    val before = Pair(w1.row, w1.col - 1)
                    if (allCells.containsKey(before)) {
                        addedErrorSet.add("Level $levelId: '${w1.wordId}' kelimesinin hemen öncesinde harf var.")
                    }
                    val after = Pair(w1.row, w1.col + w1.answerEnglish.length)
                    if (allCells.containsKey(after)) {
                        addedErrorSet.add("Level $levelId: '${w1.wordId}' kelimesinin hemen sonrasında harf var.")
                    }
                } else {
                    val before = Pair(w1.row - 1, w1.col)
                    if (allCells.containsKey(before)) {
                        addedErrorSet.add("Level $levelId: '${w1.wordId}' kelimesinin hemen öncesinde harf var.")
                    }
                    val after = Pair(w1.row + w1.answerEnglish.length, w1.col)
                    if (allCells.containsKey(after)) {
                        addedErrorSet.add("Level $levelId: '${w1.wordId}' kelimesinin hemen sonrasında harf var.")
                    }
                }

                // 2. Check each cell of w1
                for (c1 in cells1) {
                    val isIntersection = (cellToWords[c1]?.size ?: 0) > 1
                    if (!isIntersection) {
                        // For non-intersection cells, check orthogonal neighbors in the perpendicular direction
                        if (w1.direction == CrosswordDirection.HORIZONTAL) {
                            val top = Pair(c1.first - 1, c1.second)
                            val topWords = cellToWords[top] ?: emptyList()
                            for (tw in topWords) {
                                if (tw.direction == CrosswordDirection.VERTICAL) {
                                    addedErrorSet.add("Level $levelId: Forbidden perpendicular adjacency between '${w1.wordId}' and '${tw.wordId}' at (${c1.first}, ${c1.second}) without intersection.")
                                }
                            }
                            val bottom = Pair(c1.first + 1, c1.second)
                            val bottomWords = cellToWords[bottom] ?: emptyList()
                            for (bw in bottomWords) {
                                if (bw.direction == CrosswordDirection.VERTICAL) {
                                    addedErrorSet.add("Level $levelId: Forbidden perpendicular adjacency between '${w1.wordId}' and '${bw.wordId}' at (${c1.first}, ${c1.second}) without intersection.")
                                }
                            }
                        } else {
                            val left = Pair(c1.first, c1.second - 1)
                            val leftWords = cellToWords[left] ?: emptyList()
                            for (lw in leftWords) {
                                if (lw.direction == CrosswordDirection.HORIZONTAL) {
                                    addedErrorSet.add("Level $levelId: Forbidden perpendicular adjacency between '${w1.wordId}' and '${lw.wordId}' at (${c1.first}, ${c1.second}) without intersection.")
                                }
                            }
                            val right = Pair(c1.first, c1.second + 1)
                            val rightWords = cellToWords[right] ?: emptyList()
                            for (rw in rightWords) {
                                if (rw.direction == CrosswordDirection.HORIZONTAL) {
                                    addedErrorSet.add("Level $levelId: Forbidden perpendicular adjacency between '${w1.wordId}' and '${rw.wordId}' at (${c1.first}, ${c1.second}) without intersection.")
                                }
                            }
                        }
                    }
                }
            }

            // Also check parallel overlap (same direction, overlapping cells)
            for (i in level.words.indices) {
                val w1 = level.words[i]
                val cells1 = wordCellMap[w1.wordId] ?: emptyList()
                for (j in i + 1 until level.words.size) {
                    val w2 = level.words[j]
                    val cells2 = wordCellMap[w2.wordId] ?: emptyList()
                    val overlapCells = cells1.filter { cells2.contains(it) }
                    if (overlapCells.isNotEmpty()) {
                        if (w1.direction == w2.direction) {
                            addedErrorSet.add("Level $levelId: Parallel overlap between '${w1.wordId}' and '${w2.wordId}'")
                        }
                    }
                }
            }

            errors.addAll(addedErrorSet)

            // Check bounds / grid calculation consistency
            val maxRow = level.words.maxOfOrNull { pw ->
                if (pw.direction == CrosswordDirection.VERTICAL) pw.row + pw.answerEnglish.length else pw.row + 1
            } ?: 0
            val maxCol = level.words.maxOfOrNull { pw ->
                if (pw.direction == CrosswordDirection.HORIZONTAL) pw.col + pw.answerEnglish.length else pw.col + 1
            } ?: 0
            if (maxRow == 0 || maxCol == 0) {
                errors.add("Level $levelId: Empty grid boundaries.")
            }
        }

        return CrosswordValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    private fun sharesCell(w1: CrosswordWord, w2: CrosswordWord): Boolean {
        val cells1 = getWordCells(w1)
        val cells2 = getWordCells(w2)
        return cells1.any { cells2.contains(it) }
    }

    private fun getWordCells(w: CrosswordWord): Set<Pair<Int, Int>> {
        val cells = mutableSetOf<Pair<Int, Int>>()
        for (i in w.answerEnglish.indices) {
            val r = if (w.direction == CrosswordDirection.HORIZONTAL) w.row else w.row + i
            val c = if (w.direction == CrosswordDirection.HORIZONTAL) w.col + i else w.col
            cells.add(Pair(r, c))
        }
        return cells
    }
}

data class CrosswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)
