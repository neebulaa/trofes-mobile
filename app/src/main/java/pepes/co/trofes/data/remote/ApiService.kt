package pepes.co.trofes.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Endpoint API Trofes.
 * Base URL diset di [RetrofitClient] dan sudah mengandung "/api/v1/" (lihat local.properties).
 */
interface ApiService {
    // Home
    @GET("home")
    suspend fun getHome(): HomeApiResponse

    // Recipes
    @GET("recipes")
    suspend fun getRecipes(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("search") search: String? = null,
        @Query("filter_type") filterType: String? = null,
        @Query("filter_id") filterId: Int? = null,
    ): RecipesApiResponse

    // Guides
    @GET("guides")
    suspend fun getGuides(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("search") search: String? = null,
    ): GuidesApiResponse

    // Ingredients
    @GET("ingredients")
    suspend fun getIngredients(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("search") search: String? = null,
    ): IngredientsApiResponse

    // Dietary Preferences
    @GET("dietary-preferences")
    suspend fun getDietaryPreferences(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("search") search: String? = null,
    ): DietaryPreferencesApiResponse

    // Allergies
    @GET("allergies")
    suspend fun getAllergies(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("search") search: String? = null,
    ): AllergiesApiResponse

    // Messages (Contact/Chat)
    @GET("messages")
    suspend fun getMessages(): MessagesApiResponse

    @POST("messages")
    suspend fun sendMessage(@Body body: SendMessageRequest): SendMessageResponse

    // Auth
    @POST("login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): AuthResponse
}
