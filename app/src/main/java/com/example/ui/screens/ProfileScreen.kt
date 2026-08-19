package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.util.cleanTurkishText
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: GameViewModel) {
    val progress by viewModel.progress.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    val totalStars by viewModel.totalStars.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isClaimedToday by viewModel.isClaimedToday.collectAsState()
    val nextAvailableRewardDay by viewModel.nextAvailableRewardDay.collectAsState()

    val isSoundEnabled by viewModel.soundEnabled.collectAsState()
    val isVibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditDinoNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var tempDinoName by remember { mutableStateOf("") }

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Oyuncu Profili".cleanTurkishText(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Profile Info Box
            progress?.let { prog ->
                ProfileInfoCard(
                    progress = prog,
                    onEditNameClick = {
                        tempName = prog.username
                        showEditNameDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dino Evolution Progress Block
                DinoProgressBlock(
                    progress = prog,
                    completedCount = completedCount,
                    totalStars = totalStars,
                    onEditDinoNameClick = {
                        tempDinoName = prog.dinoNickname
                        showEditDinoNameDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Daily Reward Grid
                DailyLoginRewardGrid(
                    currentStreak = prog.streak,
                    lastClaimedDay = prog.lastClaimedLoginDay,
                    isClaimedToday = isClaimedToday,
                    nextAvailableDay = nextAvailableRewardDay,
                    onClaimClick = { day ->
                        viewModel.claimDailyReward(day) { result ->
                            coroutineScope.launch {
                                when (result) {
                                    is DailyRewardResult.Claimed -> snackbarHostState.showSnackbar(
                                        "${result.day}. gün ödülü alındı: +${result.coins} jeton".cleanTurkishText()
                                    )
                                    DailyRewardResult.AlreadyClaimedToday -> snackbarHostState.showSnackbar(
                                        "Bugünkü giriş ödülünü zaten aldın.".cleanTurkishText()
                                    )
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player Statistics Grid
            PlayerStatsBlock(
                completedCount = completedCount,
                learnedCount = learnedCount,
                xp = progress?.xp ?: 0
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Notifications log (Event Log)
            NotificationLogBlock(
                notifications = notifications,
                onClearAll = { viewModel.clearAllNotifications() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Settings Block
            SettingsBlock(
                isSoundEnabled = isSoundEnabled,
                onSoundToggle = { viewModel.setSoundEnabled(it) },
                isVibrationEnabled = isVibrationEnabled,
                onVibrationToggle = { viewModel.setVibrationEnabled(it) },
                onResetClick = { showResetDialog = true }
            )
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Kullanıcı Adını Düzenle".cleanTurkishText(), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Kullanıcı Adı".cleanTurkishText()) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.updateProfile(tempName, "ic_avatar_default")
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("İptal".cleanTurkishText(), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Dino Name Dialog
    if (showEditDinoNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditDinoNameDialog = false },
            title = { Text("Dinozorunun Adını Değiştir".cleanTurkishText(), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempDinoName,
                    onValueChange = { tempDinoName = it },
                    label = { Text("Dino Adı (2-14 karakter)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempDinoName.length in 2..14) {
                            viewModel.renameDino(tempDinoName)
                            showEditDinoNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DinoMint)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDinoNameDialog = false }) {
                    Text("İptal".cleanTurkishText(), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Reset Progress Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("İlerlemeyi Sıfırla?".cleanTurkishText(), fontWeight = FontWeight.Bold, color = ErrorRed) },
            text = { Text("Tüm çözülen bulmacalar, toplanan jetonlar, kelimeler ve başarımlar tamamen silinecektir. Bu işlem geri alınamaz!".cleanTurkishText()) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Sıfırla".cleanTurkishText())
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Vazgeç".cleanTurkishText(), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Composable
fun DinoProgressBlock(
    progress: UserProgressEntity,
    completedCount: Int,
    totalStars: Int,
    onEditDinoNameClick: () -> Unit
) {
    val currentStage = DinoStage.safeValueOf(progress.dinoStage)
    val nextMilestone = DinoEvolutionPlan.nextMilestone(completedCount)
    val nextStage = if (nextMilestone != null) {
        nextMilestone.stage ?: currentStage
    } else {
        null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dinozorum: ${progress.dinoNickname.cleanTurkishText()}".cleanTurkishText(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = DinoMint
                )

                IconButton(onClick = onEditDinoNameClick) {
                    Icon(Icons.Filled.DriveFileRenameOutline, null, tint = DinoMint, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(DinoMint.copy(alpha = 0.05f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    DinoCharacter(size = 90.dp, stage = currentStage, completedLevels = completedCount)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = currentStage.displayName.cleanTurkishText(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = currentStage.description.cleanTurkishText(),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            if (nextStage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sıradaki değişim: ${(nextMilestone?.title ?: nextStage.displayName).cleanTurkishText()}".cleanTurkishText(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Requirement Checks
                val reqXp = when(nextStage) {
                    DinoStage.TINY_BABY -> 0
                    DinoStage.GROWING_BABY -> 0
                    DinoStage.EXPLORER -> 0
                    DinoStage.SCHOLAR -> 300
                    else -> 1000
                }
                val reqLvl = when(nextStage) {
                    DinoStage.TINY_BABY -> 1
                    DinoStage.GROWING_BABY -> 4
                    DinoStage.EXPLORER -> 10
                    DinoStage.SCHOLAR -> 20
                    else -> 35
                }
                val reqStars = when(nextStage) {
                    DinoStage.SCHOLAR -> 30
                    else -> 0
                }
                val targetLevelCount = nextMilestone?.completedLevels ?: reqLvl
                val levelsLeft = (targetLevelCount - completedCount).coerceAtLeast(0)
                val xpLeft = 0
                val starsLeft = 0
                val nextHint = when {
                    levelsLeft == 1 && xpLeft == 0 && starsLeft == 0 -> "1 bölüm sonra büyüyecek!".cleanTurkishText()
                    levelsLeft > 1 && xpLeft == 0 && starsLeft == 0 -> "$levelsLeft bölüm sonra büyüyecek.".cleanTurkishText()
                    levelsLeft == 0 && xpLeft > 0 -> "$xpLeft XP daha kazanması gerekiyor.".cleanTurkishText()
                    levelsLeft == 0 && starsLeft > 0 -> "$starsLeft yıldız daha gerekiyor.".cleanTurkishText()
                    else -> "Büyümeye yaklaşıyor.".cleanTurkishText()
                }
                Surface(
                    color = DinoMint.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DinoMint.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = nextHint,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DinoMint
                    )
                }

                // Dino gelişimi artık her birkaç bölümde bir görünür değişim verir.
                EvolutionRequirementRow("Bölüm", completedCount, targetLevelCount)
                if (false && reqStars > 0) {
                    EvolutionRequirementRow("Yıldız", totalStars, reqStars)
                }

                // Developmental text
                val devText = (nextMilestone?.description ?: "Usta formundan sonra her 5 bölümde yeni kuyruk parıltısı eklenir.").cleanTurkishText()
                Text(
                    text = "• $devText".cleanTurkishText(),
                    fontSize = 11.sp,
                    color = DinoMint,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EvolutionRequirementRow(label: String, current: Int, target: Int) {
    val isMet = current >= target
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            contentDescription = null,
            tint = if (isMet) EmeraldGreen else Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "${label.cleanTurkishText()}: $current / $target", fontSize = 11.sp, color = if (isMet) TextDark else Color.Gray)
    }
}

@Composable
fun ProfileInfoCard(
    progress: UserProgressEntity,
    onEditNameClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Profile Avatar Container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(DinoMint.copy(alpha = 0.15f), CircleShape)
                        .border(3.dp, DinoMint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    DinoHeadAvatar(
                        size = 56.dp,
                        stage = DinoStage.safeValueOf(progress.dinoStage),
                        completedLevels = (progress.highestUnlockedLevelId - 1).coerceAtLeast(0)
                    )
                }
                // Edit Button Overlay
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(DinoMint, CircleShape)
                        .clickable { onEditNameClick() }
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Düzenle".cleanTurkishText(),
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = progress.username.cleanTurkishText(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Seviye ${progress.level} Çaprazcı".cleanTurkishText(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // XP Progress slider
            val currentLevel = progress.level
            val nextLevelXp = currentLevel * 200
            val currentXp = progress.xp
            val progressRatio = (currentXp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Deneyim Puanı (XP)".cleanTurkishText(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$currentXp / $nextLevelXp XP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
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
}

@Composable
fun DailyLoginRewardGrid(
    currentStreak: Int,
    lastClaimedDay: Int,
    isClaimedToday: Boolean,
    nextAvailableDay: Int,
    onClaimClick: (Int) -> Unit
) {
    val activeDay = when {
        nextAvailableDay in 1..7 -> nextAvailableDay
        isClaimedToday && lastClaimedDay in 1..7 -> lastClaimedDay
        currentStreak > 0 -> ((currentStreak - 1) % 7) + 1
        else -> 1
    }
    val activeRewardCoins = DailyRewardCatalog.rewards.firstOrNull { it.day == activeDay }?.coins ?: 40

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Günlük Giriş Ödülü".cleanTurkishText(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isClaimedToday) {
                            "Bugün +$activeRewardCoins jeton aldın.".cleanTurkishText()
                        } else {
                            "Kazanılan Seri: $currentStreak Gün".cleanTurkishText()
                        },
                        fontSize = 11.sp,
                        fontWeight = if (isClaimedToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isClaimedToday) EmeraldGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7 Days Grid Row layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (day in 1..7) {
                    val rewardCoins = DailyRewardCatalog.rewards.firstOrNull { it.day == day }?.coins ?: 40
                    val isClaimed = if (isClaimedToday) {
                        day <= activeDay
                    } else {
                        day < activeDay
                    }
                    val isAvailable = !isClaimedToday && day == activeDay

                    val dayBg = when {
                        isClaimed -> EmeraldGreen.copy(alpha = 0.1f)
                        isAvailable -> GoldenAmber.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }

                    val dayBorder = when {
                        isClaimed -> EmeraldGreen
                        isAvailable -> GoldenAmber
                        else -> Color.Transparent
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(dayBg)
                            .border(1.dp, dayBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = isAvailable) { onClaimClick(day) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Gün $day".cleanTurkishText(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClaimed) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = if (day == 7) Icons.Filled.CardGiftcard else Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = if (isClaimed) EmeraldGreen else if (isAvailable) GoldenAmber else Color.Gray,
                            modifier = Modifier.size(if (day == 7) 20.dp else 16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = (if (day == 7) "Hazine" else "+$rewardCoins").cleanTurkishText(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isClaimed) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isClaimedToday) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    color = EmeraldGreen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.22f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ödül hesabına eklendi: +$activeRewardCoins jeton".cleanTurkishText(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            Button(
                onClick = { onClaimClick(activeDay) },
                enabled = !isClaimedToday,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isClaimedToday) Icons.Filled.CheckCircle else Icons.Filled.CardGiftcard,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = (if (isClaimedToday) "BUGÜN +$activeRewardCoins JETON ALINDI" else "$activeDay. GÜN ÖDÜLÜNÜ AL").cleanTurkishText(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun PlayerStatsBlock(
    completedCount: Int,
    learnedCount: Int,
    xp: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Öğrenim İstatistikleri".cleanTurkishText(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge(
                    title = "Çözülen Bölüm".cleanTurkishText(),
                    value = "$completedCount / 100",
                    icon = Icons.Filled.Explore,
                    color = SkyBlue
                )
                StatBadge(
                    title = "Öğrenilen Kelime".cleanTurkishText(),
                    value = "$learnedCount Kelime",
                    icon = Icons.Filled.MenuBook,
                    color = EmeraldGreen
                )
                StatBadge(
                    title = "Toplam Skor",
                    value = "$xp XP",
                    icon = Icons.Filled.EmojiEvents,
                    color = GoldenAmber
                )
            }
        }
    }
}

@Composable
fun RowScope.StatBadge(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(4.dp)
            .background(color.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title.cleanTurkishText(),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NotificationLogBlock(
    notifications: List<NotificationEntity>,
    onClearAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                text = "Aktivite Günlüğü".cleanTurkishText(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Temizle".cleanTurkishText(), fontSize = 11.sp, color = ErrorRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (notifications.isEmpty()) {
                Text(
                    text = "Henüz kaydedilmiş aktivite bulunmuyor.".cleanTurkishText(),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    notifications.forEach { notif ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val cleanTitle = notif.title.cleanTurkishText()
                            val isAchievement = cleanTitle.contains("Başarım", ignoreCase = true)
                            val isTask = cleanTitle.contains("Görev", ignoreCase = true)
                            val isLevel = cleanTitle.contains("Seviye", ignoreCase = true) || cleanTitle.contains("Can", ignoreCase = true)

                            Icon(
                                imageVector = when {
                                    isAchievement -> Icons.Filled.EmojiEvents
                                    isTask -> Icons.Filled.CheckCircle
                                    isLevel -> Icons.Filled.Explore
                                    else -> Icons.Filled.Info
                                },
                                contentDescription = null,
                                tint = when {
                                    isAchievement -> GoldenAmber
                                    isTask -> SkyBlue
                                    isLevel -> EmeraldGreen
                                    else -> Color.Gray
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = notif.message.cleanTurkishText(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatTimestamp(notif.timestamp),
                                    fontSize = 9.sp,
                                    color = Color.Gray
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
fun SettingsBlock(
    isSoundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    isVibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    onResetClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ayarlar".cleanTurkishText(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Sound Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Ses Efektleri".cleanTurkishText(), fontSize = 13.sp)
                }
                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = onSoundToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen)
                )
            }

            // Vibration Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Vibration, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Titreşim".cleanTurkishText(), fontSize = 13.sp)
                }
                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = onVibrationToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Danger action (Reset)
            Button(
                onClick = onResetClick,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, ErrorRed),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Tüm İlerlemeyi Sıfırla".cleanTurkishText(), color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    // Elegant fallback formatting
    return "Şimdi"
}
