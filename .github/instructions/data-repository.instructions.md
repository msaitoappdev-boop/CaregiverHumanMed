
# データ層（Repository/UseCase）

## 適用範囲
- `:data`（Repository 実装）と `:domain`（UseCase）。

## 基本方針
- **ビジネスロジックは UseCase** に分離し、UI からは ViewModel 経由で呼ぶ。
- ViewModel は `StateFlow<UiState>` と公開 API（イベント）を持ち、UI はそれを購読する。
- Android の `Context` をドメイン／データ層へ直接渡さない（必要なら `@ApplicationContext` で注入）。

