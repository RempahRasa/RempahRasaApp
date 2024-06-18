package com.example.rempahrasa

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val token: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?
)

data class ClassificationResponse(
    val success: Boolean,
    val message: String?,
    val spices: List<String>,
    val recipes: List<String>
)

interface ApiService {
//    @POST("post/register")
//    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
//
//    @POST("post/login")
//    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
//
//    @Multipart
//    @POST("post/spice-classification")
//    suspend fun classifySpice(@Part image: MultipartBody.Part): Response<ClassificationResponse>
}
