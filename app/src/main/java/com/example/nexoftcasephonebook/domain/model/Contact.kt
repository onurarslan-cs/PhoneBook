package com.example.nexoftcasephonebook.domain.model

import com.squareup.moshi.Json

data class Contact(
     val id: String,
     val createdAt: String,
     val firstName: String,
     val lastName: String,
     val phoneNumber: String,
     val profileImageUrl: String?)
{
    val fullName: String get() = "$firstName $lastName".trim()
}
