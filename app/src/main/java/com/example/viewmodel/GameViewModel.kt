package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.GameRepository
import com.example.data.repository.LevelsData
import com.example.data.repository.PuzzleResult
import com.example.data.repository.RewardItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LevelStartReason {
    MAP,
    NEXT_LEVEL,
    REPLAY
}

sealed interface LevelStartResult {
    data object Started : LevelStartResult
    data object Locked : LevelStartResult
    data object NotEnoughLives : LevelStartResult
    data object ChestNode : LevelStartResult
    data object AlreadyStarting : LevelStartResult
    data object InvalidLevel : LevelStartResult
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    // Data Flows from Database
    val progress: StateFlow<UserProgressEntity?> = repository.progressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val levelStates: StateFlow<List<LevelStateEntity>> = repository.levelStatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dictionary: StateFlow<List<WordEntity>> = repository.dictionaryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<UserTaskEntity>> = repository.tasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStars: StateFlow<Int> = repository.totalStarsFlow
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedCount: StateFlow<Int> = repository.completedCountFlow
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val learnedCount: StateFlow<Int> = repository.learnedCountFlow
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI States (Closed write access from UI)
    private val _selectedLevelId = MutableStateFlow<Int?>(null)
    val selectedLevelId: StateFlow<Int?> = _selectedLevelId.asStateFlow()

    private val _activeLevel = MutableStateFlow<PuzzleLevel?>(null)
    val activeLevel: StateFlow<PuzzleLevel?> = _activeLevel.asStateFlow()
    
    // Crossword Active Game State
    private val _selectedCell = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedCell: StateFlow<Pair<Int, Int>?> = _selectedCell.asStateFlow()

    private val _selectedWordId = MutableStateFlow<String?>(null)
    val selectedWordId: StateFlow<String?> = _selectedWordId.asStateFlow()

    private val _enteredLetters = MutableStateFlow<Map<Pair<Int, Int>, Char>>(emptyMap())
    val enteredLetters: StateFlow<Map<Pair<Int, Int>, Char>> = _enteredLetters.asStateFlow()

    private val _correctWords = MutableStateFlow<Set<String>>(emptySet())
    val correctWords: StateFlow<Set<String>> = _correctWords.asStateFlow()

    private val _errorsCount = MutableStateFlow(0)
    val errorsCount: StateFlow<Int> = _errorsCount.asStateFlow()

    private val _hintsUsed = MutableStateFlow(0)
    val hintsUsed: StateFlow<Int> = _hintsUsed.asStateFlow()

    private val _timeElapsed = MutableStateFlow(0)
    val timeElapsed: StateFlow<Int> = _timeElapsed.asStateFlow()

    private val _isGameFinished = MutableStateFlow(false)
    val isGameFinished: StateFlow<Boolean> = _isGameFinished.asStateFlow()

    private val _gameResult = MutableStateFlow<PuzzleResult?>(null)
    val gameResult: StateFlow<PuzzleResult?> = _gameResult.asStateFlow()

    // Dictionary UI States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Tümü") // "Tümü", "Favoriler", "Öğrendiklerim", "A1", "A2", "B1"
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Start Level Result Flow
    private val _startLevelResult = MutableStateFlow<LevelStartResult?>(null)
    val startLevelResult: StateFlow<LevelStartResult?> = _startLevelResult.asStateFlow()

    // Double-Click / Hint Safety and tracking lists
    private val _isHintProcessing = MutableStateFlow(false)
    val isHintProcessing: StateFlow<Boolean> = _isHintProcessing.asStateFlow()

    private val registeredSolvedWordIds = mutableSetOf<String>()
    private val lastWrongAttemptByWordId = mutableMapOf<String, String>()

    // Timer Job
    private var timerJob: Job? = null
    private var _isStartingLevel = false

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    // Setters/Updaters for UI state flows
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun clearStartLevelResult() {
        _startLevelResult.value = null
    }

    // Central Level Startup Flow
    fun requestStartLevel(levelId: Int, startReason: LevelStartReason) {
        if (_isStartingLevel) {
            _startLevelResult.value = LevelStartResult.AlreadyStarting
            return
        }
        _isStartingLevel = true

        viewModelScope.launch {
            try {
                val levels = LevelsData.getLevels()
                val level = levels.find { it.id == levelId }
                if (level == null) {
                    _startLevelResult.value = LevelStartResult.InvalidLevel
                    return@launch
                }

                if (level.isChest) {
                    _startLevelResult.value = LevelStartResult.ChestNode
                    return@launch
                }

                val currentProgress = progress.value
                if (currentProgress == null) {
                    _startLevelResult.value = LevelStartResult.InvalidLevel
                    return@launch
                }

                if (levelId > currentProgress.highestUnlockedLevelId) {
                    _startLevelResult.value = LevelStartResult.Locked
                    return@launch
                }

                if (currentProgress.hearts <= 0) {
                    _startLevelResult.value = LevelStartResult.NotEnoughLives
                    return@launch
                }

                // Verify with repository transaction
                val repositorySuccess = repository.startPlayLevel(levelId)
                if (!repositorySuccess) {
                    val updatedProgress = repository.progressFlow.firstOrNull() ?: currentProgress
                    if (updatedProgress.hearts <= 0) {
                        _startLevelResult.value = LevelStartResult.NotEnoughLives
                    } else {
                        _startLevelResult.value = LevelStartResult.Locked
                    }
                    return@launch
                }

                // Initialize Game Session states
                startGameSession(levelId)

                _startLevelResult.value = LevelStartResult.Started
            } finally {
                _isStartingLevel = false
            }
        }
    }

    // Session Management
    fun startGameSession(levelId: Int) {
        stopTimer()

        val level = LevelsData.getLevels().find { it.id == levelId } ?: return
        _selectedLevelId.value = levelId
        _activeLevel.value = level

        // Reset game state
        _enteredLetters.value = emptyMap()
        _correctWords.value = emptySet()
        _errorsCount.value = 0
        _hintsUsed.value = 0
        _timeElapsed.value = 0
        _isGameFinished.value = false
        _gameResult.value = null
        registeredSolvedWordIds.clear()
        lastWrongAttemptByWordId.clear()

        // Set initial selected word and cell
        if (level.words.isNotEmpty()) {
            val firstWord = level.words.first()
            _selectedWordId.value = firstWord.wordId
            _selectedCell.value = Pair(firstWord.row, firstWord.col)
        } else {
            _selectedWordId.value = null
            _selectedCell.value = null
        }

        startTimer()
    }

    fun restartGameSession() {
        val currentLevelId = _selectedLevelId.value ?: return
        requestStartLevel(currentLevelId, LevelStartReason.REPLAY)
    }

    fun quitGameSession() {
        stopTimer()

        // Clear active session states
        _selectedLevelId.value = null
        _activeLevel.value = null
        _enteredLetters.value = emptyMap()
        _correctWords.value = emptySet()
        _errorsCount.value = 0
        _hintsUsed.value = 0
        _timeElapsed.value = 0
        _isGameFinished.value = false
        _gameResult.value = null
        registeredSolvedWordIds.clear()
        lastWrongAttemptByWordId.clear()

        _startLevelResult.value = null
    }

    fun finishGameSession() {
        viewModelScope.launch {
            val level = _activeLevel.value ?: return@launch
            if (_isGameFinished.value) return@launch

            _isGameFinished.value = true
            stopTimer()

            // Calculate Stars
            val finalErrors = _errorsCount.value
            val finalHints = _hintsUsed.value
            val stars = when {
                finalErrors == 0 && finalHints == 0 -> 3 // Perfect
                finalErrors <= 2 && finalHints <= 1 -> 2
                else -> 1
            }

            // Save completion to database
            val result = repository.completeLevel(
                levelId = level.id,
                timeTakenSeconds = _timeElapsed.value,
                starsEarned = stars,
                hintsUsed = finalHints,
                errorsCount = finalErrors,
                isBoss = level.isBoss
            )
            _gameResult.value = result
        }
    }

    // Timer functions
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_isGameFinished.value) {
                    _timeElapsed.value += 1
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    // Cell checks
    fun isCellLocked(row: Int, col: Int): Boolean {
        val words = getWordsAtCell(row, col)
        return words.any { _correctWords.value.contains(it.wordId) }
    }

    fun getWordsAtCell(row: Int, col: Int): List<CrosswordWord> {
        val level = _activeLevel.value ?: return emptyList()
        return level.words.filter { word ->
            if (word.direction == CrosswordDirection.HORIZONTAL) {
                row == word.row && col in word.col until (word.col + word.answerEnglish.length)
            } else {
                col == word.col && row in word.row until (word.row + word.answerEnglish.length)
            }
        }
    }

    fun validateWordsAffectedByCell(row: Int, col: Int, solveMethod: WordSolveMethod = WordSolveMethod.PLAYER) {
        val words = getWordsAtCell(row, col)
        for (word in words) {
            checkWordCompletion(word, solveMethod)
        }
    }

    fun isEntireGridCorrect(): Boolean {
        val level = _activeLevel.value ?: return false
        if (level.words.isEmpty()) return false
        for (word in level.words) {
            for (i in 0 until word.answerEnglish.length) {
                val r = if (word.direction == CrosswordDirection.VERTICAL) word.row + i else word.row
                val c = if (word.direction == CrosswordDirection.HORIZONTAL) word.col + i else word.col
                val enteredChar = _enteredLetters.value[Pair(r, c)]
                if (enteredChar == null || !enteredChar.equals(word.answerEnglish[i], ignoreCase = true)) {
                    return false
                }
            }
        }
        return true
    }

    // Spend coins to unlock a letter hint
    fun revealSelectedCell() {
        if (_isHintProcessing.value) return
        val cell = _selectedCell.value ?: return
        val wordId = _selectedWordId.value ?: return
        val level = _activeLevel.value ?: return
        val crosswordWord = level.words.find { it.wordId == wordId } ?: return

        // Calculate correct char index
        val offset = if (crosswordWord.direction == CrosswordDirection.HORIZONTAL) {
            cell.second - crosswordWord.col
        } else {
            cell.first - crosswordWord.row
        }
        if (offset < 0 || offset >= crosswordWord.answerEnglish.length) return
        val correctChar = crosswordWord.answerEnglish[offset]

        // Check if already correct or cell is locked
        val currentEnteredChar = _enteredLetters.value[cell]
        if (currentEnteredChar != null && currentEnteredChar.uppercaseChar() == correctChar.uppercaseChar()) {
            return
        }
        if (isCellLocked(cell.first, cell.second)) return

        // Spend coin validation
        val progressVal = progress.value ?: return
        if (progressVal.coins < 15) return

        _isHintProcessing.value = true
        viewModelScope.launch {
            try {
                val success = repository.spendCoins(15) // Letter hint costs 15 coins
                if (success) {
                    _hintsUsed.value += 1
                    val updatedLetters = _enteredLetters.value.toMutableMap()
                    updatedLetters[cell] = correctChar
                    _enteredLetters.value = updatedLetters

                    // Recheck all words using this intersection cell
                    validateWordsAffectedByCell(cell.first, cell.second, WordSolveMethod.LETTER_HINT)
                }
            } finally {
                _isHintProcessing.value = false
            }
        }
    }

    // Spend coins to reveal entire active word
    fun revealActiveWord() {
        if (_isHintProcessing.value) return
        val wordId = _selectedWordId.value ?: return
        val level = _activeLevel.value ?: return
        val crosswordWord = level.words.find { it.wordId == wordId } ?: return

        // If already correct, skip and refund
        if (_correctWords.value.contains(wordId)) {
            return
        }

        val progressVal = progress.value ?: return
        if (progressVal.coins < 50) return

        _isHintProcessing.value = true
        viewModelScope.launch {
            try {
                val success = repository.spendCoins(50) // Full word reveal costs 50 coins
                if (success) {
                    _hintsUsed.value += 1
                    val updatedLetters = _enteredLetters.value.toMutableMap()
                    for (i in 0 until crosswordWord.answerEnglish.length) {
                        val row = if (crosswordWord.direction == CrosswordDirection.VERTICAL) crosswordWord.row + i else crosswordWord.row
                        val col = if (crosswordWord.direction == CrosswordDirection.HORIZONTAL) crosswordWord.col + i else crosswordWord.col
                        updatedLetters[Pair(row, col)] = crosswordWord.answerEnglish[i]
                    }
                    _enteredLetters.value = updatedLetters

                    lastWrongAttemptByWordId.remove(wordId)

                    // Register as correct in Room
                    if (!registeredSolvedWordIds.contains(wordId)) {
                        registeredSolvedWordIds.add(wordId)
                        val updatedCorrect = _correctWords.value.toMutableSet()
                        updatedCorrect.add(wordId)
                        _correctWords.value = updatedCorrect

                        repository.registerWordSolved(wordId, true, WordSolveMethod.WORD_HINT)
                        checkPuzzleCompletionState()
                    }

                    // Validate all other intersecting words affected by these letter additions
                    for (i in 0 until crosswordWord.answerEnglish.length) {
                        val row = if (crosswordWord.direction == CrosswordDirection.VERTICAL) crosswordWord.row + i else crosswordWord.row
                        val col = if (crosswordWord.direction == CrosswordDirection.HORIZONTAL) crosswordWord.col + i else crosswordWord.col
                        val intersectingWords = getWordsAtCell(row, col).filter { it.wordId != wordId }
                        for (iw in intersectingWords) {
                            checkWordCompletion(iw, WordSolveMethod.WORD_HINT)
                        }
                    }
                }
            } finally {
                _isHintProcessing.value = false
            }
        }
    }

    private fun findNextEditableCell(currentCell: Pair<Int, Int>, crosswordWord: CrosswordWord): Pair<Int, Int>? {
        val direction = crosswordWord.direction
        val length = crosswordWord.answerEnglish.length
        val currentIndex = if (direction == CrosswordDirection.HORIZONTAL) {
            currentCell.second - crosswordWord.col
        } else {
            currentCell.first - crosswordWord.row
        }
        for (i in (currentIndex + 1) until length) {
            val nextRow = if (direction == CrosswordDirection.VERTICAL) crosswordWord.row + i else crosswordWord.row
            val nextCol = if (direction == CrosswordDirection.HORIZONTAL) crosswordWord.col + i else crosswordWord.col
            val candidate = Pair(nextRow, nextCol)
            if (!isCellLocked(candidate.first, candidate.second)) {
                return candidate
            }
        }
        return null
    }

    // Custom English keyboard input
    fun inputLetter(char: Char) {
        if (_isGameFinished.value) return
        val cell = _selectedCell.value ?: return
        val wordId = _selectedWordId.value ?: return
        val level = _activeLevel.value ?: return
        val crosswordWord = level.words.find { it.wordId == wordId } ?: return

        // Locked cells (solved cells) cannot be modified or deleted
        if (isCellLocked(cell.first, cell.second)) return

        // Place letter in enteredLetters map
        val updatedLetters = _enteredLetters.value.toMutableMap()
        updatedLetters[cell] = char.uppercaseChar()
        _enteredLetters.value = updatedLetters

        // Move to next cell in word direction, skipping locked cells
        val nextCell = findNextEditableCell(cell, crosswordWord)
        if (nextCell != null) {
            _selectedCell.value = nextCell
        }

        // Validate all intersecting words affected by this letter input
        validateWordsAffectedByCell(cell.first, cell.second, WordSolveMethod.PLAYER)
    }

    // Delete character at selected cell and step backward
    fun deleteLetter() {
        if (_isGameFinished.value) return
        val cell = _selectedCell.value ?: return
        val wordId = _selectedWordId.value ?: return
        val level = _activeLevel.value ?: return
        val crosswordWord = level.words.find { it.wordId == wordId } ?: return

        // Locked cells (solved cells) cannot be deleted
        if (isCellLocked(cell.first, cell.second)) return

        val updatedLetters = _enteredLetters.value.toMutableMap()
        updatedLetters.remove(cell)
        _enteredLetters.value = updatedLetters

        // Move cursor backward
        val prevCell = if (crosswordWord.direction == CrosswordDirection.HORIZONTAL) {
            Pair(cell.first, cell.second - 1)
        } else {
            Pair(cell.first - 1, cell.second)
        }

        if (prevCell.first >= crosswordWord.row && prevCell.second >= crosswordWord.col) {
            _selectedCell.value = prevCell
        }
    }

    // Select different crossword cell
    fun selectCell(row: Int, col: Int) {
        val level = _activeLevel.value ?: return
        val wordsContainingCell = level.words.filter { word ->
            if (word.direction == CrosswordDirection.HORIZONTAL) {
                row == word.row && col in word.col until (word.col + word.answerEnglish.length)
            } else {
                col == word.col && row in word.row until (word.row + word.answerEnglish.length)
            }
        }

        _selectedCell.value = Pair(row, col)

        if (wordsContainingCell.isNotEmpty()) {
            val currentWordId = _selectedWordId.value
            val matchesCurrent = wordsContainingCell.any { it.wordId == currentWordId }
            if (!matchesCurrent) {
                _selectedWordId.value = wordsContainingCell.first().wordId
            }
        }
    }

    // Select a word from the clue list
    fun selectWord(wordId: String) {
        val level = _activeLevel.value ?: return
        val crosswordWord = level.words.find { it.wordId == wordId } ?: return
        _selectedWordId.value = wordId
        _selectedCell.value = Pair(crosswordWord.row, crosswordWord.col)
    }

    // Toggle selected word's direction if cell is at intersection
    fun toggleWordDirection() {
        val cell = _selectedCell.value ?: return
        val level = _activeLevel.value ?: return
        val row = cell.first
        val col = cell.second

        val wordsContainingCell = level.words.filter { word ->
            if (word.direction == CrosswordDirection.HORIZONTAL) {
                row == word.row && col in word.col until (word.col + word.answerEnglish.length)
            } else {
                col == word.col && row in word.row until (word.row + word.answerEnglish.length)
            }
        }

        if (wordsContainingCell.size > 1) {
            val currentWordId = _selectedWordId.value
            val nextWord = wordsContainingCell.find { it.wordId != currentWordId }
            if (nextWord != null) {
                _selectedWordId.value = nextWord.wordId
            }
        }
    }

    // Check if active word is completely filled and validate correctness
    private fun checkWordCompletion(crosswordWord: CrosswordWord, solveMethod: WordSolveMethod) {
        val enteredWordBuilder = StringBuilder()
        var isFullyFilled = true

        for (i in 0 until crosswordWord.answerEnglish.length) {
            val r = if (crosswordWord.direction == CrosswordDirection.VERTICAL) crosswordWord.row + i else crosswordWord.row
            val c = if (crosswordWord.direction == CrosswordDirection.HORIZONTAL) crosswordWord.col + i else crosswordWord.col
            val enteredChar = _enteredLetters.value[Pair(r, c)]
            if (enteredChar == null) {
                isFullyFilled = false
                break
            }
            enteredWordBuilder.append(enteredChar)
        }

        if (isFullyFilled) {
            val enteredString = enteredWordBuilder.toString()
            if (enteredString.equals(crosswordWord.answerEnglish, ignoreCase = true)) {
                // Correctly solved word
                lastWrongAttemptByWordId.remove(crosswordWord.wordId)
                if (!registeredSolvedWordIds.contains(crosswordWord.wordId)) {
                    registeredSolvedWordIds.add(crosswordWord.wordId)
                    val updatedCorrect = _correctWords.value.toMutableSet()
                    updatedCorrect.add(crosswordWord.wordId)
                    _correctWords.value = updatedCorrect

                    viewModelScope.launch {
                        repository.registerWordSolved(crosswordWord.wordId, true, solveMethod)
                        checkPuzzleCompletionState()
                    }
                }
            } else {
                // Incorrect word, only log error if the incorrect attempt changed
                val previousWrong = lastWrongAttemptByWordId[crosswordWord.wordId]
                if (previousWrong != enteredString) {
                    lastWrongAttemptByWordId[crosswordWord.wordId] = enteredString
                    _errorsCount.value += 1
                    viewModelScope.launch {
                        repository.registerWordSolved(crosswordWord.wordId, false, solveMethod)
                    }
                }
            }
        }
    }

    // Check if entire puzzle level is solved correctly
    private suspend fun checkPuzzleCompletionState() {
        val level = _activeLevel.value ?: return
        if (level.words.isEmpty() && level.isChest) return

        // Verify correctWords contains all wordIds AND grid characters match answers
        val allWordsSolved = level.words.all { _correctWords.value.contains(it.wordId) }
        val isGridCorrect = isEntireGridCorrect()

        if (allWordsSolved && isGridCorrect && !_isGameFinished.value) {
            finishGameSession()
        }
    }

    // Spend heart on starting level (legacy UI hook for backwards compatibility if needed, but redirects to useHeart)
    fun startPlayLevel(levelId: Int, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            val progressVal = progress.value
            if (progressVal == null) {
                onFailure()
                return@launch
            }

            val level = LevelsData.getLevels().find { it.id == levelId }
            if (level != null && level.isChest) {
                onSuccess()
                return@launch
            }

            val success = repository.useHeart()
            if (success) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }

    // Claims chest reward
    fun claimChestReward(levelId: Int, onResult: (ChestClaimResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.claimChest(levelId)
            onResult(result)
        }
    }

    // Restores heart
    fun buyHeart() {
        viewModelScope.launch {
            repository.buyHeart()
        }
    }

    // Claims daily task reward
    fun claimTaskReward(taskId: String) {
        viewModelScope.launch {
            repository.claimTaskReward(taskId)
        }
    }

    // Claim 7-day reward
    fun claimDailyReward(day: Int) {
        viewModelScope.launch {
            repository.claimDailyLoginReward()
        }
    }

    // Complete dictionary practice session and award rewards
    fun completePracticeSession(wordsSolved: Int, correctAnswers: Int) {
        viewModelScope.launch {
            repository.completePracticeSession(wordsSolved, correctAnswers)
        }
    }

    // Favorites
    fun toggleFavorite(wordId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(wordId)
        }
    }

    fun markWordLearned(wordId: String) {
        viewModelScope.launch {
            repository.markLearned(wordId)
        }
    }

    // Profile updates
    fun updateProfile(username: String, avatar: String) {
        viewModelScope.launch {
            repository.updateProfile(username, avatar)
        }
    }

    // Notification Clear
    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    // Reset whole game
    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            _selectedLevelId.value = null
            _activeLevel.value = null
            _isGameFinished.value = false
            _gameResult.value = null
            registeredSolvedWordIds.clear()
            lastWrongAttemptByWordId.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
