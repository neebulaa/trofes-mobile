package pepes.co.trofes

import androidx.annotation.DrawableRes

data class HeroBannerItem(
    @DrawableRes val imageRes: Int? = null,
    val imageUrl: String? = null,
    val title: String,
)
