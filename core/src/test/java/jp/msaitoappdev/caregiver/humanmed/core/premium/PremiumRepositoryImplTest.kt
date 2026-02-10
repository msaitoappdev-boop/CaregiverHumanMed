package jp.msaitoappdev.caregiver.humanmed.core.premium

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*

class PremiumRepositoryImplTest {

    private val mockDataSource = mock(PremiumDataSource::class.java)
    private val repository = PremiumRepositoryImpl(mockDataSource)

    @Test
    fun `test fetch premium status`() = runBlocking {
        // Arrange
        val mockStatus = true
        `when`(mockDataSource.isPremiumUser()).thenReturn(mockStatus)

        // Act
        val result = repository.isPremiumUser()

        // Assert
        assertNotNull(result)
        verify(mockDataSource, times(1)).isPremiumUser()
    }
}
