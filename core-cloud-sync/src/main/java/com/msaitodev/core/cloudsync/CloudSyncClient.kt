package com.msaitodev.core.cloudsync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * クラウド同期（Firestore）の低レベル操作を担うクライアント。
 * 特定のドメイン（クイズ等）に依存しない汎用的な設計。
 */
interface CloudSyncClient {
    /** 現在のユーザーIDを取得（未サインイン時は匿名サインインを試行） */
    suspend fun getUserId(): String?
    
    /** 指定されたコレクションとドキュメントにデータをアップロード */
    suspend fun upload(collection: String, documentId: String, data: Map<String, Any>): Boolean
    
    /** 指定されたコレクションとドキュメントからデータをダウンロード */
    suspend fun download(collection: String, documentId: String): Map<String, Any>?
}

@Singleton
class CloudSyncClientImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CloudSyncClient {

    override suspend fun getUserId(): String? {
        val currentUser = auth.currentUser
        if (currentUser != null) return currentUser.uid
        
        return try {
            auth.signInAnonymously().await().user?.uid
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun upload(collection: String, documentId: String, data: Map<String, Any>): Boolean {
        val uid = getUserId() ?: return false
        return try {
            firestore.collection("users").document(uid)
                .collection(collection).document(documentId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun download(collection: String, documentId: String): Map<String, Any>? {
        val uid = getUserId() ?: return null
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection(collection).document(documentId)
                .get()
                .await()
            snapshot.data
        } catch (e: Exception) {
            null
        }
    }
}
