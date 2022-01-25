package pro.leaco.gltf.buffer

import de.javagl.jgltf.impl.v2.Accessor
import de.javagl.jgltf.impl.v2.BufferView
import de.javagl.jgltf.impl.v2.GlTF
import de.javagl.jgltf.impl.v2.MeshPrimitive
import pro.leaco.gltf.GltfBuilder
import java.nio.ByteBuffer
import javax.vecmath.Vector3f

/**
 * Serializer for normal vector primitives.
 * @author Leaco
 */
class Normals(name: String) : BaseBuffer(name) {
    private val vecList = ArrayList<Vector3f>()
    private val minVec = Vector3f()
    private val maxVec = Vector3f()

    init {
        clear()
    }

    fun add(vertex: Vector3f) {
        minVec.x = Math.min(minVec.x, vertex.x)
        minVec.y = Math.min(minVec.y, vertex.y)
        minVec.z = Math.min(minVec.z, vertex.z)
        maxVec.x = Math.max(maxVec.x, vertex.x)
        maxVec.y = Math.max(maxVec.y, vertex.y)
        maxVec.z = Math.max(maxVec.z, vertex.z)
        vecList.add(vertex)
    }

    override fun clear() {
        vecList.clear()
        minVec.x = Float.POSITIVE_INFINITY
        minVec.y = Float.POSITIVE_INFINITY
        minVec.z = Float.POSITIVE_INFINITY
        maxVec.x = Float.NEGATIVE_INFINITY
        maxVec.y = Float.NEGATIVE_INFINITY
        maxVec.z = Float.NEGATIVE_INFINITY
    }

    override fun size(): Int {
        return vecList.size
    }

    override fun build(geoWriter: GltfBuilder, meshPirimitive: MeshPrimitive): Accessor? {
        return buildAttrib(geoWriter, meshPirimitive, "NORMAL")
    }

    override fun writeBuf(buffer: ByteBuffer) {
        for (i in vecList.indices) {
            val vec = vecList[i]
            buffer.putFloat(vec.x)
            buffer.putFloat(vec.y)
            buffer.putFloat(vec.z)
        }
    }

    override fun addAccessor(gltf: GlTF, bufferView: BufferView?): Accessor? {
        val accessor = super.addAccessor(gltf, bufferView)
        accessor!!.componentType = BaseBuffer.Companion.FLOAT_TYPE
        accessor.type = "VEC3"
        accessor.max = arrayOf(maxVec.x, maxVec.y, maxVec.z)
        accessor.min = arrayOf(minVec.x, minVec.y, minVec.z)
        return accessor
    }

    override fun addBufferView(gltf: GlTF, buffer: ByteBuffer): BufferView? {
        val bufferView = super.addBufferView(gltf, buffer)
        bufferView!!.target = BaseBuffer.Companion.ARRAY_BUFFER_TYPE
        bufferView.byteStride = 12
        return bufferView
    }
}