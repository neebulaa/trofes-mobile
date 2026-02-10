package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Generic paginate wrapper untuk Laravel.
 */
data class LaravelPage<T>(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("data") val data: List<T> = emptyList(),
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
    @SerializedName("per_page") val perPage: Int? = null,
    @SerializedName("total") val total: Int? = null,
)

/**
 * GET /guides
 * Bentuk umum:
 * { success: true, data: { guides: {...paginate...} } }
 * atau ada backend yang langsung { success, data: {...paginate...} }
 * Kita buat fleksibel dengan menampung beberapa kemungkinan.
 */
data class GuidesApiResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: GuidesApiData? = null,
)

data class GuidesApiData(
    // kemungkinan A: data.guides = paginate
    @SerializedName("guides") val guides: LaravelPage<GuideApiModel>? = null,
    // kemungkinan B: data = paginate langsung
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("data") val pageData: List<GuideApiModel>? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
)

data class GuideApiModel(
    @SerializedName("guide_id") val guideId: Long? = null,
    @SerializedName("id") val id: Long? = null,

    @SerializedName("title") val title: String? = null,
    @SerializedName("excerpt") val excerpt: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null,

    @SerializedName("public_image") val publicImage: String? = null,
    @SerializedName("image") val image: String? = null,
)

fun GuidesApiResponse.extractGuides(): List<GuideApiModel> {
    val nested = data?.guides?.data
    if (!nested.isNullOrEmpty()) return nested
    return data?.pageData ?: emptyList()
}

fun GuidesApiResponse.nextPageUrl(): String? = data?.guides?.nextPageUrl ?: data?.nextPageUrl
fun GuidesApiResponse.lastPage(): Int? = data?.guides?.lastPage ?: data?.lastPage
