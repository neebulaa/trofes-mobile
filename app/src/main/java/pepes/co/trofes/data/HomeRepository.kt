package pepes.co.trofes.data

import pepes.co.trofes.data.remote.HomeApiResponse
import pepes.co.trofes.data.remote.RetrofitClient

class HomeRepository {
    suspend fun fetchHome(): HomeApiResponse = RetrofitClient.apiService.getHome()
}
