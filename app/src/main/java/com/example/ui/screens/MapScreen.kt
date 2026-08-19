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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

// Theme Colors
val CleanBgColor = Color(0xFFE6F6EE)
val PathStoneColor1 = Color(0xFFB8BEC1)
val PathStoneColor2 = Color(0xFFAEB5B8)
val PathStoneColor3 = Color(0xFFC5C9CB)
val PathStoneColor4 = Color(0xFF9FA7AA)
val LevelNodeGreen = Color(0xFF2EBD8A)
val WorldCardBg = Color(0xFFE8F5E9)
val WorldCardBorder = Color(0xFF81C784)
val TextDark = Color(0xFF2C3E50)
val GrassGreen = Color(0xFF689F38)

data class StoneData(
    val progress: Float,
    val lateralOffset: Float,
    val width: Float,
    val height: Float,
    val rotation: Float,
    val shapeVariant: Int,
    val color: Color
)

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
                        colors = listOf(
                            Color(0xFFD8F1E7),
                            Color(0xFFEAF7EF),
                            Color(0xFFDCEFE5)
                        )
                    )
                )
        ) {
            // Background Layer
            PrehistoricBackground(listState)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_lazy_column"),
                contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp),
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

                    // World Banners
                    if (level.id == 1) WorldHeaderCard("Dünya 1: Yeşil Vadi")
                    else if (level.id == 41) WorldHeaderCard("Dünya 2: Kristal Mağara")
                    else if (level.id == 76) WorldHeaderCard("Dünya 3: Lav Dağı")

                    // Level Node Row with Dino
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Place Dino next to current level
                        if (isCurrent) {
                            val curveFactor = sin(level.id.toFloat() * 0.7f) * 70f
                            DinoMascot(
                                stage = DinoStage.safeValueOf(progress?.dinoStage ?: "EGG"),
                                completedLevels = completedCount,
                                isFacingLeft = curveFactor < 0,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(
                                        x = (if (curveFactor > 0) curveFactor - 92 else curveFactor + 92).dp,
                                        y = 12.dp
                                    )
                                    .zIndex(2f)
                            )
                        }

                        // Rare Egg Decoration
                        if (level.id % 20 == 12) {
                            DinoEggDecoration(
                                modifier = Modifier
                                    .align(if (level.id % 2 != 0) Alignment.CenterEnd else Alignment.CenterStart)
                                    .padding(horizontal = 70.dp)
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

                    // Stone Path between levels
                    if (index < levels.size - 1) {
                        StonePath(
                            levelId = level.id,
                            isPassed = level.id < currentLevelId,
                            isCurrentPath = level.id == currentLevelId - 1 || level.id == currentLevelId
                        )
                    }
                }
            }
        }
    }

    // Dialogs
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

    // Dino Evolution Celebration Dialog
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
fun PrehistoricBackground(listState: LazyListState) {
    val scrollOffset = listState.firstVisibleItemIndex * 200 + listState.firstVisibleItemScrollOffset

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val hillColor = Color(0xFF7DBA84).copy(alpha = 0.13f)
            val farHill = Path().apply {
                moveTo(0f, size.height * 0.18f)
                cubicTo(size.width * 0.25f, size.height * 0.08f, size.width * 0.45f, size.height * 0.28f, size.width * 0.72f, size.height * 0.15f)
                cubicTo(size.width * 0.88f, size.height * 0.08f, size.width, size.height * 0.16f, size.width, size.height * 0.16f)
                lineTo(size.width, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(farHill, hillColor)

            repeat(18) { i ->
                val x = ((i * 73) % size.width.toInt()).toFloat()
                val y = ((i * 211 + scrollOffset / 3) % size.height.toInt()).toFloat()
                drawOval(
                    color = GrassGreen.copy(alpha = 0.08f),
                    topLeft = Offset(x, y),
                    size = Size(24.dp.toPx(), 10.dp.toPx())
                )
            }
        }

        repeat(10) { i ->
            val yPos = (i * 900).dp - (scrollOffset * 0.15f).dp
            val isLeft = i % 2 == 0

            Box(
                modifier = Modifier
                    .offset(x = if (isLeft) (-10).dp else 300.dp, y = yPos)
                    .alpha(0.04f)
            ) {
                Icon(
                    imageVector = when (i % 4) {
                        0 -> Icons.Filled.Park
                        1 -> Icons.Filled.HistoryEdu
                        2 -> Icons.Filled.AutoAwesome
                        else -> Icons.Filled.Terrain
                    },
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = TextDark
                )
            }
        }
    }
}

@Composable
fun WorldHeaderCard(title: String) {
    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WorldCardBg),
        border = BorderStroke(1.5.dp, WorldCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(DinoMint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Pets, null, tint = DinoMint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = "Minik Dino'nun ilk macerası", fontSize = 11.sp, color = TextDark.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun StonePath(levelId: Int, isPassed: Boolean, isCurrentPath: Boolean) {
    val stones = remember(levelId) {
        val random = java.util.Random(levelId.toLong() * 37)
        val stoneList = mutableListOf<StoneData>()
        val count = 8 + random.nextInt(4)

        for (i in 0 until count) {
            stoneList.add(
                StoneData(
                    progress = i.toFloat() / count + (random.nextFloat() * 0.04f),
                    lateralOffset = (random.nextFloat() - 0.5f) * 48f,
                    width = 12f + random.nextFloat() * 8f,
                    height = 9f + random.nextFloat() * 6f,
                    rotation = random.nextFloat() * 360f,
                    shapeVariant = random.nextInt(6),
                    color = when(random.nextInt(4)) {
                        0 -> PathStoneColor1
                        1 -> PathStoneColor2
                        2 -> PathStoneColor3
                        else -> PathStoneColor4
                    }
                )
            )
        }
        stoneList
    }

    val curve1 = sin(levelId.toFloat() * 0.7f) * 70f
    val curve2 = sin((levelId + 1).toFloat() * 0.7f) * 70f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = size.width / 2 + curve1.dp.toPx()
            val endX = size.width / 2 + curve2.dp.toPx()
            val startY = -5.dp.toPx()
            val endY = 95.dp.toPx()
            val cx = (startX + endX) / 2

            stones.forEachIndexed { index, stone ->
                val t = stone.progress
                val bx = (1-t)*(1-t)*startX + 2*(1-t)*t*cx + t*t*endX
                val by = startY + (endY - startY) * t

                val dx = 2*(1-t)*(cx - startX) + 2*t*(endX - cx)
                val dy = (endY - startY)
                val mag = kotlin.math.sqrt(dx*dx + dy*dy).coerceAtLeast(0.1f).toFloat()
                val nx = -dy / mag
                val ny = dx / mag

                val finalX = bx + nx * stone.lateralOffset
                val finalY = by + ny * stone.lateralOffset

                drawOrganicStone(stone, finalX, finalY, isPassed)

                if (isCurrentPath && index % 4 == 0) {
                    drawFootprint(finalX - 20f, finalY + 8f)
                }

                if (index % 7 == 0) {
                    drawMiniGrass(finalX + 25f, finalY - 12f)
                }
            }
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrganicStone(
    stone: StoneData,
    x: Float,
    y: Float,
    isPassed: Boolean
) {
    val path = Path()
    val w = stone.width.dp.toPx()
    val h = stone.height.dp.toPx()

    when (stone.shapeVariant) {
        0 -> {
            path.moveTo(-w/2, 0f)
            path.quadraticTo(-w/2, -h/2, 0f, -h/1.7f)
            path.quadraticTo(w/2, -h/2.3f, w/2.1f, 0f)
            path.quadraticTo(w/1.9f, h/2, 0f, h/2.2f)
            path.quadraticTo(-w/2.2f, h/2.1f, -w/2, 0f)
        }
        1 -> {
            path.moveTo(0f, -h/2)
            path.quadraticTo(w/2.2f, h/2.4f, w/2.8f, h/2)
            path.lineTo(-w/2.8f, h/2.1f)
            path.quadraticTo(-w/2.2f, h/2.4f, 0f, -h/2)
        }
        2 -> {
            path.moveTo(-w/3.5f, -h/2)
            path.lineTo(w/3f, -h/2.3f)
            path.lineTo(w/2.1f, h/2)
            path.lineTo(-w/2.2f, h/1.8f)
            path.close()
        }
        3 -> {
            val s = 0.75f
            path.moveTo(-w*s/2, -h*s/3)
            path.lineTo(w*s/3, -h*s/2)
            path.lineTo(w*s/2, h*s/4)
            path.lineTo(-w*s/4, h*s/2)
            path.close()
        }
        4 -> {
            path.moveTo(-w/2, h/3)
            path.lineTo(-w/3.2f, -h/2.2f)
            path.quadraticTo(0f, -h/1.4f, w/2, 0f)
            path.lineTo(w/3.5f, h/2.1f)
            path.close()
        }
        else -> {
            path.addOval(androidx.compose.ui.geometry.Rect(Offset(-w/2.2f, -h/2.5f), Size(w/2.4f, h/2.4f)))
            path.addOval(androidx.compose.ui.geometry.Rect(Offset(w/12f, h/12f), Size(w/3.2f, h/3.2f)))
        }
    }

    val baseColor = if (isPassed) stone.color.copy(0.6f) else stone.color

    drawContext.canvas.save()
    drawContext.canvas.translate(x, y)
    drawContext.canvas.rotate(stone.rotation)

    // Shadow
    drawContext.canvas.translate(2.5f, 2.5f)
    drawPath(path, Color.Black.copy(0.08f))
    drawContext.canvas.translate(-2.5f, -2.5f)

    // Fill
    drawPath(path, baseColor)

    // Top light highlight
    drawPath(path, Color.White.copy(0.12f), style = Stroke(width = 2f))

    drawContext.canvas.restore()
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFootprint(x: Float, y: Float) {
    val c = DinoMint.copy(0.25f)
    drawCircle(c, radius = 5f, center = Offset(x, y))
    drawCircle(c, radius = 3.5f, center = Offset(x - 6f, y - 5f))
    drawCircle(c, radius = 3.5f, center = Offset(x + 6f, y - 5f))
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniGrass(x: Float, y: Float) {
    val p = Path()
    p.moveTo(0f, 0f)
    p.quadraticTo(-4f, -12f, -8f, -18f)
    p.moveTo(0f, 0f)
    p.quadraticTo(0f, -15f, 2f, -22f)
    p.moveTo(0f, 0f)
    p.quadraticTo(4f, -10f, 10f, -16f)
    drawContext.canvas.save()
    drawContext.canvas.translate(x, y)
    drawPath(p, GrassGreen.copy(0.35f), style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    drawContext.canvas.restore()
}

@Composable
fun DinoMascot(stage: DinoStage, completedLevels: Int = 0, isFacingLeft: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition("dino")
    val bounce by transition.animateFloat(0f, -6f, infiniteRepeatable(tween(900), RepeatMode.Reverse), "bounce")

    Box(
        modifier = modifier
            .width(72.dp)
            .height(66.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(42.dp)
                .height(9.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.10f))
        )
        DinoCharacter(
            size = 58.dp,
            stage = stage,
            completedLevels = completedLevels,
            isFacingLeft = isFacingLeft,
            modifier = Modifier.offset(y = bounce.dp)
        )
    }
}

@Composable
fun DinoEggDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(30.dp)) {
        val w = size.width; val h = size.height
        val p = Path().apply {
            moveTo(w/2, 0f)
            cubicTo(w*0.95f, 0f, w, h*0.65f, w/2, h)
            cubicTo(0f, h*0.65f, w*0.05f, 0f, w/2, 0f)
        }
        drawPath(p, EggCream)
        drawCircle(DinoMint.copy(0.3f), radius = w*0.18f, center = Offset(w*0.35f, h*0.45f))
        drawCircle(DinoSpike.copy(0.3f), radius = w*0.12f, center = Offset(w*0.65f, h*0.65f))
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
    val curveFactor = sin(level.id.toFloat() * 0.7f) * 70f

    Column(
        modifier = Modifier
            .offset(x = curveFactor.dp)
            .clickable(enabled = isUnlocked) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            val nodeColor = if (isUnlocked) LevelNodeGreen else Color.LightGray

            if (isCurrent && !level.isChest) {
                val transition = rememberInfiniteTransition("pulse")
                val scale by transition.animateFloat(1f, 1.10f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), "scale")
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .scale(scale)
                        .border(1.5.dp, LevelNodeGreen.copy(0.28f), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(elevation = if (isCurrent) 7.dp else 4.dp, shape = CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isUnlocked) 0.34f else 0.18f),
                                nodeColor
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(width = 2.dp, color = Color.White.copy(0.58f), shape = CircleShape)
                    .border(width = 0.8.dp, color = nodeColor.copy(alpha = 0.45f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val icon = when {
                    !isUnlocked -> Icons.Filled.Lock
                    level.isChest -> Icons.Filled.Egg
                    isCurrent -> Icons.Filled.PlayArrow
                    isCompleted -> Icons.Filled.Check
                    level.isBoss -> Icons.Filled.EmojiEvents
                    else -> Icons.Default.MenuBook
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCompleted && !isCurrent) Color.White.copy(0.8f) else if (!isUnlocked) Color.White.copy(0.7f) else Color.White,
                    modifier = Modifier.size(
                        when {
                            level.isChest || level.isBoss -> 30.dp
                            isCurrent -> 25.dp
                            else -> 24.dp
                        }
                    )
                )
            }

            if (isCompleted && !level.isChest) {
                Icon(Icons.Filled.CheckCircle, null, tint = Color.White.copy(0.82f), modifier = Modifier.size(18.dp).align(Alignment.BottomEnd).offset((-7).dp, (-7).dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(level.theme, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) TextDark else Color.Gray)

        if (isUnlocked && !level.isChest) {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(3) { i ->
                    Icon(
                        imageVector = if (i < stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (i < stars) GoldenAmber else Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopPlayerBar(progress: UserProgressEntity?, totalStars: Int, completedLevels: Int, onBuyHeartClick: () -> Unit) {
    Surface(tonalElevation = 6.dp, shadowElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).background(DinoMint.copy(0.1f), CircleShape).border(2.dp, DinoMint, CircleShape), contentAlignment = Alignment.Center) {
                    DinoCharacter(size = 34.dp, stage = DinoStage.safeValueOf(progress?.dinoStage ?: "EGG"), completedLevels = completedLevels)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(progress?.username ?: "Çaprazca Gezgini", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LinearProgressIndicator(
                        progress = { (progress?.xp?.toFloat() ?: 0f) / ((progress?.level ?: 1) * 200f) },
                        modifier = Modifier.width(60.dp).height(5.dp).clip(CircleShape),
                        color = DinoMint,
                        trackColor = Color.LightGray.copy(0.3f)
                    )
                }
            }
            Row {
                StatBadge(Icons.Filled.Favorite, "${progress?.hearts ?: 5}", HeartRed, onBuyHeartClick)
                Spacer(modifier = Modifier.width(8.dp))
                StatBadge(Icons.Filled.Egg, "${progress?.coins ?: 0}", GoldenAmber)
                Spacer(modifier = Modifier.width(8.dp))
                StatBadge(Icons.Filled.Star, "$totalStars", GoldenAmber)
            }
        }
    }
}

@Composable
fun StatBadge(icon: ImageVector, value: String, color: Color, onClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TextDark)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelDetailsDialog(level: PuzzleLevel, bestTime: Int?, onDismiss: () -> Unit, onPlayClick: () -> Unit, stage: DinoStage = DinoStage.EGG, completedLevels: Int = 0) {
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
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                DinoCharacter(size = 80.dp, stage = stage, completedLevels = completedLevels)
                Spacer(Modifier.height(12.dp))
                Text("Bölüm ${level.levelNumber}", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(level.title, color = DinoMint, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DetailItem("Seviye", level.cefrLevel); DetailItem("Zorluk", difficulty); DetailItem("Süre", estimatedTime)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DetailItem("Kelime", "${level.wordIds.size}"); DetailItem("Ödül", "+${level.coinsReward}"); DetailItem("En İyi", bestTime?.let { formatCompletionTime(it) } ?: "--:--")
                }
                Spacer(Modifier.height(16.dp))
                Text("Bölüm özeti", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                Spacer(Modifier.height(24.dp))
                Button(onClick = onPlayClick, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = DinoMint), shape = RoundedCornerShape(10.dp)) {
                    Text("MACERAYA BAŞLA", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) { Text("Kapat", color = Color.Gray) }
            }
        }
    }
}

@Composable fun DetailItem(l: String, v: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(l, fontSize = 11.sp, color = Color.Gray); Text(v, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark) } }

@Composable fun NoHeartsDialog(onDismiss: () -> Unit, onBuyHeart: () -> Unit, onFreeHeart: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text("Canlar Tükendi!") }, text = { Text("Yeni bölüme başlamak için canın kalmadı. Her ${EconomyConfig.HEART_RESTORE_MINUTES} dakikada 1 can yenilenir; istersen günlük bonus canını alabilir veya altın yumurtayla can satın alabilirsin.") }, confirmButton = { Column { Button(onClick = onFreeHeart, modifier = Modifier.fillMaxWidth()) { Text("Günlük Bonus Can") }; Spacer(Modifier.height(8.dp)); Button(onClick = onBuyHeart, modifier = Modifier.fillMaxWidth()) { Text("Can Al (${EconomyConfig.HEART_COST})") } } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Kapat") } }) }

@Composable fun ChestRewardsDialog(result: ChestClaimResult, onDismiss: () -> Unit) { Dialog(onDismissRequest = onDismiss) { Card(shape = RoundedCornerShape(24.dp), border = BorderStroke(2.dp, GoldenAmber)) { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.AutoAwesome, null, tint = GoldenAmber, modifier = Modifier.size(72.dp)); Text("Hazine Bulundu!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold); Text("+${result.coinsEarned} Yumurta, +${result.xpEarned} XP", fontWeight = FontWeight.Bold); Spacer(Modifier.height(20.dp)); Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber)) { Text("Harika!", color = Color.White) } } } } }
