package pro.leaco.gltf.demo

import de.javagl.jgltf.impl.v2.Material
import pro.leaco.gltf.GltfBuilder
import org.junit.Test
import org.slf4j.LoggerFactory
import pro.leaco.gltf.MeshBuilder
import pro.leaco.gltf.MeshVertex
import pro.leaco.gltf.buildNodeWith
import java.io.File
import java.lang.Exception
import javax.vecmath.Point2f
import javax.vecmath.Point3f

class TestCubeModel {
    /**
     * Add a cube made of 6 textured meshes.
     * @see MeshBuilder.addPlane
     */
    @Test
    @Throws(Exception::class)
    fun testCube() {
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
                .addPlane(true) {
                    arrayOf(
                        arrayOf(
                            newVertex(Point3f(1f, 1f, 0f)),
                            newVertex(Point3f(1f, 0f, 0f))
                        ),
                        arrayOf(
                            newVertex(Point3f(0f, 1f, 0f)),
                            newVertex(Point3f(0f, 0f, 0f))
                        )
                    )
                }
        )


        builder.buildNodeWith(
            MeshBuilder("face-XY2")
                .withMaterial(kineticaMaterial)
                .addPlane(true) {
                    arrayOf(
                        arrayOf(
                            newVertex(Point3f(0f, 1f, -1f)),
                            newVertex(Point3f(0f, 0f, -1f)),
                        ),
                        arrayOf(
                            newVertex(Point3f(1f, 1f, -1f)),
                            newVertex(Point3f(1f, 0f, -1f)),
                        )
                    )
                }
        )


        builder.buildNodeWith(
            MeshBuilder("face-YZ1")
                .withMaterial(gltfMaterial)
                .addPlane(true) {
                    arrayOf(
                        arrayOf(
                            newVertex(Point3f(0f, 1f, 0f)),
                            newVertex(Point3f(0f, 0f, 0f)),
                        ),
                        arrayOf(
                            newVertex(Point3f(0f, 1f, -1f)),
                            newVertex(Point3f(0f, 0f, -1f)),
                        )
                    )
                }
        )



        builder.buildNodeWith(
            MeshBuilder("face-YZ2")
                .withMaterial(gltfMaterial)
                .addPlane(true) {
                    arrayOf(
                        arrayOf(
                            newVertex(Point3f(1f, 1f, -1f)),
                            newVertex(Point3f(1f, 0f, -1f)),
                        ),
                        arrayOf(
                            newVertex(Point3f(1f, 1f, 0f)),
                            newVertex(Point3f(1f, 0f, 0f)),
                        )
                    )
                }
        )




        builder.buildNodeWith(
            MeshBuilder("face-Top")
                .withMaterial(uvGridMaterial)
                .addPlane(true) {
                    arrayOf(
                        arrayOf(
                            newVertex(Point3f(1f, 1f, 0f)),
                            newVertex(Point3f(0f, 1f, 0f)),
                        ),
                        arrayOf(
                            newVertex(Point3f(1f, 1f, -1f)),
                            newVertex(Point3f(0f, 1f, -1f)),
                        )
                    )
                }
        )


        builder.buildNodeWith(
            MeshBuilder("face-Bottom")
                .withMaterial(uvGridMaterial)
                .addPlane(true) {
                    arrayOf(
                        arrayOf(
                            newVertex(Point3f(1f, 0f, -1f)),
                            newVertex(Point3f(0f, 0f, -1f)),
                        ),
                        arrayOf(
                            newVertex(Point3f(1f, 0f, 0f)),
                            newVertex(Point3f(0f, 0f, 0f)),
                        )
                    )
                }
        )


        val outFile: File = TestShapeModels.getFile("test_cube")
        builder.writeGltf(outFile)
        LOG.info("Finished generating: {}", outFile)
    }

    /**
     * Add a cube where texture coordinates are manually calculated.
     * @see TriangleBuilder.addSquare
     */
    @Test
    @Throws(Exception::class)
    fun testCubeOrig() {
        val geoWriter = GltfBuilder()

        // set the path used for finding textures
        geoWriter.withBasePath(File("src/test/resources"))

        // create materials for each of the textures
        val kineticaMaterial: Material = geoWriter.newTextureMaterial("kinetica_logo.png")
        val gltfMaterial: Material = geoWriter.newTextureMaterial("gltf_logo.png")
        val uvGridMaterial: Material = geoWriter.newTextureMaterial("uv_grid_512.png")
        var meshBuilder: MeshBuilder? = null
        meshBuilder = MeshBuilder("face-XY1")
        val vertexXY1_0: MeshVertex = meshBuilder.newVertex(Point3f(0f, 0f, 0f))
        val vertexXY1_1: MeshVertex = meshBuilder.newVertex(Point3f(1f, 0f, 0f))
        val vertexXY1_2: MeshVertex = meshBuilder.newVertex(Point3f(0f, 1f, 0f))
        val vertexXY1_3: MeshVertex = meshBuilder.newVertex(Point3f(1f, 1f, 0f))
        meshBuilder.addSquare(vertexXY1_1, vertexXY1_0, vertexXY1_3, vertexXY1_2)
        vertexXY1_0.textureCoordinate = Point2f(1f, 1f)
        vertexXY1_1.textureCoordinate = Point2f(0f, 1f)
        vertexXY1_2.textureCoordinate = Point2f(1f, 0f)
        vertexXY1_3.textureCoordinate = Point2f(0f, 0f)
        meshBuilder.withMaterial(kineticaMaterial)
        meshBuilder.build(geoWriter)
        meshBuilder = MeshBuilder("face-XY2")
        val vertexXY2_0: MeshVertex = meshBuilder.newVertex(Point3f(1f, 0f, -1f))
        val vertexXY2_1: MeshVertex = meshBuilder.newVertex(Point3f(0f, 0f, -1f))
        val vertexXY2_2: MeshVertex = meshBuilder.newVertex(Point3f(1f, 1f, -1f))
        val vertexXY2_3: MeshVertex = meshBuilder.newVertex(Point3f(0f, 1f, -1f))
        meshBuilder.addSquare(vertexXY2_1, vertexXY2_0, vertexXY2_3, vertexXY2_2)
        vertexXY2_0.textureCoordinate = Point2f(1f, 1f)
        vertexXY2_1.textureCoordinate = Point2f(0f, 1f)
        vertexXY2_2.textureCoordinate = Point2f(1f, 0f)
        vertexXY2_3.textureCoordinate = Point2f(0f, 0f)
        meshBuilder.withMaterial(kineticaMaterial)
        meshBuilder.build(geoWriter)
        meshBuilder = MeshBuilder("face-YZ1")
        val vertexYZ1_0: MeshVertex = meshBuilder.newVertex(Point3f(0f, 0f, -1f))
        val vertexYZ1_1: MeshVertex = meshBuilder.newVertex(Point3f(0f, 0f, 0f))
        val vertexYZ1_2: MeshVertex = meshBuilder.newVertex(Point3f(0f, 1f, -1f))
        val vertexYZ1_3: MeshVertex = meshBuilder.newVertex(Point3f(0f, 1f, 0f))
        meshBuilder.addSquare(vertexYZ1_1, vertexYZ1_0, vertexYZ1_3, vertexYZ1_2)
        vertexYZ1_0.textureCoordinate = Point2f(1f, 1f)
        vertexYZ1_1.textureCoordinate = Point2f(0f, 1f)
        vertexYZ1_2.textureCoordinate = Point2f(1f, 0f)
        vertexYZ1_3.textureCoordinate = Point2f(0f, 0f)
        meshBuilder.withMaterial(gltfMaterial)
        meshBuilder.build(geoWriter)
        meshBuilder = MeshBuilder("face-YZ2")
        val vertexYZ2_0: MeshVertex = meshBuilder.newVertex(Point3f(1f, 0f, 0f))
        val vertexYZ2_1: MeshVertex = meshBuilder.newVertex(Point3f(1f, 0f, -1f))
        val vertexYZ2_2: MeshVertex = meshBuilder.newVertex(Point3f(1f, 1f, 0f))
        val vertexYZ2_3: MeshVertex = meshBuilder.newVertex(Point3f(1f, 1f, -1f))
        meshBuilder.addSquare(vertexYZ2_1, vertexYZ2_0, vertexYZ2_3, vertexYZ2_2)
        vertexYZ2_0.textureCoordinate = Point2f(1f, 1f)
        vertexYZ2_1.textureCoordinate = Point2f(0f, 1f)
        vertexYZ2_2.textureCoordinate = Point2f(1f, 0f)
        vertexYZ2_3.textureCoordinate = Point2f(0f, 0f)
        meshBuilder.withMaterial(gltfMaterial)
        meshBuilder.build(geoWriter)
        meshBuilder = MeshBuilder("face-Top")
        val vertexTop_0: MeshVertex = meshBuilder.newVertex(Point3f(0f, 1f, 0f))
        val vertexTop_1: MeshVertex = meshBuilder.newVertex(Point3f(1f, 1f, 0f))
        val vertexTop_2: MeshVertex = meshBuilder.newVertex(Point3f(0f, 1f, -1f))
        val vertexTop_3: MeshVertex = meshBuilder.newVertex(Point3f(1f, 1f, -1f))
        meshBuilder.addSquare(vertexTop_1, vertexTop_0, vertexTop_3, vertexTop_2)
        vertexTop_0.textureCoordinate = Point2f(1f, 1f)
        vertexTop_1.textureCoordinate = Point2f(0f, 1f)
        vertexTop_2.textureCoordinate = Point2f(1f, 0f)
        vertexTop_3.textureCoordinate = Point2f(0f, 0f)
        meshBuilder.withMaterial(uvGridMaterial)
        meshBuilder.build(geoWriter)
        meshBuilder = MeshBuilder("face-Bottom")
        val vertexBottom_0: MeshVertex = meshBuilder.newVertex(Point3f(0f, 0f, 0f))
        val vertexBottom_1: MeshVertex = meshBuilder.newVertex(Point3f(0f, 0f, -1f))
        val vertexBottom_2: MeshVertex = meshBuilder.newVertex(Point3f(1f, 0f, 0f))
        val vertexBottom_3: MeshVertex = meshBuilder.newVertex(Point3f(1f, 0f, -1f))
        meshBuilder.addSquare(vertexBottom_1, vertexBottom_0, vertexBottom_3, vertexBottom_2)
        vertexBottom_0.textureCoordinate = Point2f(1f, 1f)
        vertexBottom_1.textureCoordinate = Point2f(0f, 1f)
        vertexBottom_2.textureCoordinate = Point2f(1f, 0f)
        vertexBottom_3.textureCoordinate = Point2f(0f, 0f)
        meshBuilder.withMaterial(uvGridMaterial)
        meshBuilder.build(geoWriter)
        val outFile: File = TestShapeModels.getFile("test_cube2")
        geoWriter.writeGltf(outFile)
        LOG.info("Finished generating: {}", outFile)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TestCubeModel::class.java)
    }
}