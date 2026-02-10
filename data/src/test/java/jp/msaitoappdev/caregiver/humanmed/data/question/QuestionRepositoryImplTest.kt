package jp.msaitoappdev.caregiver.humanmed.data.question

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*

class QuestionRepositoryImplTest {

    private val mockDataSource = mock(QuestionDataSource::class.java)
    private val repository = QuestionRepositoryImpl(mockDataSource)

    @Test
    fun `test get next questions`() = runBlocking {
        // Arrange
        val mockQuestions = listOf(Question(/* Add mock data here */))
        `when`(mockDataSource.getNextQuestions()).thenReturn(mockQuestions)

        // Act
        val result = repository.getNextQuestions()

        // Assert
        assertNotNull(result)
        verify(mockDataSource, times(1)).getNextQuestions()
    }
}
