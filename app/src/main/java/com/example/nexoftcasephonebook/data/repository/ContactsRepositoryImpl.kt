package com.example.nexoftcasephonebook.data.repository

import com.example.nexoftcasephonebook.data.remote.ContactsApi
import com.example.nexoftcasephonebook.domain.model.Contact
import com.example.nexoftcasephonebook.domain.repository.ContactsRepository
import com.example.nexoftcasephonebook.data.remote.dto.CreateUserRequest

class ContactsRepositoryImpl(
    private val api: ContactsApi
) : ContactsRepository {

    override  suspend fun createUser(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImageUrl: String
    ) {
        api.createUser(
            CreateUserRequest(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            profileImageUrl = profileImageUrl
        ))
    }
    override suspend fun getAll(): List<Contact> {
        val res = api.getAll()
        val users = res.data?.users ?: emptyList()
        return users.map {
            Contact(
                id = it.id.orEmpty(),
                createdAt = it.createdAt.orEmpty(),
                firstName = it.firstName.orEmpty(),
                lastName = it.lastName.orEmpty(),
                phoneNumber = it.phoneNumber.orEmpty(),
                profileImageUrl = it.profileImageUrl
            )
        }
    }

}
