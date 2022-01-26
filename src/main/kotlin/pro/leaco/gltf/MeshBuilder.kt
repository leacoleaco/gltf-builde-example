package pro.leaco.gltf

import de.javagl.jgltf.impl.v2.Material
import mu.KotlinLogging
import java.awt.Color
import javax.vecmath.Point2f
import javax.vecmath.Point3f
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val LOG = KotlinLogging.logger {}

/**
 * Generate 3D meshes for glTF based on 2D grid arrays
 * @author Leaco
 */
open class MeshBuilder(name: String) : TriangleBuilder(name) {
    companion object {

        /**
         * Helper function for interpolation between bounds returning a float.
         * @param max Maximum bound
         * @param part Number of parts
         * @param idx Part index
         * @return float representing position given by the index.
         */
        fun interpolateFloat(max: Int, part: Float, idx: Int): Float {
            return (idx * part / max)
        }

        /**
         * Helper function for interpolation between bounds returning an int.
         * @param max Maximum bound
         * @param part Number of parts
         * @param idx Part index
         * @return rounded integer representing position given by the index.
         */
        fun interpolateInt(max: Int, part: Float, idx: Int): Int {
            return (idx * part / max).roundToInt()
        }
    }

    override fun withMaterial(material: Material): MeshBuilder {
        super.withMaterial(material)
        return this
    }

    /**
     * Create a an elevated surface from a 2D array.
     * @param meshGrid 2D array containing vertices
     * @param isTextured Indicates if this mesh will have a texture
     */
    fun addPlane(meshGrid: Array<Array<MeshVertex>>, isTextured: Boolean): MeshBuilder {
        addGrid(meshGrid, isTextured, wrapY = false, wrapX = false)
        return this
    }

    /**
     * Create a an elevated surface from a 2D array.
     * @param isTextured Indicates if this mesh will have a texture
     */
    fun addPlane(isTextured: Boolean, meshGridSupplier: MeshBuilder.() -> Array<Array<MeshVertex>>): MeshBuilder {
        addPlane(meshGridSupplier.invoke(this), isTextured)
        return this
    }

    /**
     * Join the ends of a 2D surface along the y-axis to create a cylindrical shape as if
     * cut from a lathe.
     * @param meshGrid 2D array containing vertices
     * @param isTextured Indicates if this mesh will have a texture
     */
    fun addLathe(meshGrid: Array<Array<MeshVertex>>, isTextured: Boolean): MeshBuilder {
        addGrid(meshGrid, isTextured, wrapY = true, wrapX = false)
        return this
    }

    /**
     * Join the ends of a 2D surface along the y-axis to create a cylindrical shape as if
     * cut from a lathe.
     * @param meshGridSupplier 2D array containing vertices
     * @param isTextured Indicates if this mesh will have a texture
     */
    fun addLathe(isTextured: Boolean, meshGridSupplier: MeshBuilder.() -> Array<Array<MeshVertex>>): MeshBuilder {
        addLathe(meshGridSupplier.invoke(this), isTextured)
        return this
    }


    /**
     * Join the ends of a 2D surface along the x-axis and y-axis to create a closed manifold.
     * @param meshGrid 2D array containing vertices
     * @param isTextured Indicates if this mesh will have a texture
     */
    fun addManifold(meshGrid: Array<Array<MeshVertex>>, isTextured: Boolean): MeshBuilder {
        addGrid(meshGrid, isTextured, wrapY = true, wrapX = true)
        return this
    }

    /**
     * Join the ends of a 2D surface along the y-axis to create a cylindrical shape as if
     * cut from a lathe.
     * @param meshGridSupplier 2D array containing vertices
     * @param isTextured Indicates if this mesh will have a texture
     */
    fun addManifold(isTextured: Boolean, meshGridSupplier: MeshBuilder.() -> Array<Array<MeshVertex>>): MeshBuilder {
        addManifold(meshGridSupplier.invoke(this), isTextured)
        return this
    }

    /**
     * Convert a 2D vertex array into a mesh. If null vertex values are encountered then related
     * parts of the mesh will not be generated.
     * @param meshGridProvider 2D Vertex array that represents the mesh.
     * @param isTextured Indicates if texture coordinates should be generated.
     * @param wrapY Wrap the mesh about the Y axis.
     * @param wrapX Wrap the mesh about the X axis.
     */
    fun addGrid(
        isTextured: Boolean,
        wrapY: Boolean,
        wrapX: Boolean,
        meshGridProvider: MeshBuilder.() -> Array<Array<MeshVertex>>,
    ): MeshBuilder {
        return addGrid(meshGridProvider.invoke(this), isTextured, wrapY, wrapX)
    }

    /**
     * Convert a 2D vertex array into a mesh. If null vertex values are encountered then related
     * parts of the mesh will not be generated.
     * @param meshGrid 2D Vertex array that represents the mesh.
     * @param isTextured Indicates if texture coordinates should be generated.
     * @param wrapY Wrap the mesh about the Y axis.
     * @param wrapX Wrap the mesh about the X axis.
     */
    fun addGrid(
        meshGrid: Array<Array<MeshVertex>>,
        isTextured: Boolean,
        wrapY: Boolean,
        wrapX: Boolean,
    ): MeshBuilder {
        val xGridSize = meshGrid.size
        val yGridSize = meshGrid[0].size
        LOG.debug("Render grid: mesh=<{}> grid=<{}x{}>, wrapXY<{},{}> isTextured=<{}>",
            this.name,
            xGridSize,
            yGridSize,
            wrapX,
            wrapY,
            isTextured)

        if (isTextured) {
            // At this point the mesh is complete with correct normals except that we have no
            // texture coordinates.
            // If the mesh is textured and wrapped then we have a problem where we need to extend 
            // it so that there are separate points for the start and end vertices. If there is no 
            // wrapping then the extra points are not necessary.
            //
            // Note: It took a lot of time to determine the correct approach capable of handling 
            // a manifold that can be textured and wrapped on either axis independently. Many 
            // alternatives were tried before arriving at this relatively simple and correct method.

            // here we clear the vertices while preserving the normals. We want to keep the normals
            // and regenerate the grid.
            indices.clear()


            // create a new grid and extend it by a row or column if it is wrapped.
            var xTexSize = xGridSize
            if (wrapX) {
                xTexSize += 1
            }
            var yTexSize = yGridSize
            if (wrapY) {
                yTexSize += 1
            }
            val texGrid = (0 until xTexSize).map { xGridIdx ->
                val uPos = interpolateFloat((xTexSize - 1), 1.0f, xGridIdx)
                (0 until yTexSize).map { yGridIdx ->
                    val vertex: MeshVertex
                    if (xGridIdx >= xGridSize || yGridIdx >= yGridSize) {
                        // We are in the expanded zone so wrap back to the beginning if necessary.
                        var xIdxWrap = xGridIdx
                        if (xIdxWrap >= xGridSize) {
                            xIdxWrap = 0
                        }
                        var yIdxWrap = yGridIdx
                        if (yIdxWrap >= yGridSize) {
                            yIdxWrap = 0
                        }

                        // We copy the vertex because start and end points should overlap.
                        vertex = copyVertex(meshGrid[xIdxWrap][yIdxWrap])
                    } else {
                        // if not in the expanded zone then use vertex from the original grid.
                        vertex = meshGrid[xGridIdx][yGridIdx]
                    }
                    val vPos = interpolateFloat(yTexSize - 1, 1.0f, yGridIdx)
                    vertex.texCoord = Point2f(uPos, vPos)

                    // Assign the vertex to the texture grid.
                    vertex
                }.toTypedArray()
            }.toTypedArray()

            // Suppress generation of normals because we want to use the normals from the original
            // grid. We render the mesh with no wrapping because the start and end points overlap.
            supressNormals(true)
            renderMesh(texGrid, wrapY = false, wrapX = false)
            supressNormals(false)
        } else {
            // generate geometry and add normals for non-textured manifold.
            renderMesh(meshGrid, wrapY, wrapX)
        }
        return this
    }

    /**
     * Iterate through the provided grid generating squares where possible.
     * @param meshGrid
     * @param wrapY
     * @param wrapX
     */
    private fun renderMesh(meshGrid: Array<Array<MeshVertex>>, wrapY: Boolean, wrapX: Boolean) {
        val xGridSize = meshGrid.size
        val yGridSize = meshGrid[0].size

        // For every 4-vertex square in the mesh we call addSquare(). These squares overlap
        // which is necessary for the calculation of normals and tangents.
        for (xGridIdx in (if (wrapX) 0 else 1) until xGridSize) {
            for (yGridIdx in (if (wrapY) 0 else 1) until yGridSize) {

                // wrap around the y axis
                var yGridPrev: Int = yGridIdx - 1
                if (yGridPrev < 0) {
                    yGridPrev = yGridSize - 1
                }

                // wrap around the X axis
                var xGridPrev: Int = xGridIdx - 1
                if (xGridPrev < 0) {
                    xGridPrev = xGridSize - 1
                }
                val vtx10 = meshGrid[xGridPrev][yGridIdx]
                val vtx11 = meshGrid[xGridPrev][yGridPrev]
                val vtx00 = meshGrid[xGridIdx][yGridIdx]
                val vtx01 = meshGrid[xGridIdx][yGridPrev]

                addSquare(vtx11, vtx10, vtx01, vtx00)
            }
        }
    }

    /**
     * Add a 3D cylinder oriented in XZ.
     * @param position Base of the cylinder
     * @param radius Cylinder radius
     * @param height Cylinder height
     * @param sides Number of vertices for the sides
     * @param color Cylinder color
     */
    fun addCylinderMeshXZ(
        position: Point3f, radius: Float, height: Float, sides: Int, color: Color?,
    ): MeshBuilder {
        val bottomPos = Point3f(position)
        bottomPos.sub(Point3f(0f, height, 0f))

        // add cylinder
        val cylinderGrid: Array<Array<MeshVertex>> =
            arrayOf(newCircleVerticesXZ(position, radius, sides, color),
                newCircleVerticesXZ(bottomPos, radius, sides, color))
        addLathe(cylinderGrid, false)

        // add top and bottom
        addDiscXZ(position, radius, sides, color)
        addDiscXZ(bottomPos, -1 * radius, sides, color)
        return this
    }

    /**
     * Add a solid disc oriented in XZ.
     * @param position Center of the disc
     * @param radius Disc radius
     * @param sides Number of vertices for the sides
     * @param color Disc color
     */
    fun addDiscXZ(position: Point3f, radius: Float, sides: Int, color: Color?): MeshBuilder {
        // add center point
        val centerVtx = newVertex(position)
        centerVtx.color = color

        // create vertices for the boundary
        val discVertices = newCircleVerticesXZ(position, radius, sides, color)

        // add triangles to fill
        for (rIdx in 1 until sides) {
            val curVtx = discVertices[rIdx]
            val lastVtx = discVertices[rIdx - 1]
            addTriangle(curVtx, lastVtx, centerVtx)
        }
        addTriangle(discVertices[0], discVertices[sides - 1], centerVtx)
        return this
    }


}

/**
 * Generate a vertex array for a circle oriented in XZ.
 * @param position Location of the center of the circle
 * @param radius Radius of the circle
 * @param sides Number of sides
 * @param color Color of the vertices
 */
fun MeshBuilder.newCircleVerticesXZ(
    position: Point3f, radius: Float, sides: Int,
    color: Color?,
): Array<MeshVertex> {
    var flip = 1f
    var finalRadius = radius
    if (radius < 0) {
        // flip orientation for triangle culling
        flip *= -1f
        finalRadius *= -1f
    }
    // draw a radial section
    return (0 until sides).map { rIdx ->
        val angle = 2 * Math.PI * rIdx * flip / sides
        val xPos = (sin(angle) * finalRadius + position.x).toFloat()
        val yPos = (cos(angle) * finalRadius + position.z).toFloat()
        val vertex = newVertex(Point3f(xPos, position.y, yPos))
        vertex.color = color
        return@map vertex
    }.toTypedArray()
}