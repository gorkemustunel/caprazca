package com.example.data.repository

import com.example.data.model.WordEntity

object DictionaryData {
    fun getPreloadedWords(): List<WordEntity> {
        return listOf(
            // --- A1 LEVEL ---
            // Greetings & Basics
            WordEntity(
                id = "hello", english = "hello", turkish = "merhaba", partOfSpeech = "interjection", cefrLevel = "A1",
                englishDefinition = "Used as a greeting when you meet someone.",
                turkishDefinition = "Biriyle karşılaşıldığında kullanılan selamlama sözü.",
                exampleSentence = "Hello! How are you doing today?",
                exampleTranslation = "Merhaba! Bugün nasılsınız?", phonetic = "/həˈləʊ/",
                synonyms = "hi, hey", antonyms = "bye, goodbye", themes = "greetings, basics"
            ),
            WordEntity(
                id = "goodbye", english = "goodbye", turkish = "hoşçakal", partOfSpeech = "interjection", cefrLevel = "A1",
                englishDefinition = "Used when someone is leaving.",
                turkishDefinition = "Biri ayrılırken kullanılan esenlik sözü.",
                exampleSentence = "Goodbye, see you tomorrow!",
                exampleTranslation = "Hoşçakal, yarın görüşürüz!", phonetic = "/ˌɡʊdˈbaɪ/",
                synonyms = "bye, farewell", antonyms = "hello, welcome", themes = "greetings, basics"
            ),
            WordEntity(
                id = "please", english = "please", turkish = "lütfen", partOfSpeech = "adverb", cefrLevel = "A1",
                englishDefinition = "Used to add politeness to a request.",
                turkishDefinition = "Bir istekte bulunurken kibarlık katmak için kullanılır.",
                exampleSentence = "Could you bring me some water, please?",
                exampleTranslation = "Bana biraz su getirebilir misiniz, lütfen?", phonetic = "/pliːz/",
                synonyms = "kindly", antonyms = "", themes = "greetings, basics"
            ),
            WordEntity(
                id = "thanks", english = "thanks", turkish = "teşekkürler", partOfSpeech = "interjection", cefrLevel = "A1",
                englishDefinition = "Used to express gratitude.",
                turkishDefinition = "Minnettarlığı ifade etmek için kullanılır.",
                exampleSentence = "Thanks for your help with my homework.",
                exampleTranslation = "Ödevime yardım ettiğin için teşekkürler.", phonetic = "/θæŋks/",
                synonyms = "thank you, gratitude", antonyms = "", themes = "greetings, basics"
            ),
            WordEntity(
                id = "welcome", english = "welcome", turkish = "hoşgeldiniz", partOfSpeech = "interjection", cefrLevel = "A1",
                englishDefinition = "Used to greet someone arriving with pleasure.",
                turkishDefinition = "Gelen birini memnuniyetle karşılamak için kullanılır.",
                exampleSentence = "Welcome to our beautiful hotel!",
                exampleTranslation = "Güzel otelimize hoşgeldiniz!", phonetic = "/ˈwel.kəm/",
                synonyms = "greeting", antonyms = "farewell", themes = "greetings, basics"
            ),
            WordEntity(
                id = "yes", english = "yes", turkish = "evet", partOfSpeech = "adverb", cefrLevel = "A1",
                englishDefinition = "Used to express agreement or consent.",
                turkishDefinition = "Onay veya kabul ifade etmek için kullanılır.",
                exampleSentence = "Yes, I would love to go with you.",
                exampleTranslation = "Evet, seninle gitmeyi çok isterim.", phonetic = "/jes/",
                synonyms = "indeed, sure", antonyms = "no, nay", themes = "greetings, basics"
            ),
            WordEntity(
                id = "no", english = "no", turkish = "hayır", partOfSpeech = "adverb", cefrLevel = "A1",
                englishDefinition = "Used to express refusal or disagreement.",
                turkishDefinition = "Red veya uyuşmazlık belirtmek için kullanılır.",
                exampleSentence = "No, I do not want to watch that film.",
                exampleTranslation = "Hayır, o filmi izlemek istemiyorum.", phonetic = "/nəʊ/",
                synonyms = "negative", antonyms = "yes, yea", themes = "greetings, basics"
            ),
            WordEntity(
                id = "sorry", english = "sorry", turkish = "üzgün", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Feeling sad or regretful about a mistake.",
                turkishDefinition = "Bir hatadan ötürü üzüntü veya pişmanlık duymak.",
                exampleSentence = "I am sorry for breaking your glass.",
                exampleTranslation = "Bardağını kırdığım için üzgünüm.", phonetic = "/ˈsɒr.i/",
                synonyms = "apologetic, regretful", antonyms = "glad", themes = "greetings, basics"
            ),
            WordEntity(
                id = "friend", english = "friend", turkish = "arkadaş", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A person who you know well and like.",
                turkishDefinition = "Yakından tanınan ve sevilen kimse.",
                exampleSentence = "He is my best friend from childhood.",
                exampleTranslation = "O benim çocukluktan en yakın arkadaşım.", phonetic = "/frend/",
                synonyms = "buddy, companion", antonyms = "enemy, foe", themes = "family, greetings"
            ),
            WordEntity(
                id = "happy", english = "happy", turkish = "mutlu", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Feeling or showing pleasure and contentment.",
                turkishDefinition = "Memnuniyet ve sevinç duyan veya gösteren.",
                exampleSentence = "She felt very happy when she passed her exam.",
                exampleTranslation = "Sınavı geçince kendini çok mutlu hissetti.", phonetic = "/ˈhæp.i/",
                synonyms = "glad, joyful", antonyms = "sad, unhappy", themes = "feelings"
            ),

            // Family (Aile)
            WordEntity(
                id = "family", english = "family", turkish = "aile", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A group of people related by blood or marriage.",
                turkishDefinition = "Kan bağı veya evlilik yoluyla bağlı insan grubu.",
                exampleSentence = "I love spending my weekends with my family.",
                exampleTranslation = "Hafta sonlarımı ailemle geçirmeyi seviyorum.", phonetic = "/ˈfæm.əl.i/",
                synonyms = "household, relatives", antonyms = "", themes = "family"
            ),
            WordEntity(
                id = "father", english = "father", turkish = "baba", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A male parent.",
                turkishDefinition = "Erkek ebeveyn.",
                exampleSentence = "My father works as a high school teacher.",
                exampleTranslation = "Babam lisede öğretmen olarak çalışıyor.", phonetic = "/ˈfɑː.ðər/",
                synonyms = "dad, papa", antonyms = "mother", themes = "family"
            ),
            WordEntity(
                id = "mother", english = "mother", turkish = "anne", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A female parent.",
                turkishDefinition = "Kadın ebeveyn.",
                exampleSentence = "My mother makes the best chocolate cookies.",
                exampleTranslation = "Annem en güzel çikolatalı kurabiyeleri yapar.", phonetic = "/ˈmʌð.ər/",
                synonyms = "mom, mum", antonyms = "father", themes = "family"
            ),
            WordEntity(
                id = "brother", english = "brother", turkish = "erkek kardeş", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A boy or man who shares the same parents.",
                turkishDefinition = "Aynı anne babaya sahip erkek çocuk veya adam.",
                exampleSentence = "My brother is three years older than me.",
                exampleTranslation = "Erkek kardeşim benden üç yaş büyük.", phonetic = "/ˈbrʌð.ər/",
                synonyms = "sibling", antonyms = "sister", themes = "family"
            ),
            WordEntity(
                id = "sister", english = "sister", turkish = "kız kardeş", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A girl or woman who shares the same parents.",
                turkishDefinition = "Aynı anne babaya sahip kız çocuk veya kadın.",
                exampleSentence = "I have one sister who lives in Istanbul.",
                exampleTranslation = "İstanbul'da yaşayan bir kız kardeşim var.", phonetic = "/ˈsɪs.tər/",
                synonyms = "sibling", antonyms = "brother", themes = "family"
            ),
            WordEntity(
                id = "son", english = "son", turkish = "oğul", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A person's male child.",
                turkishDefinition = "Bir kimsenin erkek çocuğu.",
                exampleSentence = "They are proud of their young son.",
                exampleTranslation = "Genç oğullarıyla gurur duyuyorlar.", phonetic = "/sʌn/",
                synonyms = "boy", antonyms = "daughter", themes = "family"
            ),
            WordEntity(
                id = "daughter", english = "daughter", turkish = "kız evlat", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A person's female child.",
                turkishDefinition = "Bir kimsenin kız çocuğu.",
                exampleSentence = "Their daughter is studying art at university.",
                exampleTranslation = "Kızları üniversitede sanat okuyor.", phonetic = "/ˈdɔː.tər/",
                synonyms = "girl", antonyms = "son", themes = "family"
            ),
            WordEntity(
                id = "child", english = "child", turkish = "çocuk", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A young human being below the age of puberty.",
                turkishDefinition = "Ergenlik çağına gelmemiş genç insan.",
                exampleSentence = "The child was playing happily in the garden.",
                exampleTranslation = "Çocuk bahçede mutlu bir şekilde oynuyordu.", phonetic = "/tʃaɪld/",
                synonyms = "kid, youngster", antonyms = "adult", themes = "family"
            ),
            WordEntity(
                id = "baby", english = "baby", turkish = "bebek", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A very young child or infant.",
                turkishDefinition = "Çok küçük çocuk veya süt çocuğu.",
                exampleSentence = "The baby is sleeping peacefully in the crib.",
                exampleTranslation = "Bebek beşikte huzurla uyuyor.", phonetic = "/ˈbeɪ.bi/",
                synonyms = "infant", antonyms = "adult", themes = "family"
            ),
            WordEntity(
                id = "uncle", english = "uncle", turkish = "amca", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "The brother of one's father or mother.",
                turkishDefinition = "Anne veya babanın erkek kardeşi.",
                exampleSentence = "My uncle lives on a farm near the city.",
                exampleTranslation = "Amcam şehrin yakınlarında bir çiftlikte yaşıyor.", phonetic = "/ˈʌŋ.kl̩/",
                synonyms = "relative", antonyms = "aunt", themes = "family"
            ),

            // Home & School
            WordEntity(
                id = "house", english = "house", turkish = "ev", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A building for human habitation.",
                turkishDefinition = "İnsanların oturması için yapılmış bina.",
                exampleSentence = "They live in a beautiful house with a blue door.",
                exampleTranslation = "Mavi kapılı güzel bir evde yaşıyorlar.", phonetic = "/haʊs/",
                synonyms = "home, residence", antonyms = "", themes = "home"
            ),
            WordEntity(
                id = "room", english = "room", turkish = "oda", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A part of a building enclosed by walls.",
                turkishDefinition = "Duvarlarla çevrili bir binanın her bir bölümü.",
                exampleSentence = "My room is decorated in yellow and green.",
                exampleTranslation = "Odam sarı ve yeşil renklerde dekore edilmiştir.", phonetic = "/ruːm/",
                synonyms = "chamber", antonyms = "", themes = "home"
            ),
            WordEntity(
                id = "window", english = "window", turkish = "pencere", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "An opening in a wall to let in light and air.",
                turkishDefinition = "Işık ve hava girmesi için duvarda bırakılan açıklık.",
                exampleSentence = "Please open the window, it is hot inside.",
                exampleTranslation = "Lütfen pencereyi açın, içerisi çok sıcak.", phonetic = "/ˈwɪn.dəʊ/",
                synonyms = "opening", antonyms = "", themes = "home"
            ),
            WordEntity(
                id = "door", english = "door", turkish = "kapı", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A barrier used to close off an entrance.",
                turkishDefinition = "Girişi kapatmak için kullanılan bariyer.",
                exampleSentence = "Knock on the door before you enter the office.",
                exampleTranslation = "Ofise girmeden önce kapıyı çalın.", phonetic = "/dɔːr/",
                synonyms = "gate, entry", antonyms = "", themes = "home"
            ),
            WordEntity(
                id = "table", english = "table", turkish = "masa", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A piece of furniture with a flat top and legs.",
                turkishDefinition = "Düz bir tepesi ve ayakları olan mobilya parçası.",
                exampleSentence = "Put the plates on the table for dinner.",
                exampleTranslation = "Akşam yemeği için tabakları masaya koy.", phonetic = "/ˈteɪ.bl̩/",
                synonyms = "desk", antonyms = "", themes = "home"
            ),
            WordEntity(
                id = "chair", english = "chair", turkish = "sandalye", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A seat for one person, with a back support.",
                turkishDefinition = "Sırt desteği olan tek kişilik oturacak yer.",
                exampleSentence = "Sit on the comfortable chair next to the fire.",
                exampleTranslation = "Ateşin yanındaki rahat sandalyeye oturun.", phonetic = "/tʃeər/",
                synonyms = "seat, stool", antonyms = "", themes = "home"
            ),
            WordEntity(
                id = "school", english = "school", turkish = "okul", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "An institution for educating children.",
                turkishDefinition = "Çocukların eğitilmesi için kurulmuş kurum.",
                exampleSentence = "Children go to school to learn many things.",
                exampleTranslation = "Çocuklar birçok şey öğrenmek için okula giderler.", phonetic = "/skuːl/",
                synonyms = "academy", antonyms = "", themes = "school"
            ),
            WordEntity(
                id = "teacher", english = "teacher", turkish = "öğretmen", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A person who teaches, especially in school.",
                turkishDefinition = "Özellikle okulda eğitim veren kimse.",
                exampleSentence = "Our teacher explained the lesson clearly today.",
                exampleTranslation = "Öğretmenimiz bugün konuyu açıkça anlattı.", phonetic = "/ˈtiː.tʃər/",
                synonyms = "educator, tutor", antonyms = "student, pupil", themes = "school"
            ),
            WordEntity(
                id = "student", english = "student", turkish = "öğrenci", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A person who is studying at school or college.",
                turkishDefinition = "Okulda veya kolejde okuyan kimse.",
                exampleSentence = "The university student passed his final exams.",
                exampleTranslation = "Üniversite öğrencisi final sınavlarını geçti.", phonetic = "/ˈstjuː.dənt/",
                synonyms = "pupil, learner", antonyms = "teacher, mentor", themes = "school"
            ),
            WordEntity(
                id = "book", english = "book", turkish = "kitap", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A written work that is published in printed format.",
                turkishDefinition = "Basılı formatta yayınlanan yazılı eser.",
                exampleSentence = "I love reading an interesting book before sleep.",
                exampleTranslation = "Uyumadan önce ilginç bir kitap okumayı seviyorum.", phonetic = "/bʊk/",
                synonyms = "novel, volume", antonyms = "", themes = "school, home"
            ),

            // Food & Drinks
            WordEntity(
                id = "apple", english = "apple", turkish = "elma", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A round fruit with red, green, or yellow skin.",
                turkishDefinition = "Kırmızı, yeşil veya sarı kabuklu yuvarlak meyve.",
                exampleSentence = "She eats a sweet red apple every morning.",
                exampleTranslation = "Her sabah tatlı bir kırmızı elma yer.", phonetic = "/ˈæp.l̩/",
                synonyms = "fruit", antonyms = "", themes = "food"
            ),
            WordEntity(
                id = "water", english = "water", turkish = "su", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A clear liquid that falls as rain and is essential for life.",
                turkishDefinition = "Yağmur olarak yağan ve yaşam için gerekli olan berrak sıvı.",
                exampleSentence = "Drink eight glasses of clean water every day.",
                exampleTranslation = "Her gün sekiz bardak temiz su için.", phonetic = "/ˈwɔː.tər/",
                synonyms = "aqua", antonyms = "", themes = "food"
            ),
            WordEntity(
                id = "bread", english = "bread", turkish = "ekmek", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A common food made from flour, water, and yeast baked together.",
                turkishDefinition = "Un, su ve mayanın birlikte pişirilmesiyle yapılan yaygın yiyecek.",
                exampleSentence = "I always buy fresh bread from the bakery.",
                exampleTranslation = "Fırından her zaman taze ekmek alırım.", phonetic = "/bred/",
                synonyms = "loaf", antonyms = "", themes = "food"
            ),
            WordEntity(
                id = "milk", english = "milk", turkish = "süt", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A white liquid produced by female mammals for feeding their young.",
                turkishDefinition = "Dişi memelilerin yavrularını beslemek için ürettiği beyaz sıvı.",
                exampleSentence = "The children drank hot milk before bed.",
                exampleTranslation = "Çocuklar yatmadan önce sıcak süt içtiler.", phonetic = "/mɪlk/",
                synonyms = "beverage", antonyms = "", themes = "food"
            ),
            WordEntity(
                id = "tea", english = "tea", turkish = "çay", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A popular hot drink made by infusing dried leaves in water.",
                turkishDefinition = "Kurutulmuş yaprakların suda demlenmesiyle yapılan popüler sıcak içecek.",
                exampleSentence = "Turkish tea is usually served in small glasses.",
                exampleTranslation = "Türk çayı genellikle küçük bardaklarda servis edilir.", phonetic = "/tiː/",
                synonyms = "brew", antonyms = "", themes = "food"
            ),
            WordEntity(
                id = "coffee", english = "coffee", turkish = "kahve", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A dark hot drink made from roasted and ground beans.",
                turkishDefinition = "Kavrulmuş ve öğütülmüş çekirdeklerden yapılan koyu sıcak içecek.",
                exampleSentence = "I cannot start my day without a hot cup of coffee.",
                exampleTranslation = "Sıcak bir fincan kahve içmeden güne başlayamam.", phonetic = "/ˈkɒf.i/",
                synonyms = "espresso", antonyms = "", themes = "food"
            ),
            WordEntity(
                id = "cheese", english = "cheese", turkish = "peynir", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A yellow or white food product made from milk.",
                turkishDefinition = "Sütten yapılan sarı veya beyaz yiyecek ürünü.",
                exampleSentence = "I put yellow cheese in my morning sandwich.",
                exampleTranslation = "Sabah sandviçime kaşar peyniri koydum.", phonetic = "/tʃiːz/",
                synonyms = "dairy product", antonyms = "", themes = "food"
            ),
            WordEntity(
                id = "orange", english = "orange", turkish = "portakal", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A round citrus fruit with sweet orange juice inside.",
                turkishDefinition = "İçinde tatlı portakal suyu olan yuvarlak bir narenciye meyvesi.",
                exampleSentence = "Drinking fresh orange juice gives you Vitamin C.",
                exampleTranslation = "Taze portakal suyu içmek C vitamini sağlar.", phonetic = "/ˈɒr.ɪndʒ/",
                synonyms = "citrus", antonyms = "", themes = "food"
            ),
            WordEntity(
                id = "meat", english = "meat", turkish = "et", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "The flesh of an animal used as food.",
                turkishDefinition = "Yiyecek olarak kullanılan hayvan eti.",
                exampleSentence = "We grilled some delicious meat on the barbecue.",
                exampleTranslation = "Mangalda lezzetli etler ızgara yaptık.", phonetic = "/miːt/",
                synonyms = "flesh, beef", antonyms = "vegetable", themes = "food"
            ),
            WordEntity(
                id = "sugar", english = "sugar", turkish = "şeker", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A sweet crystalline substance obtained from sugar cane or sugar beet.",
                turkishDefinition = "Şeker kamışı veya şeker pancarından elde edilen tatlı kristal madde.",
                exampleSentence = "Do you take sugar or honey in your tea?",
                exampleTranslation = "Çayınıza şeker mi bal mı alırsınız?", phonetic = "/ˈʃʊɡ.ər/",
                synonyms = "sweetener", antonyms = "", themes = "food"
            ),

            // Time & Days
            WordEntity(
                id = "today", english = "today", turkish = "bugün", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "On or during this present day.",
                turkishDefinition = "İçinde bulunulan günde.",
                exampleSentence = "Today is the happiest day of my life.",
                exampleTranslation = "Bugün hayatımın en mutlu günü.", phonetic = "/təˈdeɪ/",
                synonyms = "current day", antonyms = "yesterday, tomorrow", themes = "time"
            ),
            WordEntity(
                id = "yesterday", english = "yesterday", turkish = "dün", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "On the day before today.",
                turkishDefinition = "Bugünden bir önceki günde.",
                exampleSentence = "Yesterday was cold, but today is warm.",
                exampleTranslation = "Dün soğuktu ama bugün sıcak.", phonetic = "/ˈjes.tə.deɪ/",
                synonyms = "prior day", antonyms = "today, tomorrow", themes = "time"
            ),
            WordEntity(
                id = "tomorrow", english = "tomorrow", turkish = "yarın", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "On the day after today.",
                turkishDefinition = "Bugünden bir sonraki günde.",
                exampleSentence = "Tomorrow we will go on a picnic in the park.",
                exampleTranslation = "Yarın parkta pikniğe gideceğiz.", phonetic = "/təˈmɒr.əʊ/",
                synonyms = "next day", antonyms = "yesterday, today", themes = "time"
            ),
            WordEntity(
                id = "night", english = "night", turkish = "gece", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "The time from sunset to sunrise.",
                turkishDefinition = "Gün batımından gün doğumuna kadar olan süre.",
                exampleSentence = "Stars shine very brightly during the night.",
                exampleTranslation = "Yıldızlar gece boyunca çok parlak parlar.", phonetic = "/naɪt/",
                synonyms = "darkness", antonyms = "day, daytime", themes = "time"
            ),
            WordEntity(
                id = "morning", english = "morning", turkish = "sabah", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "The period of time between dawn and midday.",
                turkishDefinition = "Şafak ile öğle vakti arasındaki süre.",
                exampleSentence = "I love the fresh air in the early morning.",
                exampleTranslation = "Sabahın erken saatlerindeki taze havayı seviyorum.", phonetic = "/ˈmɔː.nɪŋ/",
                synonyms = "dawn", antonyms = "evening, night", themes = "time"
            ),
            WordEntity(
                id = "week", english = "week", turkish = "hafta", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A period of seven days.",
                turkishDefinition = "Yedi günlük süre.",
                exampleSentence = "We have five working days in a week.",
                exampleTranslation = "Bir haftada beş iş günümüz var.", phonetic = "/wiːk/",
                synonyms = "seven days", antonyms = "", themes = "time"
            ),
            WordEntity(
                id = "month", english = "month", turkish = "ay", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "One of the twelve periods of time into which a year is divided.",
                turkishDefinition = "Bir yılın bölündüğü on iki zaman diliminden biri.",
                exampleSentence = "January is the coldest month of the year.",
                exampleTranslation = "Ocak, yılın en soğuk ayıdır.", phonetic = "/mʌnθ/",
                synonyms = "period", antonyms = "", themes = "time"
            ),
            WordEntity(
                id = "year", english = "year", turkish = "yıl", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A period of 365 days (or 366 in a leap year).",
                turkishDefinition = "365 günlük süre (artık yılda 366).",
                exampleSentence = "She spent a year studying English in London.",
                exampleTranslation = "Londra'da İngilizce çalışarak bir yıl geçirdi.", phonetic = "/jɪər/",
                synonyms = "twelve months", antonyms = "", themes = "time"
            ),
            WordEntity(
                id = "hour", english = "hour", turkish = "saat", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A unit of time equal to 60 minutes.",
                turkishDefinition = "60 dakikaya eşit olan zaman birimi.",
                exampleSentence = "The bus ride takes about one hour.",
                exampleTranslation = "Otobüs yolculuğu yaklaşık bir saat sürüyor.", phonetic = "/aʊər/",
                synonyms = "sixty minutes", antonyms = "", themes = "time"
            ),
            WordEntity(
                id = "clock", english = "clock", turkish = "saat aygıtı", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "An instrument for measuring and showing time.",
                turkishDefinition = "Zamanı ölçen ve gösteren cihaz.",
                exampleSentence = "The clock on the wall shows exactly ten o'clock.",
                exampleTranslation = "Duvardaki saat tam olarak saat onu gösteriyor.", phonetic = "/klɒk/",
                synonyms = "watch, timepiece", antonyms = "", themes = "time"
            ),

            // --- A2 LEVEL ---
            // City & Transportation
            WordEntity(
                id = "city", english = "city", turkish = "şehir", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A large and permanent human settlement.",
                turkishDefinition = "Büyük ve kalıcı insan yerleşimi.",
                exampleSentence = "New York is a famous city in America.",
                exampleTranslation = "New York, Amerika'da ünlü bir şehirdir.", phonetic = "/ˈsɪt.i/",
                synonyms = "town, metropolis", antonyms = "village, country", themes = "city"
            ),
            WordEntity(
                id = "street", english = "street", turkish = "sokak", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A public road in a city or town, with buildings on side.",
                turkishDefinition = "Şehir veya kasabada, yanlarında binalar olan halka açık yol.",
                exampleSentence = "Children were playing football in the quiet street.",
                exampleTranslation = "Çocuklar sakin sokakta futbol oynuyorlardı.", phonetic = "/striːt/",
                synonyms = "road, avenue", antonyms = "", themes = "city"
            ),
            WordEntity(
                id = "bridge", english = "bridge", turkish = "köprü", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A structure carrying a road across a river or valley.",
                turkishDefinition = "Bir yolu bir nehir veya vadinin üzerinden geçiren yapı.",
                exampleSentence = "We drove across the Bosphorus Bridge in Istanbul.",
                exampleTranslation = "İstanbul'da Boğaziçi Köprüsü'nün üzerinden geçtik.", phonetic = "/brɪdʒ/",
                synonyms = "overpass", antonyms = "", themes = "city"
            ),
            WordEntity(
                id = "station", english = "station", turkish = "istasyon", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A stopping place on a railway line where passengers get on.",
                turkishDefinition = "Yolcuların bindiği demiryolu hattındaki durak yeri.",
                exampleSentence = "I will meet you outside the train station.",
                exampleTranslation = "Seninle tren istasyonunun dışında buluşacağım.", phonetic = "/ˈsteɪ.ʃən/",
                synonyms = "terminal", antonyms = "", themes = "city, transport"
            ),
            WordEntity(
                id = "airport", english = "airport", turkish = "havalimanı", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A complex where airplanes land and take off, with passenger facilities.",
                turkishDefinition = "Uçakların indiği ve kalktığı yolcu tesislerinin bulunduğu kompleks.",
                exampleSentence = "Our plane landed at Heathrow Airport in London.",
                exampleTranslation = "Uçağımız Londra'daki Heathrow Havalimanı'na indi.", phonetic = "/ˈeə.pɔːt/",
                synonyms = "airfield", antonyms = "", themes = "city, transport"
            ),
            WordEntity(
                id = "bicycle", english = "bicycle", turkish = "bisiklet", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A vehicle with two wheels, propelled by pedals.",
                turkishDefinition = "Pedallarla hareket ettirilen, iki tekerlekli araç.",
                exampleSentence = "He rides his bicycle to school every day.",
                exampleTranslation = "Her gün okula bisikletiyle gidiyor.", phonetic = "/ˈbaɪ.sɪ.kl̩/",
                synonyms = "bike, cycle", antonyms = "", themes = "transport"
            ),
            WordEntity(
                id = "train", english = "train", turkish = "tren", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A series of connected railway carriages drawn by an engine.",
                turkishDefinition = "Lokomotif tarafından çekilen birbirine bağlı vagonlar serisi.",
                exampleSentence = "We travelled to Paris by high-speed train.",
                exampleTranslation = "Paris'e hızlı trenle seyahat ettik.", phonetic = "/treɪn/",
                synonyms = "railway", antonyms = "", themes = "transport"
            ),
            WordEntity(
                id = "ticket", english = "ticket", turkish = "bilet", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A printed piece of paper showing you paid for a journey or event.",
                turkishDefinition = "Bir yolculuk veya etkinlik için ödeme yaptığınızı gösteren basılı kağıt parçası.",
                exampleSentence = "Show your ticket to the driver when boarding.",
                exampleTranslation = "Binerken biletinizi sürücüye gösterin.", phonetic = "/ˈtɪk.ɪt/",
                synonyms = "pass, coupon", antonyms = "", themes = "transport"
            ),
            WordEntity(
                id = "journey", english = "journey", turkish = "yolculuk", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "An act of traveling from one place to another.",
                turkishDefinition = "Bir yerden başka bir yere seyahat etme eylemi.",
                exampleSentence = "Have a safe journey to your destination!",
                exampleTranslation = "Varış noktanıza güvenli bir yolculuk dilerim!", phonetic = "/ˈdʒɜː.ni/",
                synonyms = "trip, voyage", antonyms = "", themes = "transport"
            ),
            WordEntity(
                id = "tourist", english = "tourist", turkish = "turist", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person who is visiting a place for pleasure or holiday.",
                turkishDefinition = "Bir yeri keyif veya tatil amacıyla ziyaret eden kimse.",
                exampleSentence = "The city center was crowded with foreign tourists.",
                exampleTranslation = "Şehir merkezi yabancı turistlerle doluydu.", phonetic = "/ˈtʊə.rɪst/",
                synonyms = "visitor, traveler", antonyms = "resident", themes = "city, transport"
            ),

            // Shops & Shopping (Mağazalar)
            WordEntity(
                id = "market", english = "market", turkish = "pazar", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A regular gathering of people for buying and selling provisions.",
                turkishDefinition = "Yiyecek ve malzeme alıp satmak için insanların düzenli olarak toplandığı yer.",
                exampleSentence = "We bought fresh tomatoes at the street market.",
                exampleTranslation = "Sokak pazarından taze domates aldık.", phonetic = "/ˈmɑː.kɪt/",
                synonyms = "bazaar", antonyms = "", themes = "shopping"
            ),
            WordEntity(
                id = "price", english = "price", turkish = "fiyat", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "The amount of money expected or required as payment for something.",
                turkishDefinition = "Bir şey için ödeme olarak beklenen veya gereken para miktarı.",
                exampleSentence = "The price of this gold watch is too high.",
                exampleTranslation = "Bu altın saatin fiyatı çok yüksek.", phonetic = "/praɪs/",
                synonyms = "cost, value", antonyms = "", themes = "shopping"
            ),
            WordEntity(
                id = "cheap", english = "cheap", turkish = "ucuz", partOfSpeech = "adjective", cefrLevel = "A2",
                englishDefinition = "Low in price, costing little money.",
                turkishDefinition = "Fiyatı düşük, az paraya mal olan.",
                exampleSentence = "These blue shoes are very cheap and comfortable.",
                exampleTranslation = "Bu mavi ayakkabılar çok ucuz ve rahat.", phonetic = "/tʃiːp/",
                synonyms = "inexpensive", antonyms = "expensive, costly", themes = "shopping"
            ),
            WordEntity(
                id = "expensive", english = "expensive", turkish = "pahalı", partOfSpeech = "adjective", cefrLevel = "A2",
                englishDefinition = "Costing a lot of money.",
                turkishDefinition = "Çok paraya mal olan.",
                exampleSentence = "Eating at that French restaurant is very expensive.",
                exampleTranslation = "O Fransız restoranında yemek yemek çok pahalı.", phonetic = "/ɪkˈspen.sɪv/",
                synonyms = "costly, high-priced", antonyms = "cheap, inexpensive", themes = "shopping"
            ),
            WordEntity(
                id = "money", english = "money", turkish = "para", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "Current medium of exchange in the form of coins and banknotes.",
                turkishDefinition = "Madeni para ve banknot şeklindeki mevcut değişim aracı.",
                exampleSentence = "I did not carry enough money to buy that laptop.",
                exampleTranslation = "O dizüstü bilgisayarı almak için yanımda yeterli para taşımıyordum.", phonetic = "/ˈmʌn.i/",
                synonyms = "cash, currency", antonyms = "", themes = "shopping"
            ),
            WordEntity(
                id = "customer", english = "customer", turkish = "müşteri", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person who buys goods or services from a shop.",
                turkishDefinition = "Bir dükkandan mal veya hizmet alan kimse.",
                exampleSentence = "The helpful shopkeeper was serving an old customer.",
                exampleTranslation = "Yardımsever dükkan sahibi yaşlı bir müşteriye hizmet ediyordu.", phonetic = "/ˈkʌs.tə.mər/",
                synonyms = "buyer, client", antonyms = "seller", themes = "shopping"
            ),
            WordEntity(
                id = "clothes", english = "clothes", turkish = "giysiler", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "Items worn on the body, such as trousers, shirts, and jackets.",
                turkishDefinition = "Pantolon, gömlek ve ceket gibi vücuda giyilen şeyler.",
                exampleSentence = "She packed warm clothes for her winter trip.",
                exampleTranslation = "Kış gezisi için sıcak giysiler paketledi.", phonetic = "/kləʊðz/",
                synonyms = "garments, apparel", antonyms = "", themes = "shopping"
            ),
            WordEntity(
                id = "wallet", english = "wallet", turkish = "cüzdan", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A small, flat case used to carry cash and bank cards.",
                turkishDefinition = "Nakit para ve banka kartlarını taşımak için kullanılan küçük, düz çanta.",
                exampleSentence = "I realized that I left my wallet at home.",
                exampleTranslation = "Cüzdanımı evde bıraktığımı fark ettim.", phonetic = "/ˈwɒl.ɪt/",
                synonyms = "purse, pocketbook", antonyms = "", themes = "shopping, basics"
            ),
            WordEntity(
                id = "discount", english = "discount", turkish = "indirim", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A reduction in the usual cost of something.",
                turkishDefinition = "Bir şeyin normal maliyetindeki azalma.",
                exampleSentence = "The shop offers a twenty percent discount on coats.",
                exampleTranslation = "Dükkan, kabanlarda yüzde yirmi indirim sunuyor.", phonetic = "/ˈdɪs.kaʊnt/",
                synonyms = "reduction, rebate", antonyms = "increase", themes = "shopping"
            ),
            WordEntity(
                id = "gift", english = "gift", turkish = "hediye", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A thing given willingly to someone without payment.",
                turkishDefinition = "Birine ödeme yapmadan isteyerek verilen şey.",
                exampleSentence = "They gave me a beautiful gift for my graduation.",
                exampleTranslation = "Mezuniyetim için bana güzel bir hediye verdiler.", phonetic = "/ɡɪft/",
                synonyms = "present, donation", antonyms = "", themes = "shopping"
            ),

            // Jobs (Meslekler)
            WordEntity(
                id = "doctor", english = "doctor", turkish = "doktor", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A qualified practitioner of medicine who treats sick people.",
                turkishDefinition = "Hasta insanları tedavi eden nitelikli tıp uzmanı.",
                exampleSentence = "The doctor advised her to rest for three days.",
                exampleTranslation = "Doktor ona üç gün dinlenmesini tavsiye etti.", phonetic = "/ˈdɒk.tər/",
                synonyms = "physician", antonyms = "patient", themes = "jobs"
            ),
            WordEntity(
                id = "nurse", english = "nurse", turkish = "hemşire", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person trained to care for sick or injured people, especially in hospital.",
                turkishDefinition = "Özellikle hastanede hasta veya yaralı insanlara bakmak üzere eğitilmiş kimse.",
                exampleSentence = "The friendly nurse checked my body temperature.",
                exampleTranslation = "Güler yüzlü hemşire vücut sıcaklığımı kontrol etti.", phonetic = "/nɜːs/",
                synonyms = "caregiver", antonyms = "", themes = "jobs"
            ),
            WordEntity(
                id = "police", english = "police", turkish = "polis", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "The civil force of a state, responsible for enforcing laws.",
                turkishDefinition = "Bir devletin yasaları uygulamakla görevli sivil gücü.",
                exampleSentence = "Call the police if you witness an accident.",
                exampleTranslation = "Bir kazaya tanık olursanız polisi arayın.", phonetic = "/pəˈliːs/",
                synonyms = "cop, law enforcement", antonyms = "", themes = "jobs"
            ),
            WordEntity(
                id = "cook", english = "cook", turkish = "aşçı", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person who prepares food for eating.",
                turkishDefinition = "Yemek yemek için yiyecek hazırlayan kimse.",
                exampleSentence = "The professional cook prepared a delicious fish dish.",
                exampleTranslation = "Profesyonel aşçı lezzetli bir balık yemeği hazırladı.", phonetic = "/kʊk/",
                synonyms = "chef", antonyms = "", themes = "jobs"
            ),
            WordEntity(
                id = "driver", english = "driver", turkish = "sürücü", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person who drives a vehicle like a car or bus.",
                turkishDefinition = "Araba veya otobüs gibi bir aracı kullanan kimse.",
                exampleSentence = "The taxi driver knew all the shortcut roads.",
                exampleTranslation = "Taksi sürücüsü tüm kestirme yolları biliyordu.", phonetic = "/ˈdraɪ.vər/",
                synonyms = "chauffeur, motorist", antonyms = "passenger", themes = "jobs"
            ),
            WordEntity(
                id = "builder", english = "builder", turkish = "inşaatçı", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person whose job is to construct houses or buildings.",
                turkishDefinition = "İşi evler veya binalar inşa etmek olan kimse.",
                exampleSentence = "The builders finished constructing the school in six months.",
                exampleTranslation = "İnşaatçılar okulun yapımını altı ayda tamamladı.", phonetic = "/ˈbɪl.dər/",
                synonyms = "contractor, mason", antonyms = "", themes = "jobs"
            ),
            WordEntity(
                id = "pilot", english = "pilot", turkish = "pilot", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person who operates the flying controls of an aircraft.",
                turkishDefinition = "Bir uçağın uçuş kontrollerini yöneten kimse.",
                exampleSentence = "The pilot announced that the flight would be smooth.",
                exampleTranslation = "Pilot uçuşun sorunsuz geçeceğini duyurdu.", phonetic = "/ˈpaɪ.lət/",
                synonyms = "aviator", antonyms = "", themes = "jobs"
            ),
            WordEntity(
                id = "lawyer", english = "lawyer", turkish = "avukat", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person who practices or studies law and defends clients.",
                turkishDefinition = "Hukuk okuyan veya uygulayan ve müvekkillerini savunan kimse.",
                exampleSentence = "We consulted a lawyer to read the house contract.",
                exampleTranslation = "Ev sözleşmesini okumak için bir avukata danıştık.", phonetic = "/ˈlɔɪ.ər/",
                synonyms = "attorney, counsel", antonyms = "", themes = "jobs"
            ),
            WordEntity(
                id = "dentist", english = "dentist", turkish = "diş hekimi", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person qualified to treat diseases of the teeth.",
                turkishDefinition = "Diş hastalıklarını tedavi etmeye yetkili kimse.",
                exampleSentence = "I visit my dentist twice a year for cleanings.",
                exampleTranslation = "Temizlik için diş hekimimi yılda iki kez ziyaret ederim.", phonetic = "/ˈden.tɪst/",
                synonyms = "dental surgeon", antonyms = "", themes = "jobs"
            ),
            WordEntity(
                id = "farmer", english = "farmer", turkish = "çiftçi", partOfSpeech = "noun", cefrLevel = "A2",
                englishDefinition = "A person who owns or manages a farm.",
                turkishDefinition = "Bir çiftliğe sahip olan veya yöneten kimse.",
                exampleSentence = "The farmer grew organic wheat and corn.",
                exampleTranslation = "Çiftçi organik buğday ve mısır yetiştirdi.", phonetic = "/ˈfɑː.mər/",
                synonyms = "cultivator, grower", antonyms = "", themes = "jobs"
            ),

            // --- B1 LEVEL ---
            // Animals (Hayvanlar)
            WordEntity(
                id = "monkey", english = "monkey", turkish = "maymun", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A clever wild animal with a long tail that climbs trees.",
                turkishDefinition = "Ağaçlara tırmanan, uzun kuyruklu, zeki ve vahşi bir hayvan.",
                exampleSentence = "The funny monkey was peeling a yellow banana.",
                exampleTranslation = "Komik maymun sarı bir muzu soyuyordu.", phonetic = "/ˈmʌŋ.ki/",
                synonyms = "primate", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "elephant", english = "elephant", turkish = "fil", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A very heavy wild mammal with grey skin, tusks, and a trunk.",
                turkishDefinition = "Gri derili, fildişi dişli ve hortumlu, çok ağır ve vahşi bir memeli.",
                exampleSentence = "Elephants are the largest living land animals.",
                exampleTranslation = "Filler yaşayan en büyük kara hayvanlarıdır.", phonetic = "/ˈel.ɪ.fənt/",
                synonyms = "pachyderm", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "dolphin", english = "dolphin", turkish = "yunus", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "An intelligent marine mammal with a beaklike snout.",
                turkishDefinition = "Gaga şeklinde burnu olan zeki bir deniz memelisi.",
                exampleSentence = "We watched a group of dolphins jumping near the boat.",
                exampleTranslation = "Teknenin yakınında zıplayan bir yunus grubunu izledik.", phonetic = "/ˈdɒl.fɪn/",
                synonyms = "marine mammal", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "eagle", english = "eagle", turkish = "kartal", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A large bird of prey with a curved beak and long wings.",
                turkishDefinition = "Kıvrık gagalı ve uzun kanatlı, büyük bir yırtıcı kuş.",
                exampleSentence = "The eagle flew high above the mountain tops.",
                exampleTranslation = "Kartal dağ zirvelerinin üzerinde yükseklerde uçtu.", phonetic = "/ˈiː.ɡl̩/",
                synonyms = "bird of prey", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "snake", english = "snake", turkish = "yılan", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A long, legless reptile, some of which are poisonous.",
                turkishDefinition = "Bazıları zehirli olan, uzun ve bacakları olmayan bir sürüngen.",
                exampleSentence = "A green snake crawled silently through the grass.",
                exampleTranslation = "Yeşil bir yılan çimlerin arasından sessizce süründü.", phonetic = "/sneɪk/",
                synonyms = "serpent", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "lion", english = "lion", turkish = "aslan", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A large, powerful cat that lives in prides and is called king of jungle.",
                turkishDefinition = "Sürüler halinde yaşayan ve ormanın kralı olarak adlandırılan, büyük ve güçlü kedi.",
                exampleSentence = "The male lion has a beautiful golden mane around his neck.",
                exampleTranslation = "Erkek aslanın boynunun etrafında güzel altın rengi bir yelesi vardır.", phonetic = "/ˈlaɪ.ən/",
                synonyms = "king of beasts", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "giraffe", english = "giraffe", turkish = "zürafa", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A large mammal with a very long neck and spotted skin.",
                turkishDefinition = "Çok uzun boyunlu ve lekeli derili, büyük bir memeli.",
                exampleSentence = "The giraffe reached up to eat leaves from the tall tree.",
                exampleTranslation = "Zürafa yüksek ağaçtan yaprak yemek için yukarı doğru uzandı.", phonetic = "/dʒɪˈrɑːf/",
                synonyms = "ruminant", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "shark", english = "shark", turkish = "köpekbalığı", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A large sea fish with sharp teeth and a dorsal fin.",
                turkishDefinition = "Keskin dişleri ve sırt yüzgeci olan, büyük bir deniz balığı.",
                exampleSentence = "Great white sharks are dangerous predators of the ocean.",
                exampleTranslation = "Büyük beyaz köpekbalıkları okyanusun tehlikeli yırtıcılarıdır.", phonetic = "/ʃɑːk/",
                synonyms = "marine predator", antonyms = "", themes = "animals"
            ),

            // Nature & Environment (Doğa)
            WordEntity(
                id = "nature", english = "nature", turkish = "doğa", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "The physical world collective, including plants, animals, and landscapes.",
                turkishDefinition = "Bitkiler, hayvanlar ve manzaralar da dahil olmak üzere fiziksel dünyanın tamamı.",
                exampleSentence = "We went for a walk to enjoy the beauty of nature.",
                exampleTranslation = "Doğanın güzelliğinin tadını çıkarmak için yürüyüşe çıktık.", phonetic = "/ˈneɪ.tʃər/",
                synonyms = "environment, countryside", antonyms = "urban", themes = "nature"
            ),
            WordEntity(
                id = "mountain", english = "mountain", turkish = "dağ", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A very high, steep hill that rises above surrounding land.",
                turkishDefinition = "Çevreleyen arazinin üzerinde yükselen, çok yüksek ve dik tepe.",
                exampleSentence = "Mount Ararat is the highest mountain in Turkey.",
                exampleTranslation = "Ağrı Dağı, Türkiye'deki en yüksek dağdır.", phonetic = "/ˈmaʊn.tɪn/",
                synonyms = "peak, mount", antonyms = "valley", themes = "nature"
            ),
            WordEntity(
                id = "river", english = "river", turkish = "nehir", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A large natural stream of water flowing in a channel to the sea.",
                turkishDefinition = "Yatak içinde denize doğru akan, büyük ve doğal su akıntısı.",
                exampleSentence = "They went fishing on the banks of the quiet river.",
                exampleTranslation = "Sakin nehrin kıyısında balık tutmaya gittiler.", phonetic = "/ˈrɪv.ər/",
                synonyms = "stream, brook", antonyms = "", themes = "nature"
            ),
            WordEntity(
                id = "forest", english = "forest", turkish = "orman", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A large area covered chiefly with trees and undergrowth.",
                turkishDefinition = "Esas olarak ağaçlar ve çalılarla kaplı geniş alan.",
                exampleSentence = "Many wild animals live inside this dense pine forest.",
                exampleTranslation = "Bu sık çam ormanının içinde birçok vahşi hayvan yaşıyor.", phonetic = "/ˈfɒr.ɪst/",
                synonyms = "woods, jungle", antonyms = "desert", themes = "nature"
            ),
            WordEntity(
                id = "island", english = "island", turkish = "ada", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A piece of land completely surrounded by water.",
                turkishDefinition = "Etrafı tamamen suyla çevrili kara parçası.",
                exampleSentence = "They sailed to a small tropical island in Greece.",
                exampleTranslation = "Yunanistan'da küçük tropikal bir adaya yelken açtılar.", phonetic = "/ˈaɪ.lənd/",
                synonyms = "isle", antonyms = "mainland", themes = "nature"
            ),
            WordEntity(
                id = "ocean", english = "ocean", turkish = "okyanus", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A very large expanse of sea, in particular, each of the main areas of salt water.",
                turkishDefinition = "Çok geniş deniz alanı, özellikle tuzlu suyun ana alanlarının her biri.",
                exampleSentence = "The Atlantic Ocean lies between Europe and America.",
                exampleTranslation = "Atlantik Okyanusu, Avrupa ile Amerika arasında yer alır.", phonetic = "/ˈəʊ.ʃən/",
                synonyms = "sea, deep", antonyms = "", themes = "nature"
            ),
            WordEntity(
                id = "desert", english = "desert", turkish = "çöl", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A barren, dry area of land with little water and sparse vegetation.",
                turkishDefinition = "Az suyu ve seyrek bitki örtüsü olan çorak, kuru arazi parçası.",
                exampleSentence = "The Sahara Desert is extremely hot during the day.",
                exampleTranslation = "Sahra Çölü gün boyunca aşırı derecede sıcaktır.", phonetic = "/ˈdez.ət/",
                synonyms = "wasteland", antonyms = "forest, wetland", themes = "nature"
            ),
            WordEntity(
                id = "valley", english = "valley", turkish = "vadi", partOfSpeech = "noun", cefrLevel = "B1",
                englishDefinition = "A low area of land between hills or mountains, typically with a river.",
                turkishDefinition = "Tepeler veya dağlar arasında kalan, genellikle içinden nehir geçen alçak arazi.",
                exampleSentence = "The small green village is located in a deep valley.",
                exampleTranslation = "Küçük yeşil köy, derin bir vadide yer almaktadır.", phonetic = "/ˈvæl.i/",
                synonyms = "canyon, dale", antonyms = "mountain, ridge", themes = "nature"
            ),
            // --- NEW A1 ADDITIONS ---
            WordEntity(
                id = "blue", english = "blue", turkish = "mavi", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "The color of the clear sky or the deep sea.",
                turkishDefinition = "Açık gökyüzü veya derin denizin rengi.",
                exampleSentence = "The sky is bright blue today.",
                exampleTranslation = "Bugün gökyüzü parlak mavi.", phonetic = "/bluː/",
                synonyms = "azure", antonyms = "", themes = "colors"
            ),
            WordEntity(
                id = "red", english = "red", turkish = "kırmızı", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "The color of blood or a ripe tomato.",
                turkishDefinition = "Kanın veya olgun bir domatesin rengi.",
                exampleSentence = "She is wearing a beautiful red dress.",
                exampleTranslation = "Güzel kırmızı bir elbise giyiyor.", phonetic = "/red/",
                synonyms = "crimson", antonyms = "", themes = "colors"
            ),
            WordEntity(
                id = "green", english = "green", turkish = "yeşil", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "The color of grass or leaves.",
                turkishDefinition = "Çimlerin veya yaprakların rengi.",
                exampleSentence = "The park is very green in the spring.",
                exampleTranslation = "Park baharda çok yeşildir.", phonetic = "/ɡriːn/",
                synonyms = "verdant", antonyms = "", themes = "colors"
            ),
            WordEntity(
                id = "white", english = "white", turkish = "beyaz", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "The color of snow or milk.",
                turkishDefinition = "Karın veya sütün rengi.",
                exampleSentence = "The walls of the room are painted white.",
                exampleTranslation = "Odanın duvarları beyaza boyanmış.", phonetic = "/waɪt/",
                synonyms = "snowy", antonyms = "black", themes = "colors"
            ),
            WordEntity(
                id = "black", english = "black", turkish = "siyah", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "The darkest color, like the night sky.",
                turkishDefinition = "Gece gökyüzü gibi en koyu renk.",
                exampleSentence = "He has a black cat named Midnight.",
                exampleTranslation = "Midnight adında siyah bir kedisi var.", phonetic = "/blæk/",
                synonyms = "dark", antonyms = "white", themes = "colors"
            ),
            WordEntity(
                id = "cat", english = "cat", turkish = "kedi", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A small domesticated carnivorous mammal with soft fur.",
                turkishDefinition = "Yumuşak kürklü, küçük evcil etçil memeli.",
                exampleSentence = "The cat is sleeping on the sofa.",
                exampleTranslation = "Kedi kanepede uyuyor.", phonetic = "/kæt/",
                synonyms = "feline", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "dog", english = "dog", turkish = "köpek", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A common domesticated animal kept as a pet or for work.",
                turkishDefinition = "Evcil hayvan olarak veya iş için beslenen yaygın bir hayvan.",
                exampleSentence = "My dog loves to play with a ball.",
                exampleTranslation = "Köpeğim topla oynamayı çok seviyor.", phonetic = "/dɒɡ/",
                synonyms = "canine", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "bird", english = "bird", turkish = "kuş", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A warm-blooded egg-laying vertebrate with feathers and wings.",
                turkishDefinition = "Tüyleri ve kanatları olan, sıcakkanlı yumurtlayan omurgalı.",
                exampleSentence = "A small bird is singing in the tree.",
                exampleTranslation = "Ağaçta küçük bir kuş şarkı söylüyor.", phonetic = "/bɜːd/",
                synonyms = "fowl", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "fish", english = "fish", turkish = "balık", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A limbless cold-blooded animal with gills and fins living in water.",
                turkishDefinition = "Suda yaşayan, solungaçlı ve yüzgeçli, bacaksız soğukkanlı hayvan.",
                exampleSentence = "The fish are swimming in the aquarium.",
                exampleTranslation = "Balıklar akvaryumda yüzüyorlar.", phonetic = "/fɪʃ/",
                synonyms = "marine life", antonyms = "", themes = "animals"
            ),
            WordEntity(
                id = "car", english = "car", turkish = "araba", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A road vehicle, typically with four wheels, powered by an engine.",
                turkishDefinition = "Genellikle dört tekerlekli, bir motorla çalışan yol aracı.",
                exampleSentence = "He drives a red car to work.",
                exampleTranslation = "İşe kırmızı bir araba sürüyor.", phonetic = "/kɑːr/",
                synonyms = "automobile", antonyms = "", themes = "transport"
            ),
            WordEntity(
                id = "bus", english = "bus", turkish = "otobüs", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A large motor vehicle carrying passengers by road.",
                turkishDefinition = "Yol üzerinden yolcu taşıyan büyük motorlu araç.",
                exampleSentence = "The school bus arrives at eight o'clock.",
                exampleTranslation = "Okul otobüsü saat sekizde geliyor.", phonetic = "/bʌs/",
                synonyms = "coach", antonyms = "", themes = "transport"
            ),
            WordEntity(
                id = "plane", english = "plane", turkish = "uçak", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A powered flying vehicle with fixed wings.",
                turkishDefinition = "Sabit kanatlı motorlu uçan araç.",
                exampleSentence = "The plane flew over the mountains.",
                exampleTranslation = "Uçak dağların üzerinden uçtu.", phonetic = "/pleɪn/",
                synonyms = "airplane", antonyms = "", themes = "transport"
            ),
            WordEntity(
                id = "sun", english = "sun", turkish = "güneş", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "The star around which the earth orbits and provides light.",
                turkishDefinition = "Dünyanın etrafında döndüğü ve ışık sağlayan yıldız.",
                exampleSentence = "The sun is shining brightly today.",
                exampleTranslation = "Güneş bugün parlak bir şekilde parlıyor.", phonetic = "/sʌn/",
                synonyms = "star", antonyms = "moon", themes = "nature"
            ),
            WordEntity(
                id = "moon", english = "moon", turkish = "ay", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "The natural satellite of the earth, visible by night.",
                turkishDefinition = "Dünyanın geceleri görülen doğal uydusu.",
                exampleSentence = "The moon was full last night.",
                exampleTranslation = "Dün gece ay dolunaydı.", phonetic = "/muːn/",
                synonyms = "satellite", antonyms = "sun", themes = "nature"
            ),
            WordEntity(
                id = "star", english = "star", turkish = "yıldız", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "A luminous point in the night sky.",
                turkishDefinition = "Gece gökyüzündeki ışıklı nokta.",
                exampleSentence = "The sky is full of stars tonight.",
                exampleTranslation = "Bu gece gökyüzü yıldızlarla dolu.", phonetic = "/stɑːr/",
                synonyms = "luminary", antonyms = "", themes = "nature"
            ),
            WordEntity(
                id = "rain", english = "rain", turkish = "yağmur", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "Moisture condensed from the atmosphere that falls in separate drops.",
                turkishDefinition = "Atmosferden yoğunlaşan ve ayrı damlalar halinde düşen nem.",
                exampleSentence = "We need some rain for the flowers.",
                exampleTranslation = "Çiçekler için biraz yağmura ihtiyacımız var.", phonetic = "/reɪn/",
                synonyms = "precipitation", antonyms = "sunshine", themes = "nature"
            ),
            WordEntity(
                id = "snow", english = "snow", turkish = "kar", partOfSpeech = "noun", cefrLevel = "A1",
                englishDefinition = "Atmospheric water vapor frozen into ice crystals.",
                turkishDefinition = "Buz kristallerine dönüşmüş atmosferik su buharı.",
                exampleSentence = "The ground is covered with white snow.",
                exampleTranslation = "Yer beyaz karla kaplı.", phonetic = "/snəʊ/",
                synonyms = "ice crystals", antonyms = "", themes = "nature"
            ),
            WordEntity(
                id = "hot", english = "hot", turkish = "sıcak", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Having a high temperature.",
                turkishDefinition = "Yüksek sıcaklığa sahip.",
                exampleSentence = "The soup is very hot.",
                exampleTranslation = "Çorba çok sıcak.", phonetic = "/hɒt/",
                synonyms = "warm, burning", antonyms = "cold", themes = "weather, basics"
            ),
            WordEntity(
                id = "cold", english = "cold", turkish = "soğuk", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Having a low temperature.",
                turkishDefinition = "Düşük sıcaklığa sahip.",
                exampleSentence = "It is very cold in winter.",
                exampleTranslation = "Kışın hava çok soğuktur.", phonetic = "/kəʊld/",
                synonyms = "chilly, freezing", antonyms = "hot", themes = "weather, basics"
            ),
            WordEntity(
                id = "small", english = "small", turkish = "küçük", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Of a size that is less than normal.",
                turkishDefinition = "Normalden daha küçük olan.",
                exampleSentence = "The mouse is a small animal.",
                exampleTranslation = "Fare küçük bir hayvandır.", phonetic = "/smɔːl/",
                synonyms = "little, tiny", antonyms = "big, large", themes = "basics"
            ),
            WordEntity(
                id = "big", english = "big", turkish = "büyük", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Of considerable size or extent.",
                turkishDefinition = "Önemli boyutta veya kapsamda.",
                exampleSentence = "The elephant is a big animal.",
                exampleTranslation = "Fil büyük bir hayvandır.", phonetic = "/bɪɡ/",
                synonyms = "large, huge", antonyms = "small, little", themes = "basics"
            ),
            WordEntity(
                id = "old", english = "old", turkish = "eski / yaşlı", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Having lived for a long time; or from the past.",
                turkishDefinition = "Uzun süre yaşamış; veya geçmişten gelen.",
                exampleSentence = "My grandfather is very old.",
                exampleTranslation = "Büyükbabam çok yaşlı.", phonetic = "/əʊld/",
                synonyms = "aged, ancient", antonyms = "new, young", themes = "basics"
            ),
            WordEntity(
                id = "new", english = "new", turkish = "yeni", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Not existing before; made recently.",
                turkishDefinition = "Daha önce var olmayan; yakın zamanda yapılmış.",
                exampleSentence = "I bought a new phone yesterday.",
                exampleTranslation = "Dün yeni bir telefon aldım.", phonetic = "/njuː/",
                synonyms = "recent, fresh", antonyms = "old", themes = "basics"
            ),
            WordEntity(
                id = "fast", english = "fast", turkish = "hızlı", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Moving or capable of moving at high speed.",
                turkishDefinition = "Yüksek hızda hareket eden veya edebilen.",
                exampleSentence = "The cheetah is a fast runner.",
                exampleTranslation = "Çita hızlı bir koşucudur.", phonetic = "/fɑːst/",
                synonyms = "quick, rapid", antonyms = "slow", themes = "basics"
            ),
            WordEntity(
                id = "slow", english = "slow", turkish = "yavaş", partOfSpeech = "adjective", cefrLevel = "A1",
                englishDefinition = "Moving or operating at a low speed.",
                turkishDefinition = "Düşük hızda hareket eden veya çalışan.",
                exampleSentence = "The turtle is very slow.",
                exampleTranslation = "Kaplumbağa çok yavaştır.", phonetic = "/sləʊ/",
                synonyms = "leisurely", antonyms = "fast", themes = "basics"
            )
        )
    }
}
