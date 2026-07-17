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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WordEntity
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(viewModel: GameViewModel) {
    val words by viewModel.dictionary.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var activeReviewWord by remember { mutableStateOf<WordEntity?>(null) }
    var expandedWordId by remember { mutableStateOf<String?>(null) }

    // Localized Turkish normalization helper
    fun normalizeTurkish(text: String): String {
        return text.lowercase(Locale("tr", "TR"))
            .replace('ç', 'c')
            .replace('ğ', 'g')
            .replace('ı', 'i')
            .replace('i', 'i')
            .replace('ö', 'o')
            .replace('ş', 's')
            .replace('ü', 'u')
    }

    // Filter and search computation
    val filteredWords = remember(words, searchQuery, selectedFilter) {
        words.filter { word ->
            // Filter categories
            val matchesFilter = when (selectedFilter) {
                "Favoriler" -> word.isFavorite
                "Öğrendiklerim" -> word.isLearned
                "A1" -> word.cefrLevel == "A1"
                "A2" -> word.cefrLevel == "A2"
                "B1" -> word.cefrLevel == "B1"
                else -> true
            }

            // Search query matching
            val normalizedQuery = normalizeTurkish(searchQuery)
            val matchesSearch = if (normalizedQuery.isEmpty()) {
                true
            } else {
                val normalizedEn = normalizeTurkish(word.english)
                val normalizedTr = normalizeTurkish(word.turkish)
                normalizedEn.contains(normalizedQuery) || normalizedTr.contains(normalizedQuery)
            }

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Kişisel Sözlük",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Gördüğün tüm kelimeleri incele, tekrar et ve çalış.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Kelime ara (Türkçe veya İngilizce)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = EmeraldGreen) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
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

                // Filter tabs row
                val filterOptions = listOf("Tümü", "Favoriler", "Öğrendiklerim", "A1", "A2", "B1")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filterOptions.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val chipBg = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        val chipText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(chipBg)
                                .clickable { viewModel.updateSelectedFilter(filter) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = chipText
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (filteredWords.isEmpty()) {
                // Empty state
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
                    Text(
                        text = "Aranan Kelime Bulunamadı",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Arama kriterlerini değiştirmeyi deneyin ya da yeni bulmacalar çözerek kelime dağarcığınızı genişletin.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Word List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredWords) { word ->
                        val isExpanded = expandedWordId == word.id
                        WordCardItem(
                            word = word,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedWordId = if (isExpanded) null else word.id
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite(word.id) },
                            onLearnedToggle = { viewModel.markWordLearned(word.id) },
                            onReviewClick = { activeReviewWord = word }
                        )
                    }
                }
            }
        }
    }

    // Mini Review Game sheet
    activeReviewWord?.let { word ->
        val pool = remember(word, words) {
            val others = words.filter { it.id != word.id }.shuffled()
            val dummyCount = 3.coerceAtMost(others.size)
            (listOf(word) + others.take(dummyCount)).shuffled()
        }

        VocabularyReviewDialog(
            word = word,
            optionsPool = pool,
            onDismiss = { activeReviewWord = null },
            onReviewSuccess = {
                // Award XP and complete
                viewModel.completePracticeSession(1, 1)
                activeReviewWord = null
            }
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
            // Unexpanded Row Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Part of speech tag
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = word.partOfSpeech,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // CEFR tag
                    val cefrBg = when (word.cefrLevel) {
                        "A1" -> EmeraldGreen
                        "A2" -> SkyBlue
                        else -> GoldenAmber
                    }
                    Box(
                        modifier = Modifier
                            .background(cefrBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = word.cefrLevel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Favorite Icon
                    IconButton(
                        onClick = {
                            onFavoriteToggle()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (word.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (word.isFavorite) GoldenAmber else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expanded Details View
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // English description
                Text(
                    text = "Açıklama (EN):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = EmeraldGreen
                )
                Text(
                    text = word.englishDefinition,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Turkish description
                Text(
                    text = "Açıklama (TR):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = EmeraldGreen
                )
                Text(
                    text = word.turkishDefinition,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Example sentence card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Örnek Cümle:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = word.exampleSentence,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = word.exampleTranslation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Synonyms & Antonyms
                if (word.synonyms.isNotEmpty()) {
                    Text(
                        text = "Eş Anlamlılar: ${word.synonyms}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (word.antonyms.isNotEmpty()) {
                    Text(
                        text = "Zıt Anlamlılar: ${word.antonyms}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons inside card (Practice & Mark Learned)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onReviewClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kelimeyi Çalış", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onLearnedToggle,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (word.isLearned) Color.Gray else EmeraldGreen
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (word.isLearned) Color.LightGray else EmeraldGreen
                        )
                    ) {
                        Icon(
                            imageVector = if (word.isLearned) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (word.isLearned) "Öğrenildi İşaretli" else "Öğrenildi İşaretle",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VocabularyReviewDialog(
    word: WordEntity,
    optionsPool: List<WordEntity>,
    onDismiss: () -> Unit,
    onReviewSuccess: () -> Unit
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
                Text(
                    text = "Pratik Kelime Kartı",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldGreen
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Aşağıdaki kelimenin anlamı nedir?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // English active word
                Text(
                    text = word.english.uppercase(Locale.getDefault()),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Options list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    optionsPool.forEach { option ->
                        val isSelected = selectedOption == option
                        val isCorrect = option.id == word.id

                        val cardBg = when {
                            isSubmitted && isCorrect -> EmeraldGreen.copy(alpha = 0.15f)
                            isSubmitted && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.15f)
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }

                        val cardBorder = when {
                            isSubmitted && isCorrect -> EmeraldGreen
                            isSubmitted && isSelected && !isCorrect -> ErrorRed
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> Color.Transparent
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isSubmitted) { selectedOption = option },
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.5.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { if (!isSubmitted) selectedOption = option },
                                    enabled = !isSubmitted,
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option.turkish,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                            text = if (isUserCorrect) "TEBRİKLER! DOĞRU CEVAP 🎉" else "YALNIŞ CEVAP ❌",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isUserCorrect) EmeraldGreen else ErrorRed,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (isUserCorrect) onReviewSuccess() else onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isUserCorrect) "Ödülümü Al (+5 XP)" else "Kapat", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
