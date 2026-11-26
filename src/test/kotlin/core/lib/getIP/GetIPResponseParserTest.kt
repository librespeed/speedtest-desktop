package core.lib.getIP

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetIPResponseParserTest {
    @Test
    fun parsesLocationAndKeepsLegacyAndRawFields() {
        val raw = """{"processedString":"203.0.113.10 - Example ISP","rawIspInfo":"ISP telemetry text","location":{"city":"Springfield","region":"Illinois","country":"United States","lat":39.7817,"lon":-89.6501}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("203.0.113.10 - Example ISP", response.processedString)
        assertEquals(raw, response.rawIspInfo)
        assertEquals("Springfield", response.location?.city)
        assertEquals("Illinois", response.location?.region)
        assertEquals("United States", response.location?.country)
        assertEquals(39.7817, response.location?.latitude)
        assertEquals(-89.6501, response.location?.longitude)
        assertEquals(
            "203.0.113.10 - Example ISP\nApproximate IP-derived location: Springfield, Illinois, United States (Latitude 39.7817, Longitude -89.6501)",
            response.toDisplayText()
        )
    }

    @Test
    fun legacyJsonWithoutLocationKeepsProcessedString() {
        val raw = """{"processedString":"198.51.100.20 - Legacy ISP","rawIspInfo":"legacy telemetry"}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("198.51.100.20 - Legacy ISP", response.processedString)
        assertEquals(raw, response.rawIspInfo)
        assertNull(response.location)
        assertEquals("198.51.100.20 - Legacy ISP", response.toDisplayText())
    }

    @Test
    fun plainTextResponseRemainsBackwardCompatible() {
        val raw = "192.0.2.15 - Plain ISP"

        val response = GetIPResponseParser.parse(raw)

        assertEquals(raw, response.processedString)
        assertEquals(raw, response.rawIspInfo)
        assertNull(response.location)
        assertEquals(raw, response.toDisplayText())
    }

    @Test
    fun acceptsStringCoordinateAliasesAndPartialPlaceData() {
        val raw = """{"processedString":"203.0.113.8 - Example ISP","location":{"city":"Testville","latitude":"40.123456","longitude":"-74.5"}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("Testville", response.location?.city)
        assertEquals(40.123456, response.location?.latitude)
        assertEquals(-74.5, response.location?.longitude)
        assertEquals(
            "203.0.113.8 - Example ISP\nApproximate IP-derived location: Testville (Latitude 40.123456, Longitude -74.5)",
            response.toDisplayText()
        )
    }

    @Test
    fun rejectsOutOfRangeCoordinatesButKeepsValidPlaceData() {
        val raw = """{"processedString":"203.0.113.9 - Example ISP","location":{"city":"Springfield","region":"Illinois","country":"United States","lat":91,"lon":181}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("Springfield", response.location?.city)
        assertNull(response.location?.latitude)
        assertNull(response.location?.longitude)
        assertEquals(
            "203.0.113.9 - Example ISP\nApproximate IP-derived location: Springfield, Illinois, United States",
            response.toDisplayText()
        )
    }

    @Test
    fun ignoresEmptyOrMalformedLocationObjects() {
        val emptyRaw = """{"processedString":"203.0.113.10 - Example ISP","location":{}}"""
        val malformedRaw = """{"processedString":"203.0.113.11 - Example ISP","location":"not-an-object"}"""

        assertNull(GetIPResponseParser.parse(emptyRaw).location)
        assertNull(GetIPResponseParser.parse(malformedRaw).location)
    }

    @Test
    fun missingProcessedStringFallsBackToRawResponse() {
        val raw = """{"location":{"city":"Testville","region":"Example Region","country":"Example Country","lat":12.5,"lon":45.25}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals(raw, response.processedString)
        assertEquals(
            "$raw\nApproximate IP-derived location: Testville, Example Region, Example Country (Latitude 12.5, Longitude 45.25)",
            response.toDisplayText()
        )
    }

    @Test
    fun malformedPlaceFieldsAreSkippedAndValidAliasesAreUsed() {
        val raw = """{"processedString":"203.0.113.12 - Example ISP","location":{"city":false,"city_name":"Springfield","region":["Illinois"],"state":"Illinois","country":123,"countryName":"United States","lat":" 36.6676 ","lon":"-78.3875"}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("Springfield", response.location?.city)
        assertEquals("Illinois", response.location?.region)
        assertEquals("United States", response.location?.country)
        assertEquals("United States", response.location?.countryName)
        assertEquals(36.6676, response.location?.latitude)
        assertEquals(-78.3875, response.location?.longitude)
        assertEquals(
            "203.0.113.12 - Example ISP\nApproximate IP-derived location: Springfield, Illinois, United States (Latitude 36.6676, Longitude -78.3875)",
            response.toDisplayText()
        )
    }

    @Test
    fun countryCodeFallsBackWithoutAName() {
        val raw = """{"processedString":"203.0.113.13 - Example ISP","location":{"country":"US","countryCode":"US","lat":36.6676,"lon":-78.3875}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("US", response.location?.country)
        assertEquals("US", response.location?.countryCode)
        assertEquals(
            "203.0.113.13 - Example ISP\nApproximate IP-derived location: US (Latitude 36.6676, Longitude -78.3875)",
            response.toDisplayText()
        )
    }

    @Test
    fun nonStringProcessedStringFallsBackToRawResponse() {
        val raw = """{"processedString":false,"location":{"city":"Testville"}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals(raw, response.processedString)
        assertEquals("$raw\nApproximate IP-derived location: Testville", response.toDisplayText())
    }

    @Test
    fun explicitApproximateFalseIsExported() {
        val location = ApproximateIPLocation(
            city = "Testville",
            approximate = false
        )

        val exported = location.toJsonString()
        val json = util.json.JSONObject(exported)

        assertEquals(false, json.getBoolean("approximate"))
    }

    @Test
    fun topLevelCountryDataOverridesLegacyCountryAliases() {
        val raw = """{"processedString":"203.0.113.15 - Example ISP","location":{"country":"France","countryCode":"FR"},"rawIspInfo":{"country_name":"United States","country_code":"US","loc":"36.6676,-78.3875"}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("France", response.location?.country)
        assertEquals("FR", response.location?.countryCode)
        assertNull(response.location?.countryName)
    }

    @Test
    fun coordinateCandidatesNeverMixAxesAcrossSources() {
        val raw = """{"processedString":"203.0.113.16 - Example ISP","location":{"latitude":10,"longitude":181},"rawIspInfo":{"loc":"50,120"}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals(50.0, response.location?.latitude)
        assertEquals(120.0, response.location?.longitude)
    }

    @Test
    fun emptyProcessedStringRemainsEmpty() {
        val raw = """{"processedString":"","location":{"city":"Springfield"}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("", response.processedString)
        assertEquals("Approximate IP-derived location: Springfield", response.toDisplayText())
    }

    @Test
    fun legacyRawIspInfoStillProvidesLocationFallback() {
        val raw = """{"processedString":"128.94.2.134 - Microsoft Corporation, US","rawIspInfo":{"ip":"128.94.2.134","city":"Boydton","region":"Virginia","country":"US","country_name":"United States","loc":"36.6676,-78.3875","org":"AS8075 Microsoft Corporation","postal":"23917","timezone":"America/New_York"}}"""

        val response = GetIPResponseParser.parse(raw)

        assertEquals("128.94.2.134 - Microsoft Corporation, US", response.processedString)
        assertEquals(raw, response.rawIspInfo)
        assertEquals("Boydton", response.location?.city)
        assertEquals("Virginia", response.location?.region)
        assertEquals("United States", response.location?.countryName)
        assertEquals("US", response.location?.countryCode)
        assertEquals("23917", response.location?.postal)
        assertEquals("America/New_York", response.location?.timezone)
        assertEquals(36.6676, response.location?.latitude)
        assertEquals(-78.3875, response.location?.longitude)
    }

    @Test
    fun acceptsNestedCoordinatesAndTopLevelPostalTimezoneSource() {
        val raw = """{"processedString":"203.0.113.14 - Example ISP","location":{"city":"Springfield","region":"Illinois","country":"United States","countryCode":"US","postal":"62701","timezone":"America/Chicago","source":"ipinfo","approximate":true,"coordinates":{"latitude":"39.7817","longitude":"-89.6501"}}}"""
        val response = GetIPResponseParser.parse(raw)

        assertEquals("Springfield", response.location?.city)
        assertEquals("62701", response.location?.postal)
        assertEquals("America/Chicago", response.location?.timezone)
        assertEquals("ipinfo", response.location?.source)
        assertEquals(true, response.location?.approximate)
        assertEquals(39.7817, response.location?.latitude)
        assertEquals(-89.6501, response.location?.longitude)
    }

    @Test
    fun locationJsonExportUsesStructuredFieldsOnly() {
        val location = ApproximateIPLocation(
            city = "Boydton",
            region = "Virginia",
            country = "United States",
            latitude = 36.6676,
            longitude = -78.3875,
            countryName = "United States",
            countryCode = "US",
            postal = "23917",
            timezone = "America/New_York",
            source = "ipinfo",
            approximate = true
        )

        val exported = location.toJsonString()
        val json = util.json.JSONObject(exported)

        assertEquals("Boydton", json.getString("city"))
        assertEquals("Virginia", json.getString("region"))
        assertEquals("United States", json.getString("country"))
        assertEquals("United States", json.getString("countryName"))
        assertEquals("US", json.getString("countryCode"))
        assertEquals(36.6676, json.getDouble("latitude"))
        assertEquals(-78.3875, json.getDouble("longitude"))
        assertEquals("23917", json.getString("postal"))
        assertEquals("America/New_York", json.getString("timezone"))
        assertEquals("ipinfo", json.getString("source"))
        assertEquals(true, json.getBoolean("approximate"))
    }
}
