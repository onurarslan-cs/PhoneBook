package com.example.nexoftcasephonebook.data.remote.dto

import com.squareup.moshi.Json

data class ApiResponseDto<T>(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "messages") val messages: List<String>? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "status") val status: Int? = null
)
