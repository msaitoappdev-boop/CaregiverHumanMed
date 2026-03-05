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
        
        // 指定されたディレクトリ配下のファイルをスキャン
        loadQuestionsRecursively(config.assetDataDirectory, "", allQuestions)

        val questions = allQuestions
        cachedQuestions = questions
        questions
    }

    /**
     * @param path 現在スキャン中のアセットパス
     * @param currentCategory 現在のディレクトリ名（カテゴリとして使用）
     * @param outList 変換後の Domain モデルを格納するリスト
     */
    private fun loadQuestionsRecursively(path: String, currentCategory: String, outList: MutableList<Question>) {
        val assets = context.assets
        val items = assets.list(path) ?: return

        if (items.isEmpty()) {
            // 暗号化されたバイナリファイル (.bin) を読み込む
            if (path.endsWith(".bin")) {
                assets.open(path).use { encryptedStream ->
                    val decryptedStream = CryptoUtils.decryptStream(encryptedStream)
                    decryptedStream.bufferedReader().use { reader ->
                        val text = reader.readText()
                        val dtos = json.decodeFromString(ListSerializer(QuestionDto.serializer()), text)
                        
                        // ディレクトリ名をカテゴリ名として付与して変換
                        // currentCategory が空の場合は "general" などのデフォルト値を検討
                        val category = currentCategory.ifEmpty { "unclassified" }
                        outList.addAll(dtos.map { it.toDomain(category) })
                    }
                }
            }
        } else {
            for (item in items) {
                val fullPath = if (path.isEmpty()) item else "$path/$item"
                // ディレクトリ名を次の階層のカテゴリ候補として渡す
                // (quiz_data/01_human_social/ の場合、"01_human_social" がカテゴリになる)
                loadQuestionsRecursively(fullPath, item, outList)
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
