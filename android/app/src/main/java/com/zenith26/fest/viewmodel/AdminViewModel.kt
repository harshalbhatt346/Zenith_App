package com.zenith26.fest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith26.fest.data.model.CreateEventRequest
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationListItem
import com.zenith26.fest.data.model.StatsResponse
import com.zenith26.fest.repository.ZenithRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val stats: StatsResponse? = null,
    val events: List<Event> = emptyList(),
    val registrations: List<RegistrationListItem> = emptyList(),
    val selectedEventIdFilter: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0, // 0 = Events Table, 1 = Registrations Table
    val showAddEventModal: Boolean = false,
    val isAddingEvent: Boolean = false,
    val addEventErrorMessage: String? = null,
    val pendingCancelRegistration: RegistrationListItem? = null
)

class AdminViewModel(
    private val repository: ZenithRepository = ZenithRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Load Stats
            val statsResult = repository.getStats()
            // 2. Load Events
            val eventsResult = repository.getEvents(null, null)
            // 3. Load Registrations
            val filterId = _uiState.value.selectedEventIdFilter
            val regsResult = repository.getRegistrations(filterId)

            var error: String? = null
            var stats: StatsResponse? = _uiState.value.stats
            var events: List<Event> = _uiState.value.events
            var registrations: List<RegistrationListItem> = _uiState.value.registrations

            statsResult.fold(
                onSuccess = { stats = it },
                onFailure = { error = it.message }
            )

            eventsResult.fold(
                onSuccess = { events = it },
                onFailure = { if (error == null) error = it.message }
            )

            regsResult.fold(
                onSuccess = { registrations = it },
                onFailure = { if (error == null) error = it.message }
            )

            _uiState.update {
                it.copy(
                    stats = stats,
                    events = events,
                    registrations = registrations,
                    isLoading = false,
                    errorMessage = error
                )
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setEventFilter(eventId: Int?) {
        _uiState.update { it.copy(selectedEventIdFilter = eventId) }
        viewModelScope.launch {
            val regsResult = repository.getRegistrations(eventId)
            regsResult.onSuccess { regs ->
                _uiState.update { it.copy(registrations = regs) }
            }
        }
    }

    fun openAddEventModal() {
        _uiState.update {
            it.copy(
                showAddEventModal = true,
                addEventErrorMessage = null
            )
        }
    }

    fun closeAddEventModal() {
        _uiState.update {
            it.copy(
                showAddEventModal = false,
                addEventErrorMessage = null
            )
        }
    }

    fun createEvent(request: CreateEventRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingEvent = true, addEventErrorMessage = null) }
            val result = repository.createEvent(request)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isAddingEvent = false, showAddEventModal = false) }
                    loadDashboardData()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAddingEvent = false,
                            addEventErrorMessage = error.message ?: "Failed to create event"
                        )
                    }
                }
            )
        }
    }

    fun promptCancelRegistration(reg: RegistrationListItem) {
        _uiState.update { it.copy(pendingCancelRegistration = reg) }
    }

    fun dismissCancelModal() {
        _uiState.update { it.copy(pendingCancelRegistration = null) }
    }

    fun confirmCancelRegistration(regId: Int) {
        viewModelScope.launch {
            val result = repository.cancelRegistration(regId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(pendingCancelRegistration = null) }
                    loadDashboardData()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            pendingCancelRegistration = null,
                            errorMessage = "Cancel registration failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }
}
