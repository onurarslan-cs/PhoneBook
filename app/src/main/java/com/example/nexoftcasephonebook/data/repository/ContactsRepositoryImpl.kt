package com.example.nexoftcasephonebook.data.repository

import android.content.Context
import com.example.nexoftcasephonebook.data.remote.ContactsApi
import com.example.nexoftcasephonebook.data.remote.UpdateUserRequest
import com.example.nexoftcasephonebook.domain.model.Contact
import com.example.nexoftcasephonebook.domain.repository.ContactsRepository
import com.example.nexoftcasephonebook.data.remote.dto.CreateUserRequest
import android.net.Uri
import com.example.nexoftcasephonebook.core.network.uriToJpegPart

class ContactsRepositoryImpl(
    private val api: ContactsApi,
    private val appContext: Context
) : ContactsRepository {
    override suspend fun getAll(): List<Contact> {
        val res = api.getAll()
        val users = res.data?.users ?: emptyList()
        return users.map {
            Contact(
                id = it.id.orEmpty(),
                // istersen burada zorunlu yapabiliriz
                createdAt = it.createdAt.orEmpty(),
                firstName = it.firstName.orEmpty(),
                lastName = it.lastName.orEmpty(),
                phoneNumber = it.phoneNumber.orEmpty(),
                profileImageUrl = it.profileImageUrl
            )
        }
    }

    override suspend fun createUser(
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
            )
        )
    }

    override suspend fun uploadImageAndGetUrl(uri: Uri): String {
        val part = uriToJpegPart(appContext, uri, partName = "image")
        val res = api.uploadImage(part)

        if (res.success != true) {
            throw IllegalStateException(res.messages?.joinToString() ?: "Upload failed")
        }

        val url = res.data?.imageUrl ?: throw IllegalStateException("Upload success but imageUrl missing")
        return url
    }



    override suspend fun deleteUser(id: String) {
        api.deleteUser(id)
    }

    override suspend fun updateUser(
        id: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImageUrl: String?
    ) {
        api.updateUser(
            id = id,
            body = UpdateUserRequest(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                profileImageUrl = profileImageUrl
            )
        )
    }

}
