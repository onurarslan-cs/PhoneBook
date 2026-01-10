package com.example.nexoftcasephonebook.presentation.contacts
import com.example.nexoftcasephonebook.domain.model.Contact

data class ContactsState(

    val isLoading: Boolean = false,
    val query: String = "",
    val grouped: Map<Char, List<Contact>> = emptyMap(),
    val error: String? = null
)
sealed interface ContactsEvent {
    object Load : ContactsEvent
    data class QueryChanged(val value: String) : ContactsEvent
}