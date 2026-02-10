package jp.msaitoappdev.caregiver.humanmed.data.score

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*

class ScoreRepositoryImplTest {

    private val mockDataSource = mock(ScoreDataSource::class.java)
    private val repository = ScoreRepositoryImpl(mockDataSource)

    @Test
    fun `test save score`() = runBlocking {
        // Arrange
        val scoreEntry = ScoreEntry(/* Add mock data here */)

        // Act
        repository.save(scoreEntry)

        // Assert
        verify(mockDataSource, times(1)).save(scoreEntry)
    }
}
