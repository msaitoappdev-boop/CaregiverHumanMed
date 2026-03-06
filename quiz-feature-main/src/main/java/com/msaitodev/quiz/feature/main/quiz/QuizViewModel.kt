package com.msaitodev.quiz.feature.main.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.core.common.navigation.AppActions
import com.msaitodev.feature.settings.SettingsProvider
import com.msaitodev.quiz.core.domain.config.RemoteConfigKeys
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.usecase.GetDailyQuestionsUseCase
import com.msaitodev.quiz.core.domain.usecase.GetNextQuestionsUseCase
import com.msaitodev.quiz.core.domain.usecase.GetWeaknessQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import javax.inject.Inject

private fun calcScore(questions: List<Question>, answers: List<Int?>): Int {
    return questions.indices.count { i -> answers.getOrNull(i) == questions[i].correctIndex }
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val getDailyQuestions: GetDailyQuestionsUseCase,
    private val getNextQuestions: GetNextQuestionsUseCase,
    private val getWeaknessQuestions: GetWeaknessQuestionsUseCase,
    private val remoteConfigRepo: RemoteConfigRepository,
    private val settingsProvider: SettingsProvider,
    premiumRepository: PremiumRepository
) : ViewModel() {

    private var onQuizFinished: ((result: QuizResult) -> Unit)? = null
    private var isReviewSession: Boolean = false

    private data class InternalState(
        val isLoading: Boolean = true,
        val originalQuestions: List<Question> = emptyList(),
        val questions: List<Question> = emptyList(),
        val answers: List<Int?> = emptyList(),
        val currentIndex: Int = 0,
        val shuffleSeed: Long = System.currentTimeMillis(),
        val seenQuestionIds: Set<String> = emptySet()
    )
    private val _internalState = MutableStateFlow(InternalState())

    val uiState: StateFlow<QuizUiState> = combine(
        _internalState, 
        premiumRepository.isPremium,
        settingsProvider.isWeaknessMode,
        settingsProvider.weaknessCategoryName
    ) { internalState, isPremium, isWeaknessMode, categoryName ->
        val mode = when {
            isReviewSession -> QuizMode.Review
            isWeaknessMode -> {
                if (categoryName != null) QuizMode.WeaknessCategory(categoryName) else QuizMode.WeaknessAll
            }
            else -> QuizMode.Daily
        }
        QuizUiState(
            isLoading = internalState.isLoading,
            questions = internalState.questions,
            total = internalState.questions.size,
            currentIndex = internalState.currentIndex,
            selectedIndex = internalState.answers.getOrNull(internalState.currentIndex),
            isAnswered = internalState.answers.getOrNull(internalState.currentIndex) != null,
            correctCount = calcScore(internalState.questions, internalState.answers),
            canShowFullExplanation = isPremium,
            mode = mode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuizUiState())

    init {
        loadAndPrepare(reshuffle = true)
    }

    fun init(onQuizFinished: (result: QuizResult) -> Unit) {
        this.onQuizFinished = onQuizFinished
    }

    fun processAction(action: String) {
        when (action) {
            AppActions.ACTION_START_NEW -> {
                isReviewSession = false
                loadNextSet()
            }
            AppActions.ACTION_RESTART_SAME_ORDER -> {
                isReviewSession = true
                reset(reshuffle = false)
            }
        }
    }

    private fun loadAndPrepare(reshuffle: Boolean) {
        _internalState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val isWeaknessMode = settingsProvider.isWeaknessMode.first()
            val weaknessCategoryId = settingsProvider.weaknessCategoryId.first()
            val setSize = remoteConfigRepo.getLong(RemoteConfigKeys.SET_SIZE).toInt().coerceAtLeast(1)
            
            isReviewSession = false
            val questions = if (isWeaknessMode) {
                withContext(Dispatchers.IO) { 
                    getWeaknessQuestions.execute(
                        count = setSize,
                        categoryFilter = weaknessCategoryId
                    ) 
                }
            } else {
                try {
                    withContext(Dispatchers.IO) { getDailyQuestions(count = setSize) }
                } catch (_: Exception) { emptyList() }
            }
            
            processAndStart(questions, reshuffle, emptySet())
        }
    }

    private fun loadNextSet() {
        _internalState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val isWeaknessMode = settingsProvider.isWeaknessMode.first()
            val weaknessCategoryId = settingsProvider.weaknessCategoryId.first()
            val setSize = remoteConfigRepo.getLong(RemoteConfigKeys.SET_SIZE).toInt().coerceAtLeast(1)
            
            val nextQuestions = if (isWeaknessMode) {
                withContext(Dispatchers.IO) { 
                    getWeaknessQuestions.execute(
                        count = setSize, 
                        categoryFilter = weaknessCategoryId,
                        excludingIds = _internalState.value.seenQuestionIds
                    ) 
                }
            } else {
                withContext(Dispatchers.IO) {
                    getNextQuestions(count = setSize, excludingIds = _internalState.value.seenQuestionIds)
                }
            }

            processAndStart(nextQuestions, true, _internalState.value.seenQuestionIds + nextQuestions.map { it.id })
        }
    }

    private fun reset(reshuffle: Boolean) {
        processAndStart(_internalState.value.originalQuestions, reshuffle, _internalState.value.seenQuestionIds)
    }

    private fun processAndStart(
        source: List<Question>,
        reshuffle: Boolean,
        seenIds: Set<String>
    ) {
        val seed = if (reshuffle) System.currentTimeMillis() else _internalState.value.shuffleSeed
        val ordered = if (reshuffle) source.shuffled(Random(seed)) else source
        val questions = ordered.map { it.shuffleOptions(seed) }

        _internalState.value = InternalState(
            isLoading = false,
            originalQuestions = source,
            questions = questions,
            answers = MutableList(questions.size) { null },
            currentIndex = 0,
            shuffleSeed = seed,
            seenQuestionIds = seenIds
        )
    }

    fun selectOption(index: Int) {
        _internalState.update { state ->
            val newAnswers = state.answers.toMutableList().also { it[state.currentIndex] = index }
            state.copy(answers = newAnswers)
        }
    }

    fun next() {
        val state = _internalState.value
        val currentQuestionId = state.questions.getOrNull(state.currentIndex)?.id
        if (currentQuestionId != null) {
            _internalState.update { it.copy(seenQuestionIds = it.seenQuestionIds + currentQuestionId) }
        }

        if (state.currentIndex >= state.questions.size - 1) {
            val score = calcScore(state.questions, state.answers)
            val result = QuizResult(
                score = score,
                total = state.questions.size,
                questions = state.questions,
                answers = state.answers,
                isReview = isReviewSession
            )
            onQuizFinished?.invoke(result)
        } else {
            _internalState.update { it.copy(currentIndex = it.currentIndex + 1) }
        }
    }

    fun prev() {
        _internalState.update { it.copy(currentIndex = (it.currentIndex - 1).coerceAtLeast(0)) }
    }

    private fun Question.shuffleOptions(seed: Long): Question {
        val rnd = Random(seed + this.id.hashCode())
        val indices = this.options.indices.toList().shuffled(rnd)
        val newOptions = indices.map { this.options[it] }
        val newCorrect = indices.indexOf(this.correctIndex)
        return this.copy(options = newOptions, correctIndex = newCorrect)
    }
}
