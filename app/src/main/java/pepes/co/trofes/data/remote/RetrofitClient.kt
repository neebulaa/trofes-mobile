package pepes.co.trofes.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pepes.co.trofes.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    /**
     * BASE_URL ditentukan dari BuildConfig supaya gampang beda untuk emulator vs HP asli.
     * - Emulator: biasanya http://10.0.2.2:8000/
     * - HP asli: http://<IP-PC>:8000/
     */
    val BASE_URL: String = BuildConfig.BASE_URL.let { raw ->
        // Retrofit mengharuskan baseUrl berakhir dengan '/'
        val trimmed = raw.trim()
        if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        // Bearer token (kalau sudah login)
        .addInterceptor(AuthInterceptor(AppContextProvider.appContext))
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** API service existing (dipakai screen yang sudah ada sekarang). */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /** API service versi v1 (endpoint lengkap + Response<ApiResponse<...>>). */
    val apiServiceV1: ApiServiceV1 by lazy {
        retrofit.create(ApiServiceV1::class.java)
    }
}
