package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRewardCatalog
import com.example.data.model.UserTaskEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.SkyBlue
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: GameViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isClaimedToday by viewModel.isClaimedToday.collectAsState()
    val nextAvailableRewardDay by viewModel.nextAvailableRewardDay.collectAsState()

    val rotationKey = remember {
        SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()).toIntOrNull() ?: 0
    }
    val weeklyRotationKey = remember {
        val formatter = SimpleDateFormat("yyyyww", Locale.US)
        formatter.format(Date()).toIntOrNull() ?: rotationKey / 7
    }
    val dailyTasks = remember(tasks, rotationKey) {
        tasks.filter { it.type == "DAILY" }.rotatingSelection(rotationKey, 5)
    }
    val weeklyTasks = remember(tasks, weeklyRotationKey) {
        tasks.filter { it.type == "WEEKLY" }.rotatingSelection(weeklyRotationKey, 6)
    }
    val dailyReady = dailyTasks.count { it.isCompleted && !it.isClaimed }
    val weeklyReady = weeklyTasks.count { it.isCompleted && !it.isClaimed }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Görevler", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Günlük döngünü tamamla, haftalık hedefleri büyüt.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                DailyLoopCard(
                    streak = progress?.streak ?: 0,
                    isClaimedToday = isClaimedToday,
                    nextAvailableRewardDay = nextAvailableRewardDay,
                    dailyTasks = dailyTasks,
                    weeklyTasks = weeklyTasks,
                    onClaimDailyReward = {
                        val day = nextAvailableRewardDay.takeIf { it > 0 } ?: 1
                        viewModel.claimDailyReward(day)
                    },
                    onClaimReadyTasks = {
                        (dailyTasks + weeklyTasks)
                            .filter { it.isCompleted && !it.isClaimed }
                            .forEach { viewModel.claimTaskReward(it.id) }
                    }
                )
            }

            item {
                SectionHeader(
                    title = "Bugünün Görevleri",
                    subtitle = if (dailyReady > 0) "$dailyReady ödül toplanabilir" else "Her gece yenilenir",
                    color = EmeraldGreen
                )
            }
            if (dailyTasks.isEmpty()) {
                item { EmptyTasksPlaceholder("Bugün için aktif görev yok.") }
            } else {
                items(dailyTasks, key = { it.id }) { task ->
                    TaskCard(task = task, onClaimClick = { viewModel.claimTaskReward(task.id) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "Haftalık Hedefler",
                    subtitle = if (weeklyReady > 0) "$weeklyReady büyük ödül hazır" else "Yeni hafta başında yenilenir",
                    color = SkyBlue
                )
            }
            if (weeklyTasks.isEmpty()) {
                item { EmptyTasksPlaceholder("Bu hafta için aktif hedef yok.") }
            } else {
                items(weeklyTasks, key = { it.id }) { task ->
                    TaskCard(task = task, onClaimClick = { viewModel.claimTaskReward(task.id) })
                }
            }
        }
    }
}

private fun List<UserTaskEntity>.rotatingSelection(seed: Int, limit: Int): List<UserTaskEntity> {
    val ready = filter { it.isCompleted && !it.isClaimed }
    val active = filter { !it.isClaimed && !it.isCompleted }
    val claimed = filter { it.isClaimed }
    val rotated = (active + claimed).sortedBy { task -> kotlin.math.abs((task.id.hashCode() * 31) + seed) }
    return (ready + rotated)
        .distinctBy { it.id }
        .take(limit.coerceAtLeast(ready.size))
}

@Composable
private fun DailyLoopCard(
    streak: Int,
    isClaimedToday: Boolean,
    nextAvailableRewardDay: Int,
    dailyTasks: List<UserTaskEntity>,
    weeklyTasks: List<UserTaskEntity>,
    onClaimDailyReward: () -> Unit,
    onClaimReadyTasks: () -> Unit
) {
    val dailyProgress = dailyTasks.completionRatio()
    val weeklyProgress = weeklyTasks.completionRatio()
    val readyRewards = (dailyTasks + weeklyTasks).count { it.isCompleted && !it.isClaimed }
    val rewardDay = nextAvailableRewardDay.takeIf { it > 0 } ?: ((streak.coerceAtLeast(1) - 1) % 7) + 1
    val rewardCoins = DailyRewardCatalog.rewards.firstOrNull { it.day == rewardDay }?.coins ?: 40

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GoldenAmber.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = GoldenAmber)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Günlük Döngü", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "$streak günlük seri aktif",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                        )
                    }
                }
                RewardPill("+$rewardCoins", GoldenAmber, Icons.Filled.MonetizationOn)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LoopStatCard(
                    title = "Günlük",
                    value = "${dailyTasks.count { it.isCompleted }}/${dailyTasks.size}",
                    progress = dailyProgress,
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                LoopStatCard(
                    title = "Haftalık",
                    value = "${weeklyTasks.count { it.isCompleted }}/${weeklyTasks.size}",
                    progress = weeklyProgress,
                    color = SkyBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            DailyRewardStrip(
                streak = streak,
                rewardDay = rewardDay,
                isClaimedToday = isClaimedToday
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onClaimDailyReward,
                    enabled = !isClaimedToday,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text(if (isClaimedToday) "SANDIK ALINDI" else "GÜNLÜK ÖDÜLÜ AL", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
                OutlinedButton(
                    onClick = onClaimReadyTasks,
                    enabled = readyRewards > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text(if (readyRewards > 0) "$readyRewards ÖDÜLÜ AL" else "ÖDÜL YOK", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun LoopStatCard(
    title: String,
    value: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DailyRewardStrip(streak: Int, rewardDay: Int, isClaimedToday: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("7 günlük seri ödülü", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                if (isClaimedToday) "Yarın devam et" else "Bugün $rewardDay. gün",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            for (day in 1..7) {
                val completed = day < rewardDay || (day == rewardDay && isClaimedToday)
                val active = day == rewardDay && !isClaimedToday
                val bg by animateColorAsState(
                    targetValue = when {
                        completed -> EmeraldGreen.copy(alpha = 0.16f)
                        active -> GoldenAmber.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    label = "rewardDayBg"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(
                            1.dp,
                            when {
                                completed -> EmeraldGreen.copy(alpha = 0.55f)
                                active -> GoldenAmber
                                else -> Color.Transparent
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (day == 7) Icons.Filled.CardGiftcard else Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = when {
                            completed -> EmeraldGreen
                            active -> GoldenAmber
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(15.dp)
                    )
                    Text("G$day", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(
                subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
fun TaskCard(task: UserTaskEntity, onClaimClick: () -> Unit) {
    val progressRatio = (task.currentValue.toFloat() / task.targetValue.toFloat()).coerceIn(0f, 1f)
    val isReadyToClaim = task.isCompleted && !task.isClaimed
    val accent = when {
        task.isClaimed -> Color.Gray
        isReadyToClaim -> EmeraldGreen
        task.type == "WEEKLY" -> SkyBlue
        else -> GoldenAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isReadyToClaim) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(taskIcon(task), contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(task.title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text(
                            task.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Text(
                    "${task.currentValue.coerceAtMost(task.targetValue)}/${task.targetValue}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = accent
                )
            }

            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (task.isCompleted) EmeraldGreen else accent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    RewardChip(text = "+${task.rewardXp} XP", color = SkyBlue)
                    RewardChip(text = "+${task.rewardCoins} Jeton", color = GoldenAmber)
                }
                when {
                    task.isClaimed -> ClaimedBadge()
                    isReadyToClaim -> Button(
                        onClick = onClaimClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(9.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("ÖDÜLÜ AL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    else -> LockedBadge()
                }
            }

            AnimatedVisibility(isReadyToClaim) {
                Text(
                    "Hazır. Ödülü alıp bir sonraki hedefe geçebilirsin.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
            }
        }
    }
}

@Composable
private fun RewardPill(text: String, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun RewardChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
private fun ClaimedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldGreen.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text("Alındı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
    }
}

@Composable
private fun LockedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text("Devam", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

@Composable
fun EmptyTasksPlaceholder(message: String = "Aktif görev bulunmuyor.") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.FactCheck, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

private fun List<UserTaskEntity>.completionRatio(): Float {
    if (isEmpty()) return 0f
    return count { it.isCompleted }.toFloat() / size.toFloat()
}

private fun taskIcon(task: UserTaskEntity): ImageVector {
    return when {
        task.id.contains("weekly") -> Icons.Filled.Stars
        task.id.contains("daily_9") -> Icons.Filled.CardGiftcard
        task.id.contains("daily_7") || task.id.contains("weekly_7") -> Icons.Filled.Timer
        task.id.contains("daily_3") || task.id.contains("daily_10") || task.id.contains("daily_11") -> Icons.Filled.MenuBook
        else -> Icons.Filled.FactCheck
    }
}
