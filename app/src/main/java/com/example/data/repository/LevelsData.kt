package com.example.data.repository

import com.example.data.model.PuzzleLevel
import java.util.Locale

object LevelsData {
    private val cachedLevels: List<PuzzleLevel> by lazy {
        val levels = mutableListOf<PuzzleLevel>()
        val usedWordIds = mutableSetOf<String>()
        val dictionaryWords = DictionaryData.getPreloadedWords()
        val dictionaryById = dictionaryWords.associateBy { it.id.lowercase(Locale.ENGLISH) }
        val fallbackWordIds = dictionaryWords
            .asSequence()
            .map { it.id.lowercase(Locale.ENGLISH) }
            .filter { "_" !in it }
            .distinct()
            .toList()

        fun levelTargetRange(id: Int): IntRange = when {
            id <= 20 -> 3..7
            id <= 60 -> 4..8
            else -> 5..10
        }

        fun themedCandidates(title: String, theme: String): Set<String> {
            val key = "${title.lowercase(Locale("tr", "TR"))} ${theme.lowercase(Locale("tr", "TR"))}"
            val pools = mutableSetOf<String>()
            fun add(vararg ids: String) = pools.addAll(ids)

            when {
                key.contains("tanış") || key.contains("temel") || key.contains("iletişim") || key.contains("yardım") ->
                    add("good", "nice", "meet", "name", "ask", "answer", "say", "tell", "speak", "listen")
                key.contains("duygu") || key.contains("yorgun") ->
                    add("sad", "angry", "tired", "excited", "scared", "proud", "shy", "bored", "joy", "fear")
                key.contains("aile") || key.contains("çocuk") || key.contains("akraba") || key.contains("insan") ->
                    add("son", "daughter", "aunt", "cousin", "grandfather", "grandmother", "person", "woman", "man")
                key.contains("ev") || key.contains("eşya") || key.contains("temizlik") ->
                    add("kitchen", "bedroom", "bathroom", "floor", "wall", "bed", "sofa", "lamp", "carpet", "mirror", "key", "soap", "towel")
                key.contains("okul") || key.contains("kitap") || key.contains("okumak") || key.contains("yazmak") ->
                    add("pen", "pencil", "paper", "notebook", "desk", "bag", "library", "letter", "note", "learn", "teach")
                key.contains("meyve") || key.contains("kahvalt") || key.contains("içecek") || key.contains("yemek") ->
                    add("banana", "grape", "lemon", "strawberry", "egg", "salt", "juice", "potato", "tomato", "onion", "carrot", "plate", "cup")
                key.contains("zaman") || key.contains("gün") ->
                    add("time", "hour", "minute", "second", "day", "month", "year", "afternoon", "evening")
                key.contains("renk") || key.contains("sarı") || key.contains("ışık") ->
                    add("sunny", "rainy", "cloudy", "snowy", "windy", "silver", "purple", "grey")
                key.contains("mağara") || key.contains("taş") || key.contains("kristal") ->
                    add("rock", "stone", "hard", "ancient", "history", "deep", "tunnel", "canyon")
                key.contains("vücut") || key.contains("görünüş") ->
                    add("body", "face", "nose", "hair", "neck", "shoulder", "arm", "finger", "knee", "foot", "toe", "back")
                key.contains("giysi") ->
                    add("pants", "dress", "skirt", "socks", "jacket", "coat", "tie", "gloves", "scarf", "belt")
                key.contains("lav") || key.contains("ateş") || key.contains("sıcak") ->
                    add("volcano", "smoke", "burn", "danger", "hot", "fire", "sweat", "air")
                key.contains("koş") || key.contains("zıpla") || key.contains("tırman") || key.contains("yürü") || key.contains("sür") ->
                    add("run", "walk", "jump", "climb", "drive", "ride", "move", "quick", "slow", "legs", "feet")
                key.contains("gökyüz") || key.contains("bulut") || key.contains("uçuş") || key.contains("rüzgar") ->
                    add("sky", "cloud", "plane", "pilot", "wing", "wind", "storm", "thunder", "fly", "air")
                key.contains("şehir") || key.contains("sokak") || key.contains("banka") || key.contains("hastane") || key.contains("sinema") ->
                    add("village", "street", "road", "shop", "market", "bank", "hospital", "airport", "station", "cinema", "museum")
                key.contains("tatil") || key.contains("ülke") || key.contains("yolculuk") ->
                    add("country", "capital", "coast", "continent", "border_geo", "hotel", "beach", "traveling", "journey")
                key.contains("kayıp") || key.contains("tapınak") || key.contains("piramit") || key.contains("hazine") ->
                    add("temple", "pray", "palace", "queen", "trader", "coin", "gold", "safe", "tower", "map")
                key.contains("bilge") || key.contains("gelecek") || key.contains("zafer") ->
                    add("knowledge", "education", "success", "future", "hope", "glory", "prize", "master", "expert", "wise")
            }

            return pools
        }

        fun candidatePool(levelId: Int, title: String, theme: String, cefr: String, requestedWords: List<String>): List<String> {
            val targetRange = levelTargetRange(levelId)
            val themed = themedCandidates(title, theme)
            val requested = requestedWords
                .map { it.lowercase(Locale.ENGLISH) }
                .toSet()
            return (requestedWords + themed + fallbackWordIds).asSequence()
                .map { it.lowercase(Locale.ENGLISH) }
                .filter { it !in usedWordIds }
                .filter { it in dictionaryById }
                .distinct()
                .sortedWith(
                    compareByDescending<String> { it in requested }
                        .thenByDescending { it in themed }
                        .thenBy { if (it.length in targetRange) 0 else 1 }
                        .thenBy { kotlin.math.abs(it.length - targetRange.first) }
                        .thenBy {
                            val wordCefr = dictionaryById[it]?.cefrLevel ?: cefr
                            if (wordCefr == cefr) 0 else 1
                        }
                        .thenBy { it }
                )
                .toList()
        }

        fun uniqueWordsForLevel(levelId: Int, title: String, theme: String, cefr: String, requestedWords: List<String>): List<String> {
            val targetCount = requestedWords.size
            val candidates = candidatePool(levelId, title, theme, cefr, requestedWords)

            fun sharesLetterWithSelected(candidate: String, selected: List<String>): Boolean {
                if (selected.isEmpty()) return true
                val chars = candidate.toSet()
                return selected.any { selectedWord -> selectedWord.any { it in chars } }
            }

            fun isGoodLayout(selected: List<String>, maxDimension: Int): Boolean {
                val result = CrosswordLayoutEngine.generateLayoutResult(levelId, selected, dictionaryById)
                if (result !is CrosswordGenerationResult.Success) return false
                return result.rows <= maxDimension && result.columns <= maxDimension
            }

            fun search(selected: List<String>, remaining: List<String>, maxDimension: Int, attempts: IntArray): List<String>? {
                if (attempts[0]++ > 450) return null
                if (selected.size == targetCount) {
                    return if (isGoodLayout(selected, maxDimension)) selected else null
                }

                val filtered = remaining.filter { it !in selected && sharesLetterWithSelected(it, selected) }
                for (candidate in filtered) {
                    val next = selected + candidate
                    val stillPossible = remaining.any { it !in next && sharesLetterWithSelected(it, next) } || next.size == targetCount
                    if (!stillPossible) continue

                    val result = search(next, remaining.filterNot { it == candidate }, maxDimension, attempts)
                    if (result != null) return result
                }
                return null
            }

            val strictDimension = if (levelId <= 20) 9 else if (levelId <= 60) 10 else 11
            val selected = listOf(24, 36, 55, 80)
                .asSequence()
                .mapNotNull { candidateLimit ->
                    val limitedCandidates = candidates.take(candidateLimit)
                    search(emptyList(), limitedCandidates, strictDimension, intArrayOf(0))
                        ?: search(emptyList(), limitedCandidates, strictDimension + 1, intArrayOf(0))
                }
                .firstOrNull()
                ?: error("No theme-compatible connected word set for level $levelId ($title).")

            usedWordIds.addAll(selected)
            return selected
        }

        // Helper to add levels - much faster now because it doesn't calculate layouts
        fun addLvl(id: Int, world: Int, num: Int, title: String, theme: String, cefr: String, words: List<String>, isChest: Boolean = false, isBoss: Boolean = false) {
            levels.add(PuzzleLevel(
                id = id, worldId = world, levelNumber = num, title = title, theme = theme, cefrLevel = cefr,
                isChest = isChest, isBoss = isBoss,
                wordIds = if (isChest) emptyList() else uniqueWordsForLevel(id, title, theme, cefr, words),
                xpReward = EconomyConfig.levelXpReward(id, isBoss),
                coinsReward = EconomyConfig.levelCoinReward(id, isBoss)
            ))
        }

        // ================= WORLD 1: YEŞİL VADİ (A1) =================
        addLvl(1, 1, 1, "Tanışma", "Vadi", "A1", listOf("hello", "yes", "thanks", "please", "welcome"))
        addLvl(2, 1, 2, "Temel", "Vadi", "A1", listOf("please", "sorry", "no", "thanks", "hello"))
        addLvl(3, 1, 3, "Duygular", "Vadi", "A1", listOf("happy", "sorry", "friend", "welcome", "today"))
        addLvl(4, 1, 4, "Aile", "Vadi", "A1", listOf("family", "father", "mother", "sister", "brother"))
        addLvl(5, 1, 5, "Hazine Yumurtası", "Sandık", "A1", emptyList(), isChest = true)
        addLvl(6, 1, 6, "Çocuklar", "Vadi", "A1", listOf("child", "baby", "boy", "girl", "family"))
        addLvl(7, 1, 7, "Akrabalar", "Vadi", "A1", listOf("uncle", "brother", "sister", "mother", "father"))
        addLvl(8, 1, 8, "Evim", "Vadi", "A1", listOf("house", "room", "door", "window", "table"))
        addLvl(9, 1, 9, "Eşyalar", "Vadi", "A1", listOf("table", "chair", "window", "door", "room"))
        addLvl(10, 1, 10, "Büyük Sandık", "Sandık", "A1", emptyList(), isChest = true)
        addLvl(11, 1, 11, "Okul", "Vadi", "A1", listOf("school", "teacher", "student", "book", "chair"))
        addLvl(12, 1, 12, "Kitaplar", "Vadi", "A1", listOf("book", "student", "teacher", "school", "table"))
        addLvl(13, 1, 13, "Meyveler", "Vadi", "A1", listOf("apple", "orange", "sugar", "water", "milk"))
        addLvl(14, 1, 14, "Kahvaltı", "Vadi", "A1", listOf("bread", "cheese", "tea", "milk", "apple"))
        addLvl(15, 1, 15, "Zaman", "Vadi", "A1", listOf("morning", "today", "clock", "bread", "milk"))
        addLvl(16, 1, 16, "Renkler", "Vadi", "A1", listOf("red", "blue", "green", "white", "black"))
        addLvl(17, 1, 17, "Sarılar", "Vadi", "A1", listOf("yellow", "sun", "hot", "happy", "orange"))
        addLvl(18, 1, 18, "İçecekler", "Vadi", "A1", listOf("water", "tea", "coffee", "milk", "sugar"))
        addLvl(19, 1, 19, "Günler", "Vadi", "A1", listOf("today", "yesterday", "tomorrow", "night", "week"))
        addLvl(20, 1, 20, "Boss: Vadi Koruyucusu", "Vadi", "A1", listOf("nature", "tree", "grass", "flower", "bird", "sun"), isBoss = true)

        // ================= WORLD 2: KRİSTAL MAĞARA (A1-A2) =================
        addLvl(21, 2, 1, "Mağara Girişi", "Mağara", "A1", listOf("dark", "black", "grey", "cold", "stone"))
        addLvl(22, 2, 2, "Harf Kristalleri", "Mağara", "A1", listOf("blue", "purple", "light", "new", "star"))
        addLvl(23, 2, 3, "Parlayan Taşlar", "Mağara", "A1", listOf("white", "silver", "small", "hard", "rock"))
        addLvl(24, 2, 4, "Mağara Sırları", "Mağara", "A1", listOf("quiet", "old", "ancient", "history", "book"))
        addLvl(25, 2, 5, "Kristal Sandık", "Sandık", "A1", emptyList(), isChest = true)
        addLvl(26, 2, 6, "Derinlikler", "Mağara", "A1", listOf("water", "river", "cold", "wet", "dark"))
        addLvl(27, 2, 7, "Mağara Dostları", "Mağara", "A1", listOf("mouse", "bat", "friend", "small", "fast"))
        addLvl(28, 2, 8, "Işık Yolu", "Mağara", "A1", listOf("sun", "light", "yellow", "white", "day"))
        addLvl(29, 2, 9, "Yankı", "Mağara", "A1", listOf("noisy", "quiet", "sound", "voice", "hello"))
        addLvl(30, 2, 10, "Parlak Sandık", "Sandık", "A1", emptyList(), isChest = true)
        addLvl(31, 2, 11, "İnsanlar", "Mağara", "A2", listOf("man", "woman", "person", "child", "baby"))
        addLvl(32, 2, 12, "Vücudumuz", "Mağara", "A2", listOf("hand", "head", "eye", "ear", "mouth"))
        addLvl(33, 2, 13, "Giysiler", "Mağara", "A2", listOf("shirt", "hat", "shoes", "clothes", "new"))
        addLvl(34, 2, 14, "Görünüş", "Mağara", "A2", listOf("tall", "short", "old", "young", "strong"))
        addLvl(35, 2, 15, "Yorgunluk", "Mağara", "A2", listOf("weak", "tired", "sleep", "night", "dark"))
        addLvl(36, 2, 16, "Duygular 2", "Mağara", "A2", listOf("happy", "sad", "angry", "sorry", "brave"))
        addLvl(37, 2, 17, "İletişim", "Mağara", "A2", listOf("speak", "talk", "listen", "word", "hello"))
        addLvl(38, 2, 18, "Yardım", "Mağara", "A2", listOf("help", "please", "thanks", "friend", "kind"))
        addLvl(39, 2, 19, "Mağara Sonu", "Mağara", "A2", listOf("exit", "door", "light", "finish", "ready"))
        addLvl(40, 2, 20, "Boss: Kristal Dev", "Mağara", "A2", listOf("power", "strong", "brave", "giant", "king", "gold"), isBoss = true)

        // ================= WORLD 3: LAV DAĞI (A2) =================
        addLvl(41, 3, 1, "Sıcak Karşılama", "Lav", "A2", listOf("hot", "fire", "red", "burn", "smoke"))
        addLvl(42, 3, 2, "Koş ve Kurtul", "Lav", "A2", listOf("run", "fast", "quick", "legs", "move"))
        addLvl(43, 3, 3, "Zıplama", "Lav", "A2", listOf("jump", "high", "up", "down", "legs"))
        addLvl(44, 3, 4, "Tırmanış", "Lav", "A2", listOf("climb", "mountain", "hill", "high", "strong"))
        addLvl(45, 3, 5, "Lav Sandığı", "Sandık", "A2", emptyList(), isChest = true)
        addLvl(46, 3, 6, "Yürüme", "Lav", "A2", listOf("walk", "slow", "feet", "ground", "path"))
        addLvl(47, 3, 7, "Ateş Topu", "Lav", "A2", listOf("ball", "round", "hot", "fast", "danger"))
        addLvl(48, 3, 8, "Taş Köprü", "Lav", "A2", listOf("bridge", "stone", "cross", "over", "water"))
        addLvl(49, 3, 9, "Sıcak Hava", "Lav", "A2", listOf("air", "hot", "sun", "sweat", "drink"))
        addLvl(50, 3, 10, "Sıcak Sandık", "Sandık", "A2", emptyList(), isChest = true)
        addLvl(51, 3, 11, "Yemek Yapma", "Lav", "A2", listOf("cook", "eat", "food", "meat", "fire"))
        addLvl(52, 3, 12, "Çalışma", "Lav", "A2", listOf("work", "hard", "busy", "job", "money"))
        addLvl(53, 3, 13, "Temizlik", "Lav", "A2", listOf("clean", "wash", "water", "soap", "new"))
        addLvl(54, 3, 14, "Uyumak", "Lav", "A2", listOf("sleep", "rest", "dream", "night", "bed"))
        addLvl(55, 3, 15, "Yüzme", "Lav", "A2", listOf("swim", "water", "river", "lake", "fish"))
        addLvl(56, 3, 16, "Oynamak", "Lav", "A2", listOf("play", "game", "fun", "ball", "happy"))
        addLvl(57, 3, 17, "Sürmek", "Lav", "A2", listOf("drive", "car", "ride", "bike", "fast"))
        addLvl(58, 3, 18, "Okumak", "Lav", "A2", listOf("read", "book", "learn", "study", "word"))
        addLvl(59, 3, 19, "Yazmak", "Lav", "A2", listOf("write", "pen", "paper", "letter", "note"))
        addLvl(60, 3, 20, "Boss: Lav Ejderi", "Lav", "A2", listOf("dragon", "fight", "brave", "fire", "danger", "win"), isBoss = true)

        // ================= WORLD 4: GÖKYÜZÜ ADALARI (A2-B1) =================
        addLvl(61, 4, 1, "Yüzen Adalar", "Gökyüzü", "A2", listOf("island", "fly", "sky", "blue", "air"))
        addLvl(62, 4, 2, "Bulutlar", "Gökyüzü", "A2", listOf("cloud", "white", "soft", "rain", "wind"))
        addLvl(63, 4, 3, "Uçuş", "Gökyüzü", "A2", listOf("plane", "pilot", "fly", "high", "wing"))
        addLvl(64, 4, 4, "Rüzgar", "Gökyüzü", "A2", listOf("wind", "cold", "fast", "blow", "move"))
        addLvl(65, 4, 5, "Gök Sandığı", "Sandık", "A2", emptyList(), isChest = true)
        addLvl(66, 4, 6, "Kuş Bakışı", "Gökyüzü", "B1", listOf("view", "land", "earth", "world", "far"))
        addLvl(67, 4, 7, "Şehirler", "Gökyüzü", "B1", listOf("city", "town", "place", "home", "live"))
        addLvl(68, 4, 8, "Ülkeler", "Gökyüzü", "B1", listOf("turkey", "flag", "nation", "border", "land"))
        addLvl(69, 4, 9, "Tatil", "Gökyüzü", "B1", listOf("hotel", "beach", "summer", "sun", "sea"))
        addLvl(70, 4, 10, "Altın Sandık", "Sandık", "B1", emptyList(), isChest = true)
        addLvl(71, 4, 11, "Müze", "Gökyüzü", "B1", listOf("museum", "art", "paint", "statue", "old"))
        addLvl(72, 4, 12, "Park", "Gökyüzü", "B1", listOf("park", "tree", "grass", "bench", "walk"))
        addLvl(73, 4, 13, "Kütüphane", "Gökyüzü", "B1", listOf("library", "book", "read", "quiet", "study"))
        addLvl(74, 4, 14, "Havalimanı", "Gökyüzü", "B1", listOf("airport", "plane", "ticket", "travel", "bag"))
        addLvl(75, 4, 15, "İstasyon", "Gökyüzü", "B1", listOf("station", "train", "wait", "platform", "stop"))
        addLvl(76, 4, 16, "Sokak", "Gökyüzü", "B1", listOf("street", "road", "shop", "market", "busy"))
        addLvl(77, 4, 17, "Banka", "Gökyüzü", "B1", listOf("bank", "money", "save", "account", "card"))
        addLvl(78, 4, 18, "Hastane", "Gökyüzü", "B1", listOf("hospital", "doctor", "nurse", "sick", "well"))
        addLvl(79, 4, 19, "Sinema", "Gökyüzü", "B1", listOf("cinema", "movie", "film", "watch", "seat"))
        addLvl(80, 4, 20, "Boss: Hava Lordu", "Gökyüzü", "B1", listOf("storm", "light", "thunder", "power", "cloud", "king"), isBoss = true)

        // ================= WORLD 5: KAYIP ŞEHİR (B1) =================
        addLvl(81, 5, 1, "Şehir Kapısı", "Şehir", "B1", listOf("gate", "stone", "large", "heavy", "open"))
        addLvl(82, 5, 2, "Eski Yazıtlar", "Şehir", "B1", listOf("write", "stone", "read", "past", "old"))
        addLvl(83, 5, 3, "Tapınak", "Şehir", "B1", listOf("temple", "pray", "peace", "gold", "statue"))
        addLvl(84, 5, 4, "Piramit", "Şehir", "B1", listOf("desert", "egypt", "large", "stone", "top"))
        addLvl(85, 5, 5, "Antik Sandık", "Sandık", "B1", emptyList(), isChest = true)
        addLvl(86, 5, 6, "Kral Yolu", "Şehir", "B1", listOf("king", "queen", "palace", "rich", "gold"))
        addLvl(87, 5, 7, "Pazar Yeri", "Şehir", "B1", listOf("market", "buy", "sell", "trader", "coin"))
        addLvl(88, 5, 8, "Heykeller", "Şehir", "B1", listOf("statue", "man", "art", "stone", "face"))
        addLvl(89, 5, 9, "Bahçeler", "Şehir", "B1", listOf("garden", "flower", "water", "green", "beautiful"))
        addLvl(90, 5, 10, "Kayıp Sandık", "Sandık", "B1", emptyList(), isChest = true)
        addLvl(91, 5, 11, "Hazine Odası", "Şehir", "B1", listOf("gold", "silver", "coin", "rich", "safe"))
        addLvl(92, 5, 12, "Tüneller", "Şehir", "B1", listOf("tunnel", "dark", "long", "deep", "path"))
        addLvl(93, 5, 13, "Kuleler", "Şehir", "B1", listOf("tower", "high", "top", "view", "city"))
        addLvl(94, 5, 14, "Kayıp Harita", "Şehir", "B1", listOf("map", "path", "find", "way", "search"))
        addLvl(95, 5, 15, "Keşif", "Şehir", "B1", listOf("search", "find", "new", "world", "ready"))
        addLvl(96, 5, 16, "Bilgelik", "Şehir", "B1", listOf("wise", "smart", "know", "learn", "study"))
        addLvl(97, 5, 17, "Gelecek", "Şehir", "B1", listOf("future", "hope", "new", "life", "dream"))
        addLvl(98, 5, 18, "Zafer", "Şehir", "B1", listOf("win", "glory", "prize", "best", "finish"))
        addLvl(99, 5, 19, "Final Yolculuğu", "Şehir", "B1", listOf("last", "end", "journey", "ready", "go"))
        addLvl(100, 5, 20, "Boss: Bilge Dino", "Şehir", "B1", listOf("master", "expert", "smart", "know", "finish", "win"), isBoss = true)

        levels
    }

    fun getLevels(): List<PuzzleLevel> {
        return cachedLevels
    }
}
