
# Compose UI ルール（Route/Screen 分離）

## 適用範囲
- `:app` モジュールの `ui/` パッケージ配下、および Compose 画面全般。

## 基本方針
- UI は **Jetpack Compose + Material 3** を使用。XML レイアウトは提案しない。
- 画面は **`Route` と `Screen`** に分離する。`Route` は ViewModel の生成・状態監視・イベント橋渡し、`Screen` は**表示だけ**に責務を限定する。
- 文字列は **`strings.xml`** で管理し、**ハードコード禁止**。`stringResource()` を使う。
- `UiState` は不変（immutable）。`val` と `List` を優先し、`var`/`MutableList` の使用は避ける。

## 実装メモ
- `Route` では `collectAsStateWithLifecycle()` で `StateFlow<UiState>` を購読し、`Screen` に渡す。
- ダークテーマはデフォルト対応。色は `MaterialTheme.colorScheme` を使用。

