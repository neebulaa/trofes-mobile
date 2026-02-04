package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response baru dari backend:
 * {
 *   "success": true,
 *   "data": {
 *     "guides": [...],
 *     "recommended_recipes": [...],
 *     "popular_recipes": [...]
 *   }
 * }
 */
data class HomeApiResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: HomeApiData? = null,
)

data class HomeApiData(
    @SerializedName("guides") val guides: List<HomeGuideDto> = emptyList(),
    @SerializedName("recommended_recipes") val recommendedRecipes: List<HomeRecipeDto> = emptyList(),
    @SerializedName("popular_recipes") val popularRecipes: List<HomeRecipeDto> = emptyList(),
)

data class HomeGuideDto(
    @SerializedName("guide_id") val guideId: Long? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("admin_id") val adminId: Long? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("public_image") val publicImage: String? = null,
    @SerializedName("excerpt") val excerpt: String? = null,
)

data class HomeRecipeDto(
    @SerializedName("recipe_id") val recipeId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("cooking_time") val cookingTime: Int? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("instructions") val instructions: String? = null,
    @SerializedName("measured_ingredients") val measuredIngredients: String? = null,
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("protein") val protein: Int? = null,
    @SerializedName("fat") val fat: Int? = null,
    @SerializedName("sodium") val sodium: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("total_ingredient") val totalIngredient: Int? = null,
    @SerializedName("public_image") val publicImage: String? = null,
    @SerializedName("is_liked") val isLiked: Boolean? = null,
    @SerializedName("likes_count") val likesCount: Int? = null,
    // dari backend: Recipe::with(['dietaryPreferences'])
    @SerializedName("dietary_preferences") val dietaryPreferences: List<HomeDietaryPreferenceDto> = emptyList(),
)

/**
 * Bentuk minimal dietary preference.
 * Catatan: field name di backend bisa "name" atau "title" tergantung model.
 * Kita tampung dua-duanya supaya aman.
 */
data class HomeDietaryPreferenceDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("title") val title: String? = null,
)
