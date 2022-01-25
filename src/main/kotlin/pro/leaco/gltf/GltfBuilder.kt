package pro.leaco.gltf

import de.javagl.jgltf.impl.v2.*
import de.javagl.jgltf.model.io.Buffers
import de.javagl.jgltf.model.io.GltfModelWriter
import de.javagl.jgltf.model.io.GltfReference
import de.javagl.jgltf.model.io.GltfReferenceResolver
import de.javagl.jgltf.model.io.v2.GltfAssetV2
import de.javagl.jgltf.model.v2.GltfModelV2
import java.io.File
import java.io.OutputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.util.*

/**
 * Serialize added nodes to glTF format.
 * @author Leaco
 */
class GltfBuilder {
    /**
     * The AlphaMode is used when creating a Material.
     */
    enum class AlphaMode {
        /** Mesh is opaque but invisible from one size.  */
        OPAQUE,

        /** Mesh is opaque and double sided.  */
        OPAQUE_DS,

        /** Texture contains a mask that makes certain sections transparent.  */
        MASK,

        /** Texture alpha channel is used to blend regions with the background.  */
        BLEND
    }

    /**
     * Indicates if the gltf metadata should be JSON or binary.
     */
    enum class GltfFormat {
        GLTF, GLB
    }

    companion object {
        private const val FILTERLINEAR = 9729
        private const val WRAPCLAMPTOEDGE = 33071
        private const val MAXBUFFERSIZE = 50 * 1024 * 1024
    }

    /**
     * Buffer used for primitive serialization.  */
    val buffer: ByteBuffer = Buffers.create(MAXBUFFERSIZE)

    /**
     * Get the GlTF used for writing metadata.
     */
    val gltf = GlTF()

    /** Contains metadata for the glTF Asset type  */
    private val metaParams: MutableMap<String, Any> = TreeMap<String, Any>()

    /** The one and only Scene.  */
    private val topScene = Scene()

    /** Alpha mode used for creating materials.  */
    var alphaMode = AlphaMode.OPAQUE

    /** Path for finding texture files.  */
    private var basePath = "."

    /** Copyright for glTF Asset type  */
    private var copyright = ""

    private val generator = "GltfBuilder"

    init {
        gltf.addScenes(topScene)
    }

    /**
     * Set path for resolving images.
     * finding textures etc.
     */
    fun withBasePath(path: File): GltfBuilder {
        basePath = path.path
        return this
    }

    /**
     * Set extra metadata in the glTF Asset.
     */
    fun withMetaParam(key: String, value: Any): GltfBuilder {
        metaParams[key] = value
        return this
    }

    /**
     * Set the copyright in the glTF Asset.
     */
    fun withCopyright(value: String): GltfBuilder {
        copyright = value
        return this
    }

    /**
     * Add a node to the default Scene. This is the only way this class supports adding of
     * geometry.
     */
    fun addNode(node: Node) {
        gltf.addNodes(node)
        val gltfList = gltf.nodes
        topScene.addNodes(gltfList.indexOf(node))
    }

    /**
     * Create a default material. You would use this if you are not using a texture and you
     * are specifying vertex colors.
     */
    fun newDefaultMaterial(name: String = "default"): Material {
        val material = newMaterial()
        material.name = name
        return material
    }

    /**
     * Add a material with optional texture.
     * @param imageFile The image to use for the texture or null if none.
     */
    fun newTextureMaterial(imageFile: String?): Material {
        val material = newMaterial()
        val sampler = Sampler()
        gltf.addSamplers(sampler)
        sampler.magFilter = FILTERLINEAR
        sampler.minFilter = FILTERLINEAR
        sampler.wrapS = WRAPCLAMPTOEDGE
        sampler.wrapT = WRAPCLAMPTOEDGE
        val image = Image()
        gltf.addImages(image)
        image.name = imageFile
        image.uri = imageFile
        val texture = Texture()
        gltf.addTextures(texture)
        texture.sampler = gltf.samplers.indexOf(sampler)
        texture.source = gltf.images.indexOf(image)

        val texInfo = TextureInfo()
        texInfo.index = gltf.textures.indexOf(texture)

        val roughness: MaterialPbrMetallicRoughness = material.pbrMetallicRoughness
        roughness.baseColorTexture = texInfo
        material.name = imageFile

        return material
    }

    private fun newMaterial(): Material {
        val material = Material()
        gltf.addMaterials(material)
        val roughness = MaterialPbrMetallicRoughness()
        material.pbrMetallicRoughness = roughness
        roughness.metallicFactor = 0.05f
        roughness.roughnessFactor = 0.5f
        when (alphaMode) {
            AlphaMode.OPAQUE -> material.alphaMode = "OPAQUE"
            AlphaMode.OPAQUE_DS -> {
                material.alphaMode = "OPAQUE"
                material.isDoubleSided = true
            }
            AlphaMode.MASK -> {
                material.alphaMode = "MASK"
                material.alphaCutoff = 0.5f
                material.isDoubleSided = true
            }
            AlphaMode.BLEND -> {
                material.alphaMode = "BLEND"
                material.isDoubleSided = true
                roughness.setBaseColorFactor(floatArrayOf(1f, 1f, 1f, 1f))
            }
        }
        return material
    }

    /**
     * Write gltf to an OutputStream. Specify gltf or glb format.
     * @param format Indicates if this is JSON or binary format.
     */
    @Throws(Exception::class)
    fun writeGltf(os: OutputStream?, format: GltfFormat) {
        val gltfModel: GltfModelV2 = gltfModel
        val gltfModelWriter = GltfModelWriter()
        when (format) {
            GltfFormat.GLTF -> {
                gltfModelWriter.writeEmbedded(gltfModel, os)
            }
            GltfFormat.GLB -> {
                gltfModelWriter.writeBinary(gltfModel, os)
            }
        }
    }

    /**
     * Write a gltf to a file. The filename should have a gltf or glb extension to indicate
     * the type.
     */
    @Throws(Exception::class)
    fun writeGltf(outFile: File) {
        val gltfModelV2: GltfModelV2 = gltfModel
        val gltfModelWriter = GltfModelWriter()
        val ext: String = outFile.extension

        val parentFile = outFile.parentFile
        parentFile?.mkdirs()

        when (GltfFormat.valueOf(ext.toUpperCase())) {
            GltfFormat.GLTF -> {
                gltfModelWriter.writeEmbedded(gltfModelV2, outFile)
            }
            GltfFormat.GLB -> {
                gltfModelWriter.writeBinary(gltfModelV2, outFile)
            }
        }
    }

    @get:Throws(Exception::class)
    private val gltfModel: GltfModelV2
        get() {
            val gltfAsset: GltfAssetV2 = newGltfAsset()
            return GltfModelV2(gltfAsset)
        }

    @Throws(Exception::class)
    private fun newGltfAsset(): GltfAssetV2 {
        val asset = Asset()
        gltf.asset = asset
        asset.version = "2.0"
        asset.generator = generator
        asset.copyright = copyright
        asset.extras = metaParams

        // flip the buffer for read
        buffer.flip()
        val totalSize = buffer.remaining()
        if (totalSize <= 0) {
            throw Exception("glTF buffer has no data to write.")
        }

        // add buffer to glTF
        val gltfBuffer = Buffer()
        gltf.addBuffers(gltfBuffer)
        gltfBuffer.byteLength = totalSize

        val gltfAsset = GltfAssetV2(gltf, buffer)
        resolveImages(gltfAsset)
        return gltfAsset
    }

    private fun resolveImages(gltfAsset: GltfAssetV2) {
        val refList: List<GltfReference> = gltfAsset.getImageReferences()
        val baseUri: URI = Paths.get(basePath).toAbsolutePath().toUri()
        GltfReferenceResolver.resolveAll(refList, baseUri)
//        val refDatas: Map<String, ByteBuffer> = gltfAsset.getReferenceDatas()
//        for ((key, value) in refDatas) {
//            LOG.debug("Image[{}]: <{} bytes>", key, value.remaining())
//        }
    }

}

fun GltfBuilder.buildNodeWith(meshBuilder: TopologyBuilder): Node {
    return meshBuilder.build(this)
}
