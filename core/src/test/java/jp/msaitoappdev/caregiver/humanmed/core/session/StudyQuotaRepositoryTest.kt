package jp.msaitoappdev.caregiver.humanmed.core.session

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*

class StudyQuotaRepositoryTest {

    private val mockDataSource = mock(QuotaDataSource::class.java)
    private val repository = StudyQuotaRepository(mockDataSource)

    @Test
    fun `test fetch quota`() = runBlocking {
        // Arrange
        val mockQuota = QuotaState(/* Add mock data here */)
        `when`(mockDataSource.fetchQuota()).thenReturn(mockQuota)

        // Act
        val result = repository.fetchQuota()

        // Assert
        assertNotNull(result)
        verify(mockDataSource, times(1)).fetchQuota()
    }
}
