# ビルドと依存関係（マルチリポジトリ・バージョン方針）

## 1. マルチリポジトリ構成
本プロジェクトは、量産化と再利用性を最大化するため、以下の 3 つのリポジトリに分離されています。

| リポジトリ名 | 役割 | Group ID |
| :--- | :--- | :--- |
| **`msaitodev-android-core`** | 汎用基盤（Ads, Billing, Settings, Notifications等） | `com.msaitodev.core`, `com.msaitodev.feature` |
| **`msaitodev-quiz-engine`** | クイズドメイン特化（Domain, Data, Quiz UI等） | `com.msaitodev.quiz` |
| **`CaregiverHumanMed`** | アプリハブ（本プロジェクト）。UIと設定のみ。 | `com.msaitodev.caregiver.humanmed` |

## 2. 開発フロー（バイナリ依存）
- **本プロジェクト内にライブラリのソースコードは存在しません。**
- すべての共通モジュールは `mavenLocal()` を経由してバイナリ（AAR）として参照されます。
- 基盤モジュールを修正する場合：
    1. 対応するプロジェクト（`core` または `quiz-engine`）を開く。
    2. 修正を行い、`./gradlew publishToMavenLocal` を実行。
    3. 本プロジェクトに戻り、`Sync Project with Gradle Files` を実行して最新のバイナリを取り込む。

## 3. 主要ライブラリバージョン
- Hilt: 2.51.1
- Coroutines: 1.8.1
- Navigation-Compose: 2.8.4
- DataStore: 1.1.1
- Billing: 7.1.1
- Firebase BOM: 32.7.4
- AdMob: 22.6.0
- UMP: 2.2.0
- Compose BOM: 2024.06.00
- WorkManager: 2.9.1

## 4. 方針
- **依存関係の変更**: `AGENTS.md` 3.7項に従い、最小限の変更とビルド検証を徹底すること。
- **バージョン固定**: ライブラリのバージョンは、すべてのリポジトリ間で不整合が起きないよう同期させること。
- **難読化**: 各ライブラリ側で `consumer-rules.pro` を定義し、バイナリ経由で適切にハブプロジェクトへ伝播させる。
