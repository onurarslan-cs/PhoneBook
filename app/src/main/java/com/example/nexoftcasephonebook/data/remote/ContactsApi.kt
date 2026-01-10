package com.example.nexoftcasephonebook.data.remote
import com.example.nexoftcasephonebook.data.remote.dto.ApiResponseDto
import com.example.nexoftcasephonebook.data.remote.dto.UsersDataDto
import retrofit2.http.GET

interface ContactsApi {
    @GET("api/User/GetAll")
    suspend fun getAll(): ApiResponseDto<UsersDataDto>
}