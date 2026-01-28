package pepes.co.trofes.model

import pepes.co.trofes.RecommendationItem

/**
 * Dummy/detail model untuk halaman Detail Recipe.
 * Nanti bisa diganti dengan data dari API/DB.
 */
data class RecipeDetail(
    val id: String,
    val title: String,
    val subtitle: String,
    val rating: Double,
    val ratingCount: Int,
    val imageRes: Int,
    val chips: List<InfoChip>,
    val ingredientsTitle: String = "Measured Ingredients",
    val ingredients: List<String>,
    val steps: List<String>,
    val youtubeId: String,
) {
    data class InfoChip(
        val label: String,
        val value: String,
    )

    companion object {
        fun fromRecommendation(item: RecommendationItem): RecipeDetail {
            val rating = item.rating.toDoubleOrNull() ?: 4.8
            val ratingCount = (150..550).random()

            return RecipeDetail(
                id = item.id,
                title = item.title.ifBlank { "Miso-Butter Roast" },
                subtitle = "Halal, Lactose Free, Low Carb, Weight Loss",
                rating = rating,
                ratingCount = ratingCount,
                imageRes = item.imageRes,
                chips = listOf(
                    InfoChip(label = "Calories", value = "500kcal"),
                    InfoChip(label = "Calories", value = "500kcal"),
                    InfoChip(label = "Calories", value = "500kcal"),
                    InfoChip(label = "Calories", value = "500kcal"),
                ),
                ingredients = listOf(
                    "Chicken (1 whole)",
                    "Miso (2 tbsp)",
                    "Butter (2 tbsp)",
                    "Parsley (1 bunch)",
                ),
                steps = listOf(
                    "For the sachet, wrap the parsley, thyme, bay leaves, and peppercorns in small amount of cheesecloth and tie with butcher's twine.",
                    "Preheat the oven to 325°F.",
                    "Beginning with a large saucepan or braising pan with a lid, heat the tablespoon of grapeseed oil over medium-high heat.",
                ),
                youtubeId = "dQw4w9WgXcQ",
            )
        }
    }
}
