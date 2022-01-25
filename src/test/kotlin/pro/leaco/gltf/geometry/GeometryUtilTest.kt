package pro.leaco.gltf.geometry

import com.ai.core.math.topology.GeometryMaker
import com.ai.core.math.topology.conformingDelaunayTriangles
import com.ai.core.math.topology.conformingDelaunayTrianglesWithoutHole
import org.junit.Test

import org.junit.Assert.*

class GeometryUtilTest {

    @Test
    fun conformingDelaunayTriangles() {
        val g = GeometryMaker.parse("POLYGON ((90 370, 300 370, 300 120, 90 120, 90 370), \n" +
                "  (180 290, 150 200, 250 200, 185 226, 180 290))")
        val trans = g.conformingDelaunayTriangles()
        trans.forEach {
            println(it)
        }
        kotlin.test.assertEquals(10, trans.size)
    }

    @Test
    fun conformingDelaunayTrianglesWithoutHole() {
        val g = GeometryMaker.parse("POLYGON ((90 370, 300 370, 300 120, 90 120, 90 370), \n" +
                "  (180 290, 150 200, 250 200, 185 226, 180 290))")
        val trans = g.conformingDelaunayTrianglesWithoutHole()
        trans.forEach {
            println(it)
        }
        kotlin.test.assertEquals(8, trans.size)
    }
}