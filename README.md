# 🧩 Çaprazca

**Türkçe–İngilizce kelime bulmaca ve dil öğrenme oyunu.** Candy Crush tarzı harita ilerlemesi ile Duolingo tarzı öğrenme mekaniklerini birleştirir: bulmaca çözerken kelime dağarcığınızı geliştirirsiniz.

%100 Kotlin + Jetpack Compose ile geliştirilmiş, çevrimdışı öncelikli (offline-first) bir Android uygulamasıdır.

## ✨ Özellikler

- **Gelişmiş bulmaca motoru (`CrosswordLayoutEngine`)** — Kelime kesişimlerini ve paralel komşulukları hesaplayan, backtracking tabanlı yüksek performanslı yerleşim algoritması. Geçerli kesişme ile istenmeyen yatay/dikey temasları ayırt eder.
- **Seviye haritası** — Candy Crush tarzı harita ekranında seviye seviye ilerleme.
- **İpucu sistemi** — Harf veya tam kelime ipucu; tam kelime ipuçları kesişen kelimelerin doluluk durumunu otomatik yeniden doğrular.
- **Günlük giriş ödülleri** — 7 günlük döngüsel ödül sistemi, atomik veritabanı işlemleriyle korunur.
- **Sözlük ve pratik modu** — Öğrenilen kelimeleri görüntüleme, favorilere ekleme ve çoktan seçmeli mini test ile XP/jeton kazanma.
- **Can ve ödül sandığı** — Dinamik can sistemi, can yenileme ve seviye sonu sandık ödülleri.
- **Görevler ve başarımlar** — Günlük/haftalık görevler ve kazanılabilir başarımlarla ilerleme takibi.
- **Profil ve istatistikler** — XP, jeton ve oyuncu ilerlemesinin takibi.

## 🛠️ Teknolojiler

| Katman | Teknoloji |
|---|---|
| UI | Jetpack Compose, Material Design 3 |
| Mimari | MVVM, StateFlow, Repository pattern |
| Veritabanı | Room (atomik `withTransaction` korumalı, offline-first) |
| Navigasyon | Navigation Compose |
| Asenkron | Kotlin Coroutines |
| Test | JUnit, Robolectric, Roborazzi (ekran görüntüsü testleri) |
| Yapılandırma | Gradle Version Catalog, Secrets Gradle Plugin (`.env`) |

## 📂 Proje Yapısı

```
app/src/main/java/com/example/
├── MainActivity.kt
├── data/
│   ├── model/Models.kt              # Veri modelleri
│   ├── db/AppDatabase.kt            # Room veritabanı
│   └── repository/
│       ├── GameRepository.kt        # Oyun mantığı ve veri erişimi
│       └── CrosswordLayoutEngine.kt # Bulmaca yerleşim algoritması
└── ui/
    ├── theme/                       # Renk, tipografi, tema
    └── screens/
        ├── MapScreen.kt             # Seviye haritası
        ├── DictionaryScreen.kt      # Sözlük ve pratik modu
        ├── TasksScreen.kt           # Görevler
        ├── AchievementsScreen.kt    # Başarımlar
        ├── ProfileScreen.kt         # Profil
        └── MainAppContainer.kt      # Ana gezinme kabuğu
```

## 🚀 Kurulum

**Gereksinimler:** Android Studio (güncel sürüm), JDK 11+, Android SDK 36.

```bash
git clone https://github.com/<kullanici-adi>/caprazca.git
cd caprazca
```

API anahtarları için `.env.example` dosyasını kopyalayıp doldurun:

```bash
cp .env.example .env
```

Derleme ve test:

```bash
# Debug derlemesi
./gradlew assembleDebug

# Birim testleri (Robolectric dahil)
./gradlew :app:testDebugUnitTest
```

## 🧪 Testler

Proje, JVM üzerinde çalışan kapsamlı bir test paketiyle gelir:

- `AllCrosswordLevelsValidationTest` — Tüm seviyelerin geçerli yerleşim ürettiğinin doğrulanması
- `HintSystemTest`, `DailyRewardTest`, `LevelProgressionTest`, `GameSessionTest` — Oyun mekanikleri
- `RoomMigrationTest` — Veritabanı şema geçişleri
- `ExampleRobolectricTest` — Roborazzi ile ekran görüntüsü doğrulama

## 📄 Lisans

Bu proje kişisel bir çalışmadır. İzin almadan ticari olarak kullanmayınız.
