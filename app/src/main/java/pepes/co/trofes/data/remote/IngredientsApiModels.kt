package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

data class IngredientsApiResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: IngredientsApiData? = null,
)

data class IngredientsApiData(
    @SerializedName("ingredients") val ingredients: LaravelPage<IngredientApiModel>? = null,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("data") val pageData: List<IngredientApiModel>? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
)

data class IngredientApiModel(
    @SerializedName("ingredient_id") val ingredientId: Long? = null,
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
)

fun IngredientsApiResponse.extractIngredients(): List<IngredientApiModel> {
    val nested = data?.ingredients?.data
    if (!nested.isNullOrEmpty()) return nested
    return data?.pageData ?: emptyList()
}

fun IngredientsApiResponse.nextPageUrl(): String? = data?.ingredients?.nextPageUrl ?: data?.nextPageUrl
fun IngredientsApiResponse.lastPage(): Int? = data?.ingredients?.lastPage ?: data?.lastPage
