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
import com.example.data.model.AchievementEntity
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: GameViewModel) {
    val achievements by viewModel.achievements.collectAsState()

    val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
    val progressRatio = remember(achievements, unlockedCount) {
        if (achievements.isEmpty()) 0f else unlockedCount.toFloat() / achievements.size.toFloat()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Başarımlar & Rozetler",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Kalıcı hedefleri tamamla, şeref köşesini doldur!",
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
        ) {
            // Overall progress header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toplam İlerleme:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$unlockedCount / ${achievements.size} Başarım",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio)
                                .fillMaxHeight()
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(EmeraldGreen, MintGreen)
                                    ),
                                    CircleShape
                                )
                        )
                    }
                }
            }

            // Scrollable list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(achievements) { ach ->
                    AchievementItemRow(achievement = ach)
                }
            }
        }
    }
}

@Composable
fun AchievementItemRow(achievement: AchievementEntity) {
    val isUnlocked = achievement.isUnlocked
    val isSecretAndLocked = achievement.isSecret && !isUnlocked

    val rarityColor = when (achievement.rarity) {
        "COMMON" -> EmeraldGreen
        "RARE" -> SkyBlue
        "EPIC" -> Color(0xFF8B5CF6) // Royal Purple
        else -> GoldenAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = if (isUnlocked) 1.dp else 0.5.dp,
            color = if (isUnlocked) rarityColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isUnlocked) rarityColor.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.15f),
                        CircleShape
                    )
                    .border(
                        2.dp,
                        if (isUnlocked) rarityColor else Color.LightGray.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSecretAndLocked) Icons.Filled.HelpCenter else Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = if (isUnlocked) rarityColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSecretAndLocked) "Gizli Başarım" else achievement.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )

                    // Unlocked Tag
                    if (isUnlocked) {
                        Box(
                            modifier = Modifier
                                .background(rarityColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = achievement.rarity,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = rarityColor
                            )
                        }
                    }
                }

                Text(
                    text = if (isSecretAndLocked) "Bu başarımı açmak için sürpriz hedefleri tamamlamalısın." else achievement.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Indicator
                if (!isUnlocked && !isSecretAndLocked) {
                    val progressRatio = (achievement.currentValue.toFloat() / achievement.targetValue.toFloat()).coerceIn(0f, 1f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .fillMaxHeight()
                                    .background(rarityColor, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${achievement.currentValue}/${achievement.targetValue}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }

                // Reward label
                if (isUnlocked) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Tamamlandı (Ödüller alındı: +${achievement.rewardCoins}🪙, +${achievement.rewardXp}XP)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                } else if (!isSecretAndLocked) {
                    Text(
                        text = "Ödül: ${achievement.rewardCoins} 🪙 + ${achievement.rewardXp} XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = rarityColor
                    )
                }
            }
        }
    }
}
