package pro.leaco.gltf.test.demo

import de.javagl.jgltf.impl.v2.Material
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pro.leaco.gltf.GltfBuilder
import pro.leaco.gltf.MeshBuilder
import pro.leaco.gltf.MeshVertex
import pro.leaco.gltf.newCircleVerticesXZ
import java.awt.Color
import java.io.File
import java.nio.file.Paths
import javax.vecmath.Point3f
import kotlin.math.exp

class TestShapeModels {
    private val geoWriter: GltfBuilder = GltfBuilder()

    @Before
    fun setup() {
        // set the path where the texture files will be found
        geoWriter.withBasePath(File("src/test/resources"))
    }

    /**
     * Create a plane with the function y = 2*x*exp(-(x^2 + y^2)) using addPlane().
     * @see MeshBuilder.addPlane
     */
    @Test
    @Throws(Exception::class)
    fun testPlane() {
        // Set rendering for both sides of the plane
        geoWriter.alphaMode = GltfBuilder.AlphaMode.OPAQUE_DS
        val meshBuilder = MeshBuilder("test_plane")
        val material: Material = geoWriter.newTextureMaterial(TEST_TEXTURE_PNG)
        meshBuilder.withMaterial(material)

        // size of grid
        val length = 30

        // size of coordinates
        val coordLength = 4f

        // render the vertices in the grid
        meshBuilder.addPlane(true) {
            (0 until length).map { xIdx ->
                // interpolate to within the range [-2,2]
                val xPos: Float = MeshBuilder.interpolateFloat(length, coordLength, xIdx) - coordLength / 2f
                (0 until length).map { yIdx ->
                    // interpolate to within the range [-2,2]
                    val zPos: Float = MeshBuilder.interpolateFloat(length, coordLength, yIdx) - coordLength / 2f

                    // calculate the function 2*x*exp(-(x^2 + y^2))
                    val yPos = (2 * xPos * exp((-1 * (xPos * xPos + zPos * zPos)).toDouble())).toFloat()

                    // add the point in the mesh
                    val point = Point3f(-1 * xPos, yPos, zPos)
                    meshBuilder.newVertex(point)
                }.toTypedArray()
            }.toTypedArray()
        }

        // build the gltf buffers
        meshBuilder.build(geoWriter)
        val outFile = getFile(meshBuilder.name)
        geoWriter.writeGltf(outFile)
        LOG.info("Finished generating: {}", outFile)
    }

    /**
     * Generate a rainbow colored diamond shape using addLathe().
     * @see MeshBuilder.addLathe
     */
    @Test
    @Throws(Exception::class)
    fun testDiamond() {
        val meshBuilder = MeshBuilder("test_diamond")
        val material: Material = geoWriter.newDefaultMaterial()
        meshBuilder.withMaterial(material)

        // number of sides around the tube
        val sides = 12

        // radiuses along the tube
        val radiusList = floatArrayOf(0.2f, 1f, 0.2f)

        // y positions for the tube.
        val yPosList = floatArrayOf(1f, 0f, -1f)
        val meshGrid: Array<Array<MeshVertex>> = yPosList.indices.map { yIdx ->
            val yPos = yPosList[yIdx]
            val radius = radiusList[yIdx]
            // origin of the circle
            val circlePos = Point3f(0f, yPos, 0f)
            // create a rainbow effect along the y axis
            val color: Color = Color.getHSBColor(yIdx.toFloat() / yPosList.size.toFloat(), 0.9f, 1.0f)
            // add a circle that is part of the tube
            return@map meshBuilder.newCircleVerticesXZ(circlePos, radius, sides, color)
        }.toTypedArray()


        // join the ends of the surface to create a tube
        meshBuilder.addLathe(meshGrid, false)

        // generate gltf buffers
        meshBuilder.build(geoWriter)
        val outFile = getFile(meshBuilder.name)
        geoWriter.writeGltf(outFile)
        LOG.info("Finished generating: {}", outFile)
    }


    companion object {
        private val LOG = LoggerFactory.getLogger(TestShapeModels::class.java)
        private const val TEST_TEXTURE_PNG = "uv_grid_512.png"
        private const val OUT_PATH = "./demo"
        fun getFile(name: String): File {
            val outFile: File = Paths.get(OUT_PATH, "$name.gltf").toFile()
            outFile.parentFile.mkdirs()
            return outFile
        }
    }
}