package com.msaitodev.quiz.core.data.repository

import android.content.Context
import com.msaitodev.core.common.config.AppAssetConfig
import com.msaitodev.core.common.util.CryptoUtils
import com.msaitodev.quiz.core.data.local.dto.QuestionDto
import com.msaitodev.quiz.core.data.mapper.toDomain
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: AppAssetConfig
) : QuestionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedQuestions: List<Question>? = null

    override suspend fun loadAll(): List<Question> = withContext(Dispatchers.IO) {
        cachedQuestions?.let { return@withContext it }

        val allQuestions = mutableListOf<Question>()
        
        // 指定されたルートディレクトリ配下をスキャン
        loadQuestionsRecursively(config.assetDataDirectory, "unclassified", allQuestions)

        val questions = allQuestions
        cachedQuestions = questions
        questions
    }

    /**
     * @param path 現在スキャン中のアセットパス
     * @param parentCategory 現在のパスの親ディレクトリ名（カテゴリとして使用）
     * @param outList 変換後の Domain モデルを格納するリスト
     */
    private fun loadQuestionsRecursively(path: String, parentCategory: String, outList: MutableList<Question>) {
        val assets = context.assets
        val items = assets.list(path) ?: return

        if (items.isEmpty()) {
            // ファイルに到達。parentCategory には直近のディレクトリ名が入っている。
            if (path.endsWith(".bin")) {
                assets.open(path).use { encryptedStream ->
                    val decryptedStream = CryptoUtils.decryptStream(encryptedStream)
                    decryptedStream.bufferedReader().use { reader ->
                        val text = reader.readText()
                        val dtos = json.decodeFromString(ListSerializer(QuestionDto.serializer()), text)
                        
                        // 正しいカテゴリ名を付与して変換
                        outList.addAll(dtos.map { it.toDomain(parentCategory) })
                    }
                }
            }
        } else {
            // ディレクトリの場合
            for (item in items) {
                val fullPath = if (path.isEmpty()) item else "$path/$item"
                
                // item がファイルかディレクトリか判断するために assets.list(fullPath) を確認
                val subItems = assets.list(fullPath)
                if (subItems != null && subItems.isNotEmpty()) {
                    // サブディレクトリがある場合は、このディレクトリ名を新しいカテゴリとして再帰
                    loadQuestionsRecursively(fullPath, item, outList)
                } else {
                    // ファイル、あるいは空ディレクトリの場合は、現在の parentCategory を維持して再帰
                    loadQuestionsRecursively(fullPath, parentCategory, outList)
                }
            }
        }
    }

    override suspend fun getRandomUnseenQuestions(count: Int, excludingIds: Set<String>): List<Question> {
        val allQuestions = loadAll()
        val unseenQuestions = allQuestions.filterNot { it.id in excludingIds }
        return if (unseenQuestions.size < count) {
            allQuestions.shuffled().take(count)
        } else {
            unseenQuestions.shuffled().take(count)
        }
    }
}
