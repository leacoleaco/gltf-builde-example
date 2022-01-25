package pro.leaco.gltf

import de.javagl.jgltf.impl.v2.Material
import de.javagl.jgltf.impl.v2.MeshPrimitive
import de.javagl.jgltf.impl.v2.Node
import mu.KotlinLogging
import pro.leaco.gltf.buffer.Normals
import pro.leaco.gltf.buffer.Tangents
import pro.leaco.gltf.buffer.TextureCoordinates
import pro.leaco.gltf.buffer.TriangleIndices
import javax.vecmath.Point2f
import javax.vecmath.Vector3f

private val LOG = KotlinLogging.logger {}

/**
 * Build 3D Geometry from triangles or squares. Tangents, indices, and normals are automatically
 * generated.
 * @author Leaco
 */
open class TriangleBuilder(name: String) : TopologyBuilder(name, TopologyMode.TRIANGLES) {
    /** The indices keep track of connectivity between triangle vertices.  */
    protected val indices: TriangleIndices

    /** Suppress additions of normal vectors   */
    private var supressNormals = false

    /** Material for the mesh  */
    private var material: Material? = null

    /**
     * @param name Name of the glTF mesh node.
     */
    init {
        indices = TriangleIndices(name)
    }

    /**
     * Enable or disable suppression of normals.
     */
    fun supressNormals(isEnabled: Boolean) {
        supressNormals = isEnabled
    }

    /**
     * Set a Material that will be used when generating the mesh.
     * @param material Material from the GltfBuilder
     * @see GltfBuilder.newTextureMaterial
     */
    open fun withMaterial(material: Material): TriangleBuilder {
        this.material = material
        return this
    }


    /**
     * Add a 3D triangle specified by 3 vertices. All triangles should be added through this
     * method so that normals can be calculated.
     * @see TopologyBuilder.newVertex
     */
    fun addTriangle(vtx0: MeshVertex, vtx1: MeshVertex, vtx2: MeshVertex) {
        // add indices
        indices.add(vtx0.index, vtx1.index, vtx2.index)
        if (!supressNormals) {
            // calculate normal with cross product
            val vec01 = Vector3f()
            vec01.sub(vtx0.vertex, vtx1.vertex)
            val vec21 = Vector3f()
            vec21.sub(vtx2.vertex, vtx1.vertex)
            var normal = Vector3f()
            normal.cross(vec21, vec01)
            normal.normalize()
            if (java.lang.Float.isNaN(normal.x) || java.lang.Float.isNaN(normal.y) || java.lang.Float.isNaN(normal.z)) {
                LOG.debug("Could not calculate normal for triangle: {},{},{}",
                    vtx0.index, vtx1.index, vtx2.index)
                // create a fake normal
                normal = Vector3f(1f, 1f, 1f)
                normal.normalize()
            }

            // add this normal to each vertex
            vtx0.addNormal(normal)
            vtx1.addNormal(normal)
            vtx2.addNormal(normal)
        }
    }

    /**
     * Add a 3D square represented by 4 vertices specified counter clockwise.
     * All squares should be added though this method so that normals can be calculated.
     * @param vtx0 Start of square
     * @param vtx1 common to both triangles
     * @param vtx2 common to both triangles
     * @param vtx3 End of square
     * @see TopologyBuilder.newVertex
     */
    fun addSquare(vtx0: MeshVertex, vtx1: MeshVertex, vtx2: MeshVertex, vtx3: MeshVertex) {
        // We need to connect the points with counter-clockwise triangles.
        // Any triangles will do as long as the are CC.
        addTriangle(vtx0, vtx1, vtx2)
        addTriangle(vtx2, vtx1, vtx3)

        // calculate tangents
        val vec01 = Vector3f()
        vec01.sub(vtx0.vertex, vtx1.vertex)
        vtx0.addTangent(vec01)
        vtx1.addTangent(vec01)
        val vec23 = Vector3f()
        vec23.sub(vtx2.vertex, vtx3.vertex)
        vtx2.addTangent(vec23)
        vtx3.addTangent(vec23)
    }

    @Throws(Exception::class)
    override fun buildBuffers(geoWriter: GltfBuilder, meshPrimitive: MeshPrimitive) {
        super.buildBuffers(geoWriter, meshPrimitive)
        if (material != null) {
            val materialIdx: Int = geoWriter.gltf.getMaterials().indexOf(material)
            meshPrimitive.material = materialIdx
        }
        if (indices.size() === 0) {
            throw Exception("Mesh has no indices: " + this.name)
        }
        val textureCoordinates = TextureCoordinates(this.name)
        val normals = Normals(this.name)
        val tangents = Tangents(this.name)
        for (meshVertex in vertexList) {
            val texCoord: Point2f? = meshVertex.texCoord
            if (texCoord != null) {
                textureCoordinates.add(texCoord)
            }
            val normal: Vector3f? = meshVertex.normal
            if (normal != null) {
                normals.add(normal)
            }

            // leave out tangents for now.
            //this.tangents.add(meshVertex.getTangent());
        }
        if (normals.size() > 0 && normals.size() !== vertexList.size) {
            throw Exception("Each Vertex must have a normal")
        }
        if (textureCoordinates.size() > 0 && textureCoordinates.size() !== vertexList.size) {
            throw Exception("Each Vertex must have a texCoord")
        }

        // flush all buffers to the primitive
        indices.build(geoWriter, meshPrimitive)
        textureCoordinates.build(geoWriter, meshPrimitive)
        normals.build(geoWriter, meshPrimitive)
        tangents.build(geoWriter, meshPrimitive)
    }


}