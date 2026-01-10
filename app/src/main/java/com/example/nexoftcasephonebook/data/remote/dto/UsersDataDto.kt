package com.example.nexoftcasephonebook.data.remote.dto

import com.squareup.moshi.Json

data class UsersDataDto(
    @Json(name = "users") val users: List<UserDto> = emptyList()
)