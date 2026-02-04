package pepes.co.trofes.auth

import android.content.Context

/**
 * Penyimpanan sesi auth sederhana pakai SharedPreferences.
 * Menyimpan token Bearer + data user dasar.
 */
class AuthSession(context: Context) {

    private val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun getToken(): String? = sp.getString(KEY_TOKEN, null)

    fun saveToken(token: String) {
        sp.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clear() {
        sp.edit().clear().apply()
    }

    fun saveUser(user: UserSession) {
        sp.edit()
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_FULL_NAME, user.fullName)
            .putString(KEY_PROFILE_IMAGE, user.profileImage)
            .apply()
    }

    fun getUser(): UserSession? {
        val id = sp.getLong(KEY_USER_ID, -1L)
        if (id <= 0L) return null
        return UserSession(
            id = id,
            username = sp.getString(KEY_USERNAME, "").orEmpty(),
            email = sp.getString(KEY_EMAIL, "").orEmpty(),
            fullName = sp.getString(KEY_FULL_NAME, "").orEmpty(),
            profileImage = sp.getString(KEY_PROFILE_IMAGE, null),
        )
    }

    data class UserSession(
        val id: Long,
        val username: String,
        val email: String,
        val fullName: String,
        val profileImage: String? = null,
    )

    companion object {
        private const val PREF_NAME = "trofes_auth_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PROFILE_IMAGE = "profile_image"

        const val EXTRA_AFTER_LOGIN_TARGET = "extra_after_login_target"
        const val TARGET_HOME = "home"
        const val TARGET_RECIPE_DETAIL = "recipe_detail"
        const val TARGET_GUIDE_DETAIL = "guide_detail"
    }
}
