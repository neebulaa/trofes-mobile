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
    @DrawableRes val imageRes: Int,
    val isLiked: Boolean = false,
)
