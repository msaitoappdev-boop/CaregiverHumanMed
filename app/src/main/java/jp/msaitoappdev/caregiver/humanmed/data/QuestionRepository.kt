
package jp.msaitoappdev.caregiver.humanmed.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class QuestionRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadAll(): List<Question> = withContext(Dispatchers.IO) {
        context.assets.open("questions.json").bufferedReader().use { reader ->
            val text = reader.readText()
            json.decodeFromString(ListSerializer(Question.serializer()), text)
        }
    }
}
