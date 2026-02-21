package jp.msaitoappdev.caregiver.humanmed.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ClearScoresUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.GetDailyQuestionsUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.GetNextQuestionsUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ObserveScoresUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.SaveScoreUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.util.DailyQuestionSelector

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
}
