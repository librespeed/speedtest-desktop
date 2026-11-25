package core.lib.getIP

import util.json.JSONObject
import java.util.Locale
import kotlin.math.abs

data class ApproximateIPLocation(
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val countryName: String? = null,
    val countryCode: String? = null
) {
    fun toDisplayText(): String? {
        val place = listOf(city, region, countryName ?: country)
            .mapNotNull { it?.trim()?.takeIf { value -> value.isNotEmpty() } }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .joinToString(", ")

        val coordinates = if (latitude != null && longitude != null) {
            "Latitude ${formatCoordinate(latitude)}, Longitude ${formatCoordinate(longitude)}"
        } else {
            null
        }

        val details = when {
            place.isNotEmpty() && coordinates != null -> "$place ($coordinates)"
            place.isNotEmpty() -> place
            coordinates != null -> coordinates
            else -> null
        }

        return if (details.isNullOrEmpty()) {
            null
        } else {
            "Approximate IP-derived location: $details"
        }
    }

    private fun formatCoordinate(value: Double): String {
        val normalized = if (abs(value) < 0.0000005) 0.0 else value
        val formatted = String.format(Locale.US, "%.6f", normalized)
            .trimEnd('0')
            .trimEnd('.')
        return if (formatted == "-0") "0" else formatted
    }
}

data class GetIPResponse(
    val processedString: String,
    val rawIspInfo: String,
    val location: ApproximateIPLocation?
) {
    fun toDisplayText(): String {
        val locationText = location?.toDisplayText()
        return if (processedString.isEmpty()) {
            locationText ?: ""
        } else if (locationText.isNullOrEmpty()) {
            processedString
        } else {
            "$processedString\n$locationText"
        }
    }
}

object GetIPResponseParser {
    fun parse(rawResponse: String?): GetIPResponse {
        val rawIspInfo = rawResponse ?: ""
        if (rawIspInfo.isBlank()) {
            return GetIPResponse("", rawIspInfo, null)
        }

        return try {
            val root = JSONObject(rawIspInfo)
            val processedRaw = root.opt("processedString")
            val processedString = processedRaw as? String ?: rawIspInfo
            GetIPResponse(processedString, rawIspInfo, parseLocation(root))
        } catch (_: Throwable) {
            GetIPResponse(rawIspInfo, rawIspInfo, null)
        }
    }

    private fun parseLocation(root: JSONObject): ApproximateIPLocation? {
        val location = root.optJSONObject("location") ?: return null

        val city = parsePlace(location, "city", "city_name")
        val region = parsePlace(location, "region", "state", "state_name")
        val countryName = parsePlace(location, "countryName", "country_name")
        val countryCode = parsePlace(location, "countryCode", "country_code")
        val country = countryName ?: countryCode ?: parsePlace(location, "country")
        val latitude = parseCoordinate(location, -90.0..90.0, "latitude", "lat")
        val longitude = parseCoordinate(location, -180.0..180.0, "longitude", "lon", "lng")
        val validCoordinates = if (latitude != null && longitude != null) {
            latitude to longitude
        } else {
            null
        }

        val hasPlace = city != null || region != null || country != null
        if (!hasPlace && validCoordinates == null) {
            return null
        }

        return ApproximateIPLocation(
            city = city,
            region = region,
            country = country,
            latitude = validCoordinates?.first,
            longitude = validCoordinates?.second,
            countryName = countryName,
            countryCode = countryCode
        )
    }

    private fun parsePlace(location: JSONObject, vararg names: String): String? {
        for (name in names) {
            if (location.isNull(name)) continue
            val value = location.opt(name)
            if (value is String) {
                val trimmed = value.trim()
                if (trimmed.isNotEmpty() && !trimmed.equals("null", ignoreCase = true)) {
                    return trimmed
                }
            }
        }
        return null
    }

    private fun parseCoordinate(
        location: JSONObject,
        range: ClosedFloatingPointRange<Double>,
        vararg names: String
    ): Double? {
        for (name in names) {
            if (location.isNull(name)) continue
            val value = when (val raw = location.opt(name)) {
                is Number -> raw.toDouble()
                is String -> raw.trim().toDoubleOrNull()
                else -> null
            }
            if (value != null && value.isFinite() && value in range) {
                return value
            }
        }
        return null
    }
}
