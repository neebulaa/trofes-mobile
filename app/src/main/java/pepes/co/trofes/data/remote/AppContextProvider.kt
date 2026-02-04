package pepes.co.trofes.data.remote

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * Cara ringan untuk menyediakan Application Context ke Retrofit/Interceptor.
 * Dipanggil dari [App] (Application class).
 */
object AppContextProvider {
    @SuppressLint("StaticFieldLeak")
    lateinit var appContext: Context
        private set

    fun init(app: Application) {
        appContext = app.applicationContext
    }
}
