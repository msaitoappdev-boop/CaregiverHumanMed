package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import jp.msaitoappdev.caregiver.humanmed.domain.usecase.GetDailyQuestionsUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class ReviewItem(
    val number: Int,
    val question: String,
    val options: List<String>,
    val selectedIndex: Int?, // null = 未回答
    val correctIndex: Int,
    val explanation: String?
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val getDailyQuestions: GetDailyQuestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // 回答履歴
    private var answers: MutableList<Int?> = mutableListOf()
    private var questions: List<Question> = emptyList()

    // この ViewModel ライフサイクル内で順序を安定化
    private var shuffleSeed: Long = System.currentTimeMillis()
    private val shuffleQuestions: Boolean = true
    private val shuffleOptions: Boolean = true

    init {
        loadAndPrepare(reshuffle = false)
    }

    /** 初期ロード or 再挑戦時の再ロード */
    private fun loadAndPrepare(reshuffle: Boolean) {
        viewModelScope.launch {
            if (reshuffle) {
                shuffleSeed = System.currentTimeMillis()
            }

            val daily: List<Question> = try {
                withContext(Dispatchers.IO) { getDailyQuestions(count = 3) }
            } catch (_: Exception) {
                emptyList()
            }

            // 設問順のシャッフル（daily ベース）
            val ordered = if (shuffleQuestions) {
                val order = daily.indices.shuffled(Random(shuffleSeed))
                order.map { daily[it] }
            } else {
                daily
            }

            // 選択肢シャッフル
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

    /** 再挑戦。（reshuffle=true で新しい順序） */
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

    /** 選択肢を選ぶ（確定済みでも変更可） */
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

    /** 次の問題へ（最終なら finished=true） */
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

    /** 前の問題へ */
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

    /** 各問題の選択肢をシャッフルし、正解インデックスを再計算 */
    private fun shuffleOptionsForAll(src: List<Question>, seed: Long): List<Question> {
        return src.mapIndexed { idx, q ->
            val rnd = Random(seed + idx)
            val indices = q.options.indices.toList().shuffled(rnd)
            val newOptions = indices.map { q.options[it] }
            val newCorrect = indices.indexOf(q.correctIndex)
            q.copy(options = newOptions, correctIndex = newCorrect)
        }
    }

    /** 結果画面遷移後の完了フラグを消費 */
    fun markResultNavigated() {
        _uiState.update { it.copy(finished = false) }
    }

    /** 復習画面向けスナップショット */
    fun getReviewItems(): List<ReviewItem> {
        val qs = questions
        return qs.mapIndexed { idx, q ->
            ReviewItem(
                number = idx + 1,
                question = q.text,
                options = q.options,
                selectedIndex = answers.getOrNull(idx),
                correctIndex = q.correctIndex,
                explanation = q.explanation
            )
        }
    }
}
