package pro.leaco.gltf

import de.javagl.jgltf.impl.v2.Mesh
import de.javagl.jgltf.impl.v2.MeshPrimitive
import de.javagl.jgltf.impl.v2.Node
import mu.KotlinLogging
import pro.leaco.gltf.buffer.VertexColors
import pro.leaco.gltf.buffer.Vertices
import java.awt.Color
import java.lang.Exception
import java.util.ArrayList
import javax.vecmath.Matrix4f
import javax.vecmath.Point3f
import javax.vecmath.Vector3f

private val LOG = KotlinLogging.logger {}

/**
 * Base class for constructing glTF Mesh geometry.
 * @author Leaco
 */
open class TopologyBuilder(
    /** Mesh name used in metadata descriptions  */
    val name: String,
    /** Topology mode for MeshPrimitive. This indicates the type of data that will be output by the builder
     * and it can't be altered at runtime.  */
    private val topologyMode: TopologyMode,
) {
    /**
     * Indicates the type of topology the data in this mesh represents.
     * @see [
     * mesh.primitive.mode](https://www.khronos.org/registry/glTF/specs/2.0/glTF-2.0.html.meshprimitivemode)
     */
    enum class TopologyMode {
        POINTS, LINES, LINE_LOOP, LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN
    }
    /**
     * Return the mesh name.
     */

    /** List of vertices being added to this mesh  */
    protected val vertexList: MutableList<MeshVertex> = ArrayList()

    /**
     * Returns true if no triangles have been added.
     */
    val isEmpty: Boolean
        get() = vertexList.size == 0

    /**
     * Get the minimum bounds of all vertices. Should only be called after build().
     */
    var minBounds: Point3f? = null

    /**
     * Get the maximum bounds of all vertices. Should only be called after build().
     */
    var maxBounds: Point3f? = null
    /**
     * Get the transformation matrix.
     */
    /**
     * Set the transform used for offset and scale. This will replace any existing translations.
     */
    var transform: Matrix4f = Matrix4f()
        set(transform) {
            field.set(transform)
        }

    init {
        setScale(Vector3f(1f, 1f, 1f))
    }

    /**
     * Center all vertices about a point. This will update the transformation matrix.
     */
    fun setCenter(offset: Vector3f?) {
        val vec = Vector3f(offset)

        // negate because we are centering
        vec.negate()

        // invert the X axis
        vec.x *= INVERTX
        transform.setTranslation(vec)
    }

    /**
     * Scale all vertices in X/Y/Z. This will update the transformation matrix.
     */
    fun setScale(scale: Vector3f) {
        transform.m00 = scale.x
        transform.m11 = scale.y
        transform.m22 = scale.z

        // invert the X axis
        transform.m00 *= INVERTX
    }

    /**
     * Create a new vertex and apply the current offset and scale. This vertex will be assigned
     * an unique index that will be referenced when adding squares or triangles.
     * @param vertex 3D location of this vertex.
     */
    fun newVertex(vertex: Point3f): MeshVertex {
        val newVertex = Point3f(vertex)

        // apply offset and scale
        transform.transform(newVertex)
        val meshVertex = MeshVertex(vertexList.size, newVertex)
        vertexList.add(meshVertex)
        return meshVertex
    }

    /**
     * Make a distinct copy of the vertex.
     * @see .newVertex
     */
    fun copyVertex(vertex: MeshVertex): MeshVertex {
        val meshVertex = MeshVertex(vertexList.size, vertex)
        vertexList.add(meshVertex)
        return meshVertex
    }

    /**
     * Serialize the MeshVertex list and indices to buffers.
     * This method should be called when all shapes have added.
     * @param geoWriter Instance of writer class.
     * @return Node containing the mesh.
     */
    @Throws(Exception::class)
    fun build(geoWriter: GltfBuilder): Node {
        val meshPrimitive = MeshPrimitive()
        meshPrimitive.mode = topologyMode.ordinal
        buildBuffers(geoWriter, meshPrimitive)
        val mesh = Mesh()
        geoWriter.gltf.addMeshes(mesh)
        mesh.name = "$name-mesh"
        val meshIdx: Int = geoWriter.gltf.getMeshes().indexOf(mesh)
        LOG.debug("New Mesh[{}]: idx=<{}>", mesh.name, meshIdx)
        mesh.addPrimitives(meshPrimitive)
        val node = Node()
        geoWriter.addNode(node)
        node.mesh = meshIdx
        node.name = "$name-node"
        return node
    }

    /**
     * Generate primitive lists from the MeshVertex list and serialize to buffers.
     * @param geoWriter Instance of writer class.
     * @param meshPrimitive The glTF section containing serialized buffers.
     */
    @Throws(Exception::class)
    protected open fun buildBuffers(geoWriter: GltfBuilder, meshPrimitive: MeshPrimitive) {
        if (vertexList.size == 0) {
            throw Exception("No vertices to build!")
        }
        val vertices = Vertices(name)
        val colors = VertexColors(name)
        for (meshVertex in vertexList) {
            vertices.add(meshVertex.vertex)
            val color: Color? = meshVertex.color
            if (color != null) {
                colors.add(color)
            }
        }
        if (colors.size() > 0 && colors.size() !== vertexList.size) {
            throw Exception("Each Vertex must have a color")
        }

        // save bounds for later
        minBounds = vertices.minBounds
        maxBounds = vertices.maxBounds
        vertices.build(geoWriter, meshPrimitive)
        colors.build(geoWriter, meshPrimitive)
    }

    companion object {
        /** Indicates if the X axis should be inverted. This is necessary to correct orientations for Cesium.  */
        private const val INVERTX = -1
    }
}