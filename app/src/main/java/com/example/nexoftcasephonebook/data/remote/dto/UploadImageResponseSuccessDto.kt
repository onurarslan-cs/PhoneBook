package com.example.nexoftcasephonebook.data.remote.dto

import com.squareup.moshi.Json

data class UploadImageResponseSuccessDto(
    @Json(name = "imageUrl") val imageUrl: String? = null,
) {
    val resolvedUrl: String?
        get() = imageUrl
}
