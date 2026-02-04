package pepes.co.trofes.data.remote

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import pepes.co.trofes.auth.AuthSession

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val session = AuthSession(context)
        val token = session.getToken()

        val original = chain.request()
        val req = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(req)
    }
}
