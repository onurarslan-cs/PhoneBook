package com.example.nexoftcasephonebook.domain.repository
import com.example.nexoftcasephonebook.domain.model.Contact

interface ContactsRepository {
    suspend fun getAll(): List<Contact>
    suspend fun createUser(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImageUrl: String
    )
}