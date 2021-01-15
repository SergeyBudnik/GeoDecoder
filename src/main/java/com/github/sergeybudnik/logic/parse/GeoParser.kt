package com.github.sergeybudnik.logic.parse

import com.github.sergeybudnik.data.GDCountry
import java.io.InputStream
import java.nio.charset.Charset

interface GeoParser {
    fun parse(inputStream: InputStream, charset: Charset): List<GDCountry>
    fun parse(path: String): List<GDCountry>
}
