package com.msaitodev.quiz.core.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.repository.WrongAnswerRepository
import com.msaitodev.quiz.core.domain.usecase.ClearScoresUseCase
import com.msaitodev.quiz.core.domain.usecase.GetDailyQuestionsUseCase
import com.msaitodev.quiz.core.domain.usecase.GetNextQuestionsUseCase
import com.msaitodev.quiz.core.domain.usecase.GetWeaknessQuestionsUseCase
import com.msaitodev.quiz.core.domain.usecase.ObserveScoresUseCase
import com.msaitodev.quiz.core.domain.usecase.SaveScoreUseCase
import com.msaitodev.quiz.core.domain.ui.DailyQuestionSelector

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideObserveScoresUseCase(scoreRepository: ScoreRepository): ObserveScoresUseCase {
        return ObserveScoresUseCase(scoreRepository)
    }

    @Provides
    fun provideClearScoresUseCase(scoreRepository: ScoreRepository): ClearScoresUseCase {
        return ClearScoresUseCase(scoreRepository)
    }

    @Provides
    fun provideGetDailyQuestionsUseCase(questionRepository: QuestionRepository): GetDailyQuestionsUseCase {
        return GetDailyQuestionsUseCase(questionRepository, DailyQuestionSelector())
    }

    @Provides
    fun provideGetNextQuestionsUseCase(questionRepository: QuestionRepository): GetNextQuestionsUseCase {
        return GetNextQuestionsUseCase(questionRepository)
    }

    @Provides
    fun provideSaveScoreUseCase(scoreRepository: ScoreRepository): SaveScoreUseCase {
        return SaveScoreUseCase(scoreRepository)
    }

    @Provides
    fun provideGetWeaknessQuestionsUseCase(
        questionRepository: QuestionRepository,
        wrongAnswerRepository: WrongAnswerRepository
    ): GetWeaknessQuestionsUseCase {
        return GetWeaknessQuestionsUseCase(questionRepository, wrongAnswerRepository)
    }
}