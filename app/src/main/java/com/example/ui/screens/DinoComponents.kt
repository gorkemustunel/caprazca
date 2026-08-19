package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.*
import com.example.data.repository.PuzzleResult
import com.example.ui.theme.*
import com.example.ui.util.cleanTurkishText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DinoReactionController(
    reactionEvent: SharedFlow<DinoReactionEvent>,
    stage: DinoStage,
    completedLevels: Int = 0,
    modifier: Modifier = Modifier
) {
    var mood by remember { mutableStateOf("Idle") }
    var speechText by remember { mutableStateOf<String?>(null) }
    var showStars by remember { mutableStateOf(false) }

    val correctMessages = listOf("Harika!", "Süpersin!", "Devam edelim!")
    val wrongMessages = listOf("Bir daha deneyelim!", "Çok yaklaştın!", "Bunu birlikte buluruz!")
    val streakMessages = mapOf(
        3 to "3 doğru üst üste!",
        5 to "Müthiş seri!",
        10 to "Durdurulamıyorsun!"
    )

    LaunchedEffect(reactionEvent) {
        reactionEvent.collectLatest { event ->
            when (event) {
                is DinoReactionEvent.StreakReached -> {
                    mood = "Happy"
                    speechText = streakMessages[event.streak] ?: "Harika seri!"
                    showStars = true
                    delay(1500)
                    showStars = false
                }
                is DinoReactionEvent.CorrectAnswer -> {
                    mood = "Happy"
                    speechText = correctMessages.random()
                    showStars = true
                    delay(1000)
                    showStars = false
                }
                is DinoReactionEvent.WrongAnswer -> {
                    mood = "Thinking"
                    speechText = wrongMessages.random()
                    delay(1200)
                }
                else -> { /* Level completion handled separately */ }
            }
            mood = "Idle"
            speechText = null
        }
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(16.dp)
        ) {
            if (speechText != null) {
                DinoSpeechBubble(text = speechText!!)
                Spacer(modifier = Modifier.width(8.dp))
            }

            DinoAnimatedCharacter(
                size = 64.dp,
                stage = stage,
                completedLevels = completedLevels,
                mood = mood
            )
        }

        if (showStars) {
            DinoCelebrationEffect(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun DinoEvolutionDialog(newStage: DinoStage, onDismiss: () -> Unit, completedLevels: Int = 0) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(3.dp, DinoMint)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dinozorun Gelişti!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DinoMint
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Animated character preview
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(DinoMint.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    DinoAnimatedCharacter(size = 110.dp, stage = newStage, completedLevels = completedLevels, mood = "Happy")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Artık bir ${newStage.displayName.cleanTurkishText()}!".cleanTurkishText(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = newStage.description.cleanTurkishText(),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DinoMint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("HARİKA!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DinoLevelCompletionDialog(
    level: PuzzleLevel,
    result: PuzzleResult,
    timeElapsed: Int,
    errorsCount: Int,
    hintsUsed: Int,
    currentXp: Int,
    stage: DinoStage,
    completedLevels: Int = 0,
    onBackToMap: () -> Unit,
    onReplay: () -> Unit,
    onNextLevel: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFB)),
            border = BorderStroke(1.5.dp, GoldenAmber.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bölüm Tamamlandı!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dino Reaction based on stars
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(DinoMint.copy(alpha = 0.18f), GoldenAmber.copy(alpha = 0.08f), Color.Transparent)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    DinoCelebrationEffect(modifier = Modifier.align(Alignment.Center))
                    DinoAnimatedCharacter(
                        size = 90.dp,
                        stage = stage,
                        completedLevels = completedLevels,
                        mood = if (result.starsEarned >= 2) "Happy" else "Idle"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stars
                Row {
                    repeat(3) { i ->
                        val starScale by animateFloatAsState(
                            targetValue = if (i < result.starsEarned) 1.12f else 0.92f,
                            animationSpec = tween(durationMillis = 260, delayMillis = i * 120, easing = FastOutSlowInEasing),
                            label = "completionStarScale"
                        )
                        Icon(
                            imageVector = if (i < result.starsEarned) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = null,
                            tint = if (i < result.starsEarned) GoldenAmber else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = starScale
                                    scaleY = starScale
                                }
                                .padding(2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CompletionStatItem("Süre", formatCompletionTime(timeElapsed))
                    CompletionStatItem("Hata", errorsCount.toString())
                    CompletionStatItem("İpucu", hintsUsed.toString())
                }

                Spacer(modifier = Modifier.height(24.dp))

                // XP & Progress
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kazanılan Tecrübe", fontSize = 13.sp, color = Color.Gray)
                            Text("+${result.xpEarned} XP", fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val nextMilestone = DinoEvolutionPlan.nextMilestone(completedLevels)
                        if (nextMilestone != null) {
                            val previousThreshold = DinoEvolutionPlan.milestones
                                .lastOrNull { it.completedLevels <= completedLevels }
                                ?.completedLevels ?: 0
                            val range = (nextMilestone.completedLevels - previousThreshold).coerceAtLeast(1)
                            val done = (completedLevels - previousThreshold).coerceIn(0, range)
                            val milestoneProgress = done.toFloat() / range.toFloat()
                            val levelsLeft = (nextMilestone.completedLevels - completedLevels).coerceAtLeast(0)

                            Text(
                                text = "Sıradaki değişim: ${nextMilestone.title.cleanTurkishText()}".cleanTurkishText(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = (if (levelsLeft == 1) "1 bölüm sonra yeni görünüm!" else "$levelsLeft bölüm sonra yeni görünüm.").cleanTurkishText(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DinoMint,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color.Gray.copy(0.1f), CircleShape)) {
                                Box(modifier = Modifier
                                    .fillMaxWidth(milestoneProgress)
                                    .fillMaxHeight()
                                    .background(DinoMint, CircleShape))
                            }
                        }

                        // Next Evolution progress
                        val nextStage = when (stage) {
                            DinoStage.EGG -> DinoStage.TINY_BABY
                            DinoStage.TINY_BABY -> DinoStage.GROWING_BABY
                            DinoStage.GROWING_BABY -> DinoStage.EXPLORER
                            DinoStage.EXPLORER -> DinoStage.SCHOLAR
                            DinoStage.SCHOLAR -> DinoStage.MASTER
                            else -> stage
                        }

                        val legacyNextStage = nextStage ?: stage
                        if (false) {
                            val reqXp = when(legacyNextStage) {
                                DinoStage.TINY_BABY -> 0
                                DinoStage.GROWING_BABY -> 0
                                DinoStage.EXPLORER -> 0
                                DinoStage.SCHOLAR -> 300
                                else -> 1000
                            }
                            val progress = (currentXp.toFloat() / reqXp.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

                            Text(
                                text = "${nextStage.displayName.cleanTurkishText()} Formuna Doğru".cleanTurkishText(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color.Gray.copy(0.1f), CircleShape)) {
                                Box(modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(DinoMint, CircleShape))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DinoMint),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SONRAKİ BÖLÜM", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onReplay,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, DinoMint)
                    ) {
                        Text("TEKRAR", color = DinoMint)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = onBackToMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color.Gray)
                    ) {
                        Text("HARİTA", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DinoSpeechBubble(text: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp),
        border = BorderStroke(1.dp, DinoMint.copy(alpha = 0.3f)),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = TextDark
        )
    }
}

@Composable
fun DinoCelebrationEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")

    repeat(5) { i ->
        val scale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, delayMillis = i * 100),
                repeatMode = RepeatMode.Restart
            ), label = "star_scale"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, delayMillis = i * 100),
                repeatMode = RepeatMode.Restart
            ), label = "star_alpha"
        )

        val angle = (i * 72).toFloat()
        val radius = 40.dp

        Box(
            modifier = modifier
                .offset(
                    x = (radius.value * Math.cos(Math.toRadians(angle.toDouble()))).dp,
                    y = (radius.value * Math.sin(Math.toRadians(angle.toDouble()))).dp
                )
                .scale(scale)
                .alpha(alpha)
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun DinoAnimatedCharacter(
    size: Dp,
    stage: DinoStage,
    completedLevels: Int = 0,
    mood: String
) {
    val transition = rememberInfiniteTransition(label = "dino_idle")
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (mood == "Happy") -15f else -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (mood == "Happy") 400 else 1000),
            repeatMode = RepeatMode.Reverse
        ), label = "bounce"
    )

    DinoCharacter(
        size = size,
        stage = stage,
        completedLevels = completedLevels,
        modifier = Modifier.offset(y = bounce.dp)
    )
}


@Composable
fun CompletionStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

fun formatCompletionTime(s: Int) = "%02d:%02d".format(s / 60, s % 60)

@Composable
fun DinoHatchingDialog(onNicknameChosen: (String) -> Unit) {
    var step by remember { mutableIntStateOf(0) } // 0: Egg, 1: Cracking, 2: Baby + Name
    var nickname by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition("egg_shake")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(200), RepeatMode.Reverse),
        label = "rotation"
    )

    Dialog(onDismissRequest = {}, properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (step == 0 || step == 1) {
                    Text("Biri mi geliyor?", fontSize = 22.sp, fontWeight = FontWeight.Black, color = DinoMint)
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = if (step == 1) rotation else 0f)) {
                            val w = size.width; val h = size.height
                            val p = Path().apply {
                                moveTo(w/2, 0f)
                                cubicTo(w*0.95f, 0f, w, h*0.65f, w/2, h)
                                cubicTo(0f, h*0.65f, w*0.05f, 0f, w/2, 0f)
                            }
                            drawPath(p, EggCream)
                            drawCircle(DinoMint.copy(0.3f), radius = w*0.18f, center = Offset(w*0.35f, h*0.45f))

                            if (step == 1) {
                                // Draw some cracks
                                val crackPath = Path().apply {
                                    moveTo(w * 0.4f, h * 0.2f); lineTo(w * 0.5f, h * 0.3f); lineTo(w * 0.45f, h * 0.4f)
                                    moveTo(w * 0.6f, h * 0.5f); lineTo(w * 0.7f, h * 0.6f); lineTo(w * 0.65f, h * 0.7f)
                                }
                                drawPath(crackPath, Color.Gray, style = Stroke(2.dp.toPx()))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { if (step == 0) step = 1 else step = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = DinoMint),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (step == 0) "TIK TIK..." else "ÇATLIYOR!", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Merhaba Dünya!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = DinoMint)
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.size(140.dp).background(DinoMint.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        DinoAnimatedCharacter(size = 110.dp, stage = DinoStage.TINY_BABY, mood = "Happy")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Dinozoruna bir isim ver:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { if (it.length <= 12) nickname = it },
                        placeholder = { Text("Mino") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )

                    Button(
                        onClick = { onNicknameChosen(nickname.ifBlank { "Mino" }) },
                        enabled = nickname.length >= 2 || nickname.isEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = DinoMint),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("MACERAYA BAŞLA!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DinoHeadAvatar(
    size: Dp,
    stage: DinoStage,
    completedLevels: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = dinoImageResForStage(stage)),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(if (stage == DinoStage.EGG) 0.95f else 1.55f)
                .offset(y = if (stage == DinoStage.EGG) 0.dp else size * 0.22f),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun DinoCharacter(
    size: Dp,
    stage: DinoStage,
    completedLevels: Int = 0,
    modifier: Modifier = Modifier,
    isFacingLeft: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dino_idle")

    // Breathing animation
    val bodyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "breath"
    )

    Image(
        painter = painterResource(id = dinoImageResForStage(stage)),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = bodyScale * if (isFacingLeft) -1f else 1f
                scaleY = bodyScale
            },
        contentScale = ContentScale.Fit
    )
    return

    // Tail flick
    val tailRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "tail_flick"
    )

    // Eye blink
    val blink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 4500; 1f at 0; 1f at 4300; 0f at 4400; 1f at 4500 },
            repeatMode = RepeatMode.Restart
        ), label = "blink"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()

        if (stage == DinoStage.EGG) {
            drawDinoEgg(w, h, cracked = false)
            return@Canvas
        }

        scale(
            scaleX = if (isFacingLeft) -1f else 1f,
            scaleY = 1f,
            pivot = Offset(w / 2, h / 2)
        ) {
            // A. Bottom Shadow
            drawOval(
                color = Color(0xFF173D35).copy(alpha = 0.1f),
                topLeft = Offset(w * 0.15f, h * 0.85f),
                size = Size(w * 0.7f, h * 0.1f)
            )

            // Physical modifiers
            val tailScale = when (stage) {
                DinoStage.TINY_BABY -> 0.7f
                DinoStage.GROWING_BABY -> 0.9f
                else -> 1.05f
            } + (completedLevels / 12f).coerceAtMost(0.25f)

            // B. Tail
            val tailPath = Path().apply {
                moveTo(w * 0.40f, h * 0.64f)
                cubicTo(w * 0.28f, h * 0.62f, w * 0.14f, h * 0.57f, w * 0.08f, h * (0.48f - (tailScale - 1f) * 0.06f))
                cubicTo(w * 0.13f, h * 0.68f, w * 0.30f, h * 0.75f, w * 0.48f, h * 0.70f)
                cubicTo(w * 0.45f, h * 0.68f, w * 0.42f, h * 0.66f, w * 0.40f, h * 0.64f)
                close()
            }
            rotate(tailRotation, pivot = Offset(w * 0.4f, h * 0.65f)) {
                drawPath(tailPath, DinoMint)
                drawTailProgressBands(w, h, completedLevels)
            }

            // C. Legs
            drawDinoLeg(w * 0.45f, h * 0.71f, w, h, isBack = true)
            drawDinoLeg(w * 0.60f, h * 0.73f, w, h, isBack = false)

            // D. Body & Neck
            val bodyPath = Path().apply {
                moveTo(w * 0.36f, h * 0.72f)
                cubicTo(w * 0.31f, h * 0.58f, w * 0.39f, h * 0.41f, w * 0.56f, h * 0.35f)
                cubicTo(w * 0.75f, h * 0.35f, w * 0.86f, h * 0.49f, w * 0.83f, h * 0.65f)
                cubicTo(w * 0.79f, h * 0.82f, w * 0.58f, h * 0.86f, w * 0.43f, h * 0.79f)
                cubicTo(w * 0.40f, h * 0.77f, w * 0.38f, h * 0.75f, w * 0.36f, h * 0.72f)
                close()
            }
            scale(bodyScale, pivot = Offset(w * 0.5f, h * 0.75f)) {
                drawPath(bodyPath, DinoMint)

                // Belly
                val bellyPath = Path().apply {
                    moveTo(w * 0.66f, h * 0.42f)
                    cubicTo(w * 0.78f, h * 0.49f, w * 0.76f, h * 0.72f, w * 0.62f, h * 0.77f)
                    cubicTo(w * 0.54f, h * 0.68f, w * 0.56f, h * 0.50f, w * 0.66f, h * 0.42f)
                    close()
                }
                drawPath(bellyPath, DinoBelly)
            }

            // E. Arms
            drawDinoArm(w * 0.7f, h * 0.55f, w, h, bodyScale)

            // F. Spikes
            drawDinoSpikes(w, h, stage, tailRotation)

            // G. Head
            drawDinoHead(
                w = w,
                h = h,
                stage = stage,
                completedLevels = completedLevels,
                blink = blink,
                headBreathOffset = (bodyScale - 1f) * 50f
            )

            // H. Accessories
            drawDinoAccessories(w, h, stage, bodyScale, completedLevels)
        }
    }
}

private fun dinoImageResForStage(stage: DinoStage): Int {
    return when (stage) {
        DinoStage.EGG -> R.drawable.dino_egg
        DinoStage.TINY_BABY,
        DinoStage.GROWING_BABY -> R.drawable.dino_baby
        DinoStage.EXPLORER -> R.drawable.dino_explorer
        DinoStage.SCHOLAR -> R.drawable.dino_scholar
        DinoStage.MASTER -> R.drawable.dino_master
    }
}

private fun DrawScope.drawDinoHead(
    w: Float,
    h: Float,
    stage: DinoStage,
    completedLevels: Int,
    blink: Float,
    headBreathOffset: Float
) {
    val headScale = when (stage) {
        DinoStage.EGG -> 1f
        DinoStage.TINY_BABY -> 1.25f
        DinoStage.GROWING_BABY -> 1.15f
        else -> 1.05f
    } + (completedLevels / 50f).coerceAtMost(0.08f)

    val headX = w * 0.66f
    val headY = h * 0.23f - headBreathOffset

    scale(headScale, pivot = Offset(headX, headY + h * 0.1f)) {
        val headPath = Path().apply {
            moveTo(headX - w * 0.14f, headY + h * 0.02f)
            cubicTo(headX - w * 0.21f, headY + h * 0.11f, headX - w * 0.16f, headY + h * 0.25f, headX - w * 0.02f, headY + h * 0.28f)
            cubicTo(headX + w * 0.13f, headY + h * 0.32f, headX + w * 0.31f, headY + h * 0.24f, headX + w * 0.32f, headY + h * 0.12f)
            cubicTo(headX + w * 0.34f, headY - h * 0.01f, headX + w * 0.18f, headY - h * 0.08f, headX + w * 0.01f, headY - h * 0.07f)
            cubicTo(headX - w * 0.06f, headY - h * 0.06f, headX - w * 0.11f, headY - h * 0.03f, headX - w * 0.14f, headY + h * 0.02f)
            close()
        }
        drawPath(headPath, DinoMint)
        drawPath(headPath, Color(0xFF0F9E87).copy(alpha = 0.18f), style = Stroke(width = 1.2.dp.toPx()))

        val snoutPath = Path().apply {
            moveTo(headX + w * 0.10f, headY + h * 0.12f)
            cubicTo(headX + w * 0.20f, headY + h * 0.09f, headX + w * 0.29f, headY + h * 0.13f, headX + w * 0.28f, headY + h * 0.20f)
            cubicTo(headX + w * 0.26f, headY + h * 0.27f, headX + w * 0.13f, headY + h * 0.27f, headX + w * 0.08f, headY + h * 0.21f)
            cubicTo(headX + w * 0.05f, headY + h * 0.17f, headX + w * 0.06f, headY + h * 0.14f, headX + w * 0.10f, headY + h * 0.12f)
            close()
        }
        drawPath(snoutPath, DinoBelly.copy(alpha = 0.50f))

        val eyeRadius = w * 0.062f
        val eyeX = headX + w * 0.09f
        val eyeY = headY + h * 0.04f

        drawCircle(Color.White, radius = eyeRadius, center = Offset(eyeX, eyeY))
        val pupilRadius = (eyeRadius * 0.6f) * (if (blink > 0.5f) 1f else 0.1f)
        drawCircle(Color(0xFF173D35), radius = pupilRadius, center = Offset(eyeX + pupilRadius * 0.2f, eyeY))
        if (blink > 0.5f) {
            drawCircle(Color.White, radius = pupilRadius * 0.3f, center = Offset(eyeX + pupilRadius * 0.4f, eyeY - pupilRadius * 0.4f))
        }

        drawCircle(Color(0xFF0C7D6E).copy(alpha = 0.70f), radius = 1.7.dp.toPx(), center = Offset(headX + w * 0.24f, headY + h * 0.16f))
        drawCircle(Color(0xFF0C7D6E).copy(alpha = 0.48f), radius = 1.2.dp.toPx(), center = Offset(headX + w * 0.19f, headY + h * 0.18f))

        val mouthPath = Path().apply {
            moveTo(headX + w * 0.14f, headY + h * 0.21f)
            quadraticTo(headX + w * 0.21f, headY + h * 0.24f, headX + w * 0.27f, headY + h * 0.19f)
        }
        drawPath(mouthPath, Color(0xFF0C7D6E), style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawDinoLeg(lx: Float, ly: Float, w: Float, h: Float, isBack: Boolean) {
    val legWidth = w * 0.105f
    val legHeight = h * 0.14f
    val color = if (isBack) Color(0xFF11A983) else DinoMint
    val legPath = Path().apply {
        moveTo(lx - legWidth/2, ly)
        lineTo(lx + legWidth/2, ly)
        quadraticTo(lx + legWidth * 0.55f, ly + legHeight * 0.70f, lx + legWidth * 0.90f, ly + legHeight)
        lineTo(lx - legWidth * 0.35f, ly + legHeight)
        quadraticTo(lx - legWidth * 0.52f, ly + legHeight * 0.45f, lx - legWidth/2, ly)
        close()
    }
    drawPath(legPath, color)
    drawLine(
        color = Color(0xFF0F8F78).copy(alpha = 0.45f),
        start = Offset(lx + legWidth * 0.10f, ly + legHeight * 0.76f),
        end = Offset(lx + legWidth * 0.62f, ly + legHeight * 0.96f),
        strokeWidth = 1.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDinoArm(ax: Float, ay: Float, w: Float, h: Float, bodyScale: Float) {
    val armWidth = w * 0.075f
    val armHeight = h * 0.055f
    withTransform({
        rotate(-18f * bodyScale, pivot = Offset(ax, ay))
    }) {
        val armPath = Path().apply {
            moveTo(ax, ay)
            quadraticTo(ax + armWidth * 1.25f, ay + armHeight * 0.80f, ax + armWidth * 0.90f, ay + armHeight * 1.85f)
            quadraticTo(ax + armWidth * 0.25f, ay + armHeight * 1.60f, ax, ay + armHeight * 1.15f)
            close()
        }
        drawPath(armPath, DinoMint)
    }
}

private fun DrawScope.drawDinoSpikes(w: Float, h: Float, stage: DinoStage, tailRotation: Float) {
    val spikeCount = when (stage) {
        DinoStage.EGG -> 0
        DinoStage.TINY_BABY -> 3
        DinoStage.GROWING_BABY -> 4
        else -> 5
    }
    if (spikeCount == 0) return
    val spikeSize = when (stage) {
        DinoStage.TINY_BABY -> w * 0.03f
        else -> w * 0.05f
    }
    val points = listOf(
        Offset(w * 0.22f, h * 0.60f),
        Offset(w * 0.36f, h * 0.56f),
        Offset(w * 0.47f, h * 0.43f),
        Offset(w * 0.54f, h * 0.34f),
        Offset(w * 0.60f, h * 0.29f)
    )
    for (i in 0 until spikeCount) {
        val p = points[i]
        withTransform({
            if (i < 2) rotate(tailRotation, pivot = Offset(w * 0.4f, h * 0.65f))
        }) {
            val sPath = Path().apply {
                moveTo(p.x, p.y)
                lineTo(p.x - spikeSize * 0.72f, p.y - spikeSize * 1.08f)
                lineTo(p.x + spikeSize * 0.52f, p.y - spikeSize * 0.24f)
                close()
            }
            drawPath(sPath, DinoSpike)
        }
    }
}

private fun DrawScope.drawTailProgressBands(w: Float, h: Float, completedLevels: Int) {
    if (completedLevels < 40) return
    val bandCount = ((completedLevels - 35) / 5).coerceIn(1, 5)
    repeat(bandCount) { index ->
        drawRoundRect(
            color = DinoSpike.copy(alpha = 0.65f),
            topLeft = Offset(w * (0.17f + index * 0.045f), h * (0.56f - index * 0.012f)),
            size = Size(w * 0.035f, h * 0.095f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
        )
    }
}

private fun DrawScope.drawDinoEgg(w: Float, h: Float, cracked: Boolean) {
    drawOval(
        color = Color(0xFF173D35).copy(alpha = 0.1f),
        topLeft = Offset(w * 0.2f, h * 0.82f),
        size = Size(w * 0.6f, h * 0.1f)
    )

    val eggPath = Path().apply {
        moveTo(w * 0.5f, h * 0.12f)
        cubicTo(w * 0.86f, h * 0.14f, w * 0.92f, h * 0.64f, w * 0.62f, h * 0.84f)
        cubicTo(w * 0.55f, h * 0.89f, w * 0.45f, h * 0.89f, w * 0.38f, h * 0.84f)
        cubicTo(w * 0.08f, h * 0.64f, w * 0.14f, h * 0.14f, w * 0.5f, h * 0.12f)
        close()
    }
    drawPath(eggPath, EggCream)
    drawPath(eggPath, DinoMint.copy(alpha = 0.22f), style = Stroke(width = 2.dp.toPx()))
    drawCircle(DinoMint.copy(alpha = 0.32f), radius = w * 0.13f, center = Offset(w * 0.36f, h * 0.48f))
    drawCircle(GoldenAmber.copy(alpha = 0.25f), radius = w * 0.08f, center = Offset(w * 0.62f, h * 0.62f))
    drawCircle(Color.White.copy(alpha = 0.6f), radius = w * 0.06f, center = Offset(w * 0.42f, h * 0.28f))

    if (cracked) {
        val crackPath = Path().apply {
            moveTo(w * 0.48f, h * 0.18f)
            lineTo(w * 0.55f, h * 0.29f)
            lineTo(w * 0.49f, h * 0.38f)
            lineTo(w * 0.58f, h * 0.5f)
            moveTo(w * 0.42f, h * 0.53f)
            lineTo(w * 0.34f, h * 0.62f)
            lineTo(w * 0.41f, h * 0.7f)
        }
        drawPath(crackPath, Color(0xFF6B7280), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun DinoMilestoneDialog(milestone: DinoMilestone, stage: DinoStage, completedLevels: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(3.dp, DinoMint)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = milestone.title.cleanTurkishText(),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = DinoMint,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(DinoMint.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    DinoAnimatedCharacter(size = 118.dp, stage = stage, completedLevels = completedLevels, mood = "Happy")
                    DinoCelebrationEffect(modifier = Modifier.align(Alignment.Center))
                }
                Spacer(modifier = Modifier.height(18.dp))
                milestone.accessory?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text(it.displayName.cleanTurkishText(), fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = GoldenAmber.copy(alpha = 0.16f)),
                        border = BorderStroke(1.dp, GoldenAmber.copy(alpha = 0.35f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Text(
                    text = milestone.description.cleanTurkishText(),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(26.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DinoMint),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DEVAM", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun DrawScope.drawDinoAccessories(w: Float, h: Float, stage: DinoStage, bodyScale: Float, completedLevels: Int) {
    if (stage == DinoStage.GROWING_BABY || completedLevels >= 7) {
        drawRoundRect(
            color = GoldenAmber.copy(alpha = 0.78f),
            topLeft = Offset(w * 0.58f, h * 0.46f),
            size = Size(w * 0.14f, h * 0.035f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
        )
        drawCircle(GoldenAmber, radius = w * 0.018f, center = Offset(w * 0.71f, h * 0.477f))
    }

    if (stage == DinoStage.EXPLORER || stage == DinoStage.SCHOLAR || stage == DinoStage.MASTER || completedLevels >= 10) {
        drawRoundRect(
            color = Color(0xFF795548),
            topLeft = Offset(w * 0.30f, h * 0.48f),
            size = Size(w * 0.14f, h * 0.20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(w * 0.42f, h * 0.52f),
            size = Size(w * 0.05f, h * 0.025f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
        drawCircle(GoldenAmber.copy(alpha = 0.88f), radius = w * 0.018f, center = Offset(w * 0.37f, h * 0.57f))
    }
    if (completedLevels >= 14) {
        val hatPath = Path().apply {
            moveTo(w * 0.56f, h * 0.15f)
            lineTo(w * 0.80f, h * 0.13f)
            lineTo(w * 0.77f, h * 0.18f)
            lineTo(w * 0.58f, h * 0.20f)
            close()
        }
        drawPath(hatPath, Color(0xFF6D4C41))
        drawRoundRect(
            color = Color(0xFF4E342E),
            topLeft = Offset(w * 0.54f, h * 0.19f),
            size = Size(w * 0.31f, h * 0.028f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
        )
    }
    if (completedLevels >= 18) {
        drawCircle(Color(0xFFFFC857), radius = w * 0.035f, center = Offset(w * 0.52f, h * 0.58f))
        drawCircle(Color.White.copy(alpha = 0.45f), radius = w * 0.018f, center = Offset(w * 0.51f, h * 0.57f))
        drawLine(Color(0xFF5D4037), Offset(w * 0.5f, h * 0.54f), Offset(w * 0.52f, h * 0.58f), strokeWidth = 1.5.dp.toPx())
    }
    if (stage == DinoStage.SCHOLAR || stage == DinoStage.MASTER || completedLevels >= 22) {
        val headX = w * 0.65f
        val headY = h * 0.22f - (bodyScale - 1f) * 50f
        drawCircle(
            color = Color.Black,
            radius = w * 0.08f,
            center = Offset(headX + w * 0.12f, headY + h * 0.02f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawRect(Color(0xFFE91E63), Offset(w * 0.62f, h * 0.65f), Size(w * 0.18f, h * 0.15f))
        drawRect(Color.White, Offset(w * 0.64f, h * 0.67f), Size(w * 0.14f, h * 0.11f))
    }
    if (completedLevels >= 26) {
        val scarfPath = Path().apply {
            moveTo(w * 0.56f, h * 0.42f)
            lineTo(w * 0.78f, h * 0.45f)
            lineTo(w * 0.76f, h * 0.51f)
            lineTo(w * 0.55f, h * 0.47f)
            close()
        }
        drawPath(scarfPath, Color(0xFFE53935).copy(alpha = 0.88f))
    }
    if (completedLevels >= 30) {
        drawCircle(Color(0xFFFFD700), radius = w * 0.032f, center = Offset(w * 0.68f, h * 0.56f))
        drawCircle(Color.White.copy(alpha = 0.5f), radius = w * 0.012f, center = Offset(w * 0.67f, h * 0.55f))
    }
    if (stage == DinoStage.MASTER || completedLevels >= 35) {
        drawCircle(Color(0xFFFFD700), radius = w * 0.04f, center = Offset(w * 0.55f, h * 0.5f))
        drawCircle(Color.White.copy(0.5f), radius = w * 0.015f, center = Offset(w * 0.54f, h * 0.49f))
        val capePath = Path().apply {
            moveTo(w * 0.5f, h * 0.45f)
            lineTo(w * 0.25f, h * 0.55f)
            cubicTo(w * 0.2f, h * 0.7f, w * 0.35f, h * 0.85f, w * 0.45f, h * 0.75f)
            close()
        }
        drawPath(capePath, Color(0xFFFFD700).copy(alpha = 0.4f))
    }
}

@Preview(showBackground = true)
@Composable
fun DinoStagesPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            DinoCharacter(size = 80.dp, stage = DinoStage.EGG)
            DinoCharacter(size = 80.dp, stage = DinoStage.TINY_BABY)
            DinoCharacter(size = 80.dp, stage = DinoStage.GROWING_BABY)
            DinoCharacter(size = 80.dp, stage = DinoStage.EXPLORER)
        }
        Row {
            DinoCharacter(size = 80.dp, stage = DinoStage.SCHOLAR)
            DinoCharacter(size = 80.dp, stage = DinoStage.MASTER)
            DinoCharacter(size = 80.dp, stage = DinoStage.MASTER, isFacingLeft = true)
        }
    }
}
