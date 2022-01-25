package pro.leaco.gltf

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


        builder.buildNodeWith(
            MeshBuilder("face-XY1")
                .withMaterial(kineticaMaterial)
                .addGeometryPlane(true)
        )


        val outFile: File = TestShapeModels.getFile("test_geom")
        builder.writeGltf(outFile)

    }

}