package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CrosswordWord
import com.example.data.model.PuzzleLevel
import com.example.data.model.CrosswordDirection
import com.example.data.repository.PuzzleResult
import com.example.viewmodel.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val progress by viewModel.progress.collectAsState()
    val activeLevel by viewModel.activeLevel.collectAsState()
    val selectedCell by viewModel.selectedCell.collectAsState()
    val selectedWordId by viewModel.selectedWordId.collectAsState()
    val enteredLetters by viewModel.enteredLetters.collectAsState()
    val correctWords by viewModel.correctWords.collectAsState()
    val errorsCount by viewModel.errorsCount.collectAsState()
    val hintsUsed by viewModel.hintsUsed.collectAsState()
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val isGameFinished by viewModel.isGameFinished.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    val startLevelResult by viewModel.startLevelResult.collectAsState()
    LaunchedEffect(startLevelResult) {
        if (startLevelResult == LevelStartResult.NotEnoughLives || startLevelResult == LevelStartResult.Locked) {
            viewModel.clearStartLevelResult()
            onNavigateBack()
        }
    }

    val level = activeLevel ?: return

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back button to show exit dialog
    BackHandler(enabled = !isGameFinished) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Oyundan Çıkmak İstiyor Musunuz?") },
            text = { Text("Mevcut bulmaca ilerlemeniz ve harcadığınız can geri alınamaz. Çıkmak istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Çık")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Calculate Grid boundaries
    val gridRows = remember(level) {
        if (level.words.isEmpty()) 5 else (level.words.maxOf { word ->
            if (word.direction == CrosswordDirection.VERTICAL) word.row + word.answerEnglish.length else word.row + 1
        }).coerceAtLeast(6)
    }
    val gridCols = remember(level) {
        if (level.words.isEmpty()) 5 else (level.words.maxOf { word ->
            if (word.direction == CrosswordDirection.HORIZONTAL) word.col + word.answerEnglish.length else word.col + 1
        }).coerceAtLeast(6)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bölüm ${level.levelNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = level.title,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isGameFinished) {
                            onNavigateBack()
                        } else {
                            showExitDialog = true
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    // Timer display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Timer",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTime(timeElapsed),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Gameplay Stats Bar (Errors, Coins, Help actions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Error count
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BugReport,
                            contentDescription = "Hatalar",
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hata: $errorsCount",
                            color = ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Balance display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = "Jeton",
                        tint = GoldenAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${progress?.coins ?: 0}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                // Quick Hint buy buttons
                Row {
                    // Reveal letter
                    TextButton(
                        onClick = {
                            val coins = progress?.coins ?: 0
                            if (coins < 15) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Yetersiz Jeton! Harf ipucu için 15 jeton gerekiyor.")
                                }
                            } else {
                                viewModel.revealSelectedCell()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = EmeraldGreen),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Harf Al (15 🪙)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Reveal full word
                    TextButton(
                        onClick = {
                            val coins = progress?.coins ?: 0
                            if (coins < 50) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Yetersiz Jeton! Kelime çözümü için 50 jeton gerekiyor.")
                                }
                            } else {
                                viewModel.revealActiveWord()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = GoldenAmber),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Kelime Çöz (50 🪙)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real Crossword Board view (pannable/scrollable viewport structure for small devices)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CrosswordBoard(
                        gridRows = gridRows,
                        gridCols = gridCols,
                        level = level,
                        enteredLetters = enteredLetters,
                        correctWords = correctWords,
                        selectedCell = selectedCell,
                        selectedWordId = selectedWordId,
                        onCellClick = { row, col -> viewModel.selectCell(row, col) }
                    )
                }
            }

            // Bottom Info panel & Keyboard
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(bottom = 12.dp)
            ) {
                // Active Clue Box
                ActiveClueDisplay(
                    level = level,
                    selectedWordId = selectedWordId,
                    correctWords = correctWords,
                    onWordToggle = { viewModel.toggleWordDirection() },
                    onPrevWord = { selectRelativeWord(level, selectedWordId, -1, viewModel) },
                    onNextWord = { selectRelativeWord(level, selectedWordId, 1, viewModel) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Built-in English keyboard
                EnglishGameKeyboard(
                    onKeyInput = { viewModel.inputLetter(it) },
                    onDelete = { viewModel.deleteLetter() },
                    onToggleDirection = { viewModel.toggleWordDirection() }
                )
            }
        }
    }

    // Celebration Result dialog
    if (isGameFinished) {
        gameResult?.let { result ->
            CelebrationResultDialog(
                level = level,
                result = result,
                timeElapsed = timeElapsed,
                errorsCount = errorsCount,
                hintsUsed = hintsUsed,
                onBackToMap = onNavigateBack,
                onReplay = { viewModel.requestStartLevel(level.id, LevelStartReason.REPLAY) },
                onNextLevel = {
                    val nextId = level.id + 1
                    if (nextId <= 180) { // Updated to support more levels
                        viewModel.requestStartLevel(nextId, LevelStartReason.NEXT_LEVEL)
                    } else {
                        onNavigateBack()
                    }
                }
            )
        }
    }
}

@Composable
fun CrosswordBoard(
    gridRows: Int,
    gridCols: Int,
    level: PuzzleLevel,
    enteredLetters: Map<Pair<Int, Int>, Char>,
    correctWords: Set<String>,
    selectedCell: Pair<Int, Int>?,
    selectedWordId: String?,
    onCellClick: (Int, Int) -> Unit
) {
    // Generate cell layout map
    val cellWordOwnerMap = remember(level) {
        val map = mutableMapOf<Pair<Int, Int>, MutableList<CrosswordWord>>()
        level.words.forEach { word ->
            for (i in 0 until word.answerEnglish.length) {
                val r = if (word.direction == CrosswordDirection.VERTICAL) word.row + i else word.row
                val c = if (word.direction == CrosswordDirection.HORIZONTAL) word.col + i else word.col
                if (!map.containsKey(Pair(r, c))) {
                    map[Pair(r, c)] = mutableListOf()
                }
                map[Pair(r, c)]?.add(word)
            }
        }
        map
    }

    val firstLetterNumbers = remember(level) {
        val map = mutableMapOf<Pair<Int, Int>, Int>()
        level.words.forEach { word ->
            map[Pair(word.row, word.col)] = word.number
        }
        map
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (r in 0 until gridRows) {
            Row {
                for (c in 0 until gridCols) {
                    val coord = Pair(r, c)
                    val containingWords = cellWordOwnerMap[coord]

                    if (containingWords == null) {
                        // Empty block (black cell) - Now clickable to select nearby words
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .padding(1.dp)
                                .background(BlackBlock, RoundedCornerShape(4.dp))
                                .clickable {
                                    onCellClick(r, c)
                                }
                        )
                    } else {
                        // Character input cell
                        val typedChar = enteredLetters[coord] ?: ' '
                        val isCurrentCell = selectedCell == coord
                        val belongsToActiveWord = containingWords.any { it.wordId == selectedWordId }
                        val isInsideCorrectWord = containingWords.any { correctWords.contains(it.wordId) }

                        val cellBgColor = when {
                            isCurrentCell -> GridSelectColor
                            belongsToActiveWord -> GridActiveWordColor
                            isInsideCorrectWord -> EmeraldGreen.copy(alpha = 0.2f)
                            else -> WordCellBg
                        }

                        val cellBorderColor = when {
                            isCurrentCell -> EmeraldGreen
                            belongsToActiveWord -> GoldenAmber
                            else -> Color.LightGray
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .padding(1.dp)
                                .background(cellBgColor, RoundedCornerShape(4.dp))
                                .border(
                                    width = if (isCurrentCell) 2.5.dp else 1.dp,
                                    color = cellBorderColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { onCellClick(r, c) },
                            contentAlignment = Alignment.Center
                        ) {
                            // Render tiny starting index numbers
                            firstLetterNumbers[coord]?.let { num ->
                                Text(
                                    text = "$num",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.DarkGray,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(top = 1.dp, start = 2.dp)
                                )
                            }

                            // Render character
                            Text(
                                text = if (typedChar != ' ') "$typedChar" else "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isInsideCorrectWord) DeepEmerald else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveClueDisplay(
    level: PuzzleLevel,
    selectedWordId: String?,
    correctWords: Set<String>,
    onWordToggle: () -> Unit,
    onPrevWord: () -> Unit,
    onNextWord: () -> Unit
) {
    val activeWord = level.words.find { it.wordId == selectedWordId } ?: return
    val isSolved = correctWords.contains(activeWord.wordId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevWord) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Önceki")
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val dirText = if (activeWord.direction == CrosswordDirection.HORIZONTAL) "Yatay" else "Dikey"
                    val tagBg = if (activeWord.direction == CrosswordDirection.HORIZONTAL) SkyBlue.copy(alpha = 0.1f) else goldenAmberAlpha()
                    val tagColor = if (activeWord.direction == CrosswordDirection.HORIZONTAL) SkyBlue else GoldenAmber

                    // Number & direction tag
                    Box(
                        modifier = Modifier
                            .background(tagBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${activeWord.number}. $dirText",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "(${activeWord.answerEnglish.length} Harfli)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (isSolved) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Çözüldü",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Clue head english word
                Text(
                    text = "İpucu: \"${activeWord.clueTurkish.uppercase()}\"",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                // Clue definition
                Text(
                    text = activeWord.clueDescriptionTurkish,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            Row {
                IconButton(onClick = onWordToggle) {
                    Icon(Icons.Filled.Cached, contentDescription = "Yön Değiştir", tint = SkyBlue)
                }
                IconButton(onClick = onNextWord) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Sonraki")
                }
            }
        }
    }
}

@Composable
fun EnglishGameKeyboard(
    onKeyInput: (Char) -> Unit,
    onDelete: () -> Unit,
    onToggleDirection: () -> Unit
) {
    val row1 = listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P')
    val row2 = listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L')
    val row3 = listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            row1.forEach { char -> KeyboardButton(char.toString()) { onKeyInput(char) } }
        }
        // Row 2
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            row2.forEach { char -> KeyboardButton(char.toString()) { onKeyInput(char) } }
        }
        // Row 3 (Contains special actions and remaining characters)
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            // YÖN action
            KeyboardButton(
                text = "YÖN",
                weight = 1.3f,
                containerColor = SkyBlue.copy(alpha = 0.15f),
                textColor = SkyBlue,
                onClick = onToggleDirection
            )

            row3.forEach { char -> KeyboardButton(char.toString()) { onKeyInput(char) } }

            // SIL action
            KeyboardButton(
                text = "SIL",
                weight = 1.3f,
                containerColor = ErrorRed.copy(alpha = 0.15f),
                textColor = ErrorRed,
                onClick = onDelete
            )
        }
    }
}

@Composable
fun RowScope.KeyboardButton(
    text: String,
    weight: Float = 1.0f,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(38.dp)
            .background(containerColor, RoundedCornerShape(6.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CelebrationResultDialog(
    level: PuzzleLevel,
    result: PuzzleResult,
    timeElapsed: Int,
    errorsCount: Int,
    hintsUsed: Int,
    onBackToMap: () -> Unit,
    onReplay: () -> Unit,
    onNextLevel: () -> Unit
) {
    Dialog(
        onDismissRequest = onBackToMap,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, GoldenAmber)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Crown
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = GoldenAmber,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "BÖLÜM TAMAMLANDI!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldenAmber,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = level.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stars Animation Block
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(3) { starIdx ->
                        val isFilled = starIdx < result.starsEarned
                        Icon(
                            imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (isFilled) GoldenAmber else Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(if (starIdx == 1) 48.dp else 36.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Rewards Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RewardBadge(name = "Kazanılan XP", value = "+${result.xpEarned} XP", icon = Icons.Filled.AddReaction, color = SkyBlue)
                        RewardBadge(name = "Kazanılan Jeton", value = "+${result.coinsEarned} 🪙", icon = Icons.Filled.MonetizationOn, color = GoldenAmber)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Level stats breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Süre:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = formatTime(timeElapsed), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Hata Sayısı:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = "$errorsCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (errorsCount > 0) ErrorRed else EmeraldGreen)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Kullanılan İpucu:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = "$hintsUsed", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                if (result.didLevelUp) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, EmeraldGreen)
                    ) {
                        Text(
                            text = "🎉 SEVİYE ATLANDI! 🎉",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SONRAKİ BÖLÜM", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onReplay,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Yeniden Oyna", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onBackToMap,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Haritaya Dön", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun RewardBadge(name: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// Intersections selector helper
private fun selectRelativeWord(level: PuzzleLevel, currentWordId: String?, offset: Int, viewModel: GameViewModel) {
    val words = level.words
    if (words.isEmpty()) return
    val currentIndex = words.indexOfFirst { it.wordId == currentWordId }
    val nextIndex = if (currentIndex == -1) {
        0
    } else {
        (currentIndex + offset + words.size) % words.size
    }
    viewModel.selectWord(words[nextIndex].wordId)
}

@Composable
fun goldenAmberAlpha() = GoldenAmber.copy(alpha = 0.1f)
