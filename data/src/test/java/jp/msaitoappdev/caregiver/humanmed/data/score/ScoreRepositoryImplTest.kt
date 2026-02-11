package jp.msaitoappdev.caregiver.humanmed.data.score

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class ScoreRepositoryImplTest {

    private val mockDao = mock(ScoreDao::class.java)
    private val repository = ScoreRepositoryImpl(mockDao)

    @Test
    fun `test add score`() = runBlocking {
        // Arrange
        val scoreEntry = ScoreEntry(timestamp = System.currentTimeMillis(), score = 1, total = 3, percent = 33)

        // Act
        repository.add(scoreEntry)

        // Assert
        verify(mockDao, times(1)).insert(scoreEntry.toEntity())
    }
}
