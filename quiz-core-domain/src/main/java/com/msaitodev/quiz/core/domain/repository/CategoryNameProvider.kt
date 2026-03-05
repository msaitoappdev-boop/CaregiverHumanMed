package com.msaitodev.quiz.core.domain.repository

/**
 * カテゴリ ID から表示用の名称（日本語など）を取得するためのプロバイダー。
 * ハブ層で実装することで、ドメイン層をプラットフォームや言語から独立させます。
 */
interface CategoryNameProvider {
    /**
     * カテゴリ ID（ディレクトリ名など）を表示名に変換します。
     */
    fun getDisplayName(categoryId: String): String
}
