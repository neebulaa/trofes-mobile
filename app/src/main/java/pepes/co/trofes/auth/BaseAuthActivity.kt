package pepes.co.trofes.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pepes.co.trofes.SigninIntentFactory

/**
 * Base activity untuk memaksa user login sebelum mengakses halaman.
 *
 * Cara pakai:
 * - Ubah Activity kamu dari `AppCompatActivity()` menjadi `BaseAuthActivity()`.
 * - Tetap panggil `super.onCreate(savedInstanceState)` di awal onCreate.
 *
 * Catatan:
 * - Jika belum login, BaseAuthActivity akan redirect ke Signin dan memanggil finish().
 * - Activity turunan sebaiknya memanggil `if (isAuthRedirected) return` setelah super.onCreate
 *   untuk menghindari menjalankan setContentView saat sedang redirect.
 */
abstract class BaseAuthActivity : AppCompatActivity() {

    protected lateinit var authSession: AuthSession
        private set

    /**
     * True jika BaseAuthActivity sudah redirect ke login.
     * Activity turunan sebaiknya `return` bila bernilai true.
     */
    protected var isAuthRedirected: Boolean = false
        private set

    /**
     * Override ini di Activity turunan untuk menentukan redirect login yang tepat.
     * Default: balik ke Home.
     */
    protected open fun requiredLoginIntent(): Intent = SigninIntentFactory.forHome(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authSession = AuthSession(this)

        if (!authSession.isLoggedIn()) {
            isAuthRedirected = true
            startActivity(requiredLoginIntent())
            finish()
        }
    }
}
