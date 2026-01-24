package jp.msaitoappdev.caregiver.humanmed.data.repository

import android.content.Context
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.data.dto.QuestionDto
import jp.msaitoappdev.caregiver.humanmed.data.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class QuestionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : QuestionRepository {


    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadAll(): List<Question> = withContext(Dispatchers.IO) {
        context.assets.open("questions.json").bufferedReader().use { reader ->
            val text = reader.readText()
            val dtos = json.decodeFromString(ListSerializer(QuestionDto.serializer()), text)
            dtos.map { it.toDomain() }
        }
    }
}
