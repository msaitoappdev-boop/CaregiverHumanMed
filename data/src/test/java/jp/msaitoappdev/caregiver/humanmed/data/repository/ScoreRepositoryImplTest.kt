package jp.msaitoappdev.caregiver.humanmed.data.repository

import jp.msaitoappdev.caregiver.humanmed.data.local.db.ScoreDao
import jp.msaitoappdev.caregiver.humanmed.data.mapper.toEntity
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito

class ScoreRepositoryImplTest {

    private val mockDao = Mockito.mock(ScoreDao::class.java)
    private val repository = ScoreRepositoryImpl(mockDao)

    @Test
    fun `test add score`() = runBlocking {
        // Arrange
        val scoreEntry =
            ScoreEntry(timestamp = System.currentTimeMillis(), score = 1, total = 3, percent = 33)

        // Act
        repository.add(scoreEntry)

        // Assert
        Mockito.verify(mockDao, Mockito.times(1)).insert(scoreEntry.toEntity())
    }
}