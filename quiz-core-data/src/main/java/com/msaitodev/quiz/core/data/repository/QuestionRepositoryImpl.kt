package com.msaitodev.quiz.core.data.repository

import android.content.Context
import com.msaitodev.quiz.core.common.config.QuizAssetConfig
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
    private val config: QuizAssetConfig
) : QuestionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedQuestions: List<Question>? = null

    override suspend fun loadAll(): List<Question> = withContext(Dispatchers.IO) {
        cachedQuestions?.let { return@withContext it }

        val allQuestions = mutableListOf<QuestionDto>()
        
        // 指定されたルートディレクトリ配下のファイルを再帰的にスキャン
        loadQuestionsRecursively(config.quizDataRootDirectory, allQuestions)

        val questions = allQuestions.map { it.toDomain() }
        cachedQuestions = questions
        questions
    }

    private fun loadQuestionsRecursively(path: String, outList: MutableList<QuestionDto>) {
        val assets = context.assets
        val items = assets.list(path) ?: return

        if (items.isEmpty()) {
            // ファイルの可能性がある場合（ディレクトリでなければ空のリストが返ることがある）
            if (path.endsWith(".json")) {
                assets.open(path).bufferedReader().use { reader ->
                    val text = reader.readText()
                    val dtos = json.decodeFromString(ListSerializer(QuestionDto.serializer()), text)
                    outList.addAll(dtos)
                }
            }
        } else {
            // ディレクトリの場合
            for (item in items) {
                val fullPath = if (path.isEmpty()) item else "$path/$item"
                loadQuestionsRecursively(fullPath, outList)
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
