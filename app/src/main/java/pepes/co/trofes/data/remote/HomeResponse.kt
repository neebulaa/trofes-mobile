package pepes.co.trofes.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Model response untuk GET /api/v1/home
 *
 * Karena struktur JSON dari backend bisa beda-beda, ini dibuat fleksibel:
 * - message: biasanya string
 * - data: isi utama (bisa object / list / null)
 */
data class HomeResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Any? = null,
)
