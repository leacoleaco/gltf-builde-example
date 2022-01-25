package pro.leaco.gltf

import com.ai.core.math.topology.conformingDelaunayTrianglesWithoutHole
import org.locationtech.jts.geom.Geometry
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

    for (triangle in triangels) {
        val triangleVertexes = triangle.coordinates.map { coord ->
            val point = Point3f(coord.x.toFloat(), coord.y.toFloat(), 0f)
            val meshVertex = meshVertexes.find { p -> p.vertex == point }
            if (meshVertex == null) {
                val newVertex = this.newVertex(point)
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
            vtx0,
            vtx1,
            vtx2,
        )

        // calculate tangents
        val vec01 = Vector3f()
        vec01.sub(vtx0.vertex, vtx1.vertex)
        vtx0.addTangent(vec01)
        vtx1.addTangent(vec01)

        val vec02 = Vector3f()
        vec02.sub(vtx0.vertex, vtx2.vertex)
        vtx0.addTangent(vec02)
        vtx2.addTangent(vec02)

        val vec12 = Vector3f()
        vec12.sub(vtx1.vertex, vtx2.vertex)
        vtx1.addTangent(vec12)
        vtx2.addTangent(vec12)
    }

    this.supressNormals(true)

    return this
}