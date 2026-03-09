
# 課金（サブスクリプション）

> **【重要】マルチリポジトリ化に伴い、Google Play Billing との通信や課金状態管理のコアロジック（BillingManager, PremiumRepository 等）は外部リポジトリ（`msaitodev-android-core`）へ分離されています。本プロジェクト（ハブ）では `BillingModule` を通じた設定供給と UI への統合を担います。**

## 適用範囲
- `msaitodev-android-core` 内の `feature-billing` モジュール、および本プロジェクトの `BillingModule`。

## 基本方針
- プレミアム状態は `PremiumRepository` が単一の**真実のソース**。`StateFlow<Boolean> isPremium` を公開し、全画面が購読。
- プレミアム特典：広告完全非表示、解説全文、1日の学習上限解除、学習分析全機能の解放。

## 実装メモ
- 購入の復元機能を設定画面から提供。
- 将来はサーバー検証を検討（現状はクライアントのみ）。
- 本プロジェクトの `strings.xml` に製品 ID や特典内容を定義し、外部ライブラリへ注入する。
