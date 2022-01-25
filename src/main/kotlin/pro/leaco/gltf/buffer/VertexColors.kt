package pro.leaco.gltf.buffer

import de.javagl.jgltf.impl.v2.Accessor
import de.javagl.jgltf.impl.v2.BufferView
import de.javagl.jgltf.impl.v2.GlTF
import de.javagl.jgltf.impl.v2.MeshPrimitive
import pro.leaco.gltf.GltfBuilder
import java.awt.Color
import java.nio.ByteBuffer
import java.util.ArrayList

/**
 * Serializer for vertex color primitives.
 * @author Leaco
 */
class VertexColors(name: String) : BaseBuffer(name) {
    private val cList = ArrayList<Color>()
    fun add(color: Color) {
        cList.add(color)
    }

    override fun clear() {
        cList.clear()
    }

    override fun size(): Int {
        return cList.size
    }

    override fun build(geoWriter: GltfBuilder, meshPirimitive: MeshPrimitive): Accessor? {
        return buildAttrib(geoWriter, meshPirimitive, "COLOR_0")
    }

    override fun writeBuf(buffer: ByteBuffer) {
        for (i in cList.indices) {
            val color = cList[i]
            val r = color.red
            val g = color.green
            val b = color.blue
            val a = color.alpha
            buffer.put(r.toByte())
            buffer.put(g.toByte())
            buffer.put(b.toByte())
            buffer.put(a.toByte())
        }
    }

    override fun addAccessor(gltf: GlTF, bufferView: BufferView?): Accessor? {
        val accessor = super.addAccessor(gltf, bufferView)
        accessor!!.componentType = BaseBuffer.Companion.UNSIGNED_BYTE_TYPE
        accessor.type = "VEC4"
        accessor.isNormalized = true
        return accessor
    }

    override fun addBufferView(gltf: GlTF, buffer: ByteBuffer): BufferView? {
        val bufferView = super.addBufferView(gltf, buffer)
        bufferView!!.target = BaseBuffer.Companion.ARRAY_BUFFER_TYPE
        bufferView.byteStride = 4
        return bufferView
    }
}