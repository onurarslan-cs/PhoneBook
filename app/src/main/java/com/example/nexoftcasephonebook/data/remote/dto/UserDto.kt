package com.example.nexoftcasephonebook.data.remote.dto

import com.squareup.moshi.Json

data class UserDto(
    @Json(name = "id") val id: String?,
    @Json(name = "createdAt") val createdAt: String?,
    @Json(name = "firstName") val firstName: String?,
    @Json(name = "lastName") val lastName: String?,
    @Json(name = "phoneNumber") val phoneNumber: String?,
    @Json(name = "profileImageUrl") val profileImageUrl: String?
)
