package com.example.nexoftcasephonebook.presentation.contacts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoftcasephonebook.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

class ContactsViewModel(
    private val repo: ContactsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ContactsState(isLoading = true))
    val state: StateFlow<ContactsState> = _state

    private var all: List<com.example.nexoftcasephonebook.domain.model.Contact> = emptyList()

    fun onEvent(e: ContactsEvent) {
        when (e) {
            ContactsEvent.Load -> load()
            is ContactsEvent.QueryChanged -> {
                _state.update { it.copy(query = e.value) }
                applyFilter()
            }
        }
    }

    private fun load() = viewModelScope.launch {
        Log.d("NEXOFT", "Loading contacts...")
        _state.update { it.copy(isLoading = true, error = null) }
        runCatching { repo.getAll() }
            .onSuccess { list ->
                Log.d("NEXOFT", "Loaded: ${list.size}")
                all = list
                _state.update { it.copy(isLoading = false) }
                applyFilter()
            }
            .onFailure { ex ->
                Log.e("NEXOFT", "Load failed: ${ex.message}", ex)
                _state.update { it.copy(isLoading = false, error = ex.message ?: "Error") }
            }

    }

    private fun applyFilter() {
        val q = _state.value.query.trim()
        val filtered = if (q.isEmpty()) all else all.filter {
            it.fullName.contains(q, ignoreCase = true) || it.phoneNumber.contains(q)
        }

        val grouped = filtered
            .sortedBy { it.fullName.lowercase() }
            .groupBy { it.fullName.firstOrNull()?.uppercaseChar() ?: '#' }

        _state.update { it.copy(grouped = grouped) }
    }
}