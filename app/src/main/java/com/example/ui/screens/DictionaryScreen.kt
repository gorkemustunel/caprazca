package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WordEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.SkyBlue
import com.example.viewmodel.GameViewModel
import java.util.Locale

private enum class DictionaryMode(val label: String) {
    LIST("Sözlük"),
    MULTIPLE_CHOICE("Çoktan Seçmeli"),
    MATCHING("Eşleştirme"),
    WRONGS("Yanlışlar"),
    FAVORITES("Favoriler")
}

@Composable
fun DictionaryScreen(viewModel: GameViewModel) {
    val words by viewModel.dictionary.collectAsState()
    val wordsToReview by viewModel.wordsToReview.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var mode by remember { mutableStateOf(DictionaryMode.LIST) }
    var activeReviewWord by remember { mutableStateOf<WordEntity?>(null) }
    var expandedWordId by remember { mutableStateOf<String?>(null) }

    val filteredWords = remember(words, searchQuery, selectedFilter) {
        words.filter { word ->
            val matchesFilter = when (selectedFilter) {
                "Favoriler" -> word.isFavorite
                "Öğrendiklerim" -> word.isLearned
                "A1" -> word.cefrLevel == "A1"
                "A2" -> word.cefrLevel == "A2"
                "B1" -> word.cefrLevel == "B1"
                else -> true
            }
            val normalizedQuery = normalizeTurkish(searchQuery)
            val matchesSearch = normalizedQuery.isEmpty() ||
                normalizeTurkish(word.english).contains(normalizedQuery) ||
                normalizeTurkish(word.turkish).contains(normalizedQuery)
            matchesFilter && matchesSearch
        }
    }

    val practicePool = remember(mode, filteredWords, words, wordsToReview) {
        when (mode) {
            DictionaryMode.WRONGS -> {
                val wrongWords = words.filter { it.timesWrong > 0 && it.timesWrong >= it.timesCorrect }
                if (wrongWords.isNotEmpty()) wrongWords else wordsToReview
            }
            DictionaryMode.FAVORITES -> words.filter { it.isFavorite }
            else -> filteredWords
        }
    }

    Scaffold(
        topBar = {
            DictionaryTopBar(
                searchQuery = searchQuery,
                selectedFilter = selectedFilter,
                mode = mode,
                onSearchChange = viewModel::updateSearchQuery,
                onFilterChange = viewModel::updateSelectedFilter,
                onModeChange = { mode = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (mode) {
                DictionaryMode.LIST -> DictionaryWordList(
                    words = filteredWords,
                    expandedWordId = expandedWordId,
                    onExpandedWordChange = { expandedWordId = it },
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onLearnedToggle = viewModel::markWordLearned,
                    onReviewClick = { activeReviewWord = it }
                )
                DictionaryMode.MATCHING -> MatchingPractice(
                    words = practicePool,
                    onAnswer = { word, correct -> viewModel.updateWordReview(word.id, correct) }
                )
                else -> MultipleChoicePractice(
                    mode = mode,
                    words = practicePool,
                    allWords = words,
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onAnswer = { word, correct -> viewModel.updateWordReview(word.id, correct) }
                )
            }
        }
    }

    activeReviewWord?.let { word ->
        val pool = remember(word, words) { buildTurkishOptions(word, words).mapNotNull { option -> words.firstOrNull { it.turkish == option } } }
        VocabularyReviewDialog(
            word = word,
            optionsPool = if (pool.any { it.id == word.id }) pool else listOf(word),
            onDismiss = { activeReviewWord = null },
            onReviewSuccess = {
                viewModel.updateWordReview(word.id, true)
                activeReviewWord = null
            },
            onReviewFailure = {
                viewModel.updateWordReview(word.id, false)
                activeReviewWord = null
            }
        )
    }
}

@Composable
private fun DictionaryTopBar(
    searchQuery: String,
    selectedFilter: String,
    mode: DictionaryMode,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onModeChange: (DictionaryMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("Öğrenme Alanı", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "Kelimeleri incele, test çöz, eşleştir ve zorlandıklarını tekrar et.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Kelime ara (Türkçe veya İngilizce)...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = EmeraldGreen) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Temizle")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("dictionary_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        ChipRow(
            items = listOf("Tümü", "Favoriler", "Öğrendiklerim", "A1", "A2", "B1"),
            selected = selectedFilter,
            onSelected = onFilterChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DictionaryMode.entries.forEach { item ->
                ModeChip(
                    label = item.label,
                    selected = mode == item,
                    onClick = { onModeChange(item) }
                )
            }
        }
    }
}

@Composable
private fun ChipRow(items: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { filter ->
            ModeChip(label = filter, selected = selected == filter, onClick = { onSelected(filter) })
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (selected) EmeraldGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        label = "chipBg"
    )
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun DictionaryWordList(
    words: List<WordEntity>,
    expandedWordId: String?,
    onExpandedWordChange: (String?) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onLearnedToggle: (String) -> Unit,
    onReviewClick: (WordEntity) -> Unit
) {
    if (words.isEmpty()) {
        EmptyLearningState(
            title = "Kelime bulunamadı",
            message = "Arama kriterlerini değiştir ya da yeni bulmacalar çözerek sözlüğünü büyüt."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(words) { word ->
            val isExpanded = expandedWordId == word.id
            WordCardItem(
                word = word,
                isExpanded = isExpanded,
                onToggleExpand = { onExpandedWordChange(if (isExpanded) null else word.id) },
                onFavoriteToggle = { onFavoriteToggle(word.id) },
                onLearnedToggle = { onLearnedToggle(word.id) },
                onReviewClick = { onReviewClick(word) }
            )
        }
    }
}

@Composable
private fun MultipleChoicePractice(
    mode: DictionaryMode,
    words: List<WordEntity>,
    allWords: List<WordEntity>,
    onFavoriteToggle: (String) -> Unit,
    onAnswer: (WordEntity, Boolean) -> Unit
) {
    if (words.size < 2 || allWords.size < 4) {
        EmptyLearningState(
            title = when (mode) {
                DictionaryMode.FAVORITES -> "Favori kelime az"
                DictionaryMode.WRONGS -> "Tekrar edilecek yanlış yok"
                else -> "Quiz için kelime az"
            },
            message = when (mode) {
                DictionaryMode.FAVORITES -> "Favorilere birkaç kelime ekleyince buradan özel quiz başlayacak."
                DictionaryMode.WRONGS -> "Yanlış yaptığın kelimeler burada otomatik toplanacak."
                else -> "Yeni bölümler çözdükçe daha güçlü quizler açılacak."
            }
        )
        return
    }

    var currentWordId by remember(mode) { mutableStateOf(words.shuffled().first().id) }
    val currentWord = words.firstOrNull { it.id == currentWordId }
        ?: allWords.firstOrNull { it.id == currentWordId }
        ?: words.first()
    LaunchedEffect(mode, words.map { it.id }) {
        if (words.none { it.id == currentWordId }) {
            currentWordId = words.shuffled().first().id
        }
    }
    var selectedAnswer by remember(currentWordId) { mutableStateOf<String?>(null) }
    var submitted by remember(currentWordId) { mutableStateOf(false) }
    var correctCount by remember(mode) { mutableIntStateOf(0) }
    var totalCount by remember(mode) { mutableIntStateOf(0) }
    val options = remember(currentWord, allWords) { buildTurkishOptions(currentWord, allWords) }
    val isCorrect = selectedAnswer == currentWord.turkish

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PracticeHeader(
                title = mode.label,
                subtitle = when (mode) {
                    DictionaryMode.WRONGS -> "Yanlış yaptıklarını tekrar yakala."
                    DictionaryMode.FAVORITES -> "Yıldızladığın kelimelerden mini test."
                    else -> "İngilizce kelimenin Türkçe anlamını seç."
                },
                scoreText = "$correctCount / $totalCount"
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            currentWord.english.uppercase(Locale.getDefault()),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { onFavoriteToggle(currentWord.id) }) {
                            Icon(
                                imageVector = if (currentWord.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favori",
                                tint = if (currentWord.isFavorite) GoldenAmber else Color.Gray
                            )
                        }
                    }
                    Text(currentWord.phonetic, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                    Spacer(modifier = Modifier.height(16.dp))
                    options.forEach { option ->
                        AnswerOption(
                            text = option,
                            selected = selectedAnswer == option,
                            correct = option == currentWord.turkish,
                            submitted = submitted,
                            onClick = { selectedAnswer = option }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    AnimatedVisibility(visible = submitted) {
                        Text(
                            text = if (isCorrect) "Doğru! Hafızaya bir çentik daha." else "Yanlış. Doğrusu: ${currentWord.turkish}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) EmeraldGreen else ErrorRed,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (!submitted) {
                                submitted = true
                                totalCount += 1
                                if (isCorrect) correctCount += 1
                                onAnswer(currentWord, isCorrect)
                            } else {
                                currentWordId = words.filter { it.id != currentWord.id }.ifEmpty { words }.shuffled().first().id
                            }
                        },
                        enabled = selectedAnswer != null,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(if (submitted) "SONRAKİ KELİME" else "KONTROL ET", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchingPractice(
    words: List<WordEntity>,
    onAnswer: (WordEntity, Boolean) -> Unit
) {
    if (words.size < 4) {
        EmptyLearningState(
            title = "Eşleştirme için kelime az",
            message = "En az 4 kelime olduğunda kart eşleştirme oyunu açılacak."
        )
        return
    }

    var roundWords by remember(words) { mutableStateOf(words.shuffled().take(4)) }
    var selectedEnglish by remember(roundWords) { mutableStateOf<WordEntity?>(null) }
    var selectedTurkish by remember(roundWords) { mutableStateOf<WordEntity?>(null) }
    var matchedIds by remember(roundWords) { mutableStateOf(setOf<String>()) }
    var message by remember(roundWords) { mutableStateOf("İngilizce ve Türkçe kartları eşleştir.") }
    val turkishCards = remember(roundWords) { roundWords.shuffled() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PracticeHeader(
                title = "Eşleştirme",
                subtitle = message,
                scoreText = "${matchedIds.size} / ${roundWords.size}"
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    roundWords.forEach { word ->
                        MatchCard(
                            text = word.english.uppercase(Locale.getDefault()),
                            selected = selectedEnglish?.id == word.id,
                            matched = word.id in matchedIds,
                            onClick = {
                                if (word.id !in matchedIds) selectedEnglish = word
                            }
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    turkishCards.forEach { word ->
                        MatchCard(
                            text = word.turkish,
                            selected = selectedTurkish?.id == word.id,
                            matched = word.id in matchedIds,
                            onClick = {
                                if (word.id !in matchedIds) selectedTurkish = word
                            }
                        )
                    }
                }
            }
        }
        item {
            val english = selectedEnglish
            val turkish = selectedTurkish
            Button(
                onClick = {
                    if (english != null && turkish != null) {
                        val correct = english.id == turkish.id
                        onAnswer(english, correct)
                        if (correct) {
                            matchedIds = matchedIds + english.id
                            message = "Doğru eşleşme."
                        } else {
                            message = "Yanlış eşleşme, tekrar dene."
                        }
                        selectedEnglish = null
                        selectedTurkish = null
                    }
                },
                enabled = selectedEnglish != null && selectedTurkish != null,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("EŞLEŞTİR", fontWeight = FontWeight.ExtraBold)
            }
            if (matchedIds.size == roundWords.size) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        roundWords = words.shuffled().take(4)
                        matchedIds = emptySet()
                        message = "Yeni tur hazır."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("YENİ TUR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PracticeHeader(title: String, subtitle: String, scoreText: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Psychology, contentDescription = null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
                }
            }
            Text(scoreText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
        }
    }
}

@Composable
private fun AnswerOption(
    text: String,
    selected: Boolean,
    correct: Boolean,
    submitted: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        submitted && correct -> EmeraldGreen.copy(alpha = 0.16f)
        submitted && selected && !correct -> ErrorRed.copy(alpha = 0.15f)
        selected -> SkyBlue.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val border = when {
        submitted && correct -> EmeraldGreen
        submitted && selected && !correct -> ErrorRed
        selected -> SkyBlue
        else -> Color.Transparent
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !submitted, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.5.dp, border),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = !submitted,
                colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MatchCard(text: String, selected: Boolean, matched: Boolean, onClick: () -> Unit) {
    val bg = when {
        matched -> EmeraldGreen.copy(alpha = 0.16f)
        selected -> SkyBlue.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }
    val border = when {
        matched -> EmeraldGreen
        selected -> SkyBlue
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(enabled = !matched, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.2.dp, border),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(Modifier.fillMaxSize().padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EmptyLearningState(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
        Text(
            message,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WordCardItem(
    word: WordEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onLearnedToggle: () -> Unit,
    onReviewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isExpanded) 1.5.dp else 0.5.dp,
            color = if (isExpanded) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = word.english.uppercase(Locale.getDefault()),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = word.phonetic,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        text = word.turkish.substringBefore(","),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Doğru ${word.timesCorrect}  Yanlış ${word.timesWrong}  Tekrar ${word.reviewLevel}/5",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallTag(word.partOfSpeech, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    SmallTag(
                        word.cefrLevel,
                        when (word.cefrLevel) {
                            "A1" -> EmeraldGreen
                            "A2" -> SkyBlue
                            else -> GoldenAmber
                        },
                        Color.White
                    )
                    IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = if (word.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favori",
                            tint = if (word.isFavorite) GoldenAmber else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailLabel("Çocuk İpucu:")
                    Text(word.englishDefinition.removePrefix("Çocuk ipucu: "), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailLabel("Kısa Açıklama:")
                    Text(word.turkishDefinition, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            DetailLabel("Örnek Cümle:")
                            Text(word.exampleSentence, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(word.exampleTranslation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (word.synonyms.isNotEmpty()) Text("Eş anlamlılar: ${word.synonyms}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
                    if (word.antonyms.isNotEmpty()) Text("Zıt anlamlılar: ${word.antonyms}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onReviewClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(9.dp)
                        ) {
                            Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Çalış", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onLearnedToggle,
                            modifier = Modifier
                                .weight(1.55f)
                                .height(38.dp),
                            shape = RoundedCornerShape(9.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (word.isLearned) Color.Gray else EmeraldGreen),
                            border = BorderStroke(1.dp, if (word.isLearned) Color.LightGray else EmeraldGreen)
                        ) {
                            Icon(
                                imageVector = if (word.isLearned) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (word.isLearned) "Öğrenildi" else "Öğrendim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallTag(text: String, background: Color, content: Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = content)
    }
}

@Composable
private fun DetailLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldGreen)
}

@Composable
fun VocabularyReviewDialog(
    word: WordEntity,
    optionsPool: List<WordEntity>,
    onDismiss: () -> Unit,
    onReviewSuccess: () -> Unit,
    onReviewFailure: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<WordEntity?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pratik Kelime Kartı", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Aşağıdaki kelimenin anlamı nedir?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    word.english.uppercase(Locale.getDefault()),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(18.dp))
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    optionsPool.forEach { option ->
                        AnswerOption(
                            text = option.turkish,
                            selected = selectedOption == option,
                            correct = option.id == word.id,
                            submitted = isSubmitted,
                            onClick = { selectedOption = option }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
                if (!isSubmitted) {
                    Button(
                        onClick = { isSubmitted = true },
                        enabled = selectedOption != null,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("KONTROL ET", fontWeight = FontWeight.Bold)
                    }
                } else {
                    val isUserCorrect = selectedOption?.id == word.id
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isUserCorrect) "Doğru cevap!" else "Yanlış cevap",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isUserCorrect) EmeraldGreen else ErrorRed,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { if (isUserCorrect) onReviewSuccess() else onReviewFailure() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isUserCorrect) "TAMAM" else "KAPAT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun normalizeTurkish(text: String): String {
    return text.lowercase(Locale("tr", "TR"))
        .replace('ç', 'c')
        .replace('ğ', 'g')
        .replace('ı', 'i')
        .replace('ö', 'o')
        .replace('ş', 's')
        .replace('ü', 'u')
}

private fun buildTurkishOptions(correctWord: WordEntity, allWords: List<WordEntity>): List<String> {
    val options = mutableSetOf(correctWord.turkish)
    allWords
        .filter { it.id != correctWord.id && it.turkish.isNotBlank() }
        .shuffled()
        .forEach { word ->
            if (options.size < 4) options.add(word.turkish)
        }
    return options.toList().shuffled()
}
