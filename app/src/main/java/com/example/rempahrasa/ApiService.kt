package com.example.rempahrasa

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

data class RegisterResponse(
    val success: Boolean,
    val message: Any?,
    val token: String?
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    val accessToken: String
)

data class ClassificationResponse(
    val success: Boolean,
    val message: String?,
    val spices: List<String>,
    val recipes: List<String>
)

data class RecipeClassificationResponse(
    val success: Boolean,
    val message: String?,
    val recipes: List<Recipe>
)

data class Recipe(
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>
)

data class ProfileResponse(
    val success: Boolean,
    val data: UserProfile
)

data class UserProfile(
    val id: String,
    val email: String,
    val name: String
)

data class HistoriesResponse(
    val success: Boolean,
    val data: List<HistoryItem>
)

data class HistoryItem(
    val id: String,
    val result: String,
    val image: String,
    val createdAt: String,
    val favorite: Boolean
)

data class FavoritesResponse(
    val success: Boolean,
    val data: List<FavoriteItem>
)

data class FavoriteItem(
    val id: String,
    val result: String,
    val image: String,
    val createdAt: String,
    val favorite: Boolean
)

interface ApiService {
    // Register user
    @Multipart
    @POST("/register")
    suspend fun register(
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part image: MultipartBody.Part? // Add profile image
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // Resend verification token
    @FormUrlEncoded
    @PUT("/resend-token")
    suspend fun resendToken(
        @Field("email") email: String
    ): Response<RegisterResponse>

    // Verify user
    @GET("/verification")
    suspend fun verifyUser(
        @Query("token") token: String
    ): Response<RegisterResponse>

    // Logout user
    @DELETE("/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<RegisterResponse>

    // Get user profile
    @GET("/get-profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    // Get user histories
    @GET("/histories")
    suspend fun getHistories(
        @Header("Authorization") token: String
    ): Response<HistoriesResponse>

    // Save favorite spice
    @FormUrlEncoded
    @PUT("/favorites")
    suspend fun saveFavorite(
        @Header("Authorization") token: String,
        @Field("spiceId") spiceId: String
    ): Response<FavoritesResponse>

    // Remove favorite spice
    @FormUrlEncoded
    @DELETE("/favorites")
    suspend fun removeFavorite(
        @Header("Authorization") token: String,
        @Field("spiceId") spiceId: String
    ): Response<FavoritesResponse>

    // Get favorite spices
    @GET("/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): Response<FavoritesResponse>

    // Classify spice image
    @Multipart
    @POST("/spice-classification")
    suspend fun classifySpice(
        @Part image: MultipartBody.Part
    ): Response<ClassificationResponse>

    // Classify spice's recipe
    @Multipart
    @POST("/recipe-classification")
    suspend fun classifyRecipe(
        @Part image: MultipartBody.Part
    ): Response<RecipeClassificationResponse>

}