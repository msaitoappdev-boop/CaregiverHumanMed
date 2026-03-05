package com.msaitodev.caregiver.humanmed.notifications

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.msaitodev.caregiver.humanmed.R
import com.msaitodev.feature.settings.SettingsProvider
import com.msaitodev.quiz.core.domain.repository.CategoryNameProvider
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * クイズアプリ向けの設定画面ポリシー実装。
 * モード状態を DataStore に保存し、購読状態に応じて自動リセットを行います。
 */
@Singleton
class QuizSettingsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val premiumRepository: PremiumRepository,
    private val categoryNameProvider: CategoryNameProvider
) : SettingsProvider {

    private val scope = CoroutineScope(Dispatchers.IO)

    private object Keys {
        val IS_WEAKNESS_MODE = booleanPreferencesKey("is_weakness_mode")
        val WEAKNESS_CATEGORY_ID = stringPreferencesKey("weakness_category_id")
    }

    init {
        // 購読状態を監視し、無料ユーザーになったら弱点特訓モードを強制解除する
        scope.launch {
            premiumRepository.isPremium.collectLatest { isPremium ->
                if (!isPremium) {
                    updateWeaknessMode(false)
                }
            }
        }
    }

    override val privacyPolicyUrl: String = context.getString(R.string.privacy_policy_url)
    override val subscriptionManagementUrl: String = context.getString(R.string.subscription_management_url)

    override val weaknessModeTitle: String = context.getString(R.string.settings_weakness_mode_title)
    override val weaknessModeDescription: String = context.getString(R.string.settings_weakness_mode_desc)

    override val isWeaknessMode: Flow<Boolean> = dataStore.data.map { 
        it[Keys.IS_WEAKNESS_MODE] ?: false 
    }

    override val weaknessCategoryId: Flow<String?> = dataStore.data.map {
        it[Keys.WEAKNESS_CATEGORY_ID]
    }

    override val weaknessCategoryName: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.WEAKNESS_CATEGORY_ID]?.let { categoryNameProvider.getDisplayName(it) }
    }

    override suspend fun updateWeaknessMode(enabled: Boolean, categoryId: String?) {
        dataStore.edit { 
            it[Keys.IS_WEAKNESS_MODE] = enabled
            if (categoryId != null) {
                it[Keys.WEAKNESS_CATEGORY_ID] = categoryId
            } else if (!enabled || categoryId == null) {
                // 明示的な null 指定、または無効化時はカテゴリをクリアする
                it.remove(Keys.WEAKNESS_CATEGORY_ID)
            }
        }
    }
}
