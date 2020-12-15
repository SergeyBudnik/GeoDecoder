package com.github.sergeybudnik.logic.parse

import com.github.sergeybudnik.data.GDCountry

interface GeoParser {
    fun parse(path: String): List<GDCountry>
}
