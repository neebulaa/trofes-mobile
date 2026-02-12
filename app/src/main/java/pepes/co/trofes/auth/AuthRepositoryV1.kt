package pepes.co.trofes.auth

import okhttp3.ResponseBody
import org.json.JSONObject
import pepes.co.trofes.data.local.TokenManager
import pepes.co.trofes.data.remote.ApiServiceV1
import pepes.co.trofes.data.remote.AuthResponseV1
import pepes.co.trofes.data.remote.GoogleLoginRequestV1
import pepes.co.trofes.data.remote.LoginRequestV1
import pepes.co.trofes.data.remote.RegisterRequestV1
import retrofit2.Response

/**
 * Repository untuk API V1 (Response<ApiResponse<...>>).
 *
 * Opsi 2 token storage:
 * - simpan token di DataStore (TokenManager)
 * - simpan juga token di SharedPreferences lama (AuthSession) supaya AuthInterceptor existing tetap jalan
 */
class AuthRepositoryV1(
    private val apiService: ApiServiceV1,
    private val tokenManager: TokenManager,
    private val authSession: AuthSession,
) {

    suspend fun login(login: String, password: String): Result<AuthResponseV1> = runCatching {
        val res = apiService.login(LoginRequestV1(login = login, password = password))
        val body = res.body()

        if (res.isSuccessful && body?.success == true && body.data != null) {
            persistSession(body.data)
            body.data
        } else {
            throw Exception(extractErrorMessage(res, body?.message) ?: "Login failed")
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmation: String,
    ): Result<AuthResponseV1> = runCatching {
        val res = apiService.register(
            RegisterRequestV1(
                username = username,
                email = email,
                password = password,
                password_confirmation = confirmation,
            )
        )
        val body = res.body()

        if (res.isSuccessful && body?.success == true && body.data != null) {
            persistSession(body.data)
            body.data
        } else {
            throw Exception(extractErrorMessage(res, body?.message) ?: "Register failed")
        }
    }

    suspend fun googleLogin(idToken: String): Result<AuthResponseV1> = runCatching {
        val res = apiService.loginWithGoogle(GoogleLoginRequestV1(id_token = idToken))
        val body = res.body()

        if (res.isSuccessful && body?.success == true && body.data != null) {
            persistSession(body.data)
            body.data
        } else {
            throw Exception(extractErrorMessage(res, body?.message) ?: "Google login failed")
        }
    }

    suspend fun logout(): Result<Unit> = runCatching {
        try {
            // kalau endpoint gagal pun, token lokal tetap dibersihkan
            apiService.logout()
        } finally {
            tokenManager.clearToken()
            authSession.clear()
        }
    }

    private suspend fun persistSession(data: AuthResponseV1) {
        val token = data.token
        tokenManager.saveToken(token)
        authSession.saveToken(token)
        authSession.saveUser(
            AuthSession.UserSession(
                id = data.user.userId.toLong(),
                username = data.user.username,
                email = data.user.email,
                fullName = data.user.fullName ?: data.user.username,
                profileImage = data.user.profileImage,
            )
        )
    }

    private fun extractErrorMessage(res: Response<*>, fallback: String?): String? {
        val bodyMessage = fallback?.takeIf { it.isNotBlank() }
        if (bodyMessage != null) return bodyMessage

        val raw = res.errorBody()?.safeString()?.trim().orEmpty()
        if (raw.isBlank()) {
            return res.message().takeIf { it.isNotBlank() }
        }

        // Coba parse JSON standar Laravel: { message: "...", errors: { field: ["..."] } }
        val parsed = parseLaravelError(raw)
        if (!parsed.isNullOrBlank()) return parsed

        // Fallback: jangan tampilkan payload terlalu panjang
        return raw.take(300)
    }

    private fun parseLaravelError(raw: String): String? {
        return try {
            val start = raw.indexOf('{')
            if (start < 0) return null

            val json = JSONObject(raw.substring(start))
            val msg = json.optString("message").takeIf { it.isNotBlank() }

            val errors = json.optJSONObject("errors")
            if (errors != null) {
                // priority keys: login/email/username/password
                fun firstErr(key: String): String? {
                    val arr = errors.optJSONArray(key) ?: return null
                    return arr.optString(0).takeIf { it.isNotBlank() }
                }

                val loginErr = firstErr("login") ?: firstErr("email") ?: firstErr("username")
                val passErr = firstErr("password")

                // kalau ada error field, gabungkan singkat (SigninActivity juga akan parse untuk set field error)
                val combined = listOfNotNull(loginErr, passErr).joinToString("\n").trim()
                if (combined.isNotBlank()) return combined
            }

            msg
        } catch (_: Exception) {
            null
        }
    }

    private fun ResponseBody.safeString(): String = try {
        string()
    } catch (_: Exception) {
        ""
    }
}
