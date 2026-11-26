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
    val countryCode: String? = null,
    val postal: String? = null,
    val timezone: String? = null,
    val source: String? = null,
    val approximate: Boolean = true
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

    fun toJsonString(): String {
        val json = JSONObject()
        city?.trim()?.takeIf { it.isNotEmpty() }?.let { json.put("city", it) }
        region?.trim()?.takeIf { it.isNotEmpty() }?.let { json.put("region", it) }
        countryName?.trim()?.takeIf { it.isNotEmpty() }?.let { json.put("countryName", it) }
        countryCode?.trim()?.takeIf { it.isNotEmpty() }?.let { json.put("countryCode", it) }
        country?.trim()?.takeIf { it.isNotEmpty() }?.let { json.put("country", it) }
        latitude?.let { json.put("latitude", it) }
        longitude?.let { json.put("longitude", it) }
        postal?.trim()?.takeIf { it.isNotEmpty() }?.let { json.put("postal", it) }
        timezone?.trim()?.takeIf { it.isNotEmpty() }?.let { json.put("timezone", it) }
        source?.trim()?.takeIf { it.isNotEmpty() }?.let { json.put("source", it) }
        if (approximate) json.put("approximate", true)
        return json.toString()
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
        val location = root.optJSONObject("location")
        val rawIspInfo = parseRawIspInfo(root.opt("rawIspInfo"))

        val city = parsePlace(location, "city", "city_name")
            ?: parsePlace(rawIspInfo, "city", "city_name")
        val region = parsePlace(location, "region", "state", "state_name")
            ?: parsePlace(location, "subdivision", "subdivision_name")
            ?: parsePlace(rawIspInfo, "region", "region_name", "state", "subdivision", "subdivision_name")
        val countryName = parsePlace(location, "countryName", "country_name")
            ?: parsePlace(rawIspInfo, "countryName", "country_name")
        val countryCode = parsePlace(location, "countryCode", "country_code", "country_iso_code", "iso_code")
            ?: parsePlace(rawIspInfo, "countryCode", "country_code", "country_iso_code", "iso_code")
            ?: parseCountryCode(location, rawIspInfo)
        val topCountry = parsePlace(location, "country")
        val rawCountry = parsePlace(rawIspInfo, "country")
        val country = countryName ?: topCountry ?: rawCountry ?: countryCode
        val latitude = parseCoordinate(location, -90.0..90.0, "latitude", "lat")
            ?: parseNestedCoordinate(location, "coordinates", -90.0..90.0, "latitude", "lat")
            ?: parseLocationString(location, "loc")
            ?: parseCoordinate(rawIspInfo, -90.0..90.0, "latitude", "lat")
            ?: parseNestedCoordinate(rawIspInfo, "coordinates", -90.0..90.0, "latitude", "lat")
            ?: parseLocationString(rawIspInfo, "loc")
        val longitude = parseCoordinate(location, -180.0..180.0, "longitude", "lon", "lng")
            ?: parseNestedCoordinate(location, "coordinates", -180.0..180.0, "longitude", "lon", "lng")
            ?: parseLocationStringLongitude(location, "loc")
            ?: parseCoordinate(rawIspInfo, -180.0..180.0, "longitude", "lon", "lng")
            ?: parseNestedCoordinate(rawIspInfo, "coordinates", -180.0..180.0, "longitude", "lon", "lng")
            ?: parseLocationStringLongitude(rawIspInfo, "loc")
        val validCoordinates = if (latitude != null && longitude != null) {
            latitude to longitude
        } else {
            null
        }
        val postal = parsePlace(location, "postal", "postal_code")
            ?: parsePlace(rawIspInfo, "postal", "postal_code")
        val timezone = parsePlace(location, "timezone")
            ?: parsePlace(rawIspInfo, "timezone")
        val source = parsePlace(location, "source")
            ?: parsePlace(rawIspInfo, "source")
        val approximate = parseApproximate(location)
            ?: parseApproximate(rawIspInfo)
            ?: true

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
            countryCode = countryCode,
            postal = postal,
            timezone = timezone,
            source = source,
            approximate = approximate
        )
    }

    private fun parseRawIspInfo(raw: Any?): JSONObject? {
        return when (raw) {
            is JSONObject -> raw
            is String -> try {
                JSONObject(raw)
            } catch (_: Throwable) {
                null
            }
            else -> null
        }
    }

    private fun parseCountryCode(vararg objects: JSONObject?): String? {
        for (location in objects) {
            if (location == null) continue
            val country = parsePlace(location, "country")
            if (country != null && country.matches(Regex("^[A-Za-z]{2}$"))) {
                return country.uppercase(Locale.ROOT)
            }
        }
        return null
    }

    private fun parseApproximate(vararg objects: JSONObject?): Boolean? {
        for (location in objects) {
            if (location == null) continue
            val raw = location.opt("approximate")
            if (raw is Boolean) return raw
        }
        return null
    }

    private fun parsePlace(location: JSONObject?, vararg names: String): String? {
        if (location == null) return null
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
        location: JSONObject?,
        range: ClosedFloatingPointRange<Double>,
        vararg names: String
    ): Double? {
        if (location == null) return null
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

    private fun parseNestedCoordinate(
        location: JSONObject?,
        nestedName: String,
        range: ClosedFloatingPointRange<Double>,
        vararg names: String
    ): Double? {
        val nested = location?.optJSONObject(nestedName) ?: return null
        return parseCoordinate(nested, range, *names)
    }

    private fun parseLocationString(location: JSONObject?, name: String): Double? {
        return parseLocationCoordinates(location, name)?.first
    }

    private fun parseLocationStringLongitude(location: JSONObject?, name: String): Double? {
        return parseLocationCoordinates(location, name)?.second
    }

    private fun parseLocationCoordinates(location: JSONObject?, name: String): Pair<Double, Double>? {
        if (location == null || location.isNull(name)) return null
        val value = location.opt(name)
        if (value !is String) return null
        val parts = value.split(",")
        if (parts.size != 2) return null
        val latitude = parts[0].trim().toDoubleOrNull()
            ?.takeIf { it.isFinite() && it in -90.0..90.0 }
            ?: return null
        val longitude = parts[1].trim().toDoubleOrNull()
            ?.takeIf { it.isFinite() && it in -180.0..180.0 }
            ?: return null
        return latitude to longitude
    }
}
