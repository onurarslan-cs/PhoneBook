package com.example.nexoftcasephonebook.presentation.contacts
import android.R
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
        _state.update { it.copy(isLoading = true, error = null) }
        val result = kotlin.runCatching {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                repo.getAll()
            }
        }
            result.onSuccess { list ->
                all = list
                _state.update { it.copy(isLoading = false) }
                applyFilter()
            }
            .onFailure { ex ->
                _state.update { it.copy(isLoading = false, error = ex.message ?: "Error") }
            }

    }

     fun createUser(firstName: String, lastName: String, phoneNumber: String, profileImageUrl: String){
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {   repo.createUser(firstName, lastName, phoneNumber, profileImageUrl)
        }.onSuccess {
            load()
            }
                .onFailure { ex ->
                    _state.update { it.copy(isLoading = false, error = ex.message ?: "Error") }
                }
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