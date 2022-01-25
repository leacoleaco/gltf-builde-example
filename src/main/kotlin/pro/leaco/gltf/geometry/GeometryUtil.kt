package com.ai.core.math.topology

import org.locationtech.jts.geom.*
import org.locationtech.jts.triangulate.ConformingDelaunayTriangulationBuilder
import javax.vecmath.Vector2f

object GeometryUtil {}

fun <T : Geometry> T.getAllVectices(): List<Vector2f> {
    return this.coordinates?.map { Vector2f(it.x.toFloat(), it.y.toFloat()) } ?: listOf()
}

fun Polygon.toVectices(): List<Vector2f> {
    return this.getAllVectices()
}

fun LinearRing.toVectices(): List<Vector2f> {
    return this.getAllVectices()
}


fun <T : MultiPolygon> T.getSubPolygons(): List<Polygon> {
    val result = mutableListOf<Polygon>()
    for (i in 0 until this.numGeometries) {
        result.add(this.getGeometryN(i) as Polygon)
    }
    return result
}


fun <T : Geometry> T.getSubGeometries(): List<Geometry> {
    val result = mutableListOf<Geometry>()
    for (i in 0 until this.numGeometries) {
        result.add(this.getGeometryN(i))
    }
    return result
}

fun <T : Polygon> T.getInternalRings(): List<LinearRing> {
    val result = mutableListOf<LinearRing>()
    for (i in 0 until this.numInteriorRing) {
        result.add(this.getInteriorRingN(i) as LinearRing)
    }
    return result
}

fun <T : Geometry> T.keepPolygonOnly(): Polygon {
    if (this is Polygon) {
        return this
    } else if (this is MultiPolygon) {
        throw IllegalStateException("not support multipolygon")
    } else if (this is GeometryCollection) {
        val polygons = this.getSubGeometries().filterIsInstance<Polygon>()
        if (polygons.size == 1) {
            return polygons.first()
        } else if (polygons.isEmpty()) {
            return GeometryMaker.createEmpty(2) as Polygon
        } else {
            throw IllegalStateException("not support multipolygon in geometry collection")
        }
    }
    return GeometryMaker.createEmpty(2) as Polygon
}

fun Point.switch(): Vector2f {
    return Vector2f(this.x.toFloat(), this.y.toFloat())
}

fun LinearRing.toPolygon(): Polygon {
    return GeometryMaker.f.createPolygon(this)
}


fun <T : Geometry> T.conformingDelaunayTriangles(): List<Polygon> {
    val builder = ConformingDelaunayTriangulationBuilder()
    builder.setSites(this)
    val triangles = builder.getTriangles(GeometryMaker.f)
    return triangles.getSubGeometries().map { it as Polygon }
}

fun <T : Geometry> T.conformingDelaunayTrianglesWithoutHole(): List<Polygon> {
    return this.conformingDelaunayTriangles().filter { this.contains(it) }
}

