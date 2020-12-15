package com.github.sergeybudnik.logic.decode

import com.github.sergeybudnik.data.GDCoordinate
import com.github.sergeybudnik.data.GDCountry
import com.github.sergeybudnik.data.GDCountryBound
import com.github.sergeybudnik.data.GDCountryInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GeoDecoderImplTest {
    @Test
    fun testSimple() {
        val gdCountries = listOf(
                GDCountry(
                        info = GDCountryInfo(code = "A"),
                        bounds = listOf(
                                GDCountryBound(
                                        coordinates = listOf(
                                                GDCoordinate(lat =  0.0, lon =  0.0),
                                                GDCoordinate(lat =  0.0, lon = 10.0),
                                                GDCoordinate(lat = 10.0, lon = 10.0),
                                                GDCoordinate(lat = 10.0, lon =  0.0)
                                        )
                                )
                        )
                )
        )

        /* Inside */
        assertEquals("A", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 5.0, lon = 5.0), countries = gdCountries)?.code)
        /* On bounds */
        assertEquals("A", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 5.0, lon = 0.0), countries = gdCountries)?.code)
        /* Outside */
        assertEquals(null, GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 12.0, lon = 13.0), countries = gdCountries)?.code)
    }

    @Test
    fun testCountryWithMultiplePolygons() {
        val gdCountries = listOf(
                GDCountry(
                        info = GDCountryInfo(code = "A"),
                        bounds = listOf(
                                GDCountryBound(
                                        coordinates = listOf(
                                                GDCoordinate(lat =  0.0, lon =  0.0),
                                                GDCoordinate(lat =  0.0, lon = 10.0),
                                                GDCoordinate(lat = 10.0, lon = 10.0),
                                                GDCoordinate(lat = 10.0, lon =  0.0)
                                        )
                                ),
                                GDCountryBound(
                                        coordinates = listOf(
                                                GDCoordinate(lat = 15.0, lon = 15.0),
                                                GDCoordinate(lat = 15.0, lon = 25.0),
                                                GDCoordinate(lat = 25.0, lon = 25.0),
                                                GDCoordinate(lat = 25.0, lon = 15.0)
                                        )
                                )
                        )
                )
        )

        /* Inside first */
        assertEquals("A", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 5.0, lon = 5.0), countries = gdCountries)?.code)
        /* On first bound */
        assertEquals("A", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 5.0, lon = 0.0), countries = gdCountries)?.code)
        /* Inside second */
        assertEquals("A", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 20.0, lon = 20.0), countries = gdCountries)?.code)
        /* On second bound */
        assertEquals("A", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 20.0, lon = 15.0), countries = gdCountries)?.code)
        /* Outside */
        assertEquals(null, GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 27.0, lon = 28.0), countries = gdCountries)?.code)
    }

    @Test
    fun testMultipleCountries() {
        val gdCountries = listOf(
                GDCountry(
                        info = GDCountryInfo(code = "A"),
                        bounds = listOf(
                                GDCountryBound(
                                        coordinates = listOf(
                                                GDCoordinate(lat =  0.0, lon =  0.0),
                                                GDCoordinate(lat =  0.0, lon = 10.0),
                                                GDCoordinate(lat = 10.0, lon = 10.0),
                                                GDCoordinate(lat = 10.0, lon =  0.0)
                                        )
                                )
                        )
                ),
                GDCountry(
                        info = GDCountryInfo(code = "B"),
                        bounds = listOf(
                                GDCountryBound(
                                        coordinates = listOf(
                                                GDCoordinate(lat = 15.0, lon = 15.0),
                                                GDCoordinate(lat = 15.0, lon = 25.0),
                                                GDCoordinate(lat = 25.0, lon = 25.0),
                                                GDCoordinate(lat = 25.0, lon = 15.0)
                                        )
                                )
                        )
                )
        )

        /* Inside first */
        assertEquals("A", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 5.0, lon = 5.0), countries = gdCountries)?.code)
        /* On first bound */
        assertEquals("A", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 5.0, lon = 0.0), countries = gdCountries)?.code)
        /* Inside second */
        assertEquals("B", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 20.0, lon = 20.0), countries = gdCountries)?.code)
        /* On second bound */
        assertEquals("B", GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 20.0, lon = 15.0), countries = gdCountries)?.code)
        /* Outside */
        assertEquals(null, GeoDecoderImpl().decode(coordinate = GDCoordinate(lat = 27.0, lon = 28.0), countries = gdCountries)?.code)
    }
}
