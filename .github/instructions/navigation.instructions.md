# 画面遷移（Navigation-Compose）

> **【重要】マルチリポジトリ化に伴い、各画面の Navigation Graph の定義（quizGraph, resultGraph, analysisGraph 等）や遷移先定数は外部リポジトリ（`msaitodev-quiz-engine` または `msaitodev-android-core`）へ分離されています。本プロジェクト（ハブ）では `AppNavHost` でこれらを統合し、画面間の結合ロジックを管理します。**

## 適用範囲
- `app/src/main/java/.../ui/AppNavHost.kt` および外部ライブラリから提供される Navigation 定義。

## 基本方針
- Jetpack Navigation（Compose）を使用し、**単方向データフロー (UDF)** を保つ。
- **非同期処理完了後に遷移が必要**な場合は、コールバックまたはワンショット `SharedFlow` で**完了を待ってから**遷移する。

## 【最重要】画面間での結果の受け渡し：疎結合の原則
（※以前の `previousBackStackEntry` を使う原則を維持。ソースが分離されても、ハブ層での遷移管理においてこの原則は極めて重要です。）

---

## 画面ごとの要点
- **HomeRoute**: （`quiz-feature-main` にて実装）
- **QuizRoute**: （`quiz-feature-main` にて実装）
- **ResultRoute**: （`quiz-feature-result` にて実装）
- **SettingsRoute**: （`feature-settings` にて実装）
- **AnalysisRoute**: （`quiz-feature-analysis` にて実装）

ハブプロジェクト側では、これら外部リポジトリから供給される `Route` や `Graph` を `AppNavHost` で呼び出し、アプリ全体の導線を完成させます。
