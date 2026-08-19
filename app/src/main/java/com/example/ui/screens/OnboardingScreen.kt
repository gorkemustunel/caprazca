package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DinoStage
import com.example.data.repository.EconomyConfig
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.SkyBlue
import com.example.ui.util.cleanTurkishText

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val completedSteps = remember { mutableStateListOf<Int>() }
    val canContinue = true
    val titles = listOf("Kelime Seç", "Harf Gir", "İpucu Al", "Dino Büyür")
    val subtitles = listOf(
        "Önce çözeceğin kelimeyi seçersin.",
        "Klavye ile İngilizce cevabı yazarsın.",
        "Takılırsan harf ya da kelime ipucu alırsın.",
        "Bulmaca çözdükçe dino yumurtadan çıkar ve büyür."
    )

    fun completeCurrentStep() {
        if (step !in completedSteps) completedSteps.add(step)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "Çaprazca'ya Hoş Geldin".cleanTurkishText(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            "30 saniyelik mini öğreticiyle oyunun kalbini görelim.".cleanTurkishText(),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
        OnboardingProgress(step = step, completedSteps = completedSteps.toSet())

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    titles[step].cleanTurkishText(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    subtitles[step].cleanTurkishText(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                when (step) {
                    0 -> SelectWordTutorial(onCompleted = ::completeCurrentStep)
                    1 -> TypeLetterTutorial(onCompleted = ::completeCurrentStep)
                    2 -> HintTutorial(onCompleted = ::completeCurrentStep)
                    else -> DinoGrowthTutorial(onCompleted = ::completeCurrentStep)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (step > 0) step -= 1 },
                enabled = step > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("GERİ".cleanTurkishText(), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    completeCurrentStep()
                    if (step < titles.lastIndex) step += 1 else onFinished()
                },
                enabled = canContinue,
                modifier = Modifier
                    .weight(1.7f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text((if (step == titles.lastIndex) "MACERAYA BAŞLA" else "SONRAKİ").cleanTurkishText(), fontWeight = FontWeight.ExtraBold)
            }
        }
        Text(
            (if (step in completedSteps) "Harika, sıradaki adıma geçebilirsin." else "İstersen dene, istersen Sonraki ile geç.").cleanTurkishText(),
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 11.sp,
            color = if (step in completedSteps) EmeraldGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnboardingProgress(step: Int, completedSteps: Set<Int>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(4) { index ->
            val active = index == step
            val done = index in completedSteps
            val color by animateColorAsState(
                targetValue = when {
                    active -> EmeraldGreen
                    done -> SkyBlue
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                label = "onboardingProgress"
            )
            Box(
                modifier = Modifier
                    .width(if (active) 34.dp else 14.dp)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun SelectWordTutorial(onCompleted: () -> Unit) {
    var selected by remember { mutableStateOf(false) }
    TutorialStage {
        MiniBoard(selectedWord = selected, typedLetters = if (selected) listOf("H", "") else listOf("", ""))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            (if (selected) "Kelime seçildi: HELLO" else "Yeşil ipucu kartına dokun.").cleanTurkishText(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) EmeraldGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        TutorialActionCard(
            title = "1. Yatay",
            body = "İpucu: Merhaba".cleanTurkishText(),
            color = EmeraldGreen,
            selected = selected,
            icon = Icons.Filled.TouchApp,
            onClick = {
                selected = true
                onCompleted()
            }
        )
    }
}

@Composable
private fun TypeLetterTutorial(onCompleted: () -> Unit) {
    var typed by remember { mutableStateOf("") }
    val answer = "HI"
    TutorialStage {
        MiniBoard(selectedWord = true, typedLetters = listOf(typed.getOrNull(0)?.toString() ?: "", typed.getOrNull(1)?.toString() ?: ""))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            (if (typed == answer) "Doğru kelime tamamlandı." else "Cevabı yaz: HI").cleanTurkishText(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (typed == answer) EmeraldGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("H", "I").forEach { letter ->
                KeyboardKey(letter) {
                    if (typed.length < 2) {
                        typed += letter
                        if (typed + letter == answer) onCompleted()
                    }
                }
            }
            KeyboardKey("SİL") {
                typed = typed.dropLast(1)
            }
        }
    }
}

@Composable
private fun HintTutorial(onCompleted: () -> Unit) {
    var usedHint by remember { mutableStateOf(false) }
    TutorialStage {
        MiniBoard(selectedWord = true, typedLetters = if (usedHint) listOf("P", "") else listOf("", ""))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            (if (usedHint) "İpucu bir harfi açtı. Jetonlarını dikkatli kullan." else "Takılırsan ipucu kullanabilirsin.").cleanTurkishText(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (usedHint) EmeraldGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TutorialActionCard(
                title = "Harf Al",
                body = "${EconomyConfig.LETTER_HINT_COST} jeton",
                color = EmeraldGreen,
                selected = usedHint,
                icon = Icons.Filled.Lightbulb,
                modifier = Modifier.weight(1f),
                onClick = {
                    usedHint = true
                    onCompleted()
                }
            )
            TutorialActionCard(
                title = "Kelime Çöz".cleanTurkishText(),
                body = "${EconomyConfig.WORD_HINT_COST} jeton",
                color = GoldenAmber,
                selected = false,
                icon = Icons.Filled.Key,
                modifier = Modifier.weight(1f),
                onClick = {
                    usedHint = true
                    onCompleted()
                }
            )
        }
    }
}

@Composable
private fun DinoGrowthTutorial(onCompleted: () -> Unit) {
    var grown by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (grown) 1.12f else 1f, label = "dinoGrowth")
    TutorialStage {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            DinoCharacter(
                size = 150.dp,
                stage = if (grown) DinoStage.TINY_BABY else DinoStage.EGG,
                completedLevels = if (grown) 1 else 0,
                modifier = Modifier.scale(scale)
            )
        }
        Text(
            (if (grown) "İlk çözümden sonra yumurta çatladı." else "Bulmaca bitince dino gelişir.").cleanTurkishText(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (grown) EmeraldGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                grown = true
                onCompleted()
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("BULMACAYI BİTİR".cleanTurkishText(), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun TutorialStage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}

@Composable
private fun MiniBoard(selectedWord: Boolean, typedLetters: List<String>) {
    val cells = listOf(
        listOf("#", "1", "1", "#"),
        listOf("#", "2", "#", "#"),
        listOf("#", "2", "#", "#")
    )
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        cells.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEachIndexed { colIndex, value ->
                    val isBlock = value == "#"
                    val isSelected = selectedWord && rowIndex == 0 && colIndex in 1..2
                    val text = when {
                        rowIndex == 0 && colIndex == 1 -> typedLetters.getOrNull(0).orEmpty()
                        rowIndex == 0 && colIndex == 2 -> typedLetters.getOrNull(1).orEmpty()
                        else -> ""
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isBlock -> Color(0xFF232323)
                                    isSelected -> EmeraldGreen.copy(alpha = 0.16f)
                                    else -> Color.White
                                }
                            )
                            .border(
                                1.3.dp,
                                if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialActionCard(
    title: String,
    body: String,
    color: Color,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (selected) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        label = "tutorialAction"
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.3.dp, if (selected) color else MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Text(body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
            }
            AnimatedVisibility(selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = EmeraldGreen)
            }
        }
    }
}

@Composable
private fun KeyboardKey(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = if (label.cleanTurkishText() == "SİL") 72.dp else 58.dp, height = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (label.cleanTurkishText() == "SİL") Color(0xFFFFE5E9) else MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label.cleanTurkishText(),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (label.cleanTurkishText() == "SİL") Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurface
        )
    }
}
