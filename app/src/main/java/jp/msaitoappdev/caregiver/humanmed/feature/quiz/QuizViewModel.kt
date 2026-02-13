package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.domain.repository.RemoteConfigRepository
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.GetDailyQuestionsUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.GetNextQuestionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val getDailyQuestions: GetDailyQuestionsUseCase,
    private val getNextQuestions: GetNextQuestionsUseCase,
    private val remoteConfigRepo: RemoteConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var answers: MutableList<Int?> = mutableListOf()
    private var questions: List<Question> = emptyList()
    private val seenQuestionIds = mutableSetOf<String>()
    private var currentOriginalQuestions: List<Question> = emptyList()

    private var shuffleSeed: Long = System.currentTimeMillis()
    private val shuffleQuestions: Boolean = true
    private val shuffleOptions: Boolean = true

    private var isReviewSession: Boolean = false

    init {
        loadAndPrepare(reshuffle = false)
    }

    fun processNavEvents(ssh: SavedStateHandle) {
        val actionTick = ssh.get<Long>("action_tick")
        val reshuffleTick = ssh.get<Long>("reshuffleTick")

        if (actionTick == null && reshuffleTick == null) return

        val action = ssh.get<String>("action")
        val reshuffle = ssh.get<Boolean>("reshuffle")
        val isReview = ssh.get<Boolean>("is_review")

        this.isReviewSession = isReview ?: false

        when {
            action == "loadNext" -> loadNextSet()
            reshuffle != null -> reset(reshuffle)
        }

        ssh.remove<String>("action")
        ssh.remove<Long>("action_tick")
        ssh.remove<Boolean>("reshuffle")
        ssh.remove<Long>("reshuffleTick")
        ssh.remove<Boolean>("is_review")
    }

    private fun getSetSize(): Int {
        return remoteConfigRepo.getLong("set_size").toInt().coerceAtLeast(1)
    }

    private fun loadAndPrepare(reshuffle: Boolean) {
        _uiState.update { it.copy(isLoading = true, finished = false) }
        viewModelScope.launch {
            val setSize = getSetSize()
            val daily = try {
                withContext(Dispatchers.IO) { getDailyQuestions(count = setSize) }
            } catch (_: Exception) {
                emptyList()
            }
            currentOriginalQuestions = daily
            seenQuestionIds.addAll(daily.map { it.id })
            processAndStart(daily, reshuffle)
        }
    }

    fun loadNextSet() {
        _uiState.update { it.copy(isLoading = true, finished = false) }
        viewModelScope.launch {
            val setSize = getSetSize()
            val nextQuestions = withContext(Dispatchers.IO) {
                getNextQuestions(count = setSize, excludingIds = seenQuestionIds)
            }
            currentOriginalQuestions = nextQuestions
            seenQuestionIds.addAll(nextQuestions.map { it.id })
            processAndStart(nextQuestions, true)
        }
    }

    fun reset(reshuffle: Boolean) {
        _uiState.update { it.copy(isLoading = true, finished = false) }
        viewModelScope.launch {
            processAndStart(currentOriginalQuestions, reshuffle)
        }
    }

    private fun processAndStart(source: List<Question>, reshuffle: Boolean) {
        if (reshuffle) {
            shuffleSeed = System.currentTimeMillis()
        }

        val ordered = if (shuffleQuestions) {
            val order = source.indices.shuffled(Random(shuffleSeed))
            order.map { source[it] }
        } else {
            source
        }

        questions = if (shuffleOptions) {
            shuffleOptionsForAll(ordered, shuffleSeed)
        } else {
            ordered
        }

        answers = MutableList(questions.size) { null }
        _uiState.update {
            it.copy(
                isLoading = false,
                questions = questions,
                currentIndex = 0,
                selectedIndex = answers.getOrNull(0),
                isAnswered = answers.getOrNull(0) != null,
                correctCount = 0,
                finished = questions.isEmpty()
            )
        }
    }

    fun selectOption(index: Int) {
        _uiState.update { state ->
            if (state.total == 0) return@update state
            val cur = state.currentIndex
            answers[cur] = index
            state.copy(
                selectedIndex = index,
                isAnswered = true,
                correctCount = calcScore(questions, answers)
            )
        }
    }

    fun next() {
        _uiState.update { state ->
            if (state.total == 0) return@update state
            val last = state.currentIndex >= state.total - 1
            if (last) {
                state.copy(
                    correctCount = calcScore(questions, answers),
                    finished = true
                )
            } else {
                val nextIndex = state.currentIndex + 1
                val sel = answers.getOrNull(nextIndex)
                state.copy(
                    currentIndex = nextIndex,
                    selectedIndex = sel,
                    isAnswered = sel != null
                )
            }
        }
    }

    fun prev() {
        _uiState.update { state ->
            if (state.total == 0 || state.currentIndex <= 0) return@update state
            val prevIndex = state.currentIndex - 1
            val sel = answers.getOrNull(prevIndex)
            state.copy(
                currentIndex = prevIndex,
                selectedIndex = sel,
                isAnswered = sel != null,
                finished = false
            )
        }
    }

    private fun calcScore(questions: List<Question>, answers: List<Int?>): Int {
        var s = 0
        for (i in questions.indices) {
            val a = answers.getOrNull(i)
            if (a != null && a == questions[i].correctIndex) s++
        }
        return s
    }

    private fun shuffleOptionsForAll(src: List<Question>, seed: Long): List<Question> {
        return src.mapIndexed { idx, q ->
            val rnd = Random(seed + idx)
            val indices = q.options.indices.toList().shuffled(rnd)
            val newOptions = indices.map { q.options[it] }
            val newCorrect = indices.indexOf(q.correctIndex)
            q.copy(options = newOptions, correctIndex = newCorrect)
        }
    }

    fun markResultNavigated() {
        _uiState.update { it.copy(finished = false) }
    }

    fun getReviewItems(isPremium: Boolean): List<ReviewItem> {
        val qs = questions
        return qs.mapIndexed { idx, q ->
            ReviewItem(
                number = idx + 1,
                question = q.text,
                options = q.options,
                selectedIndex = answers.getOrNull(idx),
                correctIndex = q.correctIndex,
                explanation = if (isPremium) q.explanation else null
            )
        }
    }

    fun isReviewSession(): Boolean = isReviewSession
}
