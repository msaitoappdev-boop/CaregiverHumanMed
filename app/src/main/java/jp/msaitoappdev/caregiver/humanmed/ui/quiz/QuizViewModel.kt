
package jp.msaitoappdev.caregiver.humanmed.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.msaitoappdev.caregiver.humanmed.data.Question
import jp.msaitoappdev.caregiver.humanmed.data.QuestionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import jp.msaitoappdev.caregiver.humanmed.ui.quiz.DailyQuestionSelector

class QuizViewModel(
    private val repository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // 各問の選択インデックスを保持（null = 未回答）
    private var answers: MutableList<Int?> = mutableListOf()
    private var questions: List<Question> = emptyList()

    // この ViewModel のライフサイクル内でシャッフルを安定化
    private var shuffleSeed: Long = System.currentTimeMillis()
    private val shuffleQuestions: Boolean = true   // 設問順をシャッフル
    private val shuffleOptions: Boolean = true     // 選択肢シャッフル

    // ★ “毎日3問” セレクタを追加
    private val selector = DailyQuestionSelector()

    init {
        loadAndPrepare(reshuffle = false)
    }

    /** 初期ロード or 再挑戦時の再ロード */
    private fun loadAndPrepare(reshuffle: Boolean) {
        viewModelScope.launch {
            if (reshuffle) {
                // 再挑戦時は seed を更新して新しい順序に
                shuffleSeed = System.currentTimeMillis()
            }

            val loaded: List<Question> = try {
                withContext(Dispatchers.IO) { repository.loadAll() }
            } catch (_: Exception) {
                emptyList()
            }

            // ★ 追加：その日限定の3問を先に選ぶ（loaded → daily）
            val daily = selector.select(all = loaded, count = 3)

    // 設問順シャッフル（ベースは loaded ではなく daily）
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
                    // ★ ここはロード結果で finished を確定（問題0件なら true）
                    finished = questions.isEmpty()
                )
            }
        }
    }

    /** 再挑戦。reshuffle=true で新しい順序に。 */
    fun reset(reshuffle: Boolean) {
        // ★ 重要：まず finished=false を即時反映して、結果画面再遷移の再発火を防ぐ
        _uiState.update {
            it.copy(
                isLoading = true,
                finished = false,   // ← これが「1回で戻る」鍵
                currentIndex = 0,   // UX的に先頭へ（任意）
                selectedIndex = null,
                isAnswered = false,
                // correctCount = 0   // 必要ならここで0に戻す（結果画面からのリセットなので0推奨の場合）
            )
        }
        loadAndPrepare(reshuffle = reshuffle)
    }

    /** 選択肢を選ぶ（確定済みでも選択変更可） */
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

    /** 前の問題へ（履歴から復元） */
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

    // --- ヘルパー ---

    private fun calcScore(questions: List<Question>, answers: List<Int?>): Int {
        var s = 0
        for (i in questions.indices) {
            val a = answers.getOrNull(i)
            if (a != null && a == questions[i].correctIndex) s++
        }
        return s
    }

    /** 各問題の選択肢をシャッフルし、正解インデックスを再計算して返す */
    private fun shuffleOptionsForAll(src: List<Question>, seed: Long): List<Question> {
        return src.mapIndexed { idx, q ->
            // 質問ごとに異なる乱数系列（seed + index で安定）
            val rnd = Random(seed + idx)
            val indices = q.options.indices.toList().shuffled(rnd)
            val newOptions = indices.map { q.options[it] }
            val newCorrect = indices.indexOf(q.correctIndex)
            q.copy(options = newOptions, correctIndex = newCorrect)
        }
    }

    // QuizViewModel.kt 内（class QuizViewModel の中）に追加
    fun markResultNavigated() {
        // 結果画面へ遷移した「完了フラグ」を消費して、直ちに finished=false に戻す
        _uiState.update { it.copy(finished = false) }
    }

    // QuizViewModel クラス内に追記（public メソッド）
    fun getReviewItems(): List<ReviewItem> {
        // 内部の questions / answers からスナップショットを生成して返す
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

data class ReviewItem(
    val number: Int,
    val question: String,
    val options: List<String>,
    val selectedIndex: Int?,   // null = 未回答
    val correctIndex: Int,
    val explanation: String?
)

