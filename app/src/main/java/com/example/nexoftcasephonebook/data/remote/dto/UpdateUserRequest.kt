package com.example.nexoftcasephonebook.data.remote.dto

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val profileImageUrl: String?
)