# 介護福祉士 毎日3問トレーナー — 開発エージェントのためのガイドライン（最新版）
最終更新: 2026-03-08

本ドキュメントは、Android 開発エージェント（AIアシスタント）が本プロジェクトに関わる際の**一次仕様**と**行動規範**を定義します。

**【重要】本プロジェクトは「マルチリポジトリ戦略」を採用しており、本リポジトリ（CaregiverHumanMed）はアプリケーションの「ハブ」として機能します。共通基盤やクイズエンジンのソースコードは外部プロジェクトに分離されていますが、開発にあたっては本ドキュメントに記載された「アプリの仕様」を完全に理解し、維持することが最優先事項です。**

---

## 1. 役割と目的（要約）
- 本プロジェクトは **Kotlin + Jetpack Compose (Material 3)**、**MVVM + 単方向データフロー (UDF)** を採用します。
- **ハブ・アーキテクチャ**: 本リポジトリは UI 画面の構成、テーマ、アプリ固有の設定、および外部ライブラリの統合を担います。ビジネスロジックの多くは外部ライブラリ（Spokes）に集約されています。

---

## 2. プロジェクト要件
### 2.1 コア機能：クイズ
- **問題セット**: 1 セット 3 問。
- **問題ソース**: 外部リポジトリで管理され、AES 暗号化されたバイナリデータ（.bin）を使用。
- **出題ロジック**:
  - **未出題優先**。未出題が尽きたら **全体ランダム**へフォールバック。
  - **「クイズを開始」時は *アプリ起動ごと* にランダムなセットを選定**。
  - **「次の3問へ」** も未出題優先ランダム。
  - **復習は学習上限にカウントしない。**
- **結果画面の動線**: 次セット／同じ順番で復習／復習一覧／スコア履歴／ホーム。 

### 2.2 収益化機能：課金と広告
- **Premium（サブスクリプション）**:
  - 特典: **広告完全非表示／解説の全文表示／1日あたりの学習上限の解除／学習分析機能の全解放**。
  - 設定画面から **購入の復元** を提供。
- **広告（無料ユーザーのみ）**:
  - **Interstitial**: セット完了→結果画面遷移時。Remote Config で頻度を制御。
  - **Rewarded**: 1 日の上限到達時に視聴で +1 セット付与（**1 日 1 回まで**）。
  - **同意（UMP）未取得時はロード/表示を抑止**。

### 2.3 学習支援機能
- **リマインド通知**: 設定時刻に通知。タップで **Home** に復帰し学習導線へ接続。WorkManager を使用。

### 2.4 学習分析（Premium 価値の最大化）
- **分野別正解率バランス**: 5カテゴリ（人間と社会、介護の基本、こころとからだ、医療的ケア、総合問題）の正解率をレーダーチャートで表示。
- **学習トレンド**: 日別・週別の正解率推移をグラフ表示。
- **ドリルダウン**: グラフの特定日を選択することで、その日の詳細な解答履歴へ遷移。

### 2.5 主要画面の構成（要点）
- **HomeRoute**: 学習開始時に `isPremium` と学習上限を判定。無料かつ上限到達時は Reward 提案へ。
- **QuizRoute**: 3 問を提示。
- **ResultRoute**: 次セット／復習／履歴／ホーム。
- **AnalysisRoute**: 分野別分析、トレンドグラフを表示。

---

## 3. 技術スタックとアーキアーキテクチャ
### 3.1 モジュール構成（マルチリポジトリ）
本プロジェクトは以下の 3 つのリポジトリの連携で構成されます。

1.  **`msaitodev-android-core`**: 汎用基盤（Ads, Billing, Settings, Notifications, Navigation等）。
2.  **`msaitodev-quiz-engine`**: クイズエンジン（Domain, Data, Quiz UI 等）。
3.  **`CaregiverHumanMed`（本リポジトリ）**: アプリハブ。UI 画面、テーマ、DI 設定、アプリ用リソースを保持。

### 3.2 開発フローとバイナリ依存
- **バイナリ参照**: 共通モジュールは `mavenLocal()` を経由してバイナリ（AAR）として参照されます。本プロジェクト内にライブラリのソースコードは存在しません。
- **修正フロー**: 基盤側の修正が必要な場合は、該当リポジトリ（`core` または `quiz-engine`）で修正・パブリッシュを行い、本プロジェクトで最新版を取り込むこと。

### 3.3 依存関係の変更に関する厳格なルール (3.7項)
過去の失敗に基づき、依存関係の追加・更新・削除は極めて慎重に行うこと。
1.  **変更目的の明確化**。
2.  **`gradle sync`の個別実行**: 一度に1つの依存関係のみ変更。
3.  **ビルド検証**: 必ず `app:assembleDebug` を実行。
4.  **段階的変更の徹底**: 1変更 → 1Sync → 1ビルド。

---

## 4. コーディング規約（まとめ）
- **MUST**:
  - **Premium 中は広告一切表示しない**。
  - **文字列は `strings.xml`**。
  - **Route/Screen 分離**。
  - **非同期完了→遷移**。
  - **最小差分編集の厳格な遵守**: 本プロジェクトでの修正は UI 層やアプリ設定に限定されるべきです。

---

## 6. 参照ドキュメント（instructions）
- **Overall Plan**: `.github/instructions/refactoring-roadmap.md`
- **新規アプリ量産プロトコル**: `.github/instructions/production-protocol.instructions.md`
- UI: `.github/instructions/ui-compose.instructions.md`
- Navigation: `.github/instructions/navigation.instructions.md`
- Billing: `.github/instructions/billing.instructions.md`
- Ads/Consent: `.github/instructions/ads.instructions.md`
- Quiz Domain: `.github/instructions/quiz-domain.instructions.md`
- Data/Repository: `.github/instructions/data-repository.instructions.md`
- **Master Data**: `.github/instructions/master-data.instructions.md`
- Notifications: `.github/instructions/notifications.instructions.md`
- Logging & Errors: `.github/instructions/logging-error-handling.instructions.md`
- Build & Deps: `.github/instructions/build-and-deps.instructions.md`
- Testing: `.github/instructions/testing.instructions.md`
- Agent Guidelines: `.github/instructions/agent-guidelines.md`

---

## 8. 重要なコミュニケーションルール（抄）
- **タスクの復唱** → **不足情報の要求** → **Plan 提示** → **承認後に差分編集** → **完了報告**。
- 同じ提案のループ禁止。セッションの独断終了禁止。

---

## 9. 【最重要】失敗の記録と拘束力のある開発プロセス
過去の失敗を繰り返さないため、依存関係の分析を怠ってはならない。
1.  **依存関係グラフの完全分析**: `find usages` で直接参照と間接参照（DI）を分類。
2.  **最小差分計画の徹底**: 計画にないファイルには手を触れない。
3.  **ビルドエラー時の手順**: 安易にビルド設定の問題と断定せず、コードの不整合を疑う。

## 10. 【最重要】テスト失敗時の根本原因分析プロセス
1.  **即時停止と全体分析**: 直ちに修正せず、関連コードをすべて読む。
2.  **状態遷移の完全なマッピング**: StateFlow の時系列変化を書き出す。
3.  **根本原因の仮説構築**: なぜ食い違いが発生したかの仮説をユーザーに提示し承認を得る。

---

## 11. 新規アプリ量産プロトコル
本プロジェクトをベースに新しい資格試験アプリを立ち上げる際の標準手順です。

### 11.1 プロジェクトの初期設定
- **Application ID**: `app/build.gradle.kts` の `applicationId` を新しい資格アプリの ID に変更。
- **Firebase**: 新しいプロジェクト用の `app/google-services.json` を配置。
- **アプリ名・表示**: `strings.xml` の `app_name` および関連する表示文字列を更新。

### 11.2 収益化の設定
- **AdMob**: `local.properties` または `AndroidManifest.xml` (meta-data) の AdMob App ID を更新。広告ユニット ID は `strings.xml` または `Remote Config` で定義。
- **Billing**: `strings.xml` または `BillingModule` 等で、新しいアプリのサブスクリプション製品 ID が正しく反映されているか確認。

### 11.3 コンテンツと科目の定義
- **クイズデータ**: `master_data/quiz_data` 配下に新しい資格用の AES 暗号化済み問題（.bin）を配置。
- **科目定義**: `strings.xml` のカテゴリ名、および `QuizCategoryNameProvider`, `QuizConfigModule` の定義を新しいアプリの科目に合わせる。
- **分析機能**: カテゴリ数が変更になる場合は、レーダーチャートの表示ロジックとの整合性を確認。

### 11.4 外部ライブラリ（Spokes）の同期
- 基盤側（`core`, `quiz-engine`）の最新バージョンを `app/build.gradle.kts` で指定し、`publishToMavenLocal` された最新のバイナリを取り込む。

---
