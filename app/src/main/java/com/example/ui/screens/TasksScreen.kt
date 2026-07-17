package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserTaskEntity
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: GameViewModel) {
    val tasks by viewModel.tasks.collectAsState()

    val dailyTasks = remember(tasks) { tasks.filter { it.type == "DAILY" } }
    val weeklyTasks = remember(tasks) { tasks.filter { it.type == "WEEKLY" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Görevler & Hedefler",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Günlük ve haftalık görevleri tamamla, XP ve Jeton kazan!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Daily Tasks Section
            SectionHeader(title = "Günlük Görevler", subtitle = "Her gece yarısı yenilenir", color = EmeraldGreen)
            if (dailyTasks.isEmpty()) {
                EmptyTasksPlaceholder()
            } else {
                dailyTasks.forEach { task ->
                    TaskCard(
                        task = task,
                        onClaimClick = { viewModel.claimTaskReward(task.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Tasks Section
            SectionHeader(title = "Haftalık Görevler", subtitle = "Büyük hedefler, büyük ödüller", color = SkyBlue)
            if (weeklyTasks.isEmpty()) {
                EmptyTasksPlaceholder()
            } else {
                weeklyTasks.forEach { task ->
                    TaskCard(
                        task = task,
                        onClaimClick = { viewModel.claimTaskReward(task.id) }
                    )
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun TaskCard(
    task: UserTaskEntity,
    onClaimClick: () -> Unit
) {
    val progressRatio = (task.currentValue.toFloat() / task.targetValue.toFloat()).coerceIn(0f, 1f)
    val isReadyToClaim = task.isCompleted && !task.isClaimed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isReadyToClaim) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Fraction progress display (e.g., 2/2 or 5/10)
                Text(
                    text = "${task.currentValue}/${task.targetValue}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (task.isCompleted) EmeraldGreen else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Progress Bar
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
                        .background(if (task.isCompleted) EmeraldGreen else SkyBlue, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer (Rewards & Claim Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reward badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ödül:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    RewardChip(text = "+${task.rewardXp} XP", color = SkyBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    RewardChip(text = "+${task.rewardCoins} 🪙", color = GoldenAmber)
                }

                // Claim Button states
                when {
                    task.isClaimed -> {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.LightGray.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Alındı", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    isReadyToClaim -> {
                        Button(
                            onClick = onClaimClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Ödülü Al", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    else -> {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Ödül Kilitli", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RewardChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun EmptyTasksPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.FactCheck, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Aktif görev bulunmuyor.", fontSize = 12.sp, color = Color.Gray)
    }
}
