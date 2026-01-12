package com.example.nexoftcasephonebook.presentation.contacts

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoftcasephonebook.core.network.uriToImagePart
import com.example.nexoftcasephonebook.data.remote.ContactsApi
import com.example.nexoftcasephonebook.domain.model.Contact
import com.example.nexoftcasephonebook.domain.repository.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsViewModel(
    private val repo: ContactsRepository

) : ViewModel() {

    private val _state = MutableStateFlow(ContactsState(isLoading = true))
    val state: StateFlow<ContactsState> = _state

    private var all: List<Contact> = emptyList()

    fun onEvent(e: ContactsEvent) {
        when (e) {
            ContactsEvent.Load -> load()
            is ContactsEvent.QueryChanged -> {
                _state.update { it.copy(query = e.value) }
                applyFilter()
            }
        }
    }

    fun createUser(firstName: String, lastName: String, phoneNumber: String, profileImageUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching {
                withContext(Dispatchers.IO) {
                    val resolved = resolveProfileUrl(profileImageUrl) // content:// ise upload edip url döner
                    repo.createUser(firstName, lastName, phoneNumber, resolved)
                }
            }.onSuccess { load() }
                .onFailure { ex -> _state.update { it.copy(isLoading = false, error = ex.message ?: "Error") } }
        }
    }


    fun deleteUser(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching {
                withContext(Dispatchers.IO) {
                    repo.deleteUser(id)
                }
            }.onSuccess {
                load()
            }.onFailure { ex ->
                _state.update { it.copy(isLoading = false, error = ex.message ?: "Error") }
            }
        }
    }

    fun updateUser(id: String, firstName: String, lastName: String, phoneNumber: String, profileImageUrl: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching {
                withContext(Dispatchers.IO) {
                    val resolved = profileImageUrl?.let { resolveProfileUrl(it) }
                    repo.updateUser(id, firstName, lastName, phoneNumber, resolved)
                }
            }.onSuccess { load() }
                .onFailure { ex -> _state.update { it.copy(isLoading = false, error = ex.message ?: "Error") } }
        }
    }


    private fun load() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        runCatching {
            withContext(Dispatchers.IO) { repo.getAll() }
        }.onSuccess { list ->
            all = list
            _state.update { it.copy(isLoading = false) }
            applyFilter()
        }.onFailure { ex ->
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
    private fun looksLikeRemoteUrl(s: String?): Boolean {
        val v = s?.trim().orEmpty()
        return v.startsWith("http://") || v.startsWith("https://")
    }

    suspend fun resolveProfileUrl(localOrRemote: String?): String {
        val v = localOrRemote?.trim()
        if (v.isNullOrBlank() || v == "null") return "https://picsum.photos/200" // default

        if (looksLikeRemoteUrl(v)) return v

        val uri = Uri.parse(v)

        return when (uri.scheme) {
            "content", "file" -> repo.uploadImageAndGetUrl(uri)
            else -> v // relative/string
        }
    }
}
