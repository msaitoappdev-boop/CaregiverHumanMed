package jp.msaitoappdev.caregiver.humanmed.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.GetDailyQuestionsUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.util.DailyQuestionSelector
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides @Singleton
    fun provideDailySelector(): DailyQuestionSelector = DailyQuestionSelector()

    @Provides @Singleton
    fun provideGetDailyQuestionsUseCase(
        repo: QuestionRepository,
        selector: DailyQuestionSelector
    ): GetDailyQuestionsUseCase = GetDailyQuestionsUseCase(repo, selector)
}
