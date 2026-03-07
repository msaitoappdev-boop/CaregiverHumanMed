package com.msaitodev.caregiver.humanmed.notifications

import android.content.Context
import com.msaitodev.caregiver.humanmed.R
import com.msaitodev.quiz.core.domain.repository.CategoryNameProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * クイズアプリ向けのカテゴリ名称供給実装。
 * ディレクトリ名（ID）を、Hub層の strings.xml に定義された日本語名に変換します。
 */
@Singleton
class QuizCategoryNameProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : CategoryNameProvider {
    override fun getDisplayName(categoryId: String): String {
        val resId = when (categoryId) {
            "01_human_social" -> R.string.cat_01_human_social
            "02_care_basic" -> R.string.cat_02_care_basic
            "03_mind_body" -> R.string.cat_03_mind_body
            "04_medical_care" -> R.string.cat_04_medical_care
            "05_comprehensive" -> R.string.cat_05_comprehensive
            "unclassified" -> R.string.cat_unclassified
            else -> null
        }
        
        return if (resId != null) {
            context.getString(resId)
        } else {
            // マッピングがない場合は ID をそのまま返し、不備に気づきやすくする
            categoryId
        }
    }
}
