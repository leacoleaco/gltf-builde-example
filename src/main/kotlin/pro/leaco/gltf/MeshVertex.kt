package pro.leaco.gltf

import mu.KotlinLogging
import java.awt.Color
import javax.vecmath.Point2f
import javax.vecmath.Point3f
import javax.vecmath.Vector3f
import javax.vecmath.Vector4f

private val LOG = KotlinLogging.logger {}

/**
 * Contains details for a vertex in a 3D mesh.
 * @author Leaco
 */
class MeshVertex {

    /**
     * Get the index of this vertex for use in TriangleIndices.
     */
    /** index to be used in the indices list.  */
    val index: Int

    /**
     * Get the position of this vertex.
     */
    val vertex: Point3f

    /** list of normals that will be averaged during build()  */
    private val normals: MutableList<Vector3f> = ArrayList<Vector3f>()

    /** list of tangents that will be averaged during build()  */
    private val tangents: MutableList<Vector3f> = ArrayList<Vector3f>()

    constructor(index: Int, vertex: Point3f) {
        this.index = index
        this.vertex = vertex
    }

    override fun toString(): String {
        return java.lang.String.format("idx=[%d] vtx=(%.6f,%.6f,%.6f) normals<%d>",
            index,
            vertex.x, vertex.y, vertex.z,
            normals.size)
    }

    /**
     * Create copy of a vertex
     * @param index Index of new vertex
     * @param mv Vertex to copy
     */
    constructor(index: Int, mv: MeshVertex) {
        this.index = index
        this.vertex = mv.vertex
        if (mv.color != null) {
            color = mv.color
        }
        if (mv.texCoord != null) {
            texCoord = Point2f(mv.texCoord)
        }
        for (normal in mv.normals) {
            normals.add(Vector3f(normal))
        }
        for (tangent in mv.tangents) {
            tangents.add(Vector3f(tangent))
        }
    }


    /**
     * Get the vertex color.
     * @return null if no color
     */
    /**
     * Set the vertex color.
     */
    var color: Color? = null

    /**
     * Get the texture coordinate of this vertex.
     * @return null if no coordinate
     */
    /**
     * Set the texture coordinate of this vertex.
     */
    var texCoord: Point2f? = null


    /**
     * Add a neighboring normal for use when calculating the average normal.
     */
    fun addNormal(vec: Vector3f) {
        normals.add(vec)
    }

    /**
     * Add a neighboring tangent for use when calculating the average normal.
     */
    fun addTangent(vec: Vector3f) {
        tangents.add(vec)
    }// create a fake normal// calculate an average normal

    /**
     * Calculate the average of the normal vectors.
     */
    @get:Throws(Exception::class)
    val normal: Vector3f?
        get() {
            if (normals.size == 0) {
                return null
            }
            var avgNormal = Vector3f()

            // calculate an average normal
            for (normal in normals) {
                avgNormal.add(normal)
            }
            avgNormal.normalize()
            if (java.lang.Float.isNaN(avgNormal.x) || java.lang.Float.isNaN(avgNormal.y) || java.lang.Float.isNaN(
                    avgNormal.z)
            ) {
                LOG.debug("Could not calculate average normal for vertex: {}", index)
                // create a fake normal
                avgNormal = Vector3f(1f, 1f, 1f)
                avgNormal.normalize()
            }
            return avgNormal
        }// calculate an average tangent

    /**
     * Calculate the average of the tangent vectors.
     */
    @get:Throws(Exception::class)
    protected val tangent: Vector4f
        protected get() {
            if (normals.size == 0) {
                throw Exception("No tangents to average for vertex: " + index)
            }
            val avgTangent = Vector3f()

            // calculate an average tangent
            for (tangent in tangents) {
                avgTangent.add(tangent)
            }
            avgTangent.normalize()
            return Vector4f(
                avgTangent.x,
                avgTangent.y,
                avgTangent.z,
                1f)
        }


}