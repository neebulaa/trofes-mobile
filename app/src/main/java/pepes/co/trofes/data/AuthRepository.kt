package pepes.co.trofes.data

import pepes.co.trofes.data.model.AuthTokenResponse
import pepes.co.trofes.data.model.User
import pepes.co.trofes.data.remote.GoogleLoginRequest
import pepes.co.trofes.data.remote.LoginRequest
import pepes.co.trofes.data.remote.RegisterRequest
import pepes.co.trofes.data.remote.RetrofitClient

class AuthRepository {

    /**
     * Login: pakai endpoint existing di ApiService.
     * Repository mengembalikan model yang sudah "rapi" untuk UI.
     */
    suspend fun login(login: String, password: String): AuthTokenResponse {
        val resp = RetrofitClient.apiService.login(LoginRequest(login = login, password = password))

        val data = resp.data ?: error(resp.message ?: "Login failed")
        val user: User = data.user?.toUser() ?: error(resp.message ?: "User not found")
        val token = data.token ?: error(resp.message ?: "Token not found")

        return AuthTokenResponse(user = user, token = token)
    }

    suspend fun register(username: String, email: String, password: String, passwordConfirmation: String): AuthTokenResponse {
        val resp = RetrofitClient.apiService.register(
            RegisterRequest(
                username = username,
                email = email,
                password = password,
                passwordConfirmation = passwordConfirmation,
            )
        )

        val data = resp.data ?: error(resp.message ?: "Register failed")
        val user: User = data.user?.toUser() ?: error(resp.message ?: "User not found")
        val token = data.token ?: error(resp.message ?: "Token not found")

        return AuthTokenResponse(user = user, token = token)
    }

    suspend fun googleLogin(idToken: String): AuthTokenResponse {
        val resp = RetrofitClient.apiService.googleLogin(GoogleLoginRequest(idToken = idToken))

        val data = resp.data ?: error(resp.message ?: "Google login failed")
        val user: User = data.user?.toUser() ?: error(resp.message ?: "User not found")
        val token = data.token ?: error(resp.message ?: "Token not found")

        return AuthTokenResponse(user = user, token = token)
    }
}
