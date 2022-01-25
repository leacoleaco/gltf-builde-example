package pro.leaco.gltf.test.demo

import de.javagl.jgltf.impl.v2.Mesh
import de.javagl.jgltf.impl.v2.Node
import pro.leaco.gltf.GltfBuilder
import org.junit.Test
import org.slf4j.LoggerFactory
import pro.leaco.gltf.MeshVertex
import pro.leaco.gltf.TopologyBuilder
import pro.leaco.gltf.buildNodeWith
import java.awt.Color
import java.io.File
import java.lang.Exception
import javax.vecmath.Point3f
import kotlin.math.cos
import kotlin.math.sin

class TestLineModels {
    private val builder: GltfBuilder = GltfBuilder()

    /**
     * Draw a sphere using the line strip topology.
     * @see TopologyMode
     */
    @Test
    @Throws(Exception::class)
    fun testLineStrip() {
        val meshBuilder = TopologyBuilder("test_line_strip", TopologyBuilder.TopologyMode.LINE_STRIP)
        val rPoints = 1000
        val rotations = 40
        for (rIdx in 0..rPoints) {
            val part = rIdx.toDouble() / rPoints.toDouble()

            // spherical coordinates
            val anglePhi = part * rotations * Math.PI
            val angleTheta = part * Math.PI

            // convert to Cartesian
            val radius = sin(angleTheta).toFloat().toDouble()
            val xPos = (radius * cos(anglePhi)).toFloat()
            val yPos = (radius * sin(anglePhi)).toFloat()
            val zPos = cos(angleTheta).toFloat()
            val point = Point3f(xPos, yPos, zPos)
            val vertex: MeshVertex = meshBuilder.newVertex(point)
            val color: Color = Color.getHSBColor(part.toFloat(), 0.6f, 0.5f)
            vertex.color = color
        }
        val node = builder.buildNodeWith(meshBuilder)

        // example of adding custom data to MeshPrimitive
        val meshIdx = node.mesh
        val mesh: Mesh = builder.gltf.meshes[meshIdx]
        val primitive = mesh.primitives[0]
        primitive.extras = arrayOf("some", "additional", "data")
        val outFile: File = TestShapeModels.Companion.getFile(meshBuilder.name)
        builder.writeGltf(outFile)
        LOG.info("Finished generating: {}", outFile)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TestLineModels::class.java)
    }
}