package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.GetDailyQuestionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import javax.inject.Inject

data class ReviewItem(
    val number: Int,
    val question: String,
    val options: List<String>,
    val selectedIndex: Int?,
    val correctIndex: Int,
    val explanation: String?
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val getDailyQuestions: GetDailyQuestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var answers: MutableList<Int?> = mutableListOf()
    private var questions: List<Question> = emptyList()

    private var shuffleSeed: Long = System.currentTimeMillis()
    private val shuffleQuestions: Boolean = true
    private val shuffleOptions: Boolean = true

    init {
        loadAndPrepare(reshuffle = false)
    }

    private fun loadAndPrepare(reshuffle: Boolean) {
        viewModelScope.launch {
            if (reshuffle) {
                shuffleSeed = System.currentTimeMillis()
            }

            val rc = Firebase.remoteConfig
            val setSize = rc.getLong("set_size").toInt().coerceAtLeast(1)
            val daily: List<Question> = try {
                withContext(Dispatchers.IO) { getDailyQuestions(count = setSize) }
            } catch (_: Exception) {
                emptyList()
            }

            val ordered = if (shuffleQuestions) {
                val order = daily.indices.shuffled(Random(shuffleSeed))
                order.map { daily[it] }
            } else {
                daily
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
    }

    fun reset(reshuffle: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = true,
                finished = false,
                currentIndex = 0,
                selectedIndex = null,
                isAnswered = false
            )
        }
        loadAndPrepare(reshuffle)
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
}
