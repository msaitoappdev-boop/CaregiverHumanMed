# 介護福祉士 毎日3問トレーナー

## 1. プロジェクト概要

「介護福祉士 毎日3問トレーナー」は、介護福祉士国家試験の合格を目指す学習者を支援するためのAndroidアプリです。毎日少しずつクイズ形式で問題を解くことで、継続的な学習をサポートします。

### 主要な技術スタック
- **言語**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **アーキテクチャ**: MVVM + UDF (単方向データフロー)
- **非同期処理**: Kotlin Coroutines & Flow
- **DI (依存性注入)**: Hilt
- **画面遷移**: Jetpack Navigation for Compose

---

## 2. アーキテクチャとモジュール構成

本プロジェクトは、関心事の分離とビルド効率の向上を目的としたマルチモジュール構成を採用しています。

```
:app ——————————————> :core ——————————> :domain
 |      |      |      |      |                   ^
 |      |      |      |      └————> :data —————┘
 |      |      |      |
 |      |      |      └—> :quiz-feature-billing
 |      |      |
 |      |      └—> :quiz-feature-review
 |      |
 |      └—> :quiz-feature-result
 |
 └—> :quiz-core-ads
```

### 各モジュールの役割

| モジュール名                 | 役割                                                              |
| -------------------------- | ----------------------------------------------------------------- |
| `:app`                     | アプリケーションのエントリポイント。画面遷移の定義など、Android固有のUI層。 |
| `:core`                    | 全モジュールで共有される基盤機能。拡張関数、ナビゲーション定義など。     |
| `:data`                    | データソースへのアクセスを抽象化するRepositoryの実装。                  |
| `:domain`                  | アプリのビジネスロジック（UseCase）とドメインモデルを定義。             |
| `:quiz-core-ads`           | 広告表示と同意取得（UMP）に関する共通機能。                         |
| `:quiz-feature-billing`    | 課金（プレミアム機能）に関するUIとロジック。                          |
| `:quiz-feature-history`    | スコア履歴機能。 (現在 `app` 内に未分離の機能あり)                     |
| `:quiz-feature-result`     | クイズ結果表示機能。                                              |
| `:quiz-feature-review`     | クイズの復習機能。                                                |

---

## 3. 開発ガイドライン

本プロジェクトにおける開発は、`AGENTS.md`に定義された規約に厳格に従います。特に重要な原則は以下の通りです。

- **MVVM + UDF**: ViewModelが不変の`UiState`を`StateFlow`で公開し、UI（Screen）はそれを購読して表示に専念します。
- **Route/Screen分離**: Composableを`Route`（状態管理とイベント処理）と`Screen`（UI表示）に分離します。
- **ビジネスロジックの集約**: ビジネスロジックは`:domain`モジュールの`UseCase`に集約し、UI層から切り離します。

詳細は `AGENTS.md` を参照してください。

### Geminiによる開発支援

リファクタリング、機能実装、エラー解析など、開発の様々な場面でAIアシスタント(Gemini in Android Studio)を活用しています。効率的かつ高品質な開発を維持するため、以下のドキュメントにまとめられた実践的なプロンプト集を積極的に利用してください。

- **プロンプト集**: [docs/gemini_prompts_android_studio.md](docs/gemini_prompts_android_studio.md)

---

## 4. セットアップとビルド

1. プロジェクトをAndroid Studioで開きます。
2. 初回ビルドの前に、Gradle Syncが自動的に実行されるのを待ちます。
3. ビルド構成として`:app`モジュールの`debug`バリアントを選択し、ビルドまたは実行します。

---

*This `README.md` was last updated by Gemini in Android Studio.*
