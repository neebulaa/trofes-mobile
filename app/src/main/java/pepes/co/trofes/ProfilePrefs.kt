package pepes.co.trofes

import android.content.Context

/**
 * Simple local storage for user profile using SharedPreferences.
 * This keeps the implementation lightweight (no DB / no backend dependency).
 */
data class UserProfile(
    val fullName: String = "",
    val username: String = "",
    val gender: String = "",
    val bio: String = "",
    val email: String = "",
    val phone: String = "",
    val birthDate: String = "" // format: dd/MM/yyyy
)

class ProfilePrefs(context: Context) {

    private val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getProfile(): UserProfile {
        return UserProfile(
            fullName = sp.getString(KEY_FULL_NAME, "") ?: "",
            username = sp.getString(KEY_USERNAME, "") ?: "",
            gender = sp.getString(KEY_GENDER, "") ?: "",
            bio = sp.getString(KEY_BIO, "") ?: "",
            email = sp.getString(KEY_EMAIL, "") ?: "",
            phone = sp.getString(KEY_PHONE, "") ?: "",
            birthDate = sp.getString(KEY_BIRTHDATE, "") ?: "",
        )
    }

    fun saveProfile(profile: UserProfile) {
        sp.edit()
            .putString(KEY_FULL_NAME, profile.fullName)
            .putString(KEY_USERNAME, profile.username)
            .putString(KEY_GENDER, profile.gender)
            .putString(KEY_BIO, profile.bio)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_PHONE, profile.phone)
            .putString(KEY_BIRTHDATE, profile.birthDate)
            .apply()
    }

    fun clear() {
        sp.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "trofes_profile"
        private const val KEY_FULL_NAME = "fullName"
        private const val KEY_USERNAME = "username"
        private const val KEY_GENDER = "gender"
        private const val KEY_BIO = "bio"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_BIRTHDATE = "birthDate"
    }
}
