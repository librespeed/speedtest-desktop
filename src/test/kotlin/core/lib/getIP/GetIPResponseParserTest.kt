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
}
