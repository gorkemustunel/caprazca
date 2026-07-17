package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: GameViewModel) {
    // Current bottom tab state
    var currentTab by remember { mutableStateOf("Map") } // "Map", "Dictionary", "Tasks", "Achievements", "Profile"

    // Active playing level
    val activeLevel by viewModel.activeLevel.collectAsState()

    // If an active level is loaded, show the crossword game screen full-bleed
    if (activeLevel != null) {
        GameScreen(
            viewModel = viewModel,
            onNavigateBack = {
                viewModel.quitGameSession()
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Tab 1: Map
                    NavigationBarItem(
                        selected = currentTab == "Map",
                        onClick = { currentTab = "Map" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "Map") Icons.Filled.Explore else Icons.Filled.Explore,
                                contentDescription = "Harita",
                                tint = if (currentTab == "Map") EmeraldGreen else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Harita",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == "Map") EmeraldGreen else Color.Gray
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_map")
                    )

                    // Tab 2: Dictionary
                    NavigationBarItem(
                        selected = currentTab == "Dictionary",
                        onClick = { currentTab = "Dictionary" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "Dictionary") Icons.Filled.MenuBook else Icons.Filled.MenuBook,
                                contentDescription = "Sözlük",
                                tint = if (currentTab == "Dictionary") EmeraldGreen else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Sözlük",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == "Dictionary") EmeraldGreen else Color.Gray
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_dictionary")
                    )

                    // Tab 3: Tasks
                    NavigationBarItem(
                        selected = currentTab == "Tasks",
                        onClick = { currentTab = "Tasks" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "Tasks") Icons.Filled.FactCheck else Icons.Filled.FactCheck,
                                contentDescription = "Görevler",
                                tint = if (currentTab == "Tasks") EmeraldGreen else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Görevler",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == "Tasks") EmeraldGreen else Color.Gray
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_tasks")
                    )

                    // Tab 4: Achievements
                    NavigationBarItem(
                        selected = currentTab == "Achievements",
                        onClick = { currentTab = "Achievements" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "Achievements") Icons.Filled.EmojiEvents else Icons.Filled.EmojiEvents,
                                contentDescription = "Başarımlar",
                                tint = if (currentTab == "Achievements") EmeraldGreen else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Başarımlar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == "Achievements") EmeraldGreen else Color.Gray
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_achievements")
                    )

                    // Tab 5: Profile
                    NavigationBarItem(
                        selected = currentTab == "Profile",
                        onClick = { currentTab = "Profile" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "Profile") Icons.Filled.Person else Icons.Filled.Person,
                                contentDescription = "Profil",
                                tint = if (currentTab == "Profile") EmeraldGreen else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Profil",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == "Profile") EmeraldGreen else Color.Gray
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Animated switch between tabs
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        "Map" -> MapScreen(
                            viewModel = viewModel,
                            onNavigateToGame = { levelId ->
                                // Triggered when a level starts
                            }
                        )
                        "Dictionary" -> DictionaryScreen(viewModel = viewModel)
                        "Tasks" -> TasksScreen(viewModel = viewModel)
                        "Achievements" -> AchievementsScreen(viewModel = viewModel)
                        "Profile" -> ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
