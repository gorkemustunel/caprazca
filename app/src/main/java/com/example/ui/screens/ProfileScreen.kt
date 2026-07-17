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
import androidx.compose.ui.window.Dialog
import com.example.data.model.NotificationEntity
import com.example.data.model.UserProgressEntity
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: GameViewModel) {
    val progress by viewModel.progress.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    var showResetDialog by remember { mutableStateOf(false) }

    // Settings States
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isVibrationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oyuncu Profili", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) }
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

                // Daily Reward Grid
                DailyLoginRewardGrid(
                    currentStreak = prog.streak,
                    lastClaimedDay = prog.lastClaimedLoginDay,
                    onClaimClick = { day -> viewModel.claimDailyReward(day) }
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
                onSoundToggle = { isSoundEnabled = it },
                isVibrationEnabled = isVibrationEnabled,
                onVibrationToggle = { isVibrationEnabled = it },
                onResetClick = { showResetDialog = true }
            )
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Kullanıcı Adını Düzenle", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Kullanıcı Adı") },
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
                    Text("İptal", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Reset Progress Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("İlerlemeyi Sıfırla?", fontWeight = FontWeight.Bold, color = ErrorRed) },
            text = { Text("Tüm çözülen bulmacalar, toplanan jetonlar, kelimeler ve başarımlar tamamen silinecektir. Bu işlem geri alınamaz!") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Sıfırla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Vazgeç", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
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
                        .background(EmeraldGreen.copy(alpha = 0.15f), CircleShape)
                        .border(3.dp, EmeraldGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
                // Edit Button Overlay
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(EmeraldGreen, CircleShape)
                        .clickable { onEditNameClick() }
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Düzenle",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = progress.username,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Seviye ${progress.level} Çaprazcı",
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
                        text = "Deneyim Puanı (XP)",
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
    onClaimClick: (Int) -> Unit
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
                Column {
                    Text(
                        text = "Günlük Giriş Ödülü",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Kazanılan Seri: $currentStreak Gün",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                    val rewardCoins = day * 50
                    val isClaimed = day <= lastClaimedDay
                    val isAvailable = day == lastClaimedDay + 1

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
                            text = "Gün $day",
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
                            text = if (day == 7) "Hazine" else "+$rewardCoins",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isClaimed) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
                text = "Öğrenim İstatistikleri",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge(
                    title = "Çözülen Bölüm",
                    value = "$completedCount / 45",
                    icon = Icons.Filled.Explore,
                    color = SkyBlue
                )
                StatBadge(
                    title = "Öğrenilen Kelime",
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
            text = title,
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
                    text = "Aktivite Günlüğü",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Temizle", fontSize = 11.sp, color = ErrorRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (notifications.isEmpty()) {
                Text(
                    text = "Henüz kaydedilmiş aktivite bulunmuyor.",
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
                            val isAchievement = notif.title.contains("Başarım", ignoreCase = true)
                            val isTask = notif.title.contains("Görev", ignoreCase = true)
                            val isLevel = notif.title.contains("Seviye", ignoreCase = true) || notif.title.contains("Can", ignoreCase = true)

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
                                    text = notif.message,
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
                text = "Ayarlar",
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
                    Text("Ses Efektleri", fontSize = 13.sp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Vibration, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Titreşim", fontSize = 13.sp)
                }
                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = onVibrationToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Danger action (Reset)
            Button(
                onClick = onResetClick,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, ErrorRed),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Tüm İlerlemeyi Sıfırla", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    // Elegant fallback formatting
    return "Şimdi"
}
