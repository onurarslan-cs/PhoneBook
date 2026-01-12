package com.example.nexoftcasephonebook.data.remote
import com.example.nexoftcasephonebook.data.remote.dto.ApiResponseDto
import com.example.nexoftcasephonebook.data.remote.dto.UsersDataDto
import com.example.nexoftcasephonebook.data.remote.dto.CreateUserRequest
import com.example.nexoftcasephonebook.data.remote.dto.UploadImageResponseSuccessDto
import com.example.nexoftcasephonebook.data.remote.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path

interface ContactsApi {

    @GET("api/User/GetAll")
    suspend fun getAll(): ApiResponseDto<UsersDataDto>
    @POST("api/User")
    suspend fun createUser(@Body req: CreateUserRequest): ApiResponseDto<UsersDataDto>

    @PUT("api/User/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body body: UpdateUserRequest
    ): UserDto

    @DELETE("api/User/{id}")
    suspend fun deleteUser(
        @Path("id") id: String
    ): retrofit2.Response<Unit>

    @Multipart
    @POST("api/User/UploadImage")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): ApiResponseDto<UploadImageResponseSuccessDto>

}

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val profileImageUrl: String?
)
