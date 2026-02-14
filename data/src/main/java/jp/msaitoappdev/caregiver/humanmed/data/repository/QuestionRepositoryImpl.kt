package jp.msaitoappdev.caregiver.humanmed.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.msaitoappdev.caregiver.humanmed.data.local.question.dto.QuestionDto
import jp.msaitoappdev.caregiver.humanmed.data.mapper.toDomain
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : QuestionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedQuestions: List<Question>? = null

    override suspend fun loadAll(): List<Question> = withContext(Dispatchers.IO) {
        cachedQuestions?.let { return@withContext it }
        val questions = context.assets.open("questions.json").bufferedReader().use { reader ->
            val text = reader.readText()
            val dtos = json.decodeFromString(ListSerializer(QuestionDto.serializer()), text)
            dtos.map { it.toDomain() }
        }
        cachedQuestions = questions
        questions
    }

    override suspend fun getRandomUnseenQuestions(count: Int, excludingIds: Set<String>): List<Question> {
        val allQuestions = loadAll()
        val unseenQuestions = allQuestions.filterNot { it.id in excludingIds }
        return if (unseenQuestions.size < count) {
            // Reset if not enough questions are available
            allQuestions.shuffled().take(count)
        } else {
            unseenQuestions.shuffled().take(count)
        }
    }
}