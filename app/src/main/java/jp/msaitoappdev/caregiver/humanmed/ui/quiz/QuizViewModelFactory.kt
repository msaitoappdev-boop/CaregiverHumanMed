package jp.msaitoappdev.caregiver.humanmed.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.msaitoappdev.caregiver.humanmed.data.QuestionRepository

class QuizViewModelFactory(
    private val repo: QuestionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(QuizViewModel::class.java))
        return QuizViewModel(repo) as T
    }
}
