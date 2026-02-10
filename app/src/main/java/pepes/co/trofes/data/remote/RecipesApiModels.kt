package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response untuk endpoint GET /recipes (Laravel).
 * Dari controller:
 * {
 *   success: true,
 *   data: {
 *     recipes: { current_page, data: [...], last_page, next_page_url, ... },
 *     hero_recipes: [...],
 *     recommended_recipes: [...]
 *   },
 *   warning: ...
 * }
 */
data class RecipesApiResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: RecipesApiData? = null,
    @SerializedName("warning") val warning: String? = null,
)

data class RecipesApiData(
    @SerializedName("recipes") val recipes: RecipesPage? = null,
    @SerializedName("hero_recipes") val heroRecipes: List<RecipeApiModel>? = null,
    @SerializedName("recommended_recipes") val recommendedRecipes: List<RecipeApiModel>? = null,
)

/**
 * Struktur paginate Laravel.
 */
data class RecipesPage(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("data") val data: List<RecipeApiModel> = emptyList(),
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
    @SerializedName("per_page") val perPage: Int? = null,
    @SerializedName("total") val total: Int? = null,
)

data class RecipeApiModel(
    @SerializedName("recipe_id") val recipeId: Long? = null,
    @SerializedName("id") val id: Long? = null,

    @SerializedName("title") val title: String? = null,
    @SerializedName("rating") val rating: Double? = null,

    @SerializedName("likes_count") val likesCount: Int? = null,
    // Laravel controller pakai liked_by_me (withExists)
    @SerializedName("liked_by_me") val likedByMe: Boolean? = null,
    // beberapa endpoint lain pakai is_liked
    @SerializedName("is_liked") val isLiked: Boolean? = null,

    @SerializedName("cooking_time") val cookingTime: Int? = null,
    @SerializedName("total_ingredient") val totalIngredient: Int? = null,

    @SerializedName("public_image") val publicImage: String? = null,
    @SerializedName("image") val image: String? = null,

    // chip favorite dari backend
    @SerializedName("is_favorite") val isFavorite: Boolean? = null,

    // dari backend: Recipe::with(['dietaryPreferences'])
    @SerializedName("dietary_preferences") val dietaryPreferences: List<HomeDietaryPreferenceDto> = emptyList(),
)

fun RecipesApiResponse.extractRecipes(): List<RecipeApiModel> {
    return data?.recipes?.data ?: emptyList()
}

fun RecipesApiResponse.nextPageUrl(): String? = data?.recipes?.nextPageUrl
fun RecipesApiResponse.currentPage(): Int = data?.recipes?.currentPage ?: 1
fun RecipesApiResponse.lastPage(): Int? = data?.recipes?.lastPage
