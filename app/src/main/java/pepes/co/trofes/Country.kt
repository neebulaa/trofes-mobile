package pepes.co.trofes

/**
 * Lightweight country model for dial-code selection.
 */
data class Country(
    val iso2: String,
    val name: String,
    val dialCode: String,
    val flagEmoji: String,
)

