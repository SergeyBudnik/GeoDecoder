package com.github.sergeybudnik.logic.parse

import com.github.sergeybudnik.data.GDCoordinate
import com.github.sergeybudnik.data.GDCountry
import com.github.sergeybudnik.data.GDCountryBound
import com.github.sergeybudnik.data.GDCountryInfo
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.FileReader

class GeoParserDatahubIOImpl : GeoParser {
    override fun parse(path: String): List<GDCountry> {
        val gdCountries = ArrayList<GDCountry>()

        BufferedReader(FileReader(path)).use {
            val data = JsonParser.parseReader(it).asJsonObject
            val features = data.get("features").asJsonArray

            features
                    .map { feature -> feature.asJsonObject }
                    .forEach { feature ->
                        val gdCountryInfo = getCountryInfo(feature = feature)

                        val geometryType = getGeometryType(feature = feature)

                        gdCountries.add(
                                GDCountry(
                                        info = gdCountryInfo,
                                        bounds = when (geometryType) {
                                            GeoParserDatahubIOGeometryType.POLYGON -> {
                                                getCountryBoundsFromPolygon(feature = feature)
                                            }
                                            GeoParserDatahubIOGeometryType.MULTIPOLYGON -> {
                                                getCountryBoundsFromMultiPolygon(feature = feature)
                                            }
                                        }
                                )
                        )
                    }
       }

        return gdCountries
    }

    private fun getCountryInfo(feature: JsonObject): GDCountryInfo {
        return GDCountryInfo(
                code = feature.get("properties").asJsonObject.get("ISO_A3").asString
        )
    }

    private fun getCountryBoundsFromPolygon(feature: JsonObject): List<GDCountryBound> {
        val gdCoordinates = ArrayList<GDCoordinate>()

        feature
                .get("geometry").asJsonObject
                .get("coordinates").asJsonArray[0].asJsonArray
                .forEach { coordinate ->
                    gdCoordinates.add(
                            GDCoordinate(
                                    lat = coordinate.asJsonArray.get(1).asDouble,
                                    lon = coordinate.asJsonArray.get(0).asDouble
                            )
                    )
                }

        return listOf(
                GDCountryBound(
                        coordinates = gdCoordinates
                )
        )
    }

    private fun getCountryBoundsFromMultiPolygon(feature: JsonObject): List<GDCountryBound> {
        val gdCountryBounds = ArrayList<GDCountryBound>()

        feature
                .get("geometry").asJsonObject
                .get("coordinates").asJsonArray
                .forEach { coordinates ->
                    val gdCoordinates = ArrayList<GDCoordinate>()

                    coordinates.asJsonArray.get(0).asJsonArray.forEach { coordinate ->
                        gdCoordinates.add(
                                GDCoordinate(
                                        lat = coordinate.asJsonArray.get(1).asDouble,
                                        lon = coordinate.asJsonArray.get(0).asDouble
                                )
                        )
                    }

                    gdCountryBounds.add(
                            GDCountryBound(
                                    coordinates = gdCoordinates
                            )
                    )
                }

        return gdCountryBounds
    }

    private fun getGeometryType(feature: JsonObject): GeoParserDatahubIOGeometryType {
        val type = feature.get("geometry").asJsonObject.get("type").asString

        return if (type == "Polygon") {
            GeoParserDatahubIOGeometryType.POLYGON
        } else {
            GeoParserDatahubIOGeometryType.MULTIPOLYGON
        }
    }
}

private enum class GeoParserDatahubIOGeometryType {
    POLYGON, MULTIPOLYGON
}
