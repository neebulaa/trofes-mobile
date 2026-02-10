package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

data class DietaryPreferencesApiResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: DietaryPreferencesApiData? = null,
)

data class DietaryPreferencesApiData(
    @SerializedName("dietary_preferences") val dietaryPreferences: LaravelPage<DietaryPreferenceApiModel>? = null,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("data") val pageData: List<DietaryPreferenceApiModel>? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
)

data class DietaryPreferenceApiModel(
    @SerializedName("dietary_preference_id") val dietaryPreferenceId: Long? = null,
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("title") val title: String? = null,
)

fun DietaryPreferencesApiResponse.extractDietaryPreferences(): List<DietaryPreferenceApiModel> {
    val nested = data?.dietaryPreferences?.data
    if (!nested.isNullOrEmpty()) return nested
    return data?.pageData ?: emptyList()
}

fun DietaryPreferencesApiResponse.nextPageUrl(): String? = data?.dietaryPreferences?.nextPageUrl ?: data?.nextPageUrl
fun DietaryPreferencesApiResponse.lastPage(): Int? = data?.dietaryPreferences?.lastPage ?: data?.lastPage
