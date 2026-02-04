package pepes.co.trofes.auth

import android.content.Context
import pepes.co.trofes.data.remote.GoogleLoginRequest
import pepes.co.trofes.data.remote.LoginRequest
import pepes.co.trofes.data.remote.RegisterRequest
import pepes.co.trofes.data.remote.RetrofitClient

class AuthRepository(private val context: Context) {

    private val session = AuthSession(context)

    suspend fun login(login: String, password: String) =
        RetrofitClient.apiService.login(LoginRequest(login = login, password = password))

    suspend fun register(username: String, email: String, password: String, confirmation: String) =
        RetrofitClient.apiService.register(
            RegisterRequest(
                username = username,
                email = email,
                password = password,
                passwordConfirmation = confirmation,
            )
        )

    suspend fun googleLogin(idToken: String) =
        RetrofitClient.apiService.googleLogin(GoogleLoginRequest(idToken = idToken))

    fun saveSession(token: String, userId: Long, username: String, email: String, fullName: String, profileImage: String?) {
        session.saveToken(token)
        session.saveUser(
            AuthSession.UserSession(
                id = userId,
                username = username,
                email = email,
                fullName = fullName,
                profileImage = profileImage,
            )
        )
    }

    fun getSession(): AuthSession = session
}
