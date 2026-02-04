package pepes.co.trofes.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Endpoint API Trofes.
 * Base URL diset di [RetrofitClient] dan sudah mengandung "/api/v1/" (lihat local.properties).
 */
interface ApiService {
    // Home
    @GET("home")
    suspend fun getHome(): HomeApiResponse

    // Auth
    @POST("login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): AuthResponse
}
