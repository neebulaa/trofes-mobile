package pepes.co.trofes

import androidx.annotation.DrawableRes

data class PopularMenuItem(
    val title: String,
    val rating: String,
    val meta: String,
    val tag: String,
    val category: String,
    val likesCount: Int,
    @DrawableRes val imageRes: Int
)
