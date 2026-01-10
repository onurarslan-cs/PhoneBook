package com.example.nexoftcasephonebook.data.remote
import com.example.nexoftcasephonebook.data.remote.dto.ApiResponseDto
import com.example.nexoftcasephonebook.data.remote.dto.UsersDataDto
import com.example.nexoftcasephonebook.data.remote.dto.CreateUserRequest
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
interface ContactsApi {
    @GET("api/User/GetAll")
    suspend fun getAll(): ApiResponseDto<UsersDataDto>
    @POST("api/User")
    suspend fun createUser(@Body req: CreateUserRequest): ApiResponseDto<UsersDataDto>

}