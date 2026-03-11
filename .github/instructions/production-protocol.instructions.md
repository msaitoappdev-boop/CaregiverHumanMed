# 新規アプリ量産化プロトコル

本ドキュメントは、`CaregiverHumanMed` プロジェクトをテンプレートとして、新しい資格試験アプリを立ち上げるための標準的な手順を定義します。

---

## ステップ 1：プロジェクトの物理的な準備

1.  **新規フォルダの作成**:
    - `C:/Users/msait/AndroidStudioProjects/` 配下に、新しいアプリ用のフォルダを作成します。（例： `socialworker-humanmed-app`）

2.  **テンプレートファイルのコピー**:
    - `caregiver-humanmed-app` プロジェクトから、以下のファイルとフォルダをすべて新しいプロジェクトフォルダへコピーします。
      - `app/` （`build/` フォルダは除く）
      - `gradle/`
      - `.github/`
      - `master_data/`
      - `.gitignore`, `AGENTS.md`, `build.gradle.kts` 等、ルートにあるすべての設定ファイル。
    - **【注意】** `.git/` と `.idea/` フォルダはコピーしません。

3.  **Android Studio で開く**:
    - Android Studio の `File > Open` から、作成した新しいプロジェクトフォルダを開きます。

---

## ステップ 2：アプリ固有設定の書き換え

新しいプロジェクトが開けたら、以下のファイルを修正して「魂」を入れ替えます。

1.  **Application ID**: `app/build.gradle.kts`
    - `applicationId` を新しいものに変更。（例： `com.msaitodev.socialworker.humanmed`）

2.  **アプリ名**: `app/src/main/res/values/strings.xml`
    - `app_name` を新しいアプリ名に変更。

3.  **プロジェクト名**: `settings.gradle.kts`
    - `rootProject.name` を新しいプロジェクト名に変更。

4.  **Firebase**: `app/google-services.json`
    - Firebase コンソールで新しいプロジェクトを作成し、ダウンロードした `google-services.json` で上書きします。

---

## ステップ 3：コンテンツと収益化の設定

1.  **科目（カテゴリ）定義**:
    - `app/src/main/res/values/strings.xml` に新しい科目の表示名を追加。
    - `app/src/main/java/com/msaitodev/caregiver/humanmed/notifications/QuizCategoryNameProvider.kt` の `when` 式を、新しい科目定義に合わせて修正。

2.  **クイズデータ**:
    - `master_data/quiz_data` 配下に、新しい資格用の暗号化済み問題データ（`.bin`）を配置。

3.  **広告 (AdMob)**:
    - `app/build.gradle.kts` の `manifestPlaceholders["admob_app_id"]` を、新しい AdMob App ID に更新。

4.  **課金 (Billing)**:
    - `app/src/main/res/values/strings.xml` の `billing_product_id_premium` などの値を、新しい Play Console の製品 ID に更新。

---

## ステップ 4：最終確認と Git 登録

1.  **ビルド確認**:
    - Gradle Sync を実行し、`Build > Make Project` でエラーが出ないことを確認します。

2.  **Git 初期化**:
    - 新しいプロジェクトのルートで `git init` を実行し、初回コミットを作成します。
    - GitHub 上で新しいリモートリポジトリを作成し、`git push` します。
