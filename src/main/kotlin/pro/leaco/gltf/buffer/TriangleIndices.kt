package pro.leaco.gltf.buffer

import de.javagl.jgltf.impl.v2.Accessor
import de.javagl.jgltf.impl.v2.BufferView
import de.javagl.jgltf.impl.v2.GlTF
import de.javagl.jgltf.impl.v2.MeshPrimitive
import pro.leaco.gltf.GltfBuilder
import java.util.Collections
import java.nio.ByteBuffer
import java.util.ArrayList

/**
 * Serializer for triangle index primitives.
 * @author Leaco
 */
class TriangleIndices(name: String) : BaseBuffer(name) {
    private val list = ArrayList<Short>()
    fun add(v1: Int, v2: Int, v3: Int) {
        list.add(v1.toShort())
        list.add(v2.toShort())
        list.add(v3.toShort())
    }

    override fun clear() {
        list.clear()
        list.clear()
    }

    override fun size(): Int {
        return list.size
    }

    override fun build(geoWriter: GltfBuilder, meshPirimitive: MeshPrimitive): Accessor? {
        val indicesAccessor = super.buildBuffer(geoWriter)
        val accessorIdx = geoWriter.gltf.accessors.indexOf(indicesAccessor)
        meshPirimitive.indices = accessorIdx
        return indicesAccessor
    }

    override fun writeBuf(buffer: ByteBuffer) {
        for (s in list) {
            buffer.putShort(s)
        }
    }

    override fun addAccessor(gltf: GlTF, bufferView: BufferView?): Accessor? {
        val accessor = super.addAccessor(gltf, bufferView)
        accessor!!.componentType = UNSIGNED_SHORT_TYPE
        accessor.type = "SCALAR"
        accessor.max = arrayOf(
            Collections.max(list))
        accessor.min = arrayOf(
            Collections.min(list))
        return accessor
    }

    override fun addBufferView(gltf: GlTF, buffer: ByteBuffer): BufferView? {
        val bufferView = super.addBufferView(gltf, buffer)
        bufferView!!.target = ELEMENT_ARRAY_BUFFER_TYPE
        alignWords(buffer)
        return bufferView
    }
}