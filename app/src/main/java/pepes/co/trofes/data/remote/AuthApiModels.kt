package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

// Requests

data class LoginRequest(
    // backend expects: {"login": "emailOrUsername", "password": "..."}
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String,
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
)

data class GoogleLoginRequest(
    // backend expects: {"id_token": "..."}
    @SerializedName("id_token") val idToken: String,
)

// Responses

data class AuthResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AuthData? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null,
)

data class AuthData(
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: ApiUser? = null,
)

data class ApiUser(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
)
