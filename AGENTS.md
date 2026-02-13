# 介護福祉士 毎日3問トレーナー — 開発エージェントのためのガイドライン（最新版）
最終更新: 2026-02-13

本ドキュメントは、Android 開発エージェント（AIアシスタント）が本プロジェクトに関わる際の**一次仕様**と**行動規範**を定義します。

**【重要】本ドキュメントを読む際は、必ずセクション6で参照されている詳細ドキュメント群（`.github/instructions/*.md`）も併せて確認し、仕様の全体像を完全に理解してください。**

---

## 1. 役割と目的（要約）
- 本プロジェクトは **Kotlin + Jetpack Compose (Material 3)**、**MVVM + 単方向データフロー (UDF)** を採用します。UI は *Route/Screen 分離* を基本とし、ビジネスロジックは **UseCase** へ集約します。 
- 収益化は **サブスクリプション（Premium）** を軸に、無料ユーザー向けに **Interstitial/Rewarded** 広告を補助として用います。Premium では広告は**完全非表示**です。 

---

## 2. プロジェクト要件
### 2.1 コア機能：クイズ
- **問題セット**: 1 セット 3 問。
- **問題ソース**: `assets/questions.json` からロード。
- **出題ロジック（確定）**:
  - **未出題優先**。未出題が尽きたら **全体ランダム**へフォールバック。
  - **「クイズを開始」時は *アプリ起動ごと* にランダムなセットを選定**（*同一日の再現性は担保しない*）。
  - **「次の3問へ」** も未出題優先ランダム。
- **結果画面の動線**: 次セット／同じ順番で復習／復習一覧／スコア履歴／ホーム。 

### 2.2 収益化機能：課金と広告
- **Premium（サブスクリプション）**:
  - `PremiumRepository` を **単一の真実のソース**とし、`StateFlow<Boolean> isPremium` を公開。全画面が購読。
  - 特典: **広告完全非表示／解説の全文表示／1日 10 セット**／（将来）弱点特訓。
  - 設定画面から **購入の復元** を提供。将来は **サーバー検証**も検討。
- **広告（無料ユーザーのみ）**:
  - **Interstitial**: セット完了→結果画面遷移時。頻度は Remote Config の `interstitial_cap_per_session` / `inter_session_interval_sec` で制御。
  - **Rewarded**: 1 日の上限到達時に +1 セット付与（**1 日 1 回まで**）。
  - **同意（UMP）未取得時はロード/表示を抑止**。

### 2.3 学習支援機能
- **リマインド通知**: 設定画面で時刻を保存し、バックグラウンド処理は **WorkManager 推奨**。通知タップで **Home** に復帰し学習導線へ接続。

### 2.4 主要画面の構成（要点）
- **HomeRoute**: 学習開始時に `isPremium` と学習上限を判定。無料かつ上限到達時は Reward 提案へ。
- **QuizRoute**: 3 問を提示。出題は未出題優先、全問出題後は全体ランダム。
- **ResultRoute**: 次セット／復習／履歴／ホーム。無料ユーザーは次遷移時に Interstitial の可能性。
- **SettingsRoute**: リマインド設定／購入の復元／定期購入管理／プライバシーポリシー。

---

## 3. 技術スタックとアーキテクチャ
### 3.1 コア
- **言語**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **アーキテクチャ**: MVVM + UDF（ViewModel が `StateFlow<UiState>` を公開、UI は `collectAsStateWithLifecycle()` で購読）
- **モジュール**: `:app`（UI/Android 依存）, `:core`（共通基盤）, `:data`（Repository）, `:domain`（UseCase/モデル）

### 3.2 UI とスタイリング
- **Route/Screen 分離**: `Route` は VM 生成・状態監視・イベント橋渡し。`Screen` は**表示専任**。
- **文字列**は **`strings.xml` 管理**（ハードコード禁止）。`stringResource()` を使用。
- **UiState は immutable**（`val`/`List` を優先）。

### 3.3 データ/ドメイン層の原則
- **ビジネスロジックは UseCase** に集約し、UI→ViewModel→UseCase の導線を徹底。
- **Android の `Context` をドメイン/データ層へ直接渡さない**。必要時は Hilt の `@ApplicationContext` を注入。

### 3.4 画面遷移（Navigation）
- Jetpack Navigation（Compose）を用い **UDF を保持**。
- **MUST: 非同期完了後の遷移は、完了を待ってから実行**（コールバック or **ワンショット `SharedFlow`** で順序保証。思い込み禁止）。

### 3.5 ログ／エラーハンドリング
- **重要イベントのみ** `android.util.Log`（`Log.d`/`Log.i`）。一時ログは**修正完了前に削除**。残置時は理由をコメントで明記。
- **ユーザー提示が必要なエラー**は **`UiState` or `SharedFlow`** で UI 層へ伝達。

### 3.6 依存とバージョン方針（例）
- Hilt 2.51.1 / Coroutines 1.8.1 / Navigation-Compose 2.8.4 / DataStore 1.1.1
- Billing 7.1.1 / Firebase BOM 32.7.4 / AdMob 22.6.0 / UMP 2.2.0 / Compose BOM 2024.06.00 / WorkManager 2.9.1
- バージョンはビルドスクリプトまたはドキュメントで明記。**非推奨 API** は検出次第、更新是非を検討し周知。

---

## 4. コーディング規約（まとめ）
- **MUST**:
  - **Premium 中は広告一切表示しない**
  - **文字列は `strings.xml`**
  - **Route/Screen 分離**
  - **Context 透過禁止**
  - **非同期完了→遷移**
  - **最小差分編集の厳格な遵守**: 1つのタスクでは、その目的達成に必要な最小限のファイル・行のみを変更する。これは、意図しない副作用（デグレード）を防ぎ、レビューの効率を最大化するための最重要原則である。特に、ビルドエラーの解消を目的として、**計画外のファイル（特に`build.gradle.kts`など）に手を加えることを固く禁じる**。
- **原則**:
  - UI ロジックは ViewModel
  - ビジネスロジックは UseCase
  - データ取得は Repository

---

## 5. 開発タスク ガイドライン
### 5.1 新機能追加（推奨フロー）
1) 関連コード・設計の現状把握（`:domain` UseCase, `:data` Repository, `:app` feature）。
2) **Plan**（影響範囲／追加・変更ファイル／リスク）を提示。
3) `ViewModel` → `Route` → `Screen` の順で**最小差分**実装。
4) **テスト**: ViewModel の状態遷移／UseCase・Repository のロジックはユニットテスト。

### 5.2 バグ修正（推奨フロー）
1) 事実の観測: 必要箇所に一時ログを追加し原因仮説を検証。
2) `find usages` で影響範囲を確認。
3) **最小差分**で修正（import 過不足/拡張関数影響をセルフチェック）。
4) `gradle build` 成功をもって自己証明。

---

## 6. 参照ドキュメント（instructions）
- UI: `.github/instructions/ui-compose.instructions.md`
- Navigation: `.github/instructions/navigation.instructions.md`
- Billing: `.github/instructions/billing.instructions.md`
- Ads/Consent: `.github/instructions/ads.instructions.md`
- Quiz Domain: `.github/instructions/quiz-domain.instructions.md`
- Data/Repository: `.github/instructions/data-repository.instructions.md`
- Notifications: `.github/instructions/notifications.instructions.md`
- Logging & Errors: `.github/instructions/logging-error-handling.instructions.md`
- Build & Deps: `.github/instructions/build-and-deps.instructions.md`
- Testing: `.github/instructions/testing.instructions.md`
- Agent Guidelines: `.github/instructions/agent-guidelines.md`

---

## 7. 既知の制約（メモ）
- 課金状態の検証は現状クライアントのみ（将来、サーバー検証を検討）。
- 問題はローカル `questions.json` から取得。

## 8. 重要なコミュニケーションルール（抄）
- **タスクの復唱** → **不足情報の要求** → **Plan 提示** → **承認後に差分編集** → **完了報告**。
- 同じ提案のループ禁止。セッションの独断終了禁止。

---

## 9. 【最重要】失敗の記録と拘束力のある開発プロセス (2024-06-12 追記)
過去のセッションにおいて、「インターフェースファイルの削除」という単純なタスクで、依存関係の分析を怠り、ビルドエラーを連発させ、最終的に`build.gradle.kts`という無関係なファイルにまで手を入れるという重大な失敗が発生した。

この失敗を二度と繰り返さないため、以下のプロセスを**全てのファイル変更・削除タスクにおける必須の行動規範**として定義し、**絶対に遵守する**。

### 9.1 拘束力のある変更・削除チェックリスト

1.  **【必須】依存関係グラフの完全分析**:
    - `find usages` の結果を、以下の2つに必ず分類する。
      - **(a) 直接参照**: コード内での直接的な呼び出し。
      - **(b) 間接参照（DI）**: Hilt の Module (`@Binds`, `@Provides`) や、その他DIフレームワークによる注入。
    - この分析なくして、修正計画を立案してはならない。

2.  **【必須】最小差分計画の徹底**:
    - 修正計画は、上記(a), (b)で特定した**全ての**依存関係の修正を網羅したものでなければならない。
    - 計画にないファイルには、たとえエラー解消のためであっても、決して手を出さない。

3.  **【必須】ビルドエラー発生時の厳格な手順**:
    - ビルドエラーが発生した場合、その原因を**「コードの依存関係の不整合（特にDI周り）」と第一に推定する**。
    - ビルド設定（`build.gradle.kts`等）の問題であると**安易に断定しない**。
    - 修正が困難と判断した場合、即座に作業を中断し、正直に状況を報告の上、**全ての変更を元に戻す選択肢を含めて**ユーザーの判断を仰ぐ。

### 9.2 遵守の表明義務
- 今後、インターフェースやクラスの削除・リファクタリング等のタスクを開始する際は、**「AGENTS.md 9項の規定に基づき、依存関係の完全分析から開始します」**と表明し、上記チェックリストの遵守を宣言する。

---

## 10. 【最重要】テスト失敗時の根本原因分析プロセス (2024-06-12 追記)
過去のセッションにおいて、「ユニットテストの修正」というタスクで、根本原因の分析を怠り、エラーメッセージの表面だけを追う場当たり的な修正を繰り返した結果、多大な手戻りを発生させ、ユーザーの時間を著しく浪費させるという重大な失敗が発生した。

この失敗を二度と繰り返さないため、`test`タスクが失敗した場合、以下のプロセスを**必須の思考規範**として定義し、**絶対に遵守する**。

### 10.1 拘束力のあるテストデバッグ・チェックリスト

1.  **【必須】即時停止と全体分析**:
    - エラーメッセージに表示されたコードを**直ちに修正しようとしない**。
    - 最初に、テスト対象の**ViewModel**、関連する**UiState**（特に**デフォルト値**）、および非同期処理を行う**UseCase**や**Repository**のコードを**全て読む**。

2.  **【必須】状態遷移の完全なマッピング**:
    - 上記の分析に基づき、テスト対象の`StateFlow`が取りうる**全ての状態遷移を時系列で書き出す**（例：`Initial(isLoading=false)` → `Loading(isLoading=true)` → `Success(isLoading=false, data=...)`）。
    - このマッピングなくして、テストコードの修正に着手してはならない。

3.  **【必須】根本原因の仮説構築**:
    - 実際のテスト結果と、上記でマッピングした「期待される状態遷移」を比較する。
    - **なぜ食い違いが発生したのか**、その根本原因についての仮説を立てる（例：「非同期処理の待機漏れ」「DIの不整合」「初期値の誤解」など）。
    - **この仮説と、それに基づいた修正計画をユーザーに提示し、承認を得る**。

### 10.2 遵守の表明義務
- 今後、`test`タスクが失敗し、その修正に取り掛かる際は、**「AGENTS.md 10項の規定に基づき、根本原因の分析から開始します」**と表明し、上記チェックリストの遵守を宣言する。
