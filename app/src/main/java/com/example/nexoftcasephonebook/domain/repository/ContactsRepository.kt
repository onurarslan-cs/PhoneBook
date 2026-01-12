package com.example.nexoftcasephonebook.domain.repository
import android.net.Uri
import com.example.nexoftcasephonebook.core.network.uriToImagePart
import com.example.nexoftcasephonebook.domain.model.Contact

interface ContactsRepository {

    suspend fun getAll(): List<Contact>
    suspend fun uploadImageAndGetUrl(uri: Uri): String
    suspend fun createUser(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImageUrl: String
    )

    suspend fun deleteUser(id: String)

    suspend fun updateUser(
        id: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImageUrl: String?
    )
}