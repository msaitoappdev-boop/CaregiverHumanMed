# W1 配布物（Android・即時上書き可能）

パッケージ名：`jp.msaitoappdev.caregiver.humanmed`

この ZIP は **プロジェクト直下に上書き展開**すれば、そのまま配置される構成です。
（`app/`配下の res/xml・res/values、Kotlinファイル、Firebaseテンプレート、web公開用 app-ads.txt など）

---

## 1. ファイル配置（この ZIP のまま上書き）

```
deliverables_w1_android/
├─ app/
│  └─ src/main/
│     ├─ res/
│     │  ├─ xml/remote_config_defaults.xml          # Remote Config デフォルト
│     │  └─ values/admob_ids.xml                    # AdMob App ID / Ad Unit ID（テストID）
│     └─ java/jp/msaitoappdev/caregiver/humanmed/config/
│        ├─ RemoteConfigKeys.kt                     # RC キー定数
│        └─ AdUnits.kt                              # Ad Unit 参照ヘルパ
├─ firebase/remote_config_template.json             # Console インポート用テンプレ
├─ deploy/web/app-ads.txt                           # Web ルートに設置するファイル
└─ documentation/README.md                          # 本ドキュメント複製
```

> **手順**：ZIP をプロジェクト直下に展開 → 既存ファイルに上書き。

---

## 2. Remote Config（アプリ側のデフォルト）
- `app/src/main/res/xml/remote_config_defaults.xml` を **setDefaultsAsync** に渡して使用します。

### 初期化例（Kotlin）
```kotlin
val rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
rc.setDefaultsAsync(R.xml.remote_config_defaults)
rc.fetchAndActivate()
```

### Console テンプレート
- `firebase/remote_config_template.json` を **Firebase Console > Remote Config > インポート**で読み込み可能。

---

## 3. AdMob（テスト ID で動作）
- `app/src/main/res/values/admob_ids.xml` は **Google テストID**を設定済みです（開発向け）。
  - App ID（テスト）：`ca-app-pub-3940256099942544~3347511713`
  - Interstitial（テスト）：`ca-app-pub-3940256099942544/1033173712`
  - Rewarded（テスト）：`ca-app-pub-3940256099942544/5224354917`
- リリース前に **実ID**へ差し替えてください（AdMob コンソールで発行）。

### 初期化例（Android）
```kotlin
com.google.android.gms.ads.MobileAds.initialize(this) {{}}
```

### 取得例（Ad Unit）
```kotlin
val interstitialId = AdUnits.interstitialWeaktrainComplete(context)
val rewardedId = AdUnits.rewardedWeaktrainPlusOne(context)
```

---

## 4. app-ads.txt（Web ルート）
- このファイルは **Android プロジェクトではなく**、あなたの**ドメインのルート**に配置します。
  - 例：`https://YOUR-DOMAIN.com/app-ads.txt`
  - 内容：`deploy/web/app-ads.txt` をアップロードし、`pub-XXXXXXXXXXXXXXXX` を **実 Publisher ID** に置換。
- AdMob 側で **ドメインを登録**すると検証されます（設置は任意ですが推奨）。

---

## 5. 依存関係（build.gradle）
```gradle
implementation platform('com.google.firebase:firebase-bom:32.7.4')
implementation 'com.google.firebase:firebase-config-ktx'
implementation 'com.google.firebase:firebase-analytics-ktx'
implementation 'com.google.android.gms:play-services-ads:22.6.0'
implementation 'com.google.android.ump:user-messaging-platform:2.2.0'
```
> 各バージョンは将来更新されるため、実装時は最新安定版をご確認ください。

---

## 6. UMP（同意）
- **EEA/UK**：起動時に UMP 同意フォーム表示。
- **日本**：同意 UI なし（任意）。アプリ内「設定＞プライバシー」に案内を常設してください。

---

## 7. よくある質問

**Q. Remote Config のキーを追加したい**
- `remote_config_defaults.xml` と Console テンプレの両方にキーを追加してください。

**Q. テスト広告が表示されない**
- 端末をテストデバイスに登録、もしくは **Google テスト AdUnit** を使用してください（本XMLはテストIDになっています）。

**Q. app-ads.txt はいつまでに必要？**
- 任意ですが、設置すると **広告配信の透明性**が向上します。AdMob コンソールで検証状態を確認できます。

---

不明点があれば、この README と同梱ファイルのままご質問ください。迅速にフォローします。
