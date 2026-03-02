package com.msaitodev.quiz.core.data.repository

import android.content.Context
import com.msaitodev.core.common.config.AppAssetConfig
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

        val allQuestions = mutableListOf<QuestionDto>()
        
        // 指定されたディレクトリ配下のファイルをスキャン
        loadQuestionsRecursively(config.assetDataDirectory, allQuestions)

        val questions = allQuestions.map { it.toDomain() }
        cachedQuestions = questions
        questions
    }

    private fun loadQuestionsRecursively(path: String, outList: MutableList<QuestionDto>) {
        val assets = context.assets
        val items = assets.list(path) ?: return

        if (items.isEmpty()) {
            if (path.endsWith(".json")) {
                assets.open(path).bufferedReader().use { reader ->
                    val text = reader.readText()
                    val dtos = json.decodeFromString(ListSerializer(QuestionDto.serializer()), text)
                    outList.addAll(dtos)
                }
            }
        } else {
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
