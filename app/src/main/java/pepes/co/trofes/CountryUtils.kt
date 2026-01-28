package pepes.co.trofes

import java.util.Locale

object CountryUtils {

    /**
     * Minimal built-in list. You can add more as needed.
     * Using a curated list keeps us dependency-free.
     */
    private val baseCountries: List<Country> = listOf(
        country("ID", "Indonesia", "+62"),
        country("US", "United States", "+1"),
        country("GB", "United Kingdom", "+44"),
        country("SG", "Singapore", "+65"),
        country("CN", "China", "+86"),
        country("MY", "Malaysia", "+60"),
        country("AU", "Australia", "+61"),
        country("IN", "India", "+91"),
        country("JP", "Japan", "+81"),
        country("KR", "South Korea", "+82"),
    )

    fun getCountries(): List<Country> = baseCountries

    fun defaultCountry(): Country {
        val localeCountry = Locale.getDefault().country
        return baseCountries.firstOrNull { it.iso2.equals(localeCountry, ignoreCase = true) }
            ?: baseCountries.first { it.iso2 == "ID" }
    }

    private fun country(iso2: String, displayName: String, dialCode: String): Country {
        return Country(
            iso2 = iso2,
            name = displayName,
            dialCode = dialCode,
            flagEmoji = toFlagEmoji(iso2),
        )
    }

    private fun toFlagEmoji(iso2: String): String {
        // Convert ISO country code to regional indicator symbols (flag emoji)
        val code = iso2.uppercase(Locale.US)
        if (code.length != 2) return ""
        val first = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
        val second = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }
}

