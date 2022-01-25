package com.ai.core.math.topology

import org.locationtech.jts.geom.*
import org.locationtech.jts.io.ParseException
import org.locationtech.jts.io.WKTReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.vecmath.Vector2d
import javax.vecmath.Vector2f
import javax.vecmath.Vector3d
import javax.vecmath.Vector3f

object GeometryMaker {

    private val logger: Logger = LoggerFactory.getLogger(GeometryMaker::class.java)
    val f = GeometryFactory()

    fun createEmpty(dimension: Int): Geometry {
        return f.createEmpty(dimension)
    }

    fun <T> from(vertices: Array<T>): Geometry {
        val size = vertices.size
        return when {
            size < 3 -> createSimpleGeom(vertices.toList())
            else -> createPolygon(vertices.toList())
        }
    }


    fun <T> from(vertices: List<T>): Geometry {
        val size = vertices.size
        return when {
            size < 3 -> createSimpleGeom(vertices.toList())
            else -> createPolygon(vertices)
        }
    }

    /**
     * create geometry from given vertices serial
     */
    fun from(vararg vertices: Vector2f): Geometry {
        val size = vertices.size
        return when {
            size < 3 -> createSimpleGeom(vertices.toList())
            else -> createPolygon(vertices.toList())
        }
    }

    /**
     * create geometry from given vertices serial
     */
    fun from(vararg vertices: Vector3f): Geometry {
        val size = vertices.size
        return when {
            size < 3 -> createSimpleGeom(vertices.toList())
            else -> createPolygon(vertices.toList())
        }
    }

    fun <T> createPoint(p: T): Point {
        return f.createPoint(toCoordinate(p))
    }

    private fun <T> createSimpleGeom(vertices: List<T>): Geometry {
        val size = vertices.size
        return when {
            size <= 0 -> f.createEmpty(0)
            size == 1 -> f.createPoint(toCoordinate(vertices[0]))
            size == 2 -> f.createLineString(arrayOf(
                toCoordinate(vertices[0]),
                toCoordinate(vertices[1]),
            ))
            else -> f.createPolygon(vertices.map { toCoordinate(it) }.toMutableList().also {
                if (it.first() != it.last()) {
                    // close the polygon if open
                    it.add(it.first())
                }
            }.toTypedArray())
        }
    }

    fun <T> createRing(vertices: List<T>): LinearRing {
        return f.createLinearRing(vertices.map { toCoordinate(it) }.toMutableList().also {
            if (it.first() != it.last()) {
                // close the polygon if open
                it.add(it.first())
            }
        }.toTypedArray())
    }

    fun createPolygon(shell: LinearRing, holes: List<LinearRing>): Polygon {
        return f.createPolygon(shell, holes.toTypedArray())
    }

    private fun <T> createPolygon(vertices: List<T>): Geometry {
        if (vertices.size < 3) {
            throw IllegalArgumentException("vertices count is less than 3")
        }
        var firstPoint: T? = null
        val innerPolygonPoints = mutableListOf<T>()
        val innerPolygons = mutableListOf<Geometry>()

        val iterator = vertices.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (firstPoint == null) {
                //内部每一个形状的开头
                firstPoint = next
                innerPolygonPoints.clear()
                innerPolygonPoints.add(next)
                continue
            }
            if (firstPoint == next) {
                //内部每一个形状的结尾
                innerPolygons.add(this.createSimpleGeom(innerPolygonPoints))
                firstPoint = null
                innerPolygonPoints.clear()
                continue
            }
            innerPolygonPoints.add(next)
        }

        if (innerPolygonPoints.isNotEmpty()) {
            //创建最末形状
            innerPolygons.add(this.createSimpleGeom(innerPolygonPoints))
        }

        return if (innerPolygons.size == 1) {
            innerPolygons[0]
        } else {
            createMultiPolygonOrCollection(innerPolygons)
        }
    }

    private fun createMultiPolygonOrCollection(geoms: List<Geometry>): Geometry {
        if (geoms.size < 2) {
            throw IllegalArgumentException("geoms size less than 2,can not create multipolygon")
        }

        val countMap = HashMap<Int, Int>(3).also {
            it[0] = 0
            it[1] = 0
            it[2] = 0
        }
        geoms.forEach {
            countMap[it.dimension] = countMap[it.dimension]!! + 1
        }

        val dimesions = countMap.filter { it.value > 0 }.map { it.key }
        // should use multiPolygon instead of collection
        val useMulti = dimesions.size == 1
        return if (useMulti) {
            when (dimesions[0]) {
                0 -> f.createMultiPoint(geoms.map { it as Point }.toTypedArray())
                1 -> f.createMultiLineString(geoms.map { it as LineString }.toTypedArray())
                2 -> f.createMultiPolygon(geoms.map { it as Polygon }.toTypedArray())
                else -> throw IllegalStateException("impossible reach")
            }
        } else {
            f.createGeometryCollection(geoms.toTypedArray())
        }
    }

    /**
     * 从 well-know text 格式字符转换到形状
     *
     * @param wktString
     * @return
     */
    fun parse(wktString: String): Geometry {
        try {
            if (wktString.isNotEmpty()) {
                val wktReader = WKTReader()
                return wktReader.read(wktString)
            }
        } catch (e: ParseException) {
            logger.error(e.message, e)
        }
        return f.createPolygon()
    }


    /**
     * 创建一个y轴对称三角形
     *
     * @return
     */
    fun polygonTriangle(dx: Float, dy: Float): Geometry {
        val px = dx / 2
        val py = dy / 2
        return from(Vector2f(0f, py), Vector2f(px, -py), Vector2f(-px, -py))
    }

    /**
     * 创建 geomCollection
     *
     * @param geomList 形状列表
     * @return
     */
    fun geometryCollectionOf(vararg geomList: Geometry): GeometryCollection {
        return when {
            geomList.isNotEmpty() -> {
                f.createGeometryCollection(geomList)
            }
            else -> {
                f.createGeometryCollection()
            }
        }
    }

    /**
     * 创建 geomCollection
     *
     * @param geomList 形状列表
     * @return
     */
    fun geometryCollectionOf(geomList: List<Geometry>): GeometryCollection {
        return when {
            geomList.isNotEmpty() -> {
                f.createGeometryCollection(geomList.toTypedArray())
            }
            else -> {
                f.createGeometryCollection()
            }
        }
    }

    private fun <T> toCoordinate(point: T): Coordinate {
        when (point) {
            is Vector2f -> {
                val px = point as Vector2f
                return Coordinate(px.x.toDouble(), px.y.toDouble())
            }
            is Vector3f -> {
                val px = point as Vector3f
                return Coordinate(px.x.toDouble(), px.y.toDouble())
            }
            is Vector2d -> {
                val px = point as Vector2d
                return Coordinate(px.x, px.y)
            }
            is Vector3d -> {
                val px = point as Vector3d
                return Coordinate(px.x, px.y)
            }
        }
        throw IllegalArgumentException("point type can not convert to coordinate")
    }

}

/**
 * 转换到geomCollection
 * @return GeometryCollection
 */
fun List<Geometry>.toGeometryCollection(): GeometryCollection {
    return GeometryMaker.geometryCollectionOf(this)
}

