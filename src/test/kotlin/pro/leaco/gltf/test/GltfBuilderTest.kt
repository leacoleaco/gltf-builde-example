package pro.leaco.gltf.test

import org.junit.Test
import pro.leaco.gltf.GltfBuilder
import java.awt.Desktop
import java.io.File

class GltfBuilderTest {

    @Test
    fun testBuild() {
        val outFile = File("E:\\testModel\\sample.gltf")
        GltfBuilder()
            .writeGltf(outFile)

        Desktop.getDesktop().open(outFile.parentFile)
    }
}