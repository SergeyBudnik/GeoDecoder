package com.github.sergeybudnik.logic.parse

import com.github.sergeybudnik.data.GDCoordinate
import com.github.sergeybudnik.data.GDCountry
import com.github.sergeybudnik.logic.decode.GeoDecoderImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import java.util.zip.ZipInputStream

internal class GeoParserDatahubIOImplTest {

    private val jsonResourceName = "/countries.geojson"
    private val zipWithJsonResourceName = "/countries.zip"

    @Test
    fun testParsingViaInputStream_FromJson() {
        javaClass.getResourceAsStream(jsonResourceName).use { inputStream ->
            val countries = GeoParserDatahubIOImpl().parse(inputStream, Charset.forName("UTF-8"))

            decodeAndCheckCorrectness(countries)
        }
    }

    @Test
    fun testParsingViaInputStream_FromZipWithJson() {
        javaClass.getResourceAsStream(zipWithJsonResourceName).use { resourceInputStream ->
            ZipInputStream(resourceInputStream).use { zipInputStream ->
                zipInputStream.getNextEntry() // positions the stream at the beginning of the entry data
                val countries = GeoParserDatahubIOImpl().parse(zipInputStream, Charset.defaultCharset())

                decodeAndCheckCorrectness(countries)
            }
        }
    }

    private fun decodeAndCheckCorrectness(countries: List<GDCountry>) {
        assertNotNull(countries)

        val monacoCoor1 = GDCoordinate(43.730191, 7.411283)
        val monacoCoor2 = GDCoordinate(43.747754, 7.431043)
        val taiwanCoor3 = GDCoordinate(23.784691, 121.041573)
        val taiwanCoor4 = GDCoordinate(25.024012, 121.781536)
        val arabianSeaCoordinate = GDCoordinate(16.318157, 62.543254)

        val monacoCode = "MCO"
        val taiwanCode = "TWN"
        assertEquals(monacoCode, decode(monacoCoor1, countries))
        assertEquals(monacoCode, decode(monacoCoor2, countries))
        assertEquals(taiwanCode, decode(taiwanCoor3, countries))
        assertEquals(taiwanCode, decode(taiwanCoor4, countries))

        assertNull(decode(arabianSeaCoordinate, countries))
    }

    private fun decode(c3: GDCoordinate, countries: List<GDCountry>) = GeoDecoderImpl().decode(c3, countries)?.code
}