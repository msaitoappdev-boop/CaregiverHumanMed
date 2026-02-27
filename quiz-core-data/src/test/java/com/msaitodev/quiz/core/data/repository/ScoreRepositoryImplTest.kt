package com.msaitodev.quiz.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.data.local.db.ScoreDao
import com.msaitodev.quiz.core.data.local.db.ScoreRecord
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.data.repository.ScoreRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ScoreRepositoryImplTest {

    private lateinit var repository: ScoreRepositoryImpl
    private val dao: ScoreDao = mock()

    @Before
    fun setUp() {
        repository = ScoreRepositoryImpl(dao)
    }

    @Test
    fun `history returns mapped domain models from dao`() = runTest {
        // Arrange
        val records = listOf(
            ScoreRecord(timestamp = 1L, score = 10, total = 20, percent = 50),
            ScoreRecord(timestamp = 2L, score = 15, total = 20, percent = 75)
        )
        whenever(dao.observeAll()).thenReturn(flowOf(records))

        // Act
        val result = repository.history().first()

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].timestamp).isEqualTo(1L)
        assertThat(result[0].percent).isEqualTo(50)
        assertThat(result[1].timestamp).isEqualTo(2L)
        assertThat(result[1].percent).isEqualTo(75)
    }

    @Test
    fun `add calls dao's insert with mapped entity`() = runTest {
        // Arrange
        val entry = ScoreEntry(timestamp = 3L, score = 18, total = 20, percent = 90)
        val expectedRecord = ScoreRecord(timestamp = 3L, score = 18, total = 20, percent = 90)

        // Act
        repository.add(entry)

        // Assert
        verify(dao).insert(expectedRecord)
    }

    @Test
    fun `clear calls dao's clear`() = runTest {
        // Act
        repository.clear()

        // Assert
        verify(dao).clear()
    }
}
