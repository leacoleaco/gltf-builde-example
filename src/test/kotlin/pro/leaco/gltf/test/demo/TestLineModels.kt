package pro.leaco.gltf.test.demo

import de.javagl.jgltf.impl.v2.Mesh
import de.javagl.jgltf.impl.v2.Node
import pro.leaco.gltf.GltfBuilder
import org.junit.Test
import org.slf4j.LoggerFactory
import pro.leaco.gltf.MeshVertex
import pro.leaco.gltf.TopologyBuilder
import java.awt.Color
import java.io.File
import java.lang.Exception
import javax.vecmath.Point3f

class TestLineModels {
    private val geoWriter: GltfBuilder = GltfBuilder()

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
            val radius = Math.sin(angleTheta).toFloat().toDouble()
            val xPos = (radius * Math.cos(anglePhi)).toFloat()
            val yPos = (radius * Math.sin(anglePhi)).toFloat()
            val zPos = Math.cos(angleTheta).toFloat()
            val point = Point3f(xPos, yPos, zPos)
            val vertex: MeshVertex = meshBuilder.newVertex(point)
            val color: Color = Color.getHSBColor(part.toFloat(), 0.6f, 0.5f)
            vertex.color = color
        }
        val node: Node = meshBuilder.build(geoWriter)

        // example of adding custom data to MeshPrimitive
        val meshIdx = node.mesh
        val mesh: Mesh = geoWriter.gltf.meshes[meshIdx]
        val primitive = mesh.primitives[0]
        primitive.extras = arrayOf("some", "additional", "data")
        val outFile: File = TestShapeModels.Companion.getFile(meshBuilder.name)
        geoWriter.writeGltf(outFile)
        LOG.info("Finished generating: {}", outFile)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TestLineModels::class.java)
    }
}