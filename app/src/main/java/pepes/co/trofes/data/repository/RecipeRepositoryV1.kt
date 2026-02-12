package pepes.co.trofes.data.repository

import pepes.co.trofes.data.remote.ApiServiceV1
import pepes.co.trofes.data.remote.LikeResponseV1
import pepes.co.trofes.data.remote.RecipesResponseV1

class RecipeRepositoryV1(private val apiService: ApiServiceV1) {

    suspend fun getRecipes(
        search: String? = null,
        perPage: Int? = null,
        filterType: String? = null,
        filterId: Int? = null,
        page: Int? = null,
    ): Result<RecipesResponseV1> {
        return try {
            val response = apiService.getRecipes(search, perPage, filterType, filterId, page)
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to fetch recipes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecipeDetail(recipeId: Int) = runCatching {
        val response = apiService.getRecipeDetail(recipeId)
        val body = response.body()
        if (response.isSuccessful && body?.success == true && body.data != null) {
            body.data
        } else {
            throw Exception(body?.message ?: "Failed to fetch recipe detail")
        }
    }

    suspend fun likeRecipe(recipeId: Int): Result<LikeResponseV1> = runCatching {
        val response = apiService.likeRecipe(recipeId)
        val body = response.body()
        if (response.isSuccessful && body?.success == true && body.data != null) {
            body.data
        } else {
            throw Exception(body?.message ?: "Failed to like recipe")
        }
    }

    suspend fun unlikeRecipe(recipeId: Int): Result<LikeResponseV1> = runCatching {
        val response = apiService.unlikeRecipe(recipeId)
        val body = response.body()
        if (response.isSuccessful && body?.success == true && body.data != null) {
            body.data
        } else {
            throw Exception(body?.message ?: "Failed to unlike recipe")
        }
    }
}
