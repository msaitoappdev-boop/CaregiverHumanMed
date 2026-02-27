package com.msaitodev.quiz.feature.main.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.msaitodev.quiz.core.common.navigation.QuizActions
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.usecase.GetDailyQuestionsUseCase
import com.msaitodev.quiz.core.domain.usecase.GetNextQuestionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val remoteConfigRepo: RemoteConfigRepository,
    premiumRepository: PremiumRepository
) : ViewModel() {

    private var onQuizFinished: ((result: QuizResult) -> Unit)? = null
    private var isReviewSession: Boolean = false

    // Internal, mutable states that drive the logic
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

    // Combined, immutable UiState for the UI
    val uiState: StateFlow<QuizUiState> = combine(
        _internalState, premiumRepository.isPremium
    ) { internalState, isPremium ->
        QuizUiState(
            isLoading = internalState.isLoading,
            questions = internalState.questions,
            total = internalState.questions.size,
            currentIndex = internalState.currentIndex,
            selectedIndex = internalState.answers.getOrNull(internalState.currentIndex),
            isAnswered = internalState.answers.getOrNull(internalState.currentIndex) != null,
            correctCount = calcScore(internalState.questions, internalState.answers),
            canShowFullExplanation = isPremium
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
            QuizActions.ACTION_START_NEW -> {
                isReviewSession = false
                loadNextSet()
            }
            QuizActions.ACTION_RESTART_SAME_ORDER -> {
                isReviewSession = true
                reset(reshuffle = false)
            }
        }
    }

    private fun loadAndPrepare(reshuffle: Boolean) {
        _internalState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val setSize = remoteConfigRepo.getLong("set_size").toInt().coerceAtLeast(1)
            val daily = try {
                withContext(Dispatchers.IO) { getDailyQuestions(count = setSize) }
            } catch (_: Exception) { emptyList() }
            processAndStart(daily, reshuffle, daily.map { it.id }.toSet())
        }
    }

    private fun loadNextSet() {
        _internalState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val setSize = remoteConfigRepo.getLong("set_size").toInt().coerceAtLeast(1)
            val nextQuestions = withContext(Dispatchers.IO) {
                getNextQuestions(count = setSize, excludingIds = _internalState.value.seenQuestionIds)
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
        if (state.currentIndex >= state.questions.size - 1) {
            val score = calcScore(state.questions, state.answers)
            val result = QuizResult(
                score = score,
                total = state.questions.size,
                questions = state.originalQuestions,
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
