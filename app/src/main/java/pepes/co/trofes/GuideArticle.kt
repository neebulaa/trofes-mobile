package pepes.co.trofes

data class GuideArticle(
    val id: String,
    val title: String,
    val desc: String,
    val date: String,
    val imageUrl: String? = null,
    val content: String? = null,
    val slug: String? = null,
    val publishedAt: String? = null,
    // fallback untuk dummy lama
    val imageRes: Int? = null,
)
