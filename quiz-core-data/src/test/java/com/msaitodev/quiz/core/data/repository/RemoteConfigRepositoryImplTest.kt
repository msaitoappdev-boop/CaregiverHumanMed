package com.msaitodev.quiz.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.msaitodev.quiz.core.data.repository.RemoteConfigRepositoryImpl
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RemoteConfigRepositoryImplTest {

    @Test
    fun `getLong returns value from remote config`() {
        // GIVEN: 特定の値を返すFirebaseRemoteConfigのモックを作成
        val mockConfig = mock<FirebaseRemoteConfig> {
            on { getLong("test_key") } doReturn 123L
        }
        val repository = RemoteConfigRepositoryImpl(mockConfig)

        // WHEN: getLongを呼び出す
        val result = repository.getLong("test_key")

        // THEN: モックが設定した通りの値が返されることを確認
        assertThat(result).isEqualTo(123L)

        // THEN: FirebaseRemoteConfigのgetLongが正しいキーで呼ばれたことを確認
        verify(mockConfig).getLong("test_key")
    }

    @Test
    fun `getBoolean returns value from remote config`() {
        // GIVEN: 特定の値を返すFirebaseRemoteConfigのモックを作成
        val mockConfig = mock<FirebaseRemoteConfig> {
            on { getBoolean("test_key") } doReturn true
        }
        val repository = RemoteConfigRepositoryImpl(mockConfig)

        // WHEN: getBooleanを呼び出す
        val result = repository.getBoolean("test_key")

        // THEN: モックが設定した通りの値が返されることを確認
        assertThat(result).isTrue()

        // THEN: FirebaseRemoteConfigのgetBooleanが正しいキーで呼ばれたことを確認
        verify(mockConfig).getBoolean("test_key")
    }
}
