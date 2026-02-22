# 画面遷移（Navigation-Compose）

## 適用範囲
- `:app` モジュールのナビゲーション設定、`HomeRoute / QuizRoute / ResultRoute / SettingsRoute`。

## 基本方針
- Jetpack Navigation（Compose）を使用し、**単方向データフロー (UDF)** を保つ。
- **非同期処理完了後に遷移が必要**な場合は、コールバックまたはワンショット `SharedFlow` で**完了を待ってから**遷移する（順序の思い込み禁止）。

## 【最重要】画面間での結果の受け渡し：疎結合の原則

画面Bが、自身を呼び出した画面Aに結果を返して閉じる際の、疎結合を維持するための**絶対的なルール**を定義する。過去のセッションにおいて、この原則の誤解が、アーキテクチャの後退と重大な混乱を引き起こした。

### 【推奨パターン：Excellent】`previousBackStackEntry` を使う

呼び出し先（画面B）は、結果を返す際、`navController.previousBackStackEntry` を使って**「直前の画面」**に結果をセットする。

**理由：** この方法では、画面Bは呼び出し元が誰であるかを一切知る必要がない。相手が画面Aであろうと、将来追加される画面Cであろうと、同じ方法で結果を返すことができる。これこそが、**再利用性と保守性を最大化する、本プロジェクトのベストプラクティス**である。

### 【禁止パターン：Bad】`getBackStackEntry("route_name")` を使う

特定のルート名を名指しで指定して `SavedStateHandle` を取得し、結果をセットしてはならない。

**理由：** この方法は、画面Bが「自分は"route_name"という名前の画面から呼び出された」という事実を知っていることになり、**画面間の密結合を生む**。これはアーキテクチャの後退であり、私が過去に犯した過ちであるため、**固く禁じる**。

--- 

**実装例（MainActivity - 呼び出し先の実装）:**
```kotlin
// Result画面（呼び出し先）が閉じる際に、呼び出し元にアクションを返す
onReviewSameOrder = {
    // 「直前の画面」のSavedStateHandleに結果をセットする
    navController.previousBackStackEntry?.savedStateHandle?.set(
        QuizActions.KEY_QUIZ_ACTION, 
        QuizActions.ACTION_RESTART_SAME_ORDER
    )
    // そして、自分自身を閉じて前の画面に戻る
    navController.popBackStack()
},
```

**実装例（QuizRoute - 呼び出し元の実装）:**
```kotlin
// Result画面から返されたアクションを監視し、処理する
// NavControllerの現在のBackStackEntryが変更されるたびに実行される
LaunchedEffect(navController.currentBackStackEntry) {
    navController.currentBackStackEntry?.savedStateHandle
        ?.get<String>(QuizActions.KEY_QUIZ_ACTION)?.let { action ->
            // アクションをViewModelに渡して処理
            vm.processAction(action)
            // 処理済のアクションを必ず削除する
            navController.currentBackStackEntry?.savedStateHandle
                ?.remove<String>(QuizActions.KEY_QUIZ_ACTION)
        }
}
```

---

## 画面ごとの要点
- **HomeRoute**: 学習開始時にプレミアム状態と学習上限を判定。無料かつ上限到達時はリワード提案へ。
- **QuizRoute**: 3問完了で結果へ。問題は未出題優先、全問出題後は全体ランダムから再出題。
- **ResultRoute**: 次セット・復習・履歴・ホームの動線を提供。無料ユーザーは次遷移時にインタースティシャルの可能性。
- **SettingsRoute**: リマインド設定、購入の復元、定期購入管理、プライバシーポリシー。

