package com.github.sergeybudnik.logic.decode

import com.github.sergeybudnik.data.GDCoordinate
import com.github.sergeybudnik.data.GDCountry
import com.github.sergeybudnik.data.GDCountryInfo
import com.github.sergeybudnik.data.GDOrientation
import kotlin.math.max
import kotlin.math.min

interface GeoDecoder {
    /**
     * Resolves a country by coordinate
     *
     * @param coordinate coordinate to resolve
     * @param countries list of countries
     *
     * @return country info if country is resolved, null otherwise
     */
    fun decode(coordinate: GDCoordinate, countries: List<GDCountry>): GDCountryInfo?
}

class GeoDecoderImpl : GeoDecoder {
    override fun decode(coordinate: GDCoordinate, countries: List<GDCountry>): GDCountryInfo? {
        return countries
                .find {
                    country -> country.bounds.any { bound ->
                        isInside(point = coordinate, bound = bound.coordinates)
                    }
                }
                ?.info
    }

    /**
     * Checks if point lies on a segment bounded by start and finish points
     *
     * @param point point to check
     * @param start segment start point
     * @param finish segment finish point
     */
    private fun isOnSegment(point: GDCoordinate, start: GDCoordinate, finish: GDCoordinate): Boolean {
        val fitsLatMax = point.lat <= max(start.lat, finish.lat)
        val fitsLatMin = point.lat >= min(start.lat, finish.lat)
        val fitsLonMax = point.lon <= max(start.lon, finish.lon)
        val fitsLonMin = point.lon >= min(start.lon, finish.lon)

        return fitsLatMax && fitsLatMin && fitsLonMax && fitsLonMin
    }

    /**
     * Checks the orientation of an ordered triplet (c1, c2, c3)
     *
     * @param c1 first point
     * @param c2 second point
     * @param c3 third point
     *
     * @return orientation - co-linear, clockwise or counter-clockwise
     */
    private fun getOrientation(c1: GDCoordinate, c2: GDCoordinate, c3: GDCoordinate): GDOrientation {
        val res = (c2.lon - c1.lon) * (c3.lat - c2.lat) - (c2.lat - c1.lat) * (c3.lon - c2.lon)

        return when {
            res == 0.0 -> {
                GDOrientation.CO_LINEAR
            }
            res > 0.0 -> {
                GDOrientation.CLOCKWISE
            }
            else -> {
                GDOrientation.COUNTER_CLOCKWISE
            }
        }
    }

    /**
     * Checks if [s1, f1] intersects with [s2, f2]
     *
     * @param s1 start of first segment
     * @param f1 finish of first segment
     * @param s2 start of second segment
     * @param f2 finish of second segment
     *
     * @return true if intersects, false otherwise
     */
    private fun isIntersects(
            s1: GDCoordinate, f1: GDCoordinate,
            s2: GDCoordinate, f2: GDCoordinate
    ): Boolean {
        val o1 = getOrientation(c1 = s1, c2 = f1, c3 = s2)
        val o2 = getOrientation(c1 = s1, c2 = f1, c3 = f2)
        val o3 = getOrientation(c1 = s2, c2 = f2, c3 = s1)
        val o4 = getOrientation(c1 = s2, c2 = f2, c3 = f1)

        @Suppress("RedundantIf")
        return if (o1 != o2 && o3 != o4) {
            /**
             * General case
             */
            true
        } else if (o1 == GDOrientation.CO_LINEAR && isOnSegment(point = s2, start = s1, finish = f1)) {
            /**
             * s1, f1 and s2 are co-linear and s2 lies on segment s1f1
             */
            true
        } else if (o2 == GDOrientation.CO_LINEAR && isOnSegment(point = f2, start = s1, finish = f1)) {
            /**
             * s1, f1 and s2 are co-linear and f2 lies on segment s1f1
             */
            true
        } else if (o3 == GDOrientation.CO_LINEAR && isOnSegment(point = s1, start = s2, finish = f2)) {
            /**
             * s2, f2 and s1 are co-linear and s1 lies on segment s2f2
             */
            true
        } else if (o4 == GDOrientation.CO_LINEAR && isOnSegment(point = f1, start = s2, finish = f2)) {
            /**
             * s2, f2 and f1 are colinear and f1 lies on segment s2f2
             */
            true
        } else {
            false
        }
    }

    /**
     * Checks if point lies inside or on bounds
     *
     * @param point point to check
     * @param bound bound to check
     *
     * @return true if point is inside or on bounds, false otherwise
     */
    private fun isInside(point: GDCoordinate, bound: List<GDCoordinate>): Boolean {
        if (bound.size < 3) {
            return false
        } else {
            val extreme = GDCoordinate(Double.MAX_VALUE, point.lon)

            var count = 0
            var current = 0

            do {
                val next: Int = (current + 1) % bound.size

                if (isIntersects(s1 = bound[current], f1 = bound[next], s2 = point, f2 = extreme)) {
                    if (getOrientation(c1 = bound[current], c2 = point, c3 = bound[next]) === GDOrientation.CO_LINEAR) {
                        return isOnSegment(point = point, start = bound[current], finish = bound[next])
                    } else {
                        count++
                    }
                }

                current = next
            } while (current != 0)

            return count % 2 == 1
        }
    }
}
