package jp.msaitoappdev.caregiver.humanmed.data.question

import android.content.Context
import android.content.res.AssetManager
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.ByteArrayInputStream

class QuestionRepositoryImplTest {

    private lateinit var mockContext: Context
    private lateinit var mockAssetManager: AssetManager
    private lateinit var repository: QuestionRepositoryImpl

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockAssetManager = mock(AssetManager::class.java)
        `when`(mockContext.assets).thenReturn(mockAssetManager)
        repository = QuestionRepositoryImpl(mockContext)
    }

    @Test
    fun `test loadAll`() = runBlocking {
        // Arrange
        val json = "[{\"id\":\"q1\",\"text\":\"Question 1\",\"options\":[\"A\",\"B\",\"C\"],\"correctIndex\":0,\"explanation\":\"Explanation 1\"}]"
        val inputStream = ByteArrayInputStream(json.toByteArray())
        `when`(mockAssetManager.open("questions.json")).thenReturn(inputStream)

        // Act
        val result = repository.loadAll()

        // Assert
        assertNotNull(result)
    }
}
