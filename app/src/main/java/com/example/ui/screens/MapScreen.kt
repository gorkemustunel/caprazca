package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.data.model.*
import com.example.data.repository.EconomyConfig
import com.example.data.repository.LevelsData
import com.example.ui.theme.*
import com.example.viewmodel.*
import kotlinx.coroutines.launch
import kotlin.math.sin

private val LevelNodeGreen = Color(0xFF1FB894)
val TextDark = MapInk

data class WorldBanner(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

private fun worldBannerForLevel(levelId: Int): WorldBanner? = when (levelId) {
    1 -> WorldBanner("Dünya 1 · Yeşil Vadi", "İlk adımlar, ilk kelimeler", Icons.Filled.Park)
    21 -> WorldBanner("Dünya 2 · Kristal Mağara", "Derinliklerde yeni kelimeler", Icons.Filled.Diamond)
    41 -> WorldBanner("Dünya 3 · Lav Dağı", "Tempo yükseliyor", Icons.Filled.LocalFireDepartment)
    61 -> WorldBanner("Dünya 4 · Gökyüzü Adaları", "Ufkunu genişlet", Icons.Filled.Cloud)
    81 -> WorldBanner("Dünya 5 · Kayıp Şehir", "Bilgelik yolculuğu", Icons.Filled.AccountBalance)
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: GameViewModel,
    onNavigateToGame: (Int) -> Unit
) {
    val progress by viewModel.progress.collectAsState()
    val levelStates by viewModel.levelStates.collectAsState()
    val totalStars by viewModel.totalStars.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()

    val levels = remember { LevelsData.getLevels() }
    val listState = rememberLazyListState()

    var selectedLevelForDialog by remember { mutableStateOf<PuzzleLevel?>(null) }
    var showNoHeartsDialog by remember { mutableStateOf(false) }
    var claimedChestRewards by remember { mutableStateOf<ChestClaimResult?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val startLevelResult by viewModel.startLevelResult.collectAsState()

    LaunchedEffect(startLevelResult) {
        if (startLevelResult == LevelStartResult.Started) {
            selectedLevelForDialog = null
            viewModel.clearStartLevelResult()
            onNavigateToGame(0)
        } else if (startLevelResult == LevelStartResult.NotEnoughLives) {
            showNoHeartsDialog = true
            viewModel.clearStartLevelResult()
        }
    }

    LaunchedEffect(progress) {
        val currentLevelId = progress?.highestUnlockedLevelId ?: 1
        val index = levels.indexOfFirst { it.id == currentLevelId }
        if (index >= 0) {
            listState.animateScrollToItem((index - 1).coerceAtLeast(0))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopPlayerBar(progress, totalStars, completedCount, onBuyHeartClick = { viewModel.buyHeart() })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MapBgTop, MapBgMid, MapBgBottom)
                    )
                )
        ) {
            MapAtmosphere(listState)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_lazy_column"),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(levels.size) { index ->
                    val level = levels[index]
                    val levelState = levelStates.find { it.levelId == level.id }
                    val currentLevelId = progress?.highestUnlockedLevelId ?: 1

                    val isUnlocked = level.id <= currentLevelId
                    val isCurrent = level.id == currentLevelId
                    val isCompleted = levelState?.completed == true
                    val stars = levelState?.stars ?: 0

                    worldBannerForLevel(level.id)?.let { banner ->
                        WorldHeaderCard(banner)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrent) {
                            val curveFactor = sin(level.id.toFloat() * 0.7f) * 56f
                            DinoMascot(
                                stage = DinoStage.safeValueOf(progress?.dinoStage ?: "EGG"),
                                completedLevels = completedCount,
                                isFacingLeft = curveFactor < 0,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(
                                        x = (if (curveFactor > 0) curveFactor - 88 else curveFactor + 88).dp,
                                        y = 8.dp
                                    )
                                    .zIndex(2f)
                            )
                        }

                        LevelNode(
                            level = level,
                            isUnlocked = isUnlocked,
                            isCurrent = isCurrent,
                            isCompleted = isCompleted,
                            stars = stars,
                            onClick = {
                                if (isUnlocked) {
                                    if (level.isChest) {
                                        viewModel.claimChestReward(level.id) { claimedChestRewards = it }
                                    } else {
                                        selectedLevelForDialog = level
                                    }
                                }
                            }
                        )
                    }

                    if (index < levels.size - 1) {
                        LevelTrail(
                            levelId = level.id,
                            isPassed = level.id < currentLevelId
                        )
                    }
                }
            }
        }
    }

    selectedLevelForDialog?.let { level ->
        LevelDetailsDialog(
            level = level,
            bestTime = levelStates.find { it.levelId == level.id }?.bestTime,
            onDismiss = { selectedLevelForDialog = null },
            onPlayClick = { viewModel.requestStartLevel(level.id, LevelStartReason.MAP) },
            stage = DinoStage.safeValueOf(progress?.dinoStage ?: "EGG"),
            completedLevels = completedCount
        )
    }

    if (showNoHeartsDialog) {
        NoHeartsDialog(
            onDismiss = { showNoHeartsDialog = false },
            onBuyHeart = {
                viewModel.buyHeart { success ->
                    scope.launch {
                        if (success) {
                            showNoHeartsDialog = false
                            snackbarHostState.showSnackbar("Can alındı! ${EconomyConfig.HEART_COST} jeton harcandı.")
                        } else {
                            snackbarHostState.showSnackbar("Can alınamadı. Jetonun yetersiz olabilir veya canların dolu.")
                        }
                    }
                }
            },
            onFreeHeart = {
                viewModel.claimFreeDailyHeart { success ->
                    scope.launch {
                        if (success) {
                            showNoHeartsDialog = false
                            snackbarHostState.showSnackbar("Günlük bonus can alındı.")
                        } else {
                            snackbarHostState.showSnackbar("Bonus can bugün alınmış olabilir veya canların dolu.")
                        }
                    }
                }
            }
        )
    }

    claimedChestRewards?.let { result ->
        ChestRewardsDialog(result, onDismiss = { claimedChestRewards = null })
    }

    progress?.let { prog ->
        if (prog.lastEvolutionSeen == "PENDING_HATCHING") {
            DinoHatchingDialog(onNicknameChosen = { nickname ->
                viewModel.renameDino(nickname)
                viewModel.markEvolutionAsSeen()
            })
        } else if (prog.lastEvolutionSeen.startsWith("PENDING_")) {
            val milestone = DinoEvolutionPlan.milestones.firstOrNull { it.pendingKey == prog.lastEvolutionSeen }
            if (milestone?.stage != null) {
                DinoEvolutionDialog(
                    newStage = milestone.stage,
                    completedLevels = completedCount,
                    onDismiss = { viewModel.markEvolutionAsSeen() }
                )
            } else if (milestone != null) {
                DinoMilestoneDialog(
                    milestone = milestone,
                    stage = DinoStage.safeValueOf(prog.dinoStage),
                    completedLevels = completedCount,
                    onDismiss = { viewModel.markEvolutionAsSeen() }
                )
            } else {
                viewModel.markEvolutionAsSeen()
            }
        }
    }
}

@Composable
fun MapAtmosphere(listState: LazyListState) {
    val scrollOffset = listState.firstVisibleItemIndex * 200 + listState.firstVisibleItemScrollOffset

    Canvas(modifier = Modifier.fillMaxSize()) {
        val farHill = Path().apply {
            moveTo(0f, size.height * 0.22f)
            cubicTo(
                size.width * 0.28f, size.height * 0.12f,
                size.width * 0.52f, size.height * 0.30f,
                size.width * 0.78f, size.height * 0.16f
            )
            cubicTo(
                size.width * 0.92f, size.height * 0.10f,
                size.width, size.height * 0.18f,
                size.width, size.height * 0.18f
            )
            lineTo(size.width, 0f)
            lineTo(0f, 0f)
            close()
        }
        drawPath(farHill, MapHillFar.copy(alpha = 0.08f))

        val nearHill = Path().apply {
            moveTo(0f, size.height * 0.78f)
            cubicTo(
                size.width * 0.2f, size.height * 0.70f,
                size.width * 0.55f, size.height * 0.86f,
                size.width, size.height * 0.72f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(nearHill, MapHillFar.copy(alpha = 0.06f))

        repeat(8) { i ->
            val x = ((i * 97 + scrollOffset / 8) % size.width.toInt()).toFloat()
            val y = ((i * 163) % (size.height * 0.7f).toInt()).toFloat() + size.height * 0.12f
            drawCircle(
                color = DinoMint.copy(alpha = 0.04f),
                radius = 48.dp.toPx() + (i % 3) * 12.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun WorldHeaderCard(banner: WorldBanner) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = DeepEmerald.copy(alpha = 0.08f),
                spotColor = DeepEmerald.copy(alpha = 0.10f)
            ),
        shape = RoundedCornerShape(18.dp),
        color = MapSurface,
        border = BorderStroke(1.dp, MapCardBorder.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MapAccentSoft.copy(alpha = 0.45f),
                            MapSurface,
                            MapSurface
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(DinoMint.copy(alpha = 0.22f), DeepEmerald.copy(alpha = 0.12f))
                        ),
                        CircleShape
                    )
                    .border(1.dp, DinoMint.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    banner.icon,
                    contentDescription = null,
                    tint = DeepEmerald,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MapInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = banner.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MapInkMuted
                )
            }
        }
    }
}

@Composable
fun LevelTrail(levelId: Int, isPassed: Boolean) {
    val curve1 = sin(levelId.toFloat() * 0.7f) * 56f
    val curve2 = sin((levelId + 1).toFloat() * 0.7f) * 56f
    val trailColor = if (isPassed) MapPathActive else MapPathLocked

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = size.width / 2f + curve1.dp.toPx()
            val endX = size.width / 2f + curve2.dp.toPx()
            val startY = 0f
            val endY = size.height
            val controlX = (startX + endX) / 2f
            val controlY = size.height / 2f

            val path = Path().apply {
                moveTo(startX, startY)
                quadraticTo(controlX, controlY, endX, endY)
            }

            // Soft outer glow for completed trail
            if (isPassed) {
                drawPath(
                    path = path,
                    color = MapPathActive.copy(alpha = 0.18f),
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            drawPath(
                path = path,
                color = trailColor.copy(alpha = if (isPassed) 0.95f else 0.55f),
                style = Stroke(
                    width = 4.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = if (isPassed) null else PathEffect.dashPathEffect(
                        floatArrayOf(14.dp.toPx(), 10.dp.toPx()),
                        0f
                    )
                )
            )

            // Subtle midpoint marker
            val midT = 0.5f
            val midX = (1 - midT) * (1 - midT) * startX + 2 * (1 - midT) * midT * controlX + midT * midT * endX
            val midY = (1 - midT) * (1 - midT) * startY + 2 * (1 - midT) * midT * controlY + midT * midT * endY
            drawCircle(
                color = trailColor.copy(alpha = if (isPassed) 0.9f else 0.4f),
                radius = 3.5.dp.toPx(),
                center = Offset(midX, midY)
            )
        }
    }
}

@Composable
fun DinoMascot(stage: DinoStage, completedLevels: Int = 0, isFacingLeft: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dino")
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce"
    )

    Box(
        modifier = modifier
            .width(76.dp)
            .height(70.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(40.dp)
                .height(8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.08f))
        )
        DinoCharacter(
            size = 60.dp,
            stage = stage,
            completedLevels = completedLevels,
            isFacingLeft = isFacingLeft,
            modifier = Modifier.offset(y = bounce.dp)
        )
    }
}

@Composable
fun LevelNode(
    level: PuzzleLevel,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    isCompleted: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    val curveFactor = sin(level.id.toFloat() * 0.7f) * 56f
    val nodeSize = when {
        isCurrent -> 72.dp
        level.isBoss || level.isChest -> 64.dp
        else -> 58.dp
    }

    Column(
        modifier = Modifier
            .offset(x = curveFactor.dp)
            .clickable(enabled = isUnlocked, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isCurrent && !level.isChest) {
                val transition = rememberInfiniteTransition(label = "pulse")
                val scale by transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "scale"
                )
                Box(
                    modifier = Modifier
                        .size(nodeSize + 18.dp)
                        .scale(scale)
                        .border(1.5.dp, LevelNodeGreen.copy(alpha = 0.22f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(nodeSize + 8.dp)
                        .background(LevelNodeGreen.copy(alpha = 0.10f), CircleShape)
                )
            }

            val fillBrush = when {
                !isUnlocked -> Brush.linearGradient(
                    listOf(Color(0xFFCED9D3), MapNodeLocked)
                )
                isCurrent -> Brush.linearGradient(
                    listOf(Color(0xFF4ED6AE), LevelNodeGreen, DeepEmerald)
                )
                isCompleted -> Brush.linearGradient(
                    listOf(Color(0xFF3CC89A), DeepEmerald)
                )
                level.isChest -> Brush.linearGradient(
                    listOf(Color(0xFFF6D06A), GoldenAmber, RichGold)
                )
                level.isBoss -> Brush.linearGradient(
                    listOf(Color(0xFFFFB457), Color(0xFFE07A2F))
                )
                else -> Brush.linearGradient(
                    listOf(Color(0xFF45CFA3), LevelNodeGreen)
                )
            }

            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .shadow(
                        elevation = if (isCurrent) 10.dp else if (isUnlocked) 5.dp else 1.dp,
                        shape = CircleShape,
                        ambientColor = if (isUnlocked) LevelNodeGreen.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.06f),
                        spotColor = if (isUnlocked) LevelNodeGreen.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.08f)
                    )
                    .background(fillBrush, CircleShape)
                    .border(
                        width = 2.5.dp,
                        color = Color.White.copy(alpha = if (isUnlocked) 0.72f else 0.35f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = when {
                    !isUnlocked -> Icons.Filled.Lock
                    level.isChest -> Icons.Filled.CardGiftcard
                    isCurrent -> Icons.Filled.PlayArrow
                    isCompleted -> Icons.Filled.Check
                    level.isBoss -> Icons.Filled.EmojiEvents
                    else -> Icons.Filled.AutoStories
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = if (isUnlocked) 1f else 0.75f),
                    modifier = Modifier.size(
                        when {
                            isCurrent -> 30.dp
                            level.isChest || level.isBoss -> 28.dp
                            else -> 24.dp
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (level.isChest) "Sandık" else level.title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isUnlocked) MapInk else MapInkMuted.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (isUnlocked && !level.isChest) {
            Spacer(modifier = Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) { i ->
                    Icon(
                        imageVector = if (i < stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (i < stars) GoldenAmber else MapPathLocked,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopPlayerBar(
    progress: UserProgressEntity?,
    totalStars: Int,
    completedLevels: Int,
    onBuyHeartClick: () -> Unit
) {
    Surface(
        color = MapSurface.copy(alpha = 0.96f),
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(2.dp, CircleShape, ambientColor = DinoMint.copy(0.15f), spotColor = DinoMint.copy(0.2f))
                        .background(MapAccentSoft, CircleShape)
                        .border(1.5.dp, DinoMint.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    DinoCharacter(
                        size = 32.dp,
                        stage = DinoStage.safeValueOf(progress?.dinoStage ?: "EGG"),
                        completedLevels = completedLevels
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.widthIn(max = 140.dp)) {
                    Text(
                        text = progress?.username ?: "Çaprazca Gezgini",
                        style = MaterialTheme.typography.titleSmall,
                        color = MapInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (progress?.xp?.toFloat() ?: 0f) / ((progress?.level ?: 1) * 200f).coerceAtLeast(1f) },
                        modifier = Modifier
                            .width(72.dp)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = DinoMint,
                        trackColor = MapPathLocked.copy(alpha = 0.55f)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatChip(
                    icon = Icons.Filled.Favorite,
                    value = "${progress?.hearts ?: 5}",
                    tint = HeartRed,
                    onClick = onBuyHeartClick
                )
                StatChip(
                    icon = Icons.Filled.Egg,
                    value = "${progress?.coins ?: 0}",
                    tint = GoldenAmber
                )
                StatChip(
                    icon = Icons.Filled.Star,
                    value = "$totalStars",
                    tint = GoldenAmber
                )
            }
        }
    }
}

@Composable
fun StatChip(
    icon: ImageVector,
    value: String,
    tint: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MapStatChip)
            .border(1.dp, MapCardBorder.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MapInk,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelDetailsDialog(
    level: PuzzleLevel,
    bestTime: Int?,
    onDismiss: () -> Unit,
    onPlayClick: () -> Unit,
    stage: DinoStage = DinoStage.EGG,
    completedLevels: Int = 0
) {
    val avgLength = remember(level.wordIds) {
        level.wordIds.map { it.length }.average().takeIf { !it.isNaN() } ?: 0.0
    }
    val minWordLength = remember(level.wordIds) { level.wordIds.minOfOrNull { it.length } ?: 0 }
    val maxWordLength = remember(level.wordIds) { level.wordIds.maxOfOrNull { it.length } ?: 0 }
    val difficulty = when {
        level.isBoss -> "Boss"
        avgLength <= 4.5 -> "Kolay"
        avgLength <= 6.5 -> "Orta"
        else -> "Zor"
    }
    val estimatedTime = when (difficulty) {
        "Kolay" -> "1-2 dk"
        "Orta" -> "2-4 dk"
        "Boss" -> "4-6 dk"
        else -> "3-5 dk"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MapSurface),
            border = BorderStroke(1.dp, MapCardBorder.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DinoCharacter(size = 76.dp, stage = stage, completedLevels = completedLevels)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Bölüm ${level.levelNumber}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MapInk
                )
                Text(
                    level.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DinoMint
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DetailItem("Seviye", level.cefrLevel)
                    DetailItem("Zorluk", difficulty)
                    DetailItem("Süre", estimatedTime)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DetailItem("Kelime", "${level.wordIds.size}")
                    DetailItem("Ödül", "+${level.coinsReward}")
                    DetailItem("En İyi", bestTime?.let { formatCompletionTime(it) } ?: "--:--")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Bölüm özeti",
                    style = MaterialTheme.typography.labelLarge,
                    color = MapInk
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${level.wordIds.size} gizli kelime", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DinoMint.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, DinoMint.copy(alpha = 0.2f))
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("$minWordLength-$maxWordLength harf", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = SkyBlue.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, SkyBlue.copy(alpha = 0.2f))
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Cevaplar oyun içinde açılır", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = GoldenAmber.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, GoldenAmber.copy(alpha = 0.2f))
                    )
                }
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = DinoMint.copy(0.3f), spotColor = DinoMint.copy(0.4f)),
                    colors = ButtonDefaults.buttonColors(containerColor = DinoMint),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Maceraya Başla", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                TextButton(onClick = onDismiss) {
                    Text("Kapat", color = MapInkMuted)
                }
            }
        }
    }
}

@Composable
fun DetailItem(l: String, v: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(l, style = MaterialTheme.typography.labelMedium, color = MapInkMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(v, style = MaterialTheme.typography.titleSmall, color = MapInk, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NoHeartsDialog(onDismiss: () -> Unit, onBuyHeart: () -> Unit, onFreeHeart: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MapSurface,
        title = { Text("Canlar Tükendi!", color = MapInk) },
        text = {
            Text(
                "Yeni bölüme başlamak için canın kalmadı. Her ${EconomyConfig.HEART_RESTORE_MINUTES} dakikada 1 can yenilenir; istersen günlük bonus canını alabilir veya altın yumurtayla can satın alabilirsin.",
                color = MapInkMuted
            )
        },
        confirmButton = {
            Column {
                Button(onClick = onFreeHeart, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DinoMint)) {
                    Text("Günlük Bonus Can")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBuyHeart, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber)) {
                    Text("Can Al (${EconomyConfig.HEART_COST})")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Kapat", color = MapInkMuted) }
        }
    )
}

@Composable
fun ChestRewardsDialog(result: ChestClaimResult, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MapSurface),
            border = BorderStroke(1.5.dp, GoldenAmber.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = GoldenAmber, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hazine Bulundu!", style = MaterialTheme.typography.headlineMedium, color = MapInk)
                Text(
                    "+${result.coinsEarned} Yumurta, +${result.xpEarned} XP",
                    style = MaterialTheme.typography.titleMedium,
                    color = MapInkMuted
                )
                Box(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Harika!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Backward-compatible aliases used by older call sites / tests
@Composable
fun PrehistoricBackground(listState: LazyListState) = MapAtmosphere(listState)

@Composable
fun WorldHeaderCard(title: String) {
    WorldHeaderCard(
        WorldBanner(
            title = title,
            subtitle = "Yolculuğuna devam et",
            icon = Icons.Filled.Pets
        )
    )
}

@Composable
fun StonePath(levelId: Int, isPassed: Boolean, isCurrentPath: Boolean) {
    LevelTrail(levelId = levelId, isPassed = isPassed || isCurrentPath)
}

@Composable
fun StatBadge(icon: ImageVector, value: String, color: Color, onClick: (() -> Unit)? = null) {
    StatChip(icon = icon, value = value, tint = color, onClick = onClick)
}

@Composable
fun DinoEggDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        val p = Path().apply {
            moveTo(w / 2, 0f)
            cubicTo(w * 0.95f, 0f, w, h * 0.65f, w / 2, h)
            cubicTo(0f, h * 0.65f, w * 0.05f, 0f, w / 2, 0f)
        }
        drawPath(p, EggCream)
        drawCircle(DinoMint.copy(0.3f), radius = w * 0.18f, center = Offset(w * 0.35f, h * 0.45f))
        drawCircle(DinoSpike.copy(0.3f), radius = w * 0.12f, center = Offset(w * 0.65f, h * 0.65f))
    }
}
