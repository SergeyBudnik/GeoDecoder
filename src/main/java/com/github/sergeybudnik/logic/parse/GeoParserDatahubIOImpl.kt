package com.github.sergeybudnik.logic.parse

import com.github.sergeybudnik.data.GDCoordinate
import com.github.sergeybudnik.data.GDCountry
import com.github.sergeybudnik.data.GDCountryBound
import com.github.sergeybudnik.data.GDCountryInfo
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.*
import java.nio.charset.Charset

class GeoParserDatahubIOImpl : GeoParser {
    private val jsonAccessor = GeoParserDatahubIOJsonAccessor()

    override fun parse(inputStream: InputStream, charset: Charset): List<GDCountry> {
        return parseInternal(InputStreamReader(inputStream, charset))
    }

    override fun parse(path: String): List<GDCountry> {
        return parseInternal(FileReader(path))
    }

    private fun parseInternal(reader: Reader): List<GDCountry> {
        return BufferedReader(reader).use { bufferedReader ->
            JsonParser.parseReader(bufferedReader).let { jsonData ->
                jsonAccessor.getFeaturesFromJson(jsonData = jsonData)
                        .map { jsonFeature ->
                            GDCountry(
                                    info = getCountryInfo(jsonFeature = jsonFeature),
                                    bounds = getCountryBounds(jsonFeature = jsonFeature)
                            )
                        }
            }
        }
    }

    private fun getCountryInfo(jsonFeature: JsonObject): GDCountryInfo {
        return GDCountryInfo(
                code = jsonAccessor.getISO3FromJson(
                        jsonFeature = jsonFeature
                )
        )
    }

    private fun getCountryBounds(jsonFeature: JsonObject): List<GDCountryBound> {
        return when (getGeometryType(jsonFeature = jsonFeature)) {
            GeoParserDatahubIOGeometryType.POLYGON -> {
                getCountryBoundsFromPolygon(jsonFeature = jsonFeature)
            }
            GeoParserDatahubIOGeometryType.MULTIPOLYGON -> {
                getCountryBoundsFromMultiPolygon(jsonFeature = jsonFeature)
            }
        }
    }

    private fun getCountryBoundsFromPolygon(jsonFeature: JsonObject): List<GDCountryBound> {
        return listOf(
                GDCountryBound(
                        coordinates = jsonAccessor
                                .getPolygonCoordinatesFromJson(jsonFeature = jsonFeature)
                                .map { coordinate -> getCoordinate(jsonCoordinate = coordinate) }
                )
        )
    }

    private fun getCountryBoundsFromMultiPolygon(jsonFeature: JsonObject): List<GDCountryBound> {
        return jsonAccessor.getMultipolygonPolygonsFromJson(jsonFeature = jsonFeature)
                .map { jsonPolygon ->
                    GDCountryBound(
                            coordinates = jsonAccessor
                                    .getMultipolygonCoordinatesFromJson(jsonPolygon = jsonPolygon)
                                    .map { jsonCoordinate -> getCoordinate(jsonCoordinate = jsonCoordinate) }
                    )
                }
    }

    private fun getCoordinate(jsonCoordinate: JsonArray): GDCoordinate {
        return GDCoordinate(
                lat = jsonAccessor.getCoordinateLatFromJson(
                        jsonCoordinate = jsonCoordinate
                ),
                lon = jsonAccessor.getCoordinateLonFromJson(
                        jsonCoordinate = jsonCoordinate
                )
        )
    }

    private fun getGeometryType(jsonFeature: JsonObject): GeoParserDatahubIOGeometryType {
        val type = jsonAccessor.getGeometryTypeFromJson(
                jsonFeature = jsonFeature
        )

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

private class GeoParserDatahubIOJsonAccessor {
    fun getFeaturesFromJson(jsonData: JsonElement): List<JsonObject> {
        return jsonData.asJsonObject.get("features").asJsonArray.map { it.asJsonObject }
    }

    fun getGeometryFromJson(jsonFeature: JsonObject): JsonObject {
        return jsonFeature.get("geometry").asJsonObject
    }

    fun getGeometryTypeFromJson(jsonFeature: JsonObject): String {
        return getGeometryFromJson(jsonFeature = jsonFeature).get("type").asString
    }

    fun getPolygonCoordinatesFromJson(jsonFeature: JsonObject): List<JsonArray> {
        return getGeometryFromJson(jsonFeature = jsonFeature)
                .get("coordinates").asJsonArray[0].asJsonArray
                .map { coordinate -> coordinate.asJsonArray }
    }

    fun getMultipolygonPolygonsFromJson(jsonFeature: JsonObject): List<JsonArray> {
        return getGeometryFromJson(jsonFeature = jsonFeature)
                .get("coordinates").asJsonArray
                .map { polygon -> polygon.asJsonArray }
    }

    fun getMultipolygonCoordinatesFromJson(jsonPolygon: JsonArray): List<JsonArray> {
        return jsonPolygon.get(0).asJsonArray
                .map { coordinate -> coordinate.asJsonArray }
    }

    fun getCoordinateLatFromJson(jsonCoordinate: JsonArray): Double {
        return jsonCoordinate.get(1).asDouble
    }

    fun getCoordinateLonFromJson(jsonCoordinate: JsonArray): Double {
        return jsonCoordinate.get(0).asDouble
    }

    fun getISO3FromJson(jsonFeature: JsonObject): String {
        return jsonFeature.get("properties").asJsonObject.get("ISO_A3").asString
    }
}
