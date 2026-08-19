package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WordEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.SkyBlue
import com.example.viewmodel.GameViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(viewModel: GameViewModel) {
    val wordsToReview by viewModel.wordsToReview.collectAsState()
    val allWords by viewModel.dictionary.collectAsState()

    var sessionWords by remember { mutableStateOf<List<WordEntity>>(emptyList()) }
    var currentWordIndex by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var wrongAnswers by remember { mutableIntStateOf(0) }
    val isQuizStarted = sessionWords.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kelime Tekrarı", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text(
                            "Zorlandığın kelimeleri sakin sakin tekrar et.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                !isQuizStarted -> ReviewStartView(
                    count = wordsToReview.size,
                    onStart = {
                        sessionWords = wordsToReview.shuffled()
                        currentWordIndex = 0
                        correctAnswers = 0
                        wrongAnswers = 0
                    }
                )
                currentWordIndex < sessionWords.size -> {
                    val currentWord = sessionWords[currentWordIndex]
                    val options = remember(currentWord.id, allWords) {
                        generateReviewOptions(currentWord, allWords)
                    }

                    QuizView(
                        word = currentWord,
                        options = options,
                        currentIndex = currentWordIndex,
                        totalCount = sessionWords.size,
                        onAnswerRecorded = { isCorrect ->
                            viewModel.updateWordReview(currentWord.id, isCorrect)
                            if (isCorrect) correctAnswers++ else wrongAnswers++
                        },
                        onNext = {
                            currentWordIndex++
                        }
                    )
                }
                else -> ReviewFinishedView(
                    correctAnswers = correctAnswers,
                    wrongAnswers = wrongAnswers,
                    onRestart = {
                        sessionWords = wordsToReview.shuffled()
                        currentWordIndex = 0
                        correctAnswers = 0
                        wrongAnswers = 0
                    },
                    onBack = {
                        sessionWords = emptyList()
                        currentWordIndex = 0
                    }
                )
            }
        }
    }
}

@Composable
fun ReviewStartView(count: Int, onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(76.dp), tint = GoldenAmber)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Tekrar Edilecek $count Kelime Var", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Önce Türkçe anlamı gör, sonra İngilizce kelimeyi seç. Cevabı gördükten sonra sıradaki kelimeye geçersin.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onStart,
            enabled = count > 0,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
        ) {
            Text("TEKRARA BAŞLA", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun QuizView(
    word: WordEntity,
    options: List<String>,
    currentIndex: Int,
    totalCount: Int,
    onAnswerRecorded: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    var selectedOption by remember(word.id) { mutableStateOf<String?>(null) }
    var submitted by remember(word.id) { mutableStateOf(false) }
    val isCorrect = selectedOption == word.english

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { ((currentIndex + 1).toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = EmeraldGreen,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("${currentIndex + 1} / $totalCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Bu kelimenin İngilizcesi nedir?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    word.turkish.uppercase(Locale("tr", "TR")),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                if (word.turkishDefinition.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldenAmber.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(word.turkishDefinition, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        options.forEach { option ->
            ReviewAnswerButton(
                option = option,
                correctAnswer = word.english,
                selected = selectedOption == option,
                submitted = submitted,
                onClick = { selectedOption = option }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = submitted) {
            Text(
                if (isCorrect) "Doğru! Güzel hatırladın." else "Yanlış. Doğru cevap: ${word.english.uppercase(Locale.ENGLISH)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCorrect) EmeraldGreen else ErrorRed,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (!submitted) {
                    submitted = true
                    onAnswerRecorded(isCorrect)
                } else {
                    onNext()
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
        ) {
            Text(if (submitted) "SONRAKİ KELİME" else "CEVABI KONTROL ET", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ReviewAnswerButton(
    option: String,
    correctAnswer: String,
    selected: Boolean,
    submitted: Boolean,
    onClick: () -> Unit
) {
    val isCorrectOption = option == correctAnswer
    val bg = when {
        submitted && isCorrectOption -> EmeraldGreen.copy(alpha = 0.16f)
        submitted && selected && !isCorrectOption -> ErrorRed.copy(alpha = 0.14f)
        selected -> SkyBlue.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }
    val border = when {
        submitted && isCorrectOption -> EmeraldGreen
        submitted && selected && !isCorrectOption -> ErrorRed
        selected -> SkyBlue
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(enabled = !submitted, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.3.dp, border),
        shape = RoundedCornerShape(13.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(option.uppercase(Locale.ENGLISH), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ReviewFinishedView(
    correctAnswers: Int,
    wrongAnswers: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = EmeraldGreen)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Tekrar Bitti!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "$correctAnswers doğru, $wrongAnswers tekrar edilecek kelime.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("TAMAM", fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text("TEKRAR ÇALIŞ", fontWeight = FontWeight.Bold)
        }
    }
}

fun generateReviewOptions(correctWord: WordEntity, allWords: List<WordEntity>): List<String> {
    val options = mutableSetOf(correctWord.english)
    allWords
        .filter { it.id != correctWord.id }
        .shuffled()
        .forEach { word ->
            if (options.size < 4) options.add(word.english)
        }
    return options.toList().shuffled()
}
