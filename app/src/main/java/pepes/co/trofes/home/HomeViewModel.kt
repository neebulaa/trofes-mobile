package pepes.co.trofes.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pepes.co.trofes.HeroBannerItem
import pepes.co.trofes.RecommendationItem
import pepes.co.trofes.data.HomeRepository
import pepes.co.trofes.data.remote.HomeRecipeDto
import pepes.co.trofes.data.remote.RetrofitClient

class HomeViewModel(
    private val repo: HomeRepository = HomeRepository(),
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val hero: List<HeroBannerItem> = emptyList(),
        val recommendations: List<RecommendationItem> = emptyList(),
        val popular: List<RecommendationItem> = emptyList(),
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun loadHome() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                Log.d("HomeViewModel", "BASE_URL=" + RetrofitClient.BASE_URL)

                val res = repo.fetchHome()
                if (res.success != true) {
                    _state.value = UiState(
                        isLoading = false,
                        errorMessage = "API success=false (cek URL/baseUrl dan server backend)",
                    )
                    return@launch
                }

                val data = res.data

                fun normalizeUrl(url: String?): String? {
                    if (url.isNullOrBlank()) return null
                    // backend mengembalikan http://127.0.0.1:8000/... tapi di HP harus pakai BASE_URL yg benar
                    return url.replace("http://127.0.0.1:8000/", RetrofitClient.BASE_URL)
                }

                val heroItems = (data?.guides ?: emptyList()).take(5).mapIndexed { idx, g ->
                    HeroBannerItem(
                        imageUrl = normalizeUrl(g.publicImage),
                        title = g.title ?: "Guide ${idx + 1}",
                    )
                }

                fun mapRecipe(r: HomeRecipeDto): RecommendationItem {
                    val id = r.recipeId?.toString() ?: r.title.orEmpty()
                    val likes = r.likesCount ?: 0
                    val ratingStr = (r.rating ?: 0.0).toString()

                    val firstDiet = r.dietaryPreferences
                        .firstOrNull()
                        ?.let { it.name ?: it.title }
                        ?.takeIf { it.isNotBlank() }
                        ?: "Recipe"

                    return RecommendationItem(
                        id = id,
                        title = r.title ?: "Untitled",
                        rating = ratingStr,
                        likesCount = likes,
                        caloriesText = (r.calories ?: 0).toString(),
                        // icon daun (di card) = total bahan
                        ingredientsCount = r.totalIngredient ?: 0,
                        timeText = "${r.cookingTime ?: 0}m",
                        // chip hijau = dietary preference pertama
                        tagText = firstDiet,
                        category = "All Menu",
                        imageUrl = normalizeUrl(r.publicImage),
                        isLiked = r.isLiked ?: false,
                        firstDietaryPreference = firstDiet,
                    )
                }

                // Response baru:
                // - recommendations: recommended_recipes (random)
                // - popular: popular_recipes (order by likes_count desc)
                val recommendations = (data?.recommendedRecipes ?: emptyList()).map { mapRecipe(it) }
                val popular = (data?.popularRecipes ?: emptyList()).map { mapRecipe(it) }

                _state.value = UiState(
                    isLoading = false,
                    hero = heroItems,
                    recommendations = recommendations,
                    popular = popular,
                )
            } catch (e: Exception) {
                _state.value = UiState(
                    isLoading = false,
                    errorMessage = (e.message ?: "Gagal mengambil data") + " | BASE_URL=" + RetrofitClient.BASE_URL,
                )
            }
        }
    }
}
