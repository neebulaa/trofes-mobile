package pepes.co.trofes

import androidx.annotation.DrawableRes

data class RecommendationItem(
    val id: String,
    val title: String,
    val rating: String,
    val likesCount: Int,
    val caloriesText: String,
    val timeText: String,
    val tagText: String,
    val category: String,
    // Total bahan (dari API: total_ingredient). Dipakai di icon daun.
    val ingredientsCount: Int = 0,
    @DrawableRes val imageRes: Int? = null,
    val imageUrl: String? = null,
    val isLiked: Boolean = false,
    // Chip hijau (dietary preference pertama). Default untuk item dummy.
    val firstDietaryPreference: String = "Recipe",
)
