package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CrosswordWord
import com.example.data.model.PuzzleLevel
import com.example.data.model.CrosswordDirection
import com.example.data.repository.EconomyConfig
import com.example.data.repository.PuzzleResult
import com.example.viewmodel.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import com.example.data.model.*

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
    val completedCount by viewModel.completedCount.collectAsState()

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

    var pendingActionAfterEvolution by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showEvolutionByDinoDialog by remember { mutableStateOf(false) }

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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
            CompactGameStatsBar(
                errorsCount = errorsCount,
                coins = progress?.coins ?: 0,
                hintsUsed = hintsUsed
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val fitWidth = ((maxWidth - 24.dp) / gridCols.coerceAtLeast(1)).coerceIn(26.dp, 40.dp)
                    val fitHeight = ((maxHeight - 24.dp) / gridRows.coerceAtLeast(1)).coerceIn(26.dp, 40.dp)
                    val adaptiveCellSize = if (fitWidth < fitHeight) fitWidth else fitHeight

                    Box(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        CrosswordBoard(
                            gridRows = gridRows,
                            gridCols = gridCols,
                            level = level,
                            enteredLetters = enteredLetters,
                            correctWords = correctWords,
                            errorsCount = errorsCount,
                            selectedCell = selectedCell,
                            selectedWordId = selectedWordId,
                            cellSize = adaptiveCellSize,
                            onCellClick = { row, col -> viewModel.selectCell(row, col) }
                        )
                    }
                }

                DinoReactionController(
                    reactionEvent = viewModel.reactionEvent,
                    stage = DinoStage.safeValueOf(progress?.dinoStage ?: "EGG"),
                    completedLevels = completedCount,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 4.dp, bottom = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 10.dp)
            ) {
                ActiveClueDisplay(
                    level = level,
                    selectedWordId = selectedWordId,
                    correctWords = correctWords,
                    onWordToggle = { viewModel.toggleWordDirection() },
                    onPrevWord = { selectRelativeWord(level, selectedWordId, -1, viewModel) },
                    onNextWord = { selectRelativeWord(level, selectedWordId, 1, viewModel) },
                    onRevealLetter = {
                        val coins = progress?.coins ?: 0
                        if (coins < EconomyConfig.LETTER_HINT_COST) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Yetersiz Jeton! Harf ipucu için ${EconomyConfig.LETTER_HINT_COST} jeton gerekiyor.")
                            }
                        } else {
                            viewModel.revealSelectedCell()
                        }
                    },
                    onRevealWord = {
                        val coins = progress?.coins ?: 0
                        if (coins < EconomyConfig.WORD_HINT_COST) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Yetersiz Jeton! Kelime çözümü için ${EconomyConfig.WORD_HINT_COST} jeton gerekiyor.")
                            }
                        } else {
                            viewModel.revealActiveWord()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                EnglishGameKeyboard(
                    onKeyInput = { viewModel.inputLetter(it) },
                    onDelete = { viewModel.deleteLetter() },
                    onToggleDirection = { viewModel.toggleWordDirection() }
                )
            }
        }
    }
}

    // Celebration Result dialog
    if (isGameFinished) {
        gameResult?.let { result ->
            val onActionWithEvolutionCheck = { action: () -> Unit ->
                if (progress?.lastEvolutionSeen?.startsWith("PENDING_") == true) {
                    pendingActionAfterEvolution = action
                    showEvolutionByDinoDialog = true
                } else {
                    action()
                }
            }

            DinoLevelCompletionDialog(
                level = level,
                result = result,
                timeElapsed = timeElapsed,
                errorsCount = errorsCount,
                hintsUsed = hintsUsed,
                currentXp = progress?.xp ?: 0,
                stage = DinoStage.safeValueOf(progress?.dinoStage ?: "EGG"),
                completedLevels = completedCount,
                onBackToMap = { onActionWithEvolutionCheck { onNavigateBack() } },
                onReplay = { viewModel.requestStartLevel(level.id, LevelStartReason.REPLAY) },
                onNextLevel = {
                    onActionWithEvolutionCheck {
                        val nextId = level.id + 1
                        if (nextId <= 100) {
                            viewModel.requestStartLevel(nextId, LevelStartReason.NEXT_LEVEL)
                        } else {
                            onNavigateBack()
                        }
                    }
                }
            )
        }
    }

    if (showEvolutionByDinoDialog) {
        val pendingEvolution = progress?.lastEvolutionSeen
        val completePendingAction = {
            val action = pendingActionAfterEvolution
            pendingActionAfterEvolution = null
            showEvolutionByDinoDialog = false
            action?.invoke()
        }

        if (pendingEvolution == "PENDING_HATCHING") {
            DinoHatchingDialog(onNicknameChosen = { nickname ->
                viewModel.finishHatchingDialog(nickname) {
                    completePendingAction()
                }
            })
        } else if (pendingEvolution?.startsWith("PENDING_") == true) {
            val milestone = DinoEvolutionPlan.milestones.firstOrNull { it.pendingKey == pendingEvolution }
            if (milestone?.stage != null) {
                DinoEvolutionDialog(
                    newStage = milestone.stage,
                    completedLevels = completedCount,
                    onDismiss = {
                        viewModel.markEvolutionAsSeen {
                            completePendingAction()
                        }
                    }
                )
            } else if (milestone != null) {
                DinoMilestoneDialog(
                    milestone = milestone,
                    stage = DinoStage.safeValueOf(progress?.dinoStage ?: "EGG"),
                    completedLevels = completedCount,
                    onDismiss = {
                        viewModel.markEvolutionAsSeen {
                            completePendingAction()
                        }
                    }
                )
            } else {
                viewModel.markEvolutionAsSeen { completePendingAction() }
            }
        } else {
            completePendingAction()
        }
    }
}

@Composable
fun CompactGameStatsBar(
    errorsCount: Int,
    coins: Int,
    hintsUsed: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactStatChip(
            icon = Icons.Filled.BugReport,
            text = "Hata $errorsCount",
            color = ErrorRed,
            modifier = Modifier.weight(1f)
        )
        CompactStatChip(
            icon = Icons.Filled.MonetizationOn,
            text = coins.toString(),
            color = GoldenAmber,
            modifier = Modifier.weight(1f)
        )
        CompactStatChip(
            icon = Icons.Filled.Lightbulb,
            text = "İpucu $hintsUsed",
            color = EmeraldGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CompactStatChip(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(30.dp),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
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
    errorsCount: Int,
    selectedCell: Pair<Int, Int>?,
    selectedWordId: String?,
    cellSize: Dp,
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
    var flashError by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(errorsCount) {
        if (errorsCount > 0) {
            flashError = true
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(-7f, tween(45))
            shakeOffset.animateTo(7f, tween(70))
            shakeOffset.animateTo(-4f, tween(55))
            shakeOffset.animateTo(0f, tween(60))
            kotlinx.coroutines.delay(180)
            flashError = false
        }
    }

    val boardBorderColor by animateColorAsState(
        targetValue = if (flashError) ErrorRed.copy(alpha = 0.85f) else Color.Transparent,
        animationSpec = tween(durationMillis = 160),
        label = "boardErrorFlash"
    )

    Column(
        modifier = Modifier
            .offset(x = shakeOffset.value.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DinoMint.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                    )
                ),
                RoundedCornerShape(14.dp)
            )
            .border(1.5.dp, boardBorderColor, RoundedCornerShape(14.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (r in 0 until gridRows) {
            Row {
                for (c in 0 until gridCols) {
                    val coord = Pair(r, c)
                    val containingWords = cellWordOwnerMap[coord]

                    if (containingWords == null) {
                        // Empty space: keep grid rhythm without drawing heavy black blocks.
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .padding(1.dp)
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
                        val animatedCellBg by animateColorAsState(
                            targetValue = cellBgColor,
                            animationSpec = tween(durationMillis = 220),
                            label = "crosswordCellBg"
                        )
                        val cellScale by animateFloatAsState(
                            targetValue = when {
                                isCurrentCell -> 1.08f
                                isInsideCorrectWord -> 1.02f
                                else -> 1f
                            },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "crosswordCellScale"
                        )
                        val cellElevation = when {
                            isCurrentCell -> 4.dp
                            isInsideCorrectWord -> 3.dp
                            else -> 0.dp
                        }

                        val cellBorderColor = when {
                            isCurrentCell -> EmeraldGreen
                            belongsToActiveWord -> GoldenAmber
                            isInsideCorrectWord -> EmeraldGreen.copy(alpha = 0.55f)
                            else -> Color.LightGray
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .padding(1.dp)
                                .graphicsLayer {
                                    scaleX = cellScale
                                    scaleY = cellScale
                                }
                                .shadow(cellElevation, RoundedCornerShape(5.dp), clip = false)
                                .background(animatedCellBg, RoundedCornerShape(4.dp))
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
                            AnimatedContent(
                                targetState = typedChar,
                                transitionSpec = {
                                    fadeIn(tween(90)) + scaleIn(initialScale = 0.7f) togetherWith fadeOut(tween(60))
                                },
                                label = "typedLetter"
                            ) { char ->
                                Text(
                                    text = if (char != ' ') "$char" else "",
                                    fontWeight = FontWeight.Black,
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
}

@Composable
fun ActiveClueDisplay(
    level: PuzzleLevel,
    selectedWordId: String?,
    correctWords: Set<String>,
    onWordToggle: () -> Unit,
    onPrevWord: () -> Unit,
    onNextWord: () -> Unit,
    onRevealLetter: () -> Unit,
    onRevealWord: () -> Unit
) {
    val activeWord = level.words.find { it.wordId == selectedWordId } ?: return
    val isSolved = correctWords.contains(activeWord.wordId)
    val dirText = if (activeWord.direction == CrosswordDirection.HORIZONTAL) "Yatay" else "Dikey"
    val tagColor = if (activeWord.direction == CrosswordDirection.HORIZONTAL) SkyBlue else GoldenAmber
    val clueBorder by animateColorAsState(
        targetValue = if (isSolved) EmeraldGreen.copy(alpha = 0.7f) else tagColor.copy(alpha = 0.35f),
        animationSpec = tween(220),
        label = "clueBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSolved) EmeraldGreen.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, clueBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevWord, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Önceki", modifier = Modifier.size(20.dp))
                }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(tagColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${activeWord.number}. $dirText",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${activeWord.answerEnglish.length} harf",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (isSolved) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Çözüldü",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                IconButton(onClick = onWordToggle, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.Cached, contentDescription = "Yön Değiştir", tint = SkyBlue, modifier = Modifier.size(19.dp))
                }
                IconButton(onClick = onNextWord, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Sonraki", modifier = Modifier.size(20.dp))
                }
            }

            Text(
                text = if (isSolved) "Tamamlandı" else activeWord.clueTurkish.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            if (activeWord.clueDescriptionTurkish.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activeWord.clueDescriptionTurkish,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = !isSolved) {
                Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRevealLetter,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(9.dp),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.45f)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(15.dp), tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Harf Al · ${EconomyConfig.LETTER_HINT_COST}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                    OutlinedButton(
                        onClick = onRevealWord,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(9.dp),
                        border = BorderStroke(1.dp, GoldenAmber.copy(alpha = 0.55f)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(15.dp), tint = GoldenAmber)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kelime Çöz · ${EconomyConfig.WORD_HINT_COST}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldenAmber)
                    }
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
            // Direction action
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "keyPress"
    )
    Box(
        modifier = Modifier
            .weight(weight)
            .height(38.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(if (isPressed) 0.dp else 2.dp, RoundedCornerShape(7.dp))
            .background(containerColor, RoundedCornerShape(7.dp))
            .border(0.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f), RoundedCornerShape(7.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
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
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, GoldenAmber)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Crown
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = GoldenAmber,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "BÖLÜM TAMAMLANDI!",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldenAmber,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = level.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stars Block
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
                                .size(if (starIdx == 1) 42.dp else 32.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rewards Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RewardBadge(name = "Kazanılan XP", value = "+${result.xpEarned} XP", icon = Icons.Filled.AddReaction, color = SkyBlue)
                        RewardBadge(name = "Kazanılan Jeton", value = "+${result.coinsEarned}", icon = Icons.Filled.MonetizationOn, color = GoldenAmber)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, EmeraldGreen)
                    ) {
                        Text(
                            text = "SEVİYE ATLANDI!",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        Text("Yeniden", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
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
                        Text("Harita", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
