package com.zenith26.fest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.repository.ZenithRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedEventForDetail: Event? = null,
    val isDetailLoading: Boolean = false,
    val isRegistering: Boolean = false,
    val registrationSuccessMessage: String? = null,
    val registrationErrorMessage: String? = null
)

class StudentViewModel(
    private val repository: ZenithRepository = ZenithRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private var searchDebounceJob: Job? = null

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentCategory = _uiState.value.selectedCategory
            val currentSearch = _uiState.value.searchQuery

            val result = repository.getEvents(currentCategory, currentSearch)
            result.fold(
                onSuccess = { events ->
                    _uiState.update {
                        it.copy(
                            events = events,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            events = emptyList(),
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to connect to backend server"
                        )
                    }
                }
            )
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }

        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(250) // Match web app 250ms debounce
            loadEvents()
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadEvents()
    }

    fun resetFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedCategory = "All") }
        loadEvents()
    }

    fun openEventDetail(event: Event) {
        _uiState.update {
            it.copy(
                selectedEventForDetail = event,
                registrationSuccessMessage = null,
                registrationErrorMessage = null
            )
        }
        // Also fetch fresh detail from server
        viewModelScope.launch {
            val result = repository.getEventDetail(event.id)
            result.onSuccess { freshEvent ->
                if (_uiState.value.selectedEventForDetail?.id == event.id) {
                    _uiState.update { it.copy(selectedEventForDetail = freshEvent) }
                }
            }
        }
    }

    fun closeEventDetail() {
        _uiState.update {
            it.copy(
                selectedEventForDetail = null,
                registrationSuccessMessage = null,
                registrationErrorMessage = null
            )
        }
    }

    fun submitRegistration(name: String, roll: String, email: String, phone: String?) {
        val eventId = _uiState.value.selectedEventForDetail?.id ?: return
        val currentEvent = _uiState.value.selectedEventForDetail

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRegistering = true,
                    registrationSuccessMessage = null,
                    registrationErrorMessage = null
                )
            }

            val request = RegistrationRequest(
                studentName = name,
                rollNumber = roll,
                email = email,
                phone = phone
            )

            val result = repository.registerForEvent(eventId, request)
            result.fold(
                onSuccess = { regResponse ->
                    val eventTitle = currentEvent?.title ?: "the event"
                    _uiState.update {
                        it.copy(
                            isRegistering = false,
                            registrationSuccessMessage = "🎉 Registered successfully for $eventTitle! Reg ID: #${regResponse.id}",
                            registrationErrorMessage = null
                        )
                    }

                    // Refresh event detail to immediately update seat count
                    val freshEventRes = repository.getEventDetail(eventId)
                    freshEventRes.onSuccess { updatedEvent ->
                        _uiState.update { it.copy(selectedEventForDetail = updatedEvent) }
                    }

                    // Refresh the main events list
                    loadEvents()

                    // Auto-close modal after 1.8 seconds matching web app
                    delay(1800)
                    closeEventDetail()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRegistering = false,
                            registrationSuccessMessage = null,
                            registrationErrorMessage = error.message ?: "Registration failed. Please check inputs."
                        )
                    }
                }
            )
        }
    }
}
