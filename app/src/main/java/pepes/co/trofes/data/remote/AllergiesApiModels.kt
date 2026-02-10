package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

data class AllergiesApiResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AllergiesApiData? = null,
)

data class AllergiesApiData(
    @SerializedName("allergies") val allergies: LaravelPage<AllergyApiModel>? = null,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("data") val pageData: List<AllergyApiModel>? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
)

data class AllergyApiModel(
    @SerializedName("allergy_id") val allergyId: Long? = null,
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
)

fun AllergiesApiResponse.extractAllergies(): List<AllergyApiModel> {
    val nested = data?.allergies?.data
    if (!nested.isNullOrEmpty()) return nested
    return data?.pageData ?: emptyList()
}

fun AllergiesApiResponse.nextPageUrl(): String? = data?.allergies?.nextPageUrl ?: data?.nextPageUrl
fun AllergiesApiResponse.lastPage(): Int? = data?.allergies?.lastPage ?: data?.lastPage
