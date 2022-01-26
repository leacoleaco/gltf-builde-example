package pro.leaco.gltf

import com.ai.core.math.topology.conformingDelaunayTrianglesWithoutHole
import org.locationtech.jts.geom.Geometry
import javax.vecmath.Point2f
import javax.vecmath.Point3f
import javax.vecmath.Vector3f

/**
 * add a 2D surface with geometry
 * @param geometry 形状
 * @param isTextured 贴图
 */
fun MeshBuilder.addGeometryPlane(geometry: Geometry, isTextured: Boolean): MeshBuilder {

    val triangels = geometry.conformingDelaunayTrianglesWithoutHole()

    val meshVertexes = mutableListOf<MeshVertex>()

    val normal = Vector3f(0f, 0f, 1f)

    for (triangle in triangels) {
        val triangleVertexes = triangle.coordinates.map { coord ->
            val point = Point3f(coord.x.toFloat(), coord.y.toFloat(), 0f)
            val meshVertex = meshVertexes.find { p -> p.vertex == point }
            if (meshVertex == null) {
                val newVertex = this.newVertex(point)
                newVertex.addNormal(normal)
                meshVertexes.add(newVertex)
                return@map newVertex
            } else {
                return@map meshVertex
            }
        }

        val vtx0 = triangleVertexes[0]
        val vtx1 = triangleVertexes[1]
        val vtx2 = triangleVertexes[2]
        this.addTriangle(
            vtx2,
            vtx1,
            vtx0,
        )

    }

    if (isTextured) {
        val minX = meshVertexes.minOf { it.vertex.x }
        val maxX = meshVertexes.maxOf { it.vertex.x }
        val minY = meshVertexes.minOf { it.vertex.y }
        val maxY = meshVertexes.maxOf { it.vertex.y }
        val dx = maxX - minX
        val dy = maxY - minY

        meshVertexes.forEach { meshVertex ->
            meshVertex.textureCoordinate = Point2f(
                (meshVertex.vertex.x - minX) / dx,
                (meshVertex.vertex.y - minY) / dy
            )
        }


    }

    return this
}