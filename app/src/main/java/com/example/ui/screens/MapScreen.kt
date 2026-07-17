package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.LevelsData
import com.example.data.repository.RewardItem
import com.example.ui.theme.*
import com.example.viewmodel.*
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: GameViewModel,
    onNavigateToGame: (Int) -> Unit
) {
    val progress by viewModel.progress.collectAsState()
    val levelStates by viewModel.levelStates.collectAsState()
    val totalStars by viewModel.totalStars.collectAsState()

    val levels = remember { LevelsData.getLevels() }
    val listState = rememberLazyListState()

    // Dialog state for starting a level
    var selectedLevelForDialog by remember { mutableStateOf<PuzzleLevel?>(null) }
    var showNoHeartsDialog by remember { mutableStateOf(false) }

    // State for claiming reward chest
    var claimedChestRewards by remember { mutableStateOf<ChestClaimResult?>(null) }

    val startLevelResult by viewModel.startLevelResult.collectAsState()

    LaunchedEffect(startLevelResult) {
        when (startLevelResult) {
            LevelStartResult.Started -> {
                selectedLevelForDialog = null
                viewModel.clearStartLevelResult()
                onNavigateToGame(0)
            }
            LevelStartResult.NotEnoughLives -> {
                showNoHeartsDialog = true
                viewModel.clearStartLevelResult()
            }
            else -> {
                if (startLevelResult != null) {
                    viewModel.clearStartLevelResult()
                }
            }
        }
    }

    // Scroll to player's current unlocked level on first launch
    LaunchedEffect(progress) {
        val currentLevelId = progress?.highestUnlockedLevelId ?: 1
        val index = levels.indexOfFirst { it.id == currentLevelId }
        if (index >= 0) {
            listState.animateScrollToItem((index - 2).coerceAtLeast(0))
        }
    }

    Scaffold(
        topBar = {
            TopPlayerBar(
                progress = progress,
                totalStars = totalStars,
                onBuyHeartClick = { viewModel.buyHeart() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // Background decorations based on scroll position
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Subtle decorative grid or clouds
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_lazy_column"),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Render World Banners and nodes
                items(levels.size) { index ->
                    val level = levels[index]
                    val levelState = levelStates.find { it.levelId == level.id }
                    val currentLevelId = progress?.highestUnlockedLevelId ?: 1

                    val isUnlocked = level.id <= currentLevelId
                    val isCompleted = levelState?.completed == true
                    val starsEarned = levelState?.stars ?: 0

                    // World transitions Header Banners
                    if (level.id == 1) {
                        WorldHeaderCard(
                            worldId = 1,
                            title = "Dünya 1: Günlük Hayat",
                            subtitle = "A1 Temel Seviye kelimeler ile Çaprazca'ya giriş yapın.",
                            color = EmeraldGreen
                        )
                    } else if (level.id == 16) {
                        WorldHeaderCard(
                            worldId = 2,
                            title = "Dünya 2: Şehir Macerası",
                            subtitle = "A2 Seviye kelimelerle şehir hayatını keşfedin.",
                            color = SkyBlue
                        )
                    } else if (level.id == 31) {
                        WorldHeaderCard(
                            worldId = 3,
                            title = "Dünya 3: Doğa Rotası",
                            subtitle = "B1 Seviye kelimelerle doğanın derinliklerine yol alın.",
                            color = GoldenAmber
                        )
                    }

                    // Calculate curved offset
                    val curveFactor = sin(level.id.toFloat() * 0.7f) * 90f
                    val worldThemeColor = when (level.worldId) {
                        1 -> EmeraldGreen
                        2 -> SkyBlue
                        else -> GoldenAmber
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Level node
                        LevelNode(
                            level = level,
                            isUnlocked = isUnlocked,
                            isCompleted = isCompleted,
                            stars = starsEarned,
                            themeColor = worldThemeColor,
                            xOffset = curveFactor,
                            onClick = {
                                if (isUnlocked) {
                                    if (level.isChest) {
                                        viewModel.claimChestReward(level.id) { result ->
                                            claimedChestRewards = result
                                        }
                                    } else {
                                        selectedLevelForDialog = level
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Bottom Floating Decorative elements (Clouds, Trees) depending on world
        }
    }

    // Level Info Dialog
    selectedLevelForDialog?.let { level ->
        val levelState = levelStates.find { it.levelId == level.id }
        LevelDetailsDialog(
            level = level,
            bestTime = levelState?.bestTime,
            onDismiss = { selectedLevelForDialog = null },
            onPlayClick = {
                viewModel.requestStartLevel(level.id, LevelStartReason.MAP)
            }
        )
    }

    // No Hearts Dialog
    if (showNoHeartsDialog) {
        AlertDialog(
            onDismissRequest = { showNoHeartsDialog = false },
            title = { Text("Canın Kalmadı!", fontWeight = FontWeight.Bold) },
            text = { Text("Yeni bölüme başlamak için canın kalmadı. Jeton harcayarak can doldurabilir veya canlarının yenilenmesini bekleyebilirsin.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.buyHeart()
                        showNoHeartsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Can Al (+1 Can / 100 Jeton)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoHeartsDialog = false }) {
                    Text("Kapat", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Chest Claim Rewards Dialog
    claimedChestRewards?.let { result ->
        val rewardsList = listOf(
            RewardItem("Jeton", result.coinsEarned, "ic_coin"),
            RewardItem("XP", result.xpEarned, "ic_xp"),
            RewardItem("Can Dolumu", result.heartsEarned, "ic_heart")
        )
        Dialog(onDismissRequest = { claimedChestRewards = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = BorderStroke(2.dp, GoldenAmber)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CardGiftcard,
                        contentDescription = "Chest",
                        tint = GoldenAmber,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hazine Sandığı Açıldı!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Harika! Sandıktan şu ödüller çıktı:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    for (reward in rewardsList) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = when (reward.iconKey) {
                                    "ic_coin" -> Icons.Filled.MonetizationOn
                                    "ic_heart" -> Icons.Filled.Favorite
                                    else -> Icons.Filled.ElectricBolt
                                },
                                contentDescription = reward.name,
                                tint = when (reward.iconKey) {
                                    "ic_coin" -> GoldenAmber
                                    "ic_heart" -> HeartRed
                                    else -> SkyBlue
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${reward.name}: +${reward.amount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { claimedChestRewards = null },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mükemmel!", color = TextDarkGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TopPlayerBar(
    progress: UserProgressEntity?,
    totalStars: Int,
    onBuyHeartClick: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(EmeraldGreen.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, EmeraldGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = progress?.username ?: "Oyuncu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lv ${progress?.level ?: 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Progress bar towards next level
                        val currentLevel = progress?.level ?: 1
                        val nextLevelXp = currentLevel * 200
                        val currentXp = progress?.xp ?: 0
                        val progressRatio = (currentXp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(6.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .fillMaxHeight()
                                    .background(EmeraldGreen, CircleShape)
                            )
                        }
                    }
                }
            }

            // Stats row (Hearts, Coins, Stars, Streak)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hearts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onBuyHeartClick() }
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Hearts",
                        tint = HeartRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${progress?.hearts ?: 5}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                // Coins
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = "Coins",
                        tint = GoldenAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${progress?.coins ?: 0}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                // Stars
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Stars",
                        tint = GoldenAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$totalStars",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                // Streak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = HeartRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${progress?.streak ?: 0}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WorldHeaderCard(
    worldId: Int,
    title: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when (worldId) {
                        1 -> Icons.Filled.Home
                        2 -> Icons.Filled.LocationCity
                        else -> Icons.Filled.Terrain
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LevelNode(
    level: PuzzleLevel,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    stars: Int,
    themeColor: Color,
    xOffset: Float,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .offset(x = xOffset.dp)
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("level_node_${level.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Node circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isUnlocked) {
                            listOf(themeColor, themeColor.copy(alpha = 0.8f))
                        } else {
                            listOf(Color.Gray, Color.DarkGray)
                        }
                    ),
                    shape = CircleShape
                )
                .border(
                    width = if (isUnlocked) 4.dp else 2.dp,
                    color = if (isCompleted) GoldenAmber else Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (level.isChest) {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = "Hazine Sandığı",
                    tint = if (isCompleted) Color.LightGray else GoldenAmber,
                    modifier = Modifier.size(32.dp)
                )
            } else if (level.isBoss) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Boss Bölümü",
                    tint = if (isCompleted) GoldenAmber else Color.White,
                    modifier = Modifier.size(34.dp)
                )
            } else {
                Text(
                    text = "${level.levelNumber}",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }

            // Locked icon overlay
            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Kilitli",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Level theme title
        Text(
            text = if (level.isChest) "Hazine" else if (level.isBoss) "Boss" else level.theme,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) themeColor else Color.Gray,
            textAlign = TextAlign.Center
        )

        // Level completed stars
        if (isUnlocked && !level.isChest) {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(3) { starIdx ->
                    Icon(
                        imageVector = if (starIdx < stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (starIdx < stars) GoldenAmber else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LevelDetailsDialog(
    level: PuzzleLevel,
    bestTime: Int?,
    onDismiss: () -> Unit,
    onPlayClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            if (level.isBoss) GoldenAmber.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (level.isBoss) Icons.Filled.EmojiEvents else Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = if (level.isBoss) GoldenAmber else EmeraldGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Bölüm ${level.levelNumber}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = level.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (level.isBoss) GoldenAmber else EmeraldGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DetailMetric(title = "Seviye", value = level.cefrLevel)
                    DetailMetric(title = "Kelimeler", value = "${level.words.size}")
                    DetailMetric(title = "En İyi Süre", value = bestTime?.let { formatTime(it) } ?: "--:--")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Additional objectives
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Bölüm Hedefleri:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ObjectiveRow(text = "Bulmacayı başarıyla tamamla (1 ⭐)")
                        ObjectiveRow(text = "Hata limitinin (maks 2) altında bitir (2 ⭐)")
                        ObjectiveRow(text = "Hiç ipucu ve hata yapmadan tamamla (3 ⭐)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Play Button
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("play_level_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (level.isBoss) GoldenAmber else EmeraldGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Oyna",
                        tint = if (level.isBoss) TextDarkGray else Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BAŞLA (-1 Can)",
                        fontWeight = FontWeight.Bold,
                        color = if (level.isBoss) TextDarkGray else Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Kapat", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun DetailMetric(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ObjectiveRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = EmeraldGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
