package com.example.data.repository

import com.example.data.model.WordEntity

object DictionaryData {
    fun getPreloadedWords(): List<WordEntity> {
        val words = mutableListOf<WordEntity>()

        fun childFriendlyDefinition(id: String, tr: String, trDef: String): String {
            val customHints = mapOf(
                "apple" to "Meyve. Kırmızı, yeşil veya sarı olabilir; elde tutulup yenir.",
                "banana" to "Meyve. Uzun ve sarıdır; kabuğu soyularak yenir.",
                "orange" to "Meyve. Turuncu renklidir; suyu da içilir.",
                "grape" to "Meyve. Küçük taneler halinde salkımda olur.",
                "lemon" to "Meyve. Sarıdır ve tadı ekşidir.",
                "strawberry" to "Meyve. Kırmızıdır, üstünde minik noktalar vardır.",
                "water" to "İçecek. Renksizdir; susayınca içilir.",
                "milk" to "İçecek. Beyazdır; kahvaltıda içilebilir.",
                "bread" to "Yiyecek. Kahvaltıda ve sandviçte kullanılır.",
                "cheese" to "Yiyecek. Sütten yapılır; genelde kahvaltıda yenir.",
                "egg" to "Yiyecek. Kabuğu vardır; haşlanabilir veya tavada pişer.",
                "potato" to "Sebze. Toprağın altında büyür; kızartması yapılır.",
                "tomato" to "Sebze. Kırmızıdır; salatada olur.",
                "onion" to "Sebze. Kat kat kabuğu vardır; doğrarken göz yaşartabilir.",
                "carrot" to "Sebze. Turuncudur; tavşanlarla akla gelir.",
                "yellow" to "Renk. Güneş, limon ve muz bu renkte olabilir.",
                "red" to "Renk. Elma, çilek ve kalp bu renkte olabilir.",
                "green" to "Renk. Çimen, yaprak ve bazı elmalar bu renkte olabilir.",
                "blue" to "Renk. Gökyüzü ve deniz bu renkte görünebilir.",
                "black" to "Renk. Gece ve kömür gibi koyu renktir.",
                "white" to "Renk. Kar, süt ve bulut gibi açık renktir.",
                "cat" to "Hayvan. Miyavlar, evde yaşayabilir.",
                "dog" to "Hayvan. Havlar, insanlarla oyun oynamayı sever.",
                "bird" to "Hayvan. Kanatları vardır ve uçabilir.",
                "fish" to "Hayvan. Suda yaşar ve yüzgeçleriyle yüzer.",
                "school" to "Yer. Çocukların ders öğrendiği yerdir.",
                "teacher" to "Kişi. Sınıfta ders anlatır ve yardımcı olur.",
                "student" to "Kişi. Okula gider ve yeni şeyler öğrenir.",
                "book" to "Nesne. Sayfaları vardır; okunur.",
                "pen" to "Nesne. Yazı yazmak için kullanılır.",
                "bag" to "Nesne. Kitap ve eşyaları taşımaya yarar."
            )

            customHints[id]?.let { return it }
            if (trDef.isBlank()) return "$tr kelimesini hatırlamak için resmine ve ipucuna bak."
            return when (trDef.trim().trimEnd('.')) {
                "Meyve" -> "Meyve. Rengi, şekli veya tadıyla tanıyabilirsin."
                "Sebze" -> "Sebze. Yemeklerde veya salatada kullanılabilir."
                "Sıvı", "Sıvı gıda" -> "İçecek. Bardakta içilebilir."
                "Besin", "Temel gıda" -> "Yiyecek. Acıkınca yenir."
                "Mobilya" -> "Ev eşyası. Evde veya sınıfta görebilirsin."
                "Yazma aracı" -> "Okul eşyası. Yazı yazmaya yarar."
                "Canlı varlık" -> "Canlı. Hareket edebilir, büyüyebilir veya yaşayabilir."
                "Kafa" -> "Vücut bölümü. Başın üst kısmıdır."
                "Çehre" -> "Vücut bölümü. Göz, burun ve ağız buradadır."
                "Afiyet" -> "Sağlıklı olma durumu."
                "Malumat" -> "Bir şeyi bilme ve öğrenme."
                else -> trDef
            }
        }

        fun addW(id: String, tr: String, pos: String = "noun", cefr: String? = null, trDef: String = "") {
            val resolvedCefr = cefr ?: inferCefr(id, pos)
            val friendlyDefinition = childFriendlyDefinition(id, tr, trDef)
            words.add(WordEntity(
                id = id, english = id, turkish = tr, partOfSpeech = pos, cefrLevel = resolvedCefr,
                englishDefinition = "Çocuk ipucu: $friendlyDefinition",
                turkishDefinition = friendlyDefinition,
                exampleSentence = simpleExampleSentence(id, tr),
                exampleTranslation = simpleExampleTranslation(id, tr),
                phonetic = ""
            ))
        }

        // --- BASICS ---
        addW("hello", "merhaba", "interjection", "A1", "Selamlama sözü.")
        addW("yes", "evet", "adverb", "A1", "Onay.")
        addW("no", "hayır", "adverb", "A1", "Red.")
        addW("thanks", "teşekkürler", "interjection", "A1", "Minnet.")
        addW("please", "lütfen", "adverb", "A1", "Rica.")
        addW("welcome", "hoşgeldiniz", "interjection", "A1", "Karşılama.")
        addW("sorry", "üzgün", "adjective", "A1", "Pişmanlık.")
        addW("happy", "mutlu", "adjective", "A1", "Sevinçli.")
        addW("friend", "arkadaş", "noun", "A1", "Sevilen kişi.")
        addW("today", "bugün", "noun", "A1", "Şu anki gün.")
        addW("yesterday", "dün", "noun", "A1", "Geçmiş gün.")
        addW("tomorrow", "yarın", "noun", "A1", "Gelecek gün.")
        addW("night", "gece", "noun", "A1", "Karanlık vakit.")
        addW("week", "hafta", "noun", "A1", "Yedi gün.")
        addW("morning", "sabah", "noun", "A1", "Günün başlangıcı.")
        addW("clock", "saat", "noun", "A1", "Zaman ölçer.")

        // --- FAMILY ---
        addW("family", "aile", "noun", "A1", "Akraba topluluğu.")
        addW("father", "baba", "noun", "A1", "Erkek ebeveyn.")
        addW("mother", "anne", "noun", "A1", "Kadın ebeveyn.")
        addW("sister", "kız kardeş", "noun", "A1", "Kız kardeş.")
        addW("brother", "erkek kardeş", "noun", "A1", "Erkek kardeş.")
        addW("child", "çocuk", "noun", "A1", "Küçük insan.")
        addW("baby", "bebek", "noun", "A1", "Çok küçük çocuk.")
        addW("son", "oğul", "noun", "A1", "Erkek çocuk.")
        addW("daughter", "kız evlat", "noun", "A1", "Kız çocuk.")
        addW("boy", "oğlan", "noun", "A1", "Erkek çocuk.")
        addW("girl", "kız", "noun", "A1", "Kız çocuk.")
        addW("uncle", "amca", "noun", "A1", "Babanın kardeşi.")
        addW("aunt", "teyze / hala", trDef = "Annenin veya babanın kız kardeşi.")
        addW("cousin", "kuzen", trDef = "Amca, hala, teyze veya dayı çocuğu.")
        addW("grandfather", "dede", trDef = "Büyükbaba.")
        addW("grandmother", "anneanne / babaanne", trDef = "Büyükanne.")

        // --- BODY PARTS ---
        addW("body", "vücut", trDef = "Gövde.")
        addW("head", "baş", trDef = "Kafa.")
        addW("face", "yüz", trDef = "Çehre.")
        addW("eye", "göz", trDef = "Görme organı.")
        addW("ear", "kulak", trDef = "İşitme organı.")
        addW("nose", "burun", trDef = "Koku alma organı.")
        addW("mouth", "ağız", trDef = "Konuşma ve yemek organı.")
        addW("hair", "saç", trDef = "Baştaki kıllar.")
        addW("neck", "boyun", trDef = "Baş ile gövde arası.")
        addW("shoulder", "omuz", trDef = "Kolun gövdeye birleştiği yer.")
        addW("arm", "kol", trDef = "Omuzdan parmak ucuna kadar.")
        addW("hand", "el", trDef = "Kolun ucu.")
        addW("finger", "parmak", trDef = "Elin uzantısı.")
        addW("leg", "bacak", trDef = "Kalçadan ayağa kadar.")
        addW("knee", "diz", trDef = "Bacağın orta eklemi.")
        addW("foot", "ayak", trDef = "Bacağın ucu.")
        addW("toe", "ayak parmağı", trDef = "Ayağın uzantısı.")
        addW("back", "sırt", trDef = "Gövdenin arka kısmı.")

        // --- ANIMALS ---
        addW("animal", "hayvan", trDef = "Canlı varlık.")
        addW("cat", "kedi", trDef = "Evcil küçük hayvan.")
        addW("dog", "köpek", trDef = "Evcil sadık hayvan.")
        addW("bird", "kuş", trDef = "Uçan hayvan.")
        addW("fish", "balık", trDef = "Suda yaşayan.")
        addW("horse", "at", trDef = "Binek hayvanı.")
        addW("cow", "inek", trDef = "Süt veren hayvan.")
        addW("sheep", "koyun", trDef = "Yünlü hayvan.")
        addW("pig", "domuz", trDef = "Pembe hayvan.")
        addW("mouse", "fare", trDef = "Kemirgen.")
        addW("rabbit", "tavşan", trDef = "Uzun kulaklı kemirgen.")
        addW("lion", "aslan", trDef = "Ormanlar kralı.")
        addW("tiger", "kaplan", trDef = "Çizgili büyük kedi.")
        addW("elephant", "fil", trDef = "Hortumlu büyük hayvan.")
        addW("monkey", "maymun", trDef = "Ağaçta yaşayan zeki hayvan.")
        addW("snake", "yılan", trDef = "Sürüngen.")
        addW("spider", "örümcek", trDef = "Ağ ören böcek.")
        addW("bee", "arı", trDef = "Bal yapan böcek.")
        addW("butterfly", "kelebek", trDef = "Renkli kanatlı böcek.")
        addW("shark", "köpekbalığı", trDef = "Deniz yırtıcısı.")
        addW("dolphin", "yunus", trDef = "Zeki deniz memelisi.")

        // --- FOOD & DRINK ---
        addW("food", "yiyecek", trDef = "Besin.")
        addW("drink", "içecek", trDef = "Sıvı gıda.")
        addW("apple", "elma", trDef = "Meyve.")
        addW("banana", "muz", trDef = "Meyve.")
        addW("orange", "portakal", trDef = "Meyve.")
        addW("grape", "üzüm", trDef = "Meyve.")
        addW("lemon", "limon", trDef = "Ekşi meyve.")
        addW("strawberry", "çilek", trDef = "Kırmızı meyve.")
        addW("water", "su", trDef = "Sıvı.")
        addW("milk", "süt", trDef = "Beyaz içecek.")
        addW("bread", "ekmek", trDef = "Temel gıda.")
        addW("cheese", "peynir", trDef = "Süt ürünü.")
        addW("egg", "yumurta", trDef = "Protein kaynağı.")
        addW("meat", "et", trDef = "Hayvan dokusu.")
        addW("fish", "balık", trDef = "Suda yaşayan gıda.")
        addW("chicken", "tavuk", trDef = "Kümes hayvanı eti.")
        addW("sugar", "şeker", trDef = "Tatlı madde.")
        addW("salt", "tuz", trDef = "Baharat.")
        addW("tea", "çay", trDef = "Sıcak içecek.")
        addW("coffee", "kahve", trDef = "Kafeinli içecek.")
        addW("juice", "meyve suyu", trDef = "Sıkılmış meyve.")
        addW("potato", "patates", trDef = "Sebze.")
        addW("tomato", "domates", trDef = "Sebze.")
        addW("onion", "soğan", trDef = "Sebze.")
        addW("carrot", "havuç", trDef = "Sebze.")

        // --- HOUSE & FURNITURE ---
        addW("house", "ev", trDef = "Yaşanan bina.")
        addW("home", "yuva", trDef = "Sıcak aile ortamı.")
        addW("room", "oda", trDef = "Bölme.")
        addW("kitchen", "mutfak", trDef = "Yemek yapılan yer.")
        addW("bedroom", "yatak odası", trDef = "Uyunan yer.")
        addW("bathroom", "banyo", trDef = "Temizlenilen yer.")
        addW("door", "kapı", trDef = "Giriş.")
        addW("window", "pencere", trDef = "Hava açıklığı.")
        addW("floor", "yer / kat", trDef = "Zemin.")
        addW("wall", "duvar", trDef = "Örtücü yapı.")
        addW("table", "masa", trDef = "Mobilya.")
        addW("chair", "sandalye", trDef = "Oturacak yer.")
        addW("bed", "yatak", trDef = "Uyuma yeri.")
        addW("sofa", "koltuk", trDef = "Rahat oturacak.")
        addW("shelf", "raf", trDef = "Eşya koyma yeri.")
        addW("lamp", "lamba", trDef = "Işık kaynağı.")
        addW("carpet", "halı", trDef = "Yer örtüsü.")
        addW("curtain", "perde", trDef = "Pencere örtüsü.")
        addW("mirror", "ayna", trDef = "Yansıtıcı.")
        addW("key", "anahtar", trDef = "Kapı, dolap veya kilit açmaya yarayan küçük araç.")

        // --- KITCHEN ITEMS ---
        addW("plate", "tabak", trDef = "Yemek kabı.")
        addW("cup", "fincan / kupa", trDef = "İçecek kabı.")
        addW("glass", "bardak", trDef = "Cam kap.")
        addW("fork", "çatal", trDef = "Yemek aracı.")
        addW("spoon", "kaşık", trDef = "Yemek aracı.")
        addW("knife", "bıçak", trDef = "Kesme aracı.")
        addW("pan", "tava", trDef = "Pişirme kabı.")
        addW("fridge", "buzdolabı", trDef = "Soğutucu.")
        addW("oven", "fırın", trDef = "Pişirici.")

        // --- SCHOOL & WORK ---
        addW("school", "okul", trDef = "Eğitim yeri.")
        addW("teacher", "öğretmen", trDef = "Eğitimci.")
        addW("student", "öğrenci", trDef = "Öğrenen.")
        addW("book", "kitap", trDef = "Yazılı eser.")
        addW("pen", "kalem", trDef = "Yazma aracı.")
        addW("pencil", "kurşun kalem", trDef = "Yazma aracı.")
        addW("paper", "kağıt", trDef = "Yazma zemini.")
        addW("notebook", "defter", trDef = "Yazma kitabı.")
        addW("desk", "sıra / çalışma masası", trDef = "Okul mobilyası.")
        addW("bag", "çanta", trDef = "Taşıma aracı.")
        addW("work", "iş", trDef = "Meslek.")
        addW("job", "meslek", trDef = "Uğraş.")
        addW("office", "ofis", trDef = "Çalışma yeri.")
        addW("money", "para", trDef = "Ödeme aracı.")

        // --- JOBS ---
        addW("doctor", "doktor", trDef = "Sağlık uzmanı.")
        addW("nurse", "hemşire", trDef = "Sağlık görevlisi.")
        addW("pilot", "pilot", trDef = "Uçak kullanıcısı.")
        addW("engineer", "mühendis", trDef = "Teknik uzman.")
        addW("artist", "sanatçı", trDef = "Sanat yapan.")
        addW("singer", "şarkıcı", trDef = "Şarkı söyleyen.")
        addW("actor", "oyuncu", trDef = "Rol yapan.")
        addW("lawyer", "avukat", trDef = "Hukukçu.")
        addW("waiter", "garson", trDef = "Hizmet eden.")
        addW("chef", "şef / aşçı", trDef = "Yemek uzmanı.")

        // --- CLOTHES ---
        addW("clothes", "kıyafetler", trDef = "Giysiler.")
        addW("shirt", "gömlek", trDef = "Üst giysi.")
        addW("pants", "pantolon", trDef = "Alt giysi.")
        addW("dress", "elbise", trDef = "Kadın giysisi.")
        addW("skirt", "etek", trDef = "Alt giysi.")
        addW("shoes", "ayakkabı", trDef = "Ayak giysisi.")
        addW("hat", "şapka", trDef = "Başlık.")
        addW("socks", "çorap", trDef = "Ayak giysisi.")
        addW("jacket", "ceket", trDef = "Üst giysi.")

        // --- WEATHER ---
        addW("weather", "hava", trDef = "Hava durumu.")
        addW("sun", "güneş", trDef = "Gök cismi.")
        addW("sunny", "güneşli", "adjective", trDef = "Hava açık.")
        addW("rain", "yağmur", trDef = "Yağış.")
        addW("rainy", "yağmurlu", "adjective", trDef = "Yağışlı hava.")
        addW("cloud", "bulut", trDef = "Buhar kümesi.")
        addW("cloudy", "bulutlu", "adjective", trDef = "Hava kapalı.")
        addW("snow", "kar", trDef = "Beyaz yağış.")
        addW("snowy", "karlı", "adjective", trDef = "Kar yağışlı.")
        addW("wind", "rüzgar", trDef = "Hava akımı.")
        addW("windy", "rüzgarlı", "adjective", trDef = "Esintili hava.")
        addW("storm", "fırtına", trDef = "Şiddetli rüzgar.")
        addW("hot", "sıcak", "adjective", trDef = "Yüksek ısı.")
        addW("cold", "soğuk", "adjective", trDef = "Düşük ısı.")

        // --- NATURE & PLACES ---
        addW("nature", "doğa", trDef = "Tabiat.")
        addW("tree", "ağaç", trDef = "Odunsu bitki.")
        addW("grass", "çimen", trDef = "Yeşil bitki.")
        addW("flower", "çiçek", trDef = "Renkli bitki.")
        addW("sky", "gökyüzü", trDef = "Mavi tavan.")
        addW("earth", "dünya", trDef = "Gezegenimiz.")
        addW("world", "dünya", trDef = "Yaşadığımız yer.")
        addW("river", "nehir", trDef = "Akarsu.")
        addW("lake", "göl", trDef = "Durgun su.")
        addW("sea", "deniz", trDef = "Büyük tuzlu su.")
        addW("ocean", "okyanus", trDef = "Çok büyük deniz.")
        addW("mountain", "dağ", trDef = "Yüksek tepe.")
        addW("hill", "tepe", trDef = "Küçük yükselti.")
        addW("island", "ada", trDef = "Etrafı suyla çevrili.")
        addW("desert", "çöl", trDef = "Kum kumsalı.")
        addW("forest", "orman", trDef = "Ağaç topluluğu.")
        addW("city", "şehir", trDef = "Büyük yerleşim.")
        addW("town", "kasaba", trDef = "Küçük şehir.")
        addW("village", "köy", trDef = "Küçük yerleşim.")
        addW("park", "park", trDef = "Yeşil alan.")
        addW("street", "sokak", trDef = "Cadde.")
        addW("road", "yol", trDef = "Güzergah.")
        addW("bridge", "köprü", trDef = "Geçit.")
        addW("bank", "banka", trDef = "Para kurumu.")
        addW("hospital", "hastane", trDef = "Sağlık merkezi.")
        addW("airport", "havalimanı", trDef = "Uçak terminali.")
        addW("station", "istasyon", trDef = "Durak.")
        addW("shop", "dükkan", trDef = "Mağaza.")
        addW("market", "pazar", trDef = "Alışveriş yeri.")
        addW("cinema", "sinema", trDef = "Film izleme yeri.")
        addW("museum", "müze", trDef = "Sergi yeri.")

        // --- TRANSPORT ---
        addW("car", "araba", trDef = "Taşıt.")
        addW("bus", "otobüs", trDef = "Vasıta.")
        addW("train", "tren", trDef = "Vasıta.")
        addW("plane", "uçak", trDef = "Uçar.")
        addW("boat", "tekne", trDef = "Yüzer.")
        addW("ship", "gemi", trDef = "Büyük yüzer.")
        addW("bike", "bisiklet", trDef = "İki tekerlekli.")
        addW("truck", "kamyon", trDef = "Yük taşıtı.")
        addW("taxi", "taksi", trDef = "Taşıma aracı.")

        // --- ADJECTIVES (OPPOSITES) ---
        addW("big", "büyük", "adjective", "A1", "Hacimli.")
        addW("small", "küçük", "adjective", "A1", "Ufak.")
        addW("tall", "uzun", "adjective", "A2", "Boylu.")
        addW("short", "kısa", "adjective", "A2", "Alçak.")
        addW("long", "uzun", "adjective", "A1", "Mesafeli.")
        addW("old", "eski / yaşlı", "adjective", "A1", "Yaşını almış.")
        addW("new", "yeni", "adjective", "A1", "Taze.")
        addW("fast", "hızlı", "adjective", "A1", "Süratli.")
        addW("slow", "yavaş", "adjective", "A1", "Hantal.")
        addW("heavy", "ağır", "adjective", "A2", "Tartıda fazla.")
        addW("light", "hafif", "adjective", "A2", "Tartıda az.")
        addW("rich", "zengin", "adjective", "A2", "Varlıklı.")
        addW("poor", "fakir", "adjective", "A2", "Yoksul.")
        addW("clean", "temiz", "adjective", "A2", "Pak.")
        addW("dirty", "kirli", "adjective", "A2", "Pis.")
        addW("full", "dolu", "adjective", "A2", "İçinde olan.")
        addW("empty", "boş", "adjective", "A2", "İçinde olmayan.")
        addW("hard", "zor / sert", "adjective", "A2", "Güçlük çıkaran.")
        addW("easy", "kolay", "adjective", "A1", "Basit.")
        addW("strong", "güçlü", "adjective", "A2", "Kuvvetli.")
        addW("weak", "zayıf", "adjective", "A2", "Güçsüz.")
        addW("beautiful", "güzel", "adjective", "A1", "Hoş.")
        addW("ugly", "çirkin", "adjective", "A1", "Hoş olmayan.")

        // --- VERBS ---
        addW("eat", "yemek", "verb", trDef = "Beslenmek.")
        addW("drink", "içmek", "verb", trDef = "Sıvı tüketmek.")
        addW("sleep", "uyumak", "verb", trDef = "Dinlenmek.")
        addW("wake", "uyanmak", "verb", trDef = "Uykudan kalkmak.")
        addW("go", "gitmek", "verb", trDef = "İlerlemek.")
        addW("come", "gelmek", "verb", trDef = "Varmak.")
        addW("see", "görmek", "verb", trDef = "Bakmak.")
        addW("look", "bakmak", "verb", trDef = "İncelemek.")
        addW("watch", "izlemek", "verb", trDef = "Seyretmek.")
        addW("listen", "dinlemek", "verb", trDef = "Kulak vermek.")
        addW("hear", "duymak", "verb", trDef = "İşitmek.")
        addW("speak", "konuşmak", "verb", trDef = "Laf etmek.")
        addW("say", "söylemek", "verb", trDef = "Dile getirmek.")
        addW("tell", "anlatmak", "verb", trDef = "Bilgi vermek.")
        addW("ask", "sormak", "verb", trDef = "Sual sormak.")
        addW("answer", "cevaplamak", "verb", trDef = "Yanıt vermek.")
        addW("open", "açmak", "verb", trDef = "Aralamak.")
        addW("close", "kapatmak", "verb", trDef = "Örtmek.")
        addW("start", "başlamak", "verb", trDef = "Girişmek.")
        addW("stop", "durmak", "verb", trDef = "Kesilmek.")
        addW("play", "oynamak", "verb", trDef = "Eğlenmek.")
        addW("buy", "almak", "verb", trDef = "Satın almak.")
        addW("sell", "satmak", "verb", trDef = "Takas etmek.")
        addW("pay", "ödemek", "verb", trDef = "Para vermek.")
        addW("use", "kullanmak", "verb", trDef = "Faydalanmak.")
        addW("make", "yapmak", "verb", trDef = "Oluşturmak.")
        addW("do", "yapmak", "verb", trDef = "Eylemek.")
        addW("take", "almak", "verb", trDef = "Götürmek.")
        addW("give", "vermek", "verb", trDef = "Sunmak.")
        addW("find", "bulmak", "verb", trDef = "Keşfetmek.")
        addW("lose", "kaybetmek", "verb", trDef = "Yitirmek.")
        addW("get", "almak / edinmek", "verb", trDef = "Sahip olmak.")
        addW("put", "koymak", "verb", trDef = "Yerleştirmek.")
        addW("read", "okumak", "verb", trDef = "Metni incelemek.")
        addW("write", "yazmak", "verb", trDef = "Çiziktirmek.")
        addW("run", "koşmak", "verb", trDef = "Hızlı gitmek.")
        addW("walk", "yürümek", "verb", trDef = "Adım atmak.")
        addW("jump", "zıplamak", "verb", trDef = "Atlamak.")
        addW("swim", "yüzmek", "verb", trDef = "Suda ilerlemek.")
        addW("drive", "sürmek", "verb", trDef = "Araç kullanmak.")
        addW("fly", "uçmak", "verb", trDef = "Havada gitmek.")

        // --- TIME ---
        addW("time", "zaman", trDef = "Süre.")
        addW("hour", "saat", trDef = "60 dakika.")
        addW("minute", "dakika", trDef = "60 saniye.")
        addW("second", "saniye", trDef = "Zaman birimi.")
        addW("day", "gün", trDef = "24 saat.")
        addW("month", "ay", trDef = "Zaman dilimi.")
        addW("year", "yıl", trDef = "12 ay.")
        addW("morning", "sabah", trDef = "Günün başı.")
        addW("afternoon", "öğleden sonra", trDef = "Günün ortası.")
        addW("evening", "akşam", trDef = "Günün sonu.")
        addW("night", "gece", trDef = "Karanlık.")

        // --- ADVANCED & MISC ---
        addW("world", "dünya", cefr = "B1", trDef = "Gezegen.")
        addW("space", "uzay", cefr = "B1", trDef = "Boşluk.")
        addW("power", "güç", cefr = "A2", trDef = "Kuvvet.")
        addW("future", "gelecek", cefr = "B1", trDef = "İstikbal.")
        addW("hope", "umut", cefr = "B1", trDef = "Beklenti.")
        addW("life", "hayat", cefr = "B1", trDef = "Yaşam.")
        addW("peace", "barış", cefr = "B1", trDef = "Huzur.")
        addW("dream", "rüya / hayal", cefr = "A2", trDef = "Düş.")
        addW("love", "sevgi / aşk", trDef = "Sevme hissi.")
        addW("hate", "nefret", trDef = "Sevmeme hissi.")

        // --- FURNITURE & TOOLS ---
        addW("stove", "ocak", trDef = "Pişirme yeri.")
        addW("sink", "lavabo", trDef = "Yıkanma yeri.")
        addW("cabinet", "dolap", trDef = "Eşya gözü.")
        addW("bucket", "kova", trDef = "Su kabı.")
        addW("brush", "fırça", trDef = "Temizlik aracı.")
        addW("soap", "sabun", trDef = "Temizlik maddesi.")
        addW("towel", "havlu", trDef = "Kurulanma bezi.")
        addW("shampoo", "şampuan", trDef = "Saç temizleyici.")
        addW("toothbrush", "diş fırçası", trDef = "Diş temizleyici.")
        addW("hammer", "çekiç", trDef = "Vurma aleti.")
        addW("saw", "testere", trDef = "Kesme aleti.")
        addW("ladder", "merdiven", trDef = "Yükselme aracı.")

        // --- SHAPES & DIRECTIONS ---
        addW("circle", "daire", trDef = "Yuvarlak şekil.")
        addW("square", "kare", trDef = "Dört kenarlı eşit şekil.")
        addW("triangle", "üçgen", trDef = "Üç kenarlı şekil.")
        addW("rectangle", "dikdörtgen", trDef = "Dört kenarlı şekil.")
        addW("north", "kuzey", trDef = "Yön.")
        addW("south", "güney", trDef = "Yön.")
        addW("east", "doğu", trDef = "Yön.")
        addW("west", "batı", trDef = "Yön.")
        addW("left", "sol", trDef = "Yön.")
        addW("right", "sağ", trDef = "Yön.")

        // --- EMOTIONS & TRAITS ---
        addW("angry", "kızgın", "adjective", trDef = "Sinirli.")
        addW("surprised", "şaşırmış", "adjective", trDef = "Hayret etmiş.")
        addW("bored", "sıkılmış", "adjective", trDef = "Bunaldı.")
        addW("excited", "heyecanlı", "adjective", trDef = "Coşkulu.")
        addW("scared", "korkmuş", "adjective", trDef = "Ürkmüş.")
        addW("proud", "gururlu", "adjective", trDef = "Kıvançlı.")
        addW("clever", "zeki", "adjective", trDef = "Akıllı.")
        addW("stupid", "aptal", "adjective", trDef = "Aptalca.")
        addW("shy", "utangaç", "adjective", trDef = "Sıkılgan.")
        addW("lazy", "tembel", "adjective", trDef = "Üşengeç.")
        addW("honest", "dürüst", "adjective", trDef = "Doğru sözlü.")
        addW("funny", "komik", "adjective", trDef = "Güldüren.")

        // --- MORE VERBS ---
        addW("learn", "öğrenmek", "verb", trDef = "Bilgi edinmek.")
        addW("study", "çalışmak", "verb", trDef = "Ders yapmak.")
        addW("teach", "öğretmek", "verb", trDef = "Bilgi vermek.")
        addW("think", "düşünmek", "verb", trDef = "Akıl yürütmek.")
        addW("believe", "inanmak", "verb", trDef = "Güvenmek.")
        addW("forget", "unutmak", "verb", trDef = "Hatırlamamak.")
        addW("remember", "hatırlamak", "verb", trDef = "Anımsamak.")
        addW("laugh", "gülmek", "verb", trDef = "Kahkaha atmak.")
        addW("cry", "ağlamak", "verb", trDef = "Gözyaşı dökmek.")
        addW("win", "kazanmak", "verb", trDef = "Galip gelmek.")
        addW("lose", "kaybetmek", "verb", trDef = "Yitirmek.")
        addW("fight", "savaşmak", "verb", trDef = "Dövüşmek.")
        addW("help", "yardım etmek", "verb", trDef = "Destek olmak.")

        // --- WORLD 4 & 5 SPECIFICS ---
        addW("ancient", "antik", "adjective", trDef = "Çok eski.")
        addW("glory", "şan", trDef = "Görkem.")
        addW("prize", "ödül", trDef = "Mükafat.")
        addW("last", "son", "adjective", trDef = "En sondaki.")
        addW("end", "son", trDef = "Nihayet.")
        addW("journey", "yolculuk", trDef = "Seyahat.")
        addW("go", "gitmek", "verb", trDef = "İlerlemek.")
        addW("master", "usta", trDef = "Ehli.")
        addW("expert", "uzman", trDef = "Bilirkişi.")
        addW("wise", "bilge", "adjective", trDef = "Akıllı.")
        addW("smart", "zeki", "adjective", trDef = "Kavrayışlı.")
        addW("know", "bilmek", "verb", trDef = "Tanımak.")
        addW("past", "geçmiş", trDef = "Mazi.")
        addW("large", "geniş", "adjective", trDef = "Büyük.")
        addW("heavy", "ağır", "adjective", trDef = "Tartıda fazla.")
        addW("open", "açmak", "verb", trDef = "Aralamak.")
        addW("temple", "tapınak", trDef = "İbadethane.")
        addW("pray", "dua etmek", "verb", trDef = "Yakarışta bulunmak.")
        addW("peace", "barış", trDef = "Huzur.")
        addW("desert", "çöl", trDef = "Kum kumsalı.")
        addW("egypt", "mısır", trDef = "Mısır ülkesi.")
        addW("top", "üst", trDef = "Tepe.")
        addW("queen", "kraliçe", trDef = "Hükümdar kadın.")
        addW("palace", "saray", trDef = "Görkemli bina.")
        addW("rich", "zengin", "adjective", trDef = "Varlıklı.")
        addW("sell", "satmak", "verb", trDef = "Takas etmek.")
        addW("trader", "tüccar", trDef = "Tacir.")
        addW("coin", "madeni para", trDef = "Metal para.")
        addW("face", "yüz", trDef = "Çehre.")
        addW("garden", "bahçe", trDef = "Yeşillik.")
        addW("beautiful", "güzel", "adjective", trDef = "Hoş.")
        addW("safe", "güvenli", "adjective", trDef = "Emin.")
        addW("tunnel", "tünel", trDef = "Yeraltı yolu.")
        addW("long", "uzun", "adjective", trDef = "Mesafeli.")
        addW("deep", "derin", "adjective", trDef = "Engin.")
        addW("tower", "kule", trDef = "Yüksek bina.")
        addW("map", "harita", trDef = "Çizim.")
        addW("path", "yol", trDef = "Yolak.")
        addW("find", "bulmak", "verb", trDef = "Keşfetmek.")
        addW("way", "yol", trDef = "Yöntem.")
        addW("search", "aramak", "verb", trDef = "Bulmaya çalışmak.")

        // --- TECHNOLOGY & MEDIA ---
        addW("computer", "bilgisayar", trDef = "Elektronik veri işleyici.")
        addW("internet", "internet", trDef = "Genel ağ.")
        addW("phone", "telefon", trDef = "İletişim aracı.")
        addW("camera", "kamera", trDef = "Görüntü kaydedici.")
        addW("screen", "ekran", trDef = "Görüntüleyici yüzey.")
        addW("keyboard", "klavye", trDef = "Yazma tuş takımı.")
        addW("mouse_tech", "fare (teknoloji)", trDef = "İşaretleme aygıtı.")
        addW("email", "e-posta", trDef = "Elektronik mektup.")
        addW("website", "web sitesi", trDef = "İnternet sayfası.")
        addW("battery", "pil / batarya", trDef = "Enerji kaynağı.")
        addW("television", "televizyon", trDef = "Görüntüleme cihazı.")
        addW("radio", "radyo", trDef = "Sesli yayın aracı.")
        addW("message", "mesaj", trDef = "İleti.")

        // --- CLOTHING & ACCESSORIES ---
        addW("coat", "kaban / palto", trDef = "Dış giysi.")
        addW("tie", "kravat", trDef = "Boyun bağı.")
        addW("gloves", "eldiven", trDef = "El giysisi.")
        addW("scarf", "atkı", trDef = "Boyun örtüsü.")
        addW("belt", "kemer", trDef = "Bel bağı.")
        addW("wallet", "cüzdan", trDef = "Para kabı.")
        addW("watch", "kol saati", trDef = "Zaman gösterici.")
        addW("glasses", "gözlük", trDef = "Görme yardımcısı.")
        addW("umbrella", "şemsiye", trDef = "Yağmurdan koruyucu.")
        addW("purse", "el çantası", trDef = "Kadın çantası.")

        // --- SPORTS & HOBBIES ---
        addW("football", "futbol", trDef = "Ayak topu.")
        addW("tennis", "tenis", trDef = "Raket oyunu.")
        addW("basketball", "basketbol", trDef = "Pota topu.")
        addW("swimming", "yüzme", trDef = "Suda ilerleme.")
        addW("running", "koşu", trDef = "Hızlı yürüme.")
        addW("dancing", "dans", trDef = "Ritmik hareket.")
        addW("painting", "resim yapma", trDef = "Çizim sanatı.")
        addW("singing", "şarkı söyleme", trDef = "Müzik icrası.")
        addW("reading", "okuma", trDef = "Metin inceleme.")
        addW("cooking", "yemek yapma", trDef = "Gıda hazırlama.")
        addW("traveling", "seyahat", trDef = "Gezme.")

        // --- ABSTRACT & ADVANCED CONCEPTS ---
        addW("freedom", "özgürlük", cefr = "B1", trDef = "Hürriyet.")
        addW("justice", "adalet", cefr = "B1", trDef = "Hak.")
        addW("knowledge", "bilgi", cefr = "B1", trDef = "Malumat.")
        addW("experience", "tecrübe", cefr = "B1", trDef = "Deneyim.")
        addW("success", "başarı", cefr = "B1", trDef = "Muvaffakiyet.")
        addW("failure", "başarısızlık", cefr = "B1", trDef = "Yetersizlik.")
        addW("education", "eğitim", cefr = "B1", trDef = "Öğrenim.")
        addW("health", "sağlık", cefr = "A2", trDef = "Afiyet.")
        addW("government", "hükümet", cefr = "B1", trDef = "Yönetim.")
        addW("society", "toplum", cefr = "B1", trDef = "Cemiyet.")
        addW("culture", "kültür", cefr = "B1", trDef = "Ekin.")
        addW("language", "dil", cefr = "A2", trDef = "Lisan.")
        addW("power_abstract", "güç / erk", cefr = "B1", trDef = "Otorite.")

        // --- FEELINGS & EMOTIONS ---
        addW("joy", "sevinç", trDef = "Neşe.")
        addW("fear", "korku", trDef = "Ürkü.")
        addW("anger", "öfke", trDef = "Kızgınlık.")
        addW("surprise", "sürpriz", trDef = "Şaşkınlık.")
        addW("loneliness", "yalnızlık", trDef = "Tek başınalık.")
        addW("excitement", "heyecan", trDef = "Coşku.")
        addW("anxiety", "endişe", trDef = "Kaygı.")
        addW("courage", "cesaret", trDef = "Yiğitlik.")
        addW("patience", "sabır", trDef = "Tahammül.")

        // --- TRAVEL & GEOGRAPHY ---
        addW("border_geo", "sınır", trDef = "Hat.")
        addW("coast", "sahil", trDef = "Kıyı.")
        addW("continent", "kıta", trDef = "Anakara.")
        addW("country", "ülke", trDef = "Memleket.")
        addW("capital", "başkent", trDef = "Merkez şehir.")
        addW("bridge_structure", "köprü", trDef = "Geçit.")
        addW("valley_nature", "vadi", trDef = "Çukurluk.")
        addW("canyon", "kanyon", trDef = "Dar vadi.")
        addW("volcano", "yanardağ", trDef = "Lav püskürten dağ.")
        addW("earthquake", "deprem", trDef = "Yer sarsıntısı.")

        // --- COMMON ADVERBS ---
        addW("always", "her zaman", "adverb", trDef = "Daima.")
        addW("never", "asla", "adverb", trDef = "Hiçbir zaman.")
        addW("sometimes", "bazen", "adverb", trDef = "Ara sıra.")
        addW("often", "sık sık", "adverb", trDef = "Çok kez.")
        addW("usually", "genellikle", "adverb", trDef = "Çoğunlukla.")
        addW("quickly", "hızlıca", "adverb", trDef = "Süratle.")
        addW("slowly", "yavaşça", "adverb", trDef = "Ağır ağır.")
        addW("well_adv", "iyi", "adverb", trDef = "Güzelce.")
        addW("badly", "kötü", "adverb", trDef = "Fenaca.")

        // --- LEVEL COMPLETION WORDS ---
        addW("account", "hesap", trDef = "Banka veya kullanıcı hesabı.")
        addW("air", "hava", trDef = "Soluduğumuz gaz karışımı.")
        addW("art", "sanat", trDef = "Duygu ve düşünceyi yaratıcı biçimde anlatma.")
        addW("ball", "top", trDef = "Yuvarlak oyun aracı.")
        addW("bat", "yarasa", trDef = "Gece uçan memeli.")
        addW("beach", "plaj", trDef = "Deniz kıyısında kumluk yer.")
        addW("bench", "bank", trDef = "Oturmak için uzun sıra.")
        addW("best", "en iyi", "adjective", trDef = "En üstün olan.")
        addW("black", "siyah", "adjective", trDef = "En koyu renk.")
        addW("blow", "esmek", "verb", trDef = "Rüzgar hareket etmek.")
        addW("blue", "mavi", "adjective", trDef = "Gökyüzü rengi.")
        addW("border", "sınır", trDef = "İki yer arasındaki çizgi.")
        addW("brave", "cesur", "adjective", trDef = "Korkmadan davranan.")
        addW("burn", "yanmak", "verb", trDef = "Ateşle zarar görmek.")
        addW("busy", "meşgul", "adjective", trDef = "İşi çok olan.")
        addW("card", "kart", trDef = "Küçük sert kağıt veya ödeme aracı.")
        addW("climb", "tırmanmak", "verb", trDef = "Yukarı doğru çıkmak.")
        addW("cook", "pişirmek", "verb", trDef = "Yemek hazırlamak.")
        addW("cross", "geçmek", "verb", trDef = "Bir yerin karşısına geçmek.")
        addW("danger", "tehlike", trDef = "Zarar görme ihtimali.")
        addW("dark", "karanlık", "adjective", trDef = "Işığı az olan.")
        addW("down", "aşağı", "adverb", trDef = "Alt tarafa doğru.")
        addW("dragon", "ejderha", trDef = "Masallarda geçen güçlü yaratık.")
        addW("exit", "çıkış", trDef = "Dışarı çıkılan yer.")
        addW("far", "uzak", "adjective", trDef = "Mesafesi çok olan.")
        addW("feet", "ayaklar", trDef = "Ayak kelimesinin çoğulu.")
        addW("film", "film", trDef = "Sinema eseri.")
        addW("finish", "bitirmek", "verb", trDef = "Tamamlamak.")
        addW("fire", "ateş", trDef = "Isı ve ışık veren yanma.")
        addW("flag", "bayrak", trDef = "Bir ülke veya grubun simgesi.")
        addW("fun", "eğlence", trDef = "Keyif veren durum.")
        addW("game", "oyun", trDef = "Eğlenmek için yapılan etkinlik.")
        addW("gate", "kapı", trDef = "Bahçe veya sur girişi.")
        addW("giant", "dev", trDef = "Çok büyük varlık.")
        addW("gold", "altın", trDef = "Değerli sarı metal.")
        addW("green", "yeşil", "adjective", trDef = "Çimen rengi.")
        addW("grey", "gri", "adjective", trDef = "Siyah ile beyaz arası renk.")
        addW("ground", "zemin", trDef = "Üzerinde durulan yer.")
        addW("high", "yüksek", "adjective", trDef = "Yukarıda olan.")
        addW("history", "tarih", trDef = "Geçmiş olayların bilgisi.")
        addW("hotel", "otel", trDef = "Konaklama yeri.")
        addW("kind", "nazik", "adjective", trDef = "İyi ve düşünceli davranan.")
        addW("king", "kral", trDef = "Erkek hükümdar.")
        addW("land", "kara", trDef = "Deniz olmayan yeryüzü parçası.")
        addW("legs", "bacaklar", trDef = "Bacak kelimesinin çoğulu.")
        addW("letter", "harf", trDef = "Alfabe işareti.")
        addW("library", "kütüphane", trDef = "Kitapların bulunduğu yer.")
        addW("live", "yaşamak", "verb", trDef = "Hayatta olmak veya bir yerde oturmak.")
        addW("man", "adam", trDef = "Yetişkin erkek.")
        addW("move", "hareket etmek", "verb", trDef = "Yer değiştirmek.")
        addW("movie", "film", trDef = "Sinema filmi.")
        addW("nation", "millet", trDef = "Ortak kimliği olan halk.")
        addW("noisy", "gürültülü", "adjective", trDef = "Çok sesli.")
        addW("note", "not", trDef = "Kısa yazı.")
        addW("over", "üzerinden", "preposition", trDef = "Bir şeyin üstünden.")
        addW("paint", "boyamak", "verb", trDef = "Renk sürmek.")
        addW("person", "kişi", trDef = "İnsan.")
        addW("place", "yer", trDef = "Konum veya mekan.")
        addW("platform", "peron", trDef = "Tren beklenen yükseltilmiş alan.")
        addW("purple", "mor", "adjective", trDef = "Kırmızı ile mavi arası renk.")
        addW("quick", "çabuk", "adjective", trDef = "Hızlı.")
        addW("quiet", "sessiz", "adjective", trDef = "Gürültüsüz.")
        addW("ready", "hazır", "adjective", trDef = "Başlamak için uygun durumda.")
        addW("red", "kırmızı", "adjective", trDef = "Kan rengi.")
        addW("rest", "dinlenmek", "verb", trDef = "Yorgunluğu gidermek.")
        addW("ride", "binmek", "verb", trDef = "Taşıta veya bisiklete binmek.")
        addW("rock", "kaya", trDef = "Büyük taş parçası.")
        addW("round", "yuvarlak", "adjective", trDef = "Dairesel biçimde.")
        addW("sad", "üzgün", "adjective", trDef = "Mutsuz.")
        addW("save", "biriktirmek", "verb", trDef = "Korumak veya para saklamak.")
        addW("seat", "koltuk", trDef = "Oturulan yer.")
        addW("sick", "hasta", "adjective", trDef = "Sağlığı bozuk.")
        addW("silver", "gümüş", trDef = "Değerli gri metal.")
        addW("smoke", "duman", trDef = "Yanma sonucu çıkan gaz.")
        addW("soft", "yumuşak", "adjective", trDef = "Sert olmayan.")
        addW("sound", "ses", trDef = "Kulağın duyduğu titreşim.")
        addW("star", "yıldız", trDef = "Gökte ışık saçan cisim.")
        addW("statue", "heykel", trDef = "Taş veya metalden insan/nesne biçimi.")
        addW("stone", "taş", trDef = "Sert mineral parçası.")
        addW("summer", "yaz", trDef = "Sıcak mevsim.")
        addW("sweat", "ter", trDef = "Vücuttan çıkan sıvı.")
        addW("talk", "konuşmak", "verb", trDef = "Sözlü iletişim kurmak.")
        addW("thunder", "gök gürültüsü", trDef = "Fırtınada duyulan güçlü ses.")
        addW("ticket", "bilet", trDef = "Giriş veya yolculuk belgesi.")
        addW("tired", "yorgun", "adjective", trDef = "Dinlenmeye ihtiyaç duyan.")
        addW("travel", "seyahat etmek", "verb", trDef = "Bir yere yolculuk yapmak.")
        addW("turkey", "Türkiye", trDef = "Bir ülke adı.")
        addW("up", "yukarı", "adverb", trDef = "Üst tarafa doğru.")
        addW("view", "manzara", trDef = "Bakınca görünen görüntü.")
        addW("voice", "ses", trDef = "İnsanın çıkardığı ses.")
        addW("wait", "beklemek", "verb", trDef = "Bir süre durmak.")
        addW("wash", "yıkamak", "verb", trDef = "Su ile temizlemek.")
        addW("well", "iyi", "adjective", trDef = "Sağlıklı veya uygun durumda.")
        addW("wet", "ıslak", "adjective", trDef = "Su ile kaplanmış.")
        addW("white", "beyaz", "adjective", trDef = "Kar rengi.")
        addW("wing", "kanat", trDef = "Uçmayı sağlayan organ veya parça.")
        addW("woman", "kadın", trDef = "Yetişkin dişi insan.")
        addW("word", "kelime", trDef = "Anlamlı dil birimi.")
        addW("yellow", "sarı", "adjective", trDef = "Güneş rengine yakın renk.")
        addW("young", "genç", "adjective", trDef = "Yaşı küçük veya taze.")

        return words
    }

    private fun inferCefr(id: String, pos: String): String {
        val normalized = id.lowercase()
        if (normalized in a1Words) return "A1"
        if (normalized in b1Words) return "B1"
        if (normalized in a2Words) return "A2"

        val lengthScore = when {
            normalized.length >= 10 -> 2
            normalized.length >= 7 -> 1
            else -> 0
        }
        val posScore = when (pos) {
            "preposition", "adverb", "verb" -> 1
            else -> 0
        }
        val abstractScore = if (
            normalized.endsWith("tion") ||
            normalized.endsWith("ment") ||
            normalized.endsWith("ness") ||
            normalized.endsWith("ity") ||
            normalized.endsWith("ence") ||
            normalized.endsWith("ance")
        ) 1 else 0

        return when {
            lengthScore + posScore + abstractScore >= 3 -> "B1"
            lengthScore + posScore + abstractScore >= 1 -> "A2"
            else -> "A1"
        }
    }

    private fun simpleExampleSentence(id: String, tr: String): String {
        return when (id) {
            "apple" -> "I eat an apple."
            "banana" -> "The banana is yellow."
            "orange" -> "I drink orange juice."
            "grape" -> "The grapes are small."
            "lemon" -> "The lemon is sour."
            "strawberry" -> "The strawberry is red."
            "yellow" -> "The sun is yellow."
            "red" -> "The apple is red."
            "green" -> "The grass is green."
            "blue" -> "The sky is blue."
            "cat" -> "The cat is small."
            "dog" -> "The dog runs."
            "school" -> "I go to school."
            "book" -> "I read a book."
            else -> "This word means \"$tr\"."
        }
    }

    private fun simpleExampleTranslation(id: String, tr: String): String {
        return when (id) {
            "apple" -> "Bir elma yerim."
            "banana" -> "Muz sarıdır."
            "orange" -> "Portakal suyu içerim."
            "grape" -> "Üzümler küçüktür."
            "lemon" -> "Limon ekşidir."
            "strawberry" -> "Çilek kırmızıdır."
            "yellow" -> "Güneş sarıdır."
            "red" -> "Elma kırmızıdır."
            "green" -> "Çimen yeşildir."
            "blue" -> "Gökyüzü mavidir."
            "cat" -> "Kedi küçüktür."
            "dog" -> "Köpek koşar."
            "school" -> "Okula giderim."
            "book" -> "Kitap okurum."
            else -> "Bu kelime \"$tr\" demektir."
        }
    }

    private val a1Words = setOf(
        "hello", "yes", "no", "thanks", "please", "welcome", "sorry", "happy", "friend",
        "today", "yesterday", "tomorrow", "night", "week", "morning", "clock",
        "family", "father", "mother", "sister", "brother", "child", "baby", "son", "daughter",
        "boy", "girl", "body", "head", "face", "eye", "ear", "nose", "mouth", "hair",
        "arm", "hand", "leg", "foot", "animal", "cat", "dog", "bird", "fish", "horse",
        "cow", "sheep", "pig", "mouse", "rabbit", "food", "drink", "apple", "banana",
        "orange", "water", "milk", "bread", "cheese", "egg", "meat", "tea", "coffee",
        "house", "home", "room", "door", "window", "table", "chair", "bed", "key",
        "school", "teacher", "student", "book", "pen", "bag", "work", "job", "money",
        "shirt", "pants", "dress", "shoes", "hat", "sun", "rain", "snow", "hot", "cold",
        "tree", "flower", "sky", "river", "sea", "city", "town", "park", "street",
        "car", "bus", "train", "plane", "boat", "bike", "big", "small", "long", "old",
        "new", "fast", "slow", "easy", "beautiful", "eat", "sleep", "go", "come",
        "see", "look", "say", "open", "close", "start", "stop", "play", "buy", "use",
        "read", "write", "run", "walk", "time", "day", "month", "year", "love", "hate"
    )

    private val a2Words = setOf(
        "uncle", "aunt", "cousin", "grandfather", "grandmother", "shoulder", "finger",
        "knee", "back", "lion", "tiger", "elephant", "monkey", "snake", "spider",
        "bee", "butterfly", "shark", "dolphin", "grape", "lemon", "strawberry",
        "chicken", "sugar", "salt", "juice", "potato", "tomato", "onion", "carrot",
        "kitchen", "bedroom", "bathroom", "floor", "wall", "sofa", "shelf", "lamp",
        "carpet", "curtain", "mirror", "plate", "glass", "fork", "spoon", "knife",
        "pan", "fridge", "oven", "office", "doctor", "nurse", "pilot", "artist",
        "singer", "actor", "waiter", "chef", "jacket", "weather", "sunny", "rainy",
        "cloud", "cloudy", "snowy", "wind", "windy", "storm", "nature", "grass",
        "earth", "world", "lake", "ocean", "mountain", "hill", "island", "forest",
        "village", "road", "bridge", "bank", "shop", "market", "cinema", "museum",
        "truck", "taxi", "tall", "short", "heavy", "light", "rich", "poor", "clean",
        "dirty", "full", "empty", "hard", "strong", "weak", "ugly", "wake", "watch",
        "listen", "hear", "speak", "tell", "ask", "answer", "sell", "pay", "make",
        "do", "take", "give", "find", "lose", "get", "put", "jump", "swim", "drive",
        "fly", "hour", "minute", "second", "afternoon", "evening", "power", "dream",
        "health", "language", "joy", "fear", "anger", "country", "capital", "valley_nature",
        "always", "never", "sometimes", "often", "usually", "quickly", "slowly",
        "best", "black", "blue", "green", "grey", "red", "white", "yellow", "brave",
        "busy", "danger", "dark", "far", "finish", "fire", "fun", "game", "high",
        "hotel", "kind", "library", "move", "noisy", "quiet", "ready", "sad", "sick",
        "soft", "sound", "star", "stone", "summer", "tired", "travel", "wait", "wash",
        "well", "wet", "young"
    )

    private val b1Words = setOf(
        "space", "future", "hope", "life", "peace", "freedom", "justice", "knowledge",
        "experience", "success", "failure", "education", "government", "society", "culture",
        "power_abstract", "loneliness", "excitement", "anxiety", "courage", "patience",
        "continent", "border_geo", "coast", "canyon", "volcano", "earthquake", "account",
        "ancient", "glory", "journey", "master", "expert", "wise", "smart", "temple",
        "palace", "trader", "safe", "tunnel", "deep", "tower", "path", "way", "search",
        "computer", "internet", "camera", "screen", "keyboard", "email", "website",
        "battery", "television", "message", "umbrella", "football", "basketball",
        "swimming", "running", "dancing", "painting", "singing", "reading", "cooking",
        "traveling", "believe", "forget", "remember", "laugh", "cry", "fight", "help",
        "history", "nation", "platform", "statue", "thunder"
    )
}
