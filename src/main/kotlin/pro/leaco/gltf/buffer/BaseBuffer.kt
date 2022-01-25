package pro.leaco.gltf.buffer

import de.javagl.jgltf.impl.v2.Accessor
import de.javagl.jgltf.impl.v2.BufferView
import de.javagl.jgltf.impl.v2.GlTF
import de.javagl.jgltf.impl.v2.MeshPrimitive
import org.slf4j.LoggerFactory
import pro.leaco.gltf.GltfBuilder
import java.nio.ByteBuffer

/**
 * Base class for primitive serializers.
 *
 * @author Leaco
 */
abstract class BaseBuffer(protected val name: String) {
    abstract fun size(): Int
    abstract fun clear()
    abstract fun build(geoWriter: GltfBuilder, meshPirimitive: MeshPrimitive): Accessor?
    protected abstract fun writeBuf(buffer: ByteBuffer)
    protected fun buildAttrib(geoWriter: GltfBuilder, meshPirimitive: MeshPrimitive, attribute: String?): Accessor? {
        val vertexAccessor = buildBuffer(geoWriter) ?: return null
        val positionIdx = geoWriter.gltf.accessors.indexOf(vertexAccessor)
        meshPirimitive.addAttributes(attribute, positionIdx)
        return vertexAccessor
    }

    protected fun buildBuffer(geoWriter: GltfBuilder): Accessor? {
        if (size() == 0) {
            return null
        }
        val texCoordBuf = addBufferView(geoWriter.gltf, geoWriter.buffer)
        return addAccessor(geoWriter.gltf, texCoordBuf)
    }

    protected open fun addAccessor(gltf: GlTF, bufferView: BufferView?): Accessor? {
        val bufferIdx = gltf.bufferViews.indexOf(bufferView)
        val accessor = Accessor()
        gltf.addAccessors(accessor)
        accessor.bufferView = bufferIdx
        accessor.byteOffset = 0
        accessor.count = size()

        //int idx = gltf.getAccessors().indexOf(accessor);
        val type = this.javaClass.simpleName
        val accessorName = String.format("%s-%s", name, type)
        accessor.name = accessorName
        LOG.debug("Accessor[{}]: buffer={} count={}", accessorName, bufferIdx, size())
        return accessor
    }

    protected open fun addBufferView(gltf: GlTF, buffer: ByteBuffer): BufferView? {
        val startPos = buffer.position()
        writeBuf(buffer)
        val length = buffer.position() - startPos
        val bufferView = BufferView()
        gltf.addBufferViews(bufferView)
        bufferView.buffer = 0
        bufferView.byteOffset = startPos
        bufferView.byteLength = length

        //int idx = gltf.getBufferViews().indexOf(bufferView);
        val bufViewName = String.format("%s-%s", name, this.javaClass.simpleName)
        bufferView.name = bufViewName
        LOG.debug("BufferView[{}]: start={}, size={}", bufViewName, startPos, length)
        return bufferView
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BaseBuffer::class.java)

        // accessor types
        const val UNSIGNED_SHORT_TYPE = 5123
        const val UNSIGNED_BYTE_TYPE = 5121
        const val FLOAT_TYPE = 5126

        // buffer types
        const val ELEMENT_ARRAY_BUFFER_TYPE = 34963
        const val ARRAY_BUFFER_TYPE = 34962

        fun alignWords(byteBuffer: ByteBuffer) {
            val limit = byteBuffer.position()
            val padding = limit % 4
            for (i in 0 until padding) {
                byteBuffer.put(0.toByte())
            }
        }
    }
}