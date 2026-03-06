package com.msaitodev.quiz.core.domain.repository

/**
 * 学習進捗や統計情報のクラウド同期を担うリポジトリ。
 */
interface SyncRepository {
    /** ローカルの最新データをクラウドにアップロード（クイズ終了時） */
    suspend fun uploadToCloud(): Boolean
    
    /** クラウドの最新データをローカルにダウンロードして統合（アプリ起動時） */
    suspend fun downloadFromCloud(): Boolean
}
