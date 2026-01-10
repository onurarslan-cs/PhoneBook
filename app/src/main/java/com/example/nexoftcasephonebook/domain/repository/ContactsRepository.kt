package com.example.nexoftcasephonebook.domain.repository
import com.example.nexoftcasephonebook.domain.model.Contact

interface ContactsRepository {
    suspend fun getAll(): List<Contact>

}