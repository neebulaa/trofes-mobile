package pepes.co.trofes.data.remote

import okhttp3.MultipartBody
import pepes.co.trofes.data.model.ApiResponse
import pepes.co.trofes.data.model.Guide
import pepes.co.trofes.data.model.Ingredient
import pepes.co.trofes.data.model.PaginatedResponse
import pepes.co.trofes.data.model.Recipe
import pepes.co.trofes.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Versi "baru" API service sesuai kontrak backend /api/v1/...
 *
 * Catatan:
 * - Project ini sudah punya `ApiService` existing yang dipakai beberapa screen.
 * - File ini ditambah sebagai bridge untuk endpoint yang lebih lengkap.
 */
interface ApiServiceV1 {

    // ========================================================================
    // AUTHENTICATION
    // ========================================================================

    @POST("v1/register")
    suspend fun register(@Body request: RegisterRequestV1): Response<ApiResponse<AuthResponseV1>>

    @POST("v1/login")
    suspend fun login(@Body request: LoginRequestV1): Response<ApiResponse<AuthResponseV1>>

    @POST("v1/logout")
    suspend fun logout(): Response<ApiResponse<Any>>

    @GET("v1/user")
    suspend fun getUser(): Response<ApiResponse<User>>

    @POST("v1/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestV1): Response<ApiResponse<Any>>

    @POST("v1/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestV1): Response<ApiResponse<Any>>

    @POST("v1/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequestV1): Response<ApiResponse<AuthResponseV1>>

    // ========================================================================
    // ONBOARDING
    // ========================================================================

    @GET("v1/onboarding/status")
    suspend fun getOnboardingStatus(): Response<ApiResponse<OnboardingStatusV1>>

    @POST("v1/onboarding/profile-setup")
    suspend fun setupProfile(@Body request: ProfileSetupRequestV1): Response<ApiResponse<User>>

    @POST("v1/onboarding/dietary-preferences-setup")
    suspend fun setupDietaryPreferences(@Body request: DietaryPreferencesRequestV1): Response<ApiResponse<User>>

    @POST("v1/onboarding/allergies-setup")
    suspend fun setupAllergies(@Body request: AllergiesRequestV1): Response<ApiResponse<User>>

    @POST("v1/onboarding/complete")
    suspend fun completeOnboarding(): Response<ApiResponse<User>>

    // ========================================================================
    // RECIPES
    // ========================================================================

    @GET("v1/recipes")
    suspend fun getRecipes(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("filter_type") filterType: String? = null,
        @Query("filter_id") filterId: Int? = null,
        @Query("page") page: Int? = null,
    ): Response<ApiResponse<RecipesResponseV1>>

    @GET("v1/recipes/{recipe}")
    suspend fun getRecipeDetail(@Path("recipe") recipeId: Int): Response<ApiResponse<Recipe>>

    @POST("v1/recipes/{recipe}/like")
    suspend fun likeRecipe(@Path("recipe") recipeId: Int): Response<ApiResponse<LikeResponseV1>>

    @DELETE("v1/recipes/{recipe}/like")
    suspend fun unlikeRecipe(@Path("recipe") recipeId: Int): Response<ApiResponse<LikeResponseV1>>

    @POST("v1/recipes/custom-search")
    suspend fun customSearchRecipes(@Body request: CustomSearchRequestV1): Response<ApiResponse<PaginatedResponse<Recipe>>>

    // ========================================================================
    // GUIDES
    // ========================================================================

    @GET("v1/guides")
    suspend fun getGuides(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("page") page: Int? = null,
    ): Response<ApiResponse<PaginatedResponse<Guide>>>

    @GET("v1/guides/{guide}")
    suspend fun getGuideDetail(@Path("guide") guideId: Int): Response<ApiResponse<GuideDetailResponseV1>>

    // ========================================================================
    // PROFILE
    // ========================================================================

    @GET("v1/profile")
    suspend fun getProfile(): Response<ApiResponse<ProfileResponseV1>>

    @PUT("v1/profile/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequestV1): Response<ApiResponse<User>>

    @Multipart
    @POST("v1/profile/update-image")
    suspend fun updateProfileImage(@Part image: MultipartBody.Part): Response<ApiResponse<ProfileImageResponseV1>>

    @DELETE("v1/profile/remove-image")
    suspend fun removeProfileImage(): Response<ApiResponse<Any>>

    // ========================================================================
    // NUTRIENTS CALCULATOR
    // ========================================================================

    @GET("v1/nutrients-calculator")
    suspend fun getNutrientsCalculatorData(): Response<ApiResponse<NutrientsCalculatorDataV1>>

    @POST("v1/nutrients-calculator")
    suspend fun calculateNutrients(@Body request: NutrientsCalculatorRequestV1): Response<ApiResponse<NutrientsCalculatorResponseV1>>

    // ========================================================================
    // ALLERGIES & DIETARY PREFERENCES
    // ========================================================================

    @GET("v1/allergies")
    suspend fun getAllergies(): Response<ApiResponse<List<pepes.co.trofes.data.model.Allergy>>>

    @GET("v1/dietary-preferences")
    suspend fun getDietaryPreferences(): Response<ApiResponse<List<pepes.co.trofes.data.model.DietaryPreference>>>

    @GET("v1/ingredients")
    suspend fun getIngredients(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null,
    ): Response<ApiResponse<PaginatedResponse<Ingredient>>>

    @GET("v1/ingredients/popular")
    suspend fun getPopularIngredients(): Response<ApiResponse<List<Ingredient>>>

    // ========================================================================
    // CONTACT
    // ========================================================================

    @POST("v1/contact-us")
    suspend fun sendContactMessage(@Body request: ContactMessageRequestV1): Response<ApiResponse<Any>>

    // ========================================================================
    // HOME
    // ========================================================================

    @GET("v1/home")
    suspend fun getHomeData(): Response<ApiResponse<HomeResponseV1>>

    // ========================================================================
    // YOUTUBE
    // ========================================================================

    @GET("v1/youtube/search")
    suspend fun searchYouTube(@Query("q") query: String): Response<ApiResponse<YouTubeResponseV1>>
}

// ========================================================================
// REQUEST MODELS
// ========================================================================

data class RegisterRequestV1(
    val username: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
)

data class LoginRequestV1(
    val login: String,
    val password: String,
)

data class ForgotPasswordRequestV1(
    val email: String,
)

data class ResetPasswordRequestV1(
    val token: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
)

data class GoogleLoginRequestV1(
    val id_token: String,
)

data class ProfileSetupRequestV1(
    val full_name: String,
    val phone: String?,
    val gender: String?,
    val birth_date: String?,
)

data class DietaryPreferencesRequestV1(
    val preferences: List<Int>?,
)

data class AllergiesRequestV1(
    val allergies: List<Int>?,
)

data class UpdateProfileRequestV1(
    val username: String,
    val email: String,
    val full_name: String,
    val bio: String?,
    val phone: String?,
    val gender: String?,
    val birth_date: String?,
    val dietary_preferences: List<Int>?,
    val allergies: List<Int>?,
)

data class CustomSearchRequestV1(
    val ingredients: List<Int>?,
    val dietary_preferences: List<Int>?,
    val allergies: List<Int>?,
    val calories: Float?,
    val protein: Float?,
    val fat: Float?,
    val sodium: Float?,
)

data class NutrientsCalculatorRequestV1(
    val age: Int,
    val gender: String,
    val weight: Float,
    val height: Float,
    val activity_level: String,
    val goal: String,
)

data class ContactMessageRequestV1(
    val name: String,
    val email: String,
    val message: String,
)

// ========================================================================
// RESPONSE MODELS
// ========================================================================

data class OnboardingStatusV1(
    val onboarding_completed: Boolean,
    val user: User,
    val allergies: List<pepes.co.trofes.data.model.Allergy>,
    val dietary_preferences: List<pepes.co.trofes.data.model.DietaryPreference>,
)

data class RecipesResponseV1(
    val recipes: PaginatedResponse<Recipe>,
    val hero_recipes: List<Recipe>,
    val recommended_recipes: List<Recipe>,
)

data class LikeResponseV1(
    val is_liked: Boolean,
    val likes_count: Int,
)

data class GuideDetailResponseV1(
    val guide: Guide,
    val next_guide: Guide?,
    val prev_guide: Guide?,
    val other_guides: List<Guide>,
)

data class ProfileResponseV1(
    val user: User,
    val liked_recipes: PaginatedResponse<Recipe>,
)

data class ProfileImageResponseV1(
    val profile_image: String,
    val profile_image_url: String?,
)

data class NutrientsCalculatorDataV1(
    val user_age: Int?,
    val activity_levels: Map<String, String>,
    val goals: Map<String, String>,
)

data class NutrientsCalculatorResponseV1(
    val calculations: CalculationsV1,
    val recommended_recipes: List<Recipe>,
    val input_data: NutrientsCalculatorRequestV1,
)

data class CalculationsV1(
    val bmr: Int,
    val tdee: Int,
    val target_calories: Int,
    val calories_per_meal: Int,
    val macros: MacrosV1,
)

data class MacrosV1(
    val protein_g: Int,
    val carbs_g: Int,
    val fat_g: Int,
    val protein_pct: Int,
    val carbs_pct: Int,
    val fat_pct: Int,
)

data class HomeResponseV1(
    val guides: List<Guide>,
    val recipes: List<Recipe>,
)

data class YouTubeResponseV1(
    val videoId: String?,
    val embedUrl: String?,
)

data class AuthResponseV1(
    val user: User,
    val token: String,
)
