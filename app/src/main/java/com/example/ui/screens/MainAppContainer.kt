package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.DinoMint
import com.example.ui.theme.MapBgMid
import com.example.ui.theme.MapCardBorder
import com.example.ui.theme.MapInkMuted
import com.example.ui.theme.MapSurface
import com.example.viewmodel.GameViewModel

private data class NavTab(
    val key: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navTabs = listOf(
    NavTab("Map", "Harita", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_tab_map"),
    NavTab("Dictionary", "Sözlük", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_tab_dictionary"),
    NavTab("Review", "Tekrar", Icons.Filled.History, Icons.Outlined.History, "nav_tab_review"),
    NavTab("Tasks", "Görev", Icons.Filled.FactCheck, Icons.Outlined.FactCheck, "nav_tab_tasks"),
    NavTab("Achievements", "Rozet", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_tab_achievements"),
    NavTab("Profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person, "nav_tab_profile"),
)

@Composable
fun MainAppContainer(viewModel: GameViewModel) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    if (!onboardingCompleted) {
        OnboardingScreen(onFinished = { viewModel.setOnboardingCompleted(true) })
        return
    }

    var currentTab by remember { mutableStateOf("Map") }
    val activeLevel by viewModel.activeLevel.collectAsState()

    if (activeLevel != null) {
        GameScreen(
            viewModel = viewModel,
            onNavigateBack = { viewModel.quitGameSession() }
        )
    } else {
        Scaffold(
            containerColor = MapBgMid,
            bottomBar = {
                PremiumBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                    },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        "Map" -> MapScreen(
                            viewModel = viewModel,
                            onNavigateToGame = { }
                        )
                        "Dictionary" -> DictionaryScreen(viewModel = viewModel)
                        "Review" -> ReviewScreen(viewModel = viewModel)
                        "Tasks" -> TasksScreen(viewModel = viewModel)
                        "Achievements" -> AchievementsScreen(viewModel = viewModel)
                        "Profile" -> ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(MapSurface)
            .border(
                width = 1.dp,
                color = MapCardBorder.copy(alpha = 0.45f),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navTabs.forEach { tab ->
                val selected = currentTab == tab.key
                val interaction = remember { MutableInteractionSource() }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = { onTabSelected(tab.key) }
                        )
                        .padding(vertical = 4.dp)
                        .testTag(tab.testTag)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 30.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) DinoMint.copy(alpha = 0.14f) else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = if (selected) DeepEmerald else MapInkMuted.copy(alpha = 0.75f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) DeepEmerald else MapInkMuted.copy(alpha = 0.72f),
                        letterSpacing = 0.1.sp
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
        )
    }
}
