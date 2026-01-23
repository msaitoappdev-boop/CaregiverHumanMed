
# Caregiver HumanMed Starter v2（app モジュール）

この ZIP は **app モジュール単体**のスターターです。既存プロジェクトのルートに展開して、
`settings.gradle(.kts)` に `include(":app")` がある構成でご利用ください。

## 特徴
- AGP 8.8 / Kotlin 1.9.25 / Compose Compiler 1.5.15 を想定
- Material3 + DayNight / NoActionBar
- クイズの ViewModel 化（10問シャッフル → 採点 → 結果画面）
- AdMob（開発時はテスト App ID）

## 統合手順
1. プロジェクト直下にこの `app/` を上書きコピーします。
2. ルート `build.gradle.kts` で以下が宣言されていることを確認：
   ```kotlin
   plugins {
       id("com.android.application") version "8.8.0" apply false
       id("org.jetbrains.kotlin.android") version "1.9.25" apply false
       id("org.jetbrains.kotlin.plugin.serialization") version "1.9.25" apply false
   }
   ```
3. `gradle-wrapper.properties` は Gradle 8.10.2 などを推奨：
   `distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip`
4. Android Studio で Sync → Run。
5. 本番前に `local.properties` に `admob.app.id` を設定して Release で自動切替されるようにしてください。

## AdMob の本番切替
- `local.properties` に `admob.app.id=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy`
- `gradle.properties` に `admob.test.app.id=ca-app-pub-3940256099942544~3347511713`（残してOK）

## JSON について
- `app/src/main/assets/questions.json` を差し替えてください（配列ルート）。
