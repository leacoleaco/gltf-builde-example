package pro.leaco.gltf

import com.ai.core.math.topology.GeometryMaker
import de.javagl.jgltf.impl.v2.Material
import org.junit.Test
import pro.leaco.gltf.demo.TestShapeModels
import java.io.File

class MeshBuilderExtKtTest {

    @Test
    fun testAddGeometryPlane() {

        val builder = GltfBuilder()
            // set the path used for finding textures
            .withBasePath(File("src/test/resources"))

        // create materials for each of the textures
        val kineticaMaterial: Material = builder.newTextureMaterial("kinetica_logo.png")
        val gltfMaterial: Material = builder.newTextureMaterial("gltf_logo.png")
        val uvGridMaterial: Material = builder.newTextureMaterial("uv_grid_512.png")

        val geom =
            GeometryMaker.parse("POLYGON ((90 370, 300 370, 300 120, 90 120, 90 370), \n" + "  (180 290, 150 200, 250 200, 185 226, 180 290))")

        builder.buildNodeWith(MeshBuilder("face-XY1").withMaterial(kineticaMaterial).addGeometryPlane(geom, true))

        val outFile: File = TestShapeModels.getFile("test_geom")
        builder.writeGltf(outFile)

    }

}