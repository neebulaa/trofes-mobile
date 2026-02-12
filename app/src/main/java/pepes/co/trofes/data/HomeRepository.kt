package pepes.co.trofes.data

import pepes.co.trofes.data.model.Guide
import pepes.co.trofes.data.model.Recipe
import pepes.co.trofes.data.remote.HomeApiResponse
import pepes.co.trofes.data.remote.RetrofitClient
import pepes.co.trofes.data.remote.extractGuides
import pepes.co.trofes.data.remote.extractRecipes

class HomeRepository {
    suspend fun fetchHome(): HomeApiResponse = RetrofitClient.apiService.getHome()

    suspend fun fetchRecipes(
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        filterType: String? = null,
        filterId: Int? = null,
    ): List<Recipe> {
        val resp = RetrofitClient.apiService.getRecipes(
            page = page,
            perPage = perPage,
            search = search,
            filterType = filterType,
            filterId = filterId,
        )
        return resp.extractRecipes().map { it.toRecipe() }
    }

    suspend fun fetchGuides(
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
    ): List<Guide> {
        val resp = RetrofitClient.apiService.getGuides(
            page = page,
            perPage = perPage,
            search = search,
        )
        return resp.extractGuides().map { it.toGuide() }
    }
}
