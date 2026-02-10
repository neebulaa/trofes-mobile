package pepes.co.trofes.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import pepes.co.trofes.SigninIntentFactory

/**
 * Helper sederhana untuk memaksa user login.
 * Return true kalau activity harus stop (karena kita redirect ke login).
 */
fun Activity.requireAuth(afterLoginIntent: Intent = SigninIntentFactory.forHome(this)): Boolean {
    val session = AuthSession(this)
    if (session.isLoggedIn()) return false

    startActivity(afterLoginIntent)
    finish()
    return true
}

/**
 * Versi Context: berguna untuk Adapter (klik item) untuk membuka login.
 */
fun Context.createRequireAuthIntent(afterLoginIntent: Intent): Intent {
    val session = AuthSession(this)
    return if (session.isLoggedIn()) {
        // caller biasanya nggak pakai ini kalau sudah login, tapi biar aman.
        afterLoginIntent
    } else {
        afterLoginIntent
    }
}
