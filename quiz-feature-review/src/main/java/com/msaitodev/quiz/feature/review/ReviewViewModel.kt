package com.msaitodev.quiz.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.core.navigation.ReviewDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private const val JSON_EMPTY_LIST = "[]"

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val questionsJson = savedStateHandle.get<String>(ReviewDestination.ARG_QUESTIONS_JSON) ?: JSON_EMPTY_LIST
            val answersJson = savedStateHandle.get<String>(ReviewDestination.ARG_ANSWERS_JSON) ?: JSON_EMPTY_LIST

            val questions = Json.decodeFromString<List<Question>>(questionsJson)
            val answers = Json.decodeFromString<List<Int?>>(answersJson)

            premiumRepository.isPremium.collect { isPremium ->
                val items = questions.mapIndexed { index, question ->
                    ReviewItem(
                        number = index + 1,
                        question = question.text,
                        options = question.options,
                        selectedIndex = answers.getOrNull(index),
                        correctIndex = question.correctIndex,
                        explanation = if (isPremium) question.explanation else null
                    )
                }
                _uiState.value = ReviewUiState(items = items, isLoading = false)
            }
        }
    }
}
