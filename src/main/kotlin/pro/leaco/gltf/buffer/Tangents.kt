package pro.leaco.gltf.buffer

import de.javagl.jgltf.impl.v2.Accessor
import de.javagl.jgltf.impl.v2.BufferView
import de.javagl.jgltf.impl.v2.GlTF
import de.javagl.jgltf.impl.v2.MeshPrimitive
import pro.leaco.gltf.GltfBuilder
import java.nio.ByteBuffer
import javax.vecmath.Vector4f
import kotlin.math.max
import kotlin.math.min

/**
 * Serializer for Tangent primitives.
 * @author Leaco
 */
class Tangents(name: String) : BaseBuffer(name) {
    private val vecList = ArrayList<Vector4f>()
    private val minVec = Vector4f()
    private val maxVec = Vector4f()

    init {
        clear()
    }

    fun add(tangent: Vector4f) {
        minVec.x = min(minVec.x, tangent.x)
        minVec.y = min(minVec.y, tangent.y)
        minVec.z = min(minVec.z, tangent.z)
        minVec.w = min(minVec.w, tangent.w)
        maxVec.x = max(maxVec.x, tangent.x)
        maxVec.y = max(maxVec.y, tangent.y)
        maxVec.z = max(maxVec.z, tangent.z)
        maxVec.w = max(maxVec.w, tangent.w)
        vecList.add(tangent)
    }

    override fun clear() {
        vecList.clear()
        minVec.x = Float.POSITIVE_INFINITY
        minVec.y = Float.POSITIVE_INFINITY
        minVec.z = Float.POSITIVE_INFINITY
        minVec.w = Float.POSITIVE_INFINITY
        maxVec.x = Float.NEGATIVE_INFINITY
        maxVec.y = Float.NEGATIVE_INFINITY
        maxVec.z = Float.NEGATIVE_INFINITY
        maxVec.w = Float.NEGATIVE_INFINITY
    }

    override fun size(): Int {
        return vecList.size
    }

    override fun build(geoWriter: GltfBuilder, meshPirimitive: MeshPrimitive): Accessor? {
        return buildAttrib(geoWriter, meshPirimitive, "TANGENT")
    }

    override fun writeBuf(buffer: ByteBuffer) {
        for (i in vecList.indices) {
            val vec = vecList[i]
            buffer.putFloat(vec.x)
            buffer.putFloat(vec.y)
            buffer.putFloat(vec.z)
            buffer.putFloat(vec.w)
        }
    }

    override fun addAccessor(gltf: GlTF, bufferView: BufferView?): Accessor? {
        val accessor = super.addAccessor(gltf, bufferView)
        accessor!!.componentType = BaseBuffer.Companion.FLOAT_TYPE
        accessor.type = "VEC4"
        accessor.max = arrayOf(
            maxVec.x,
            maxVec.y,
            maxVec.z,
            maxVec.w)
        accessor.min = arrayOf(
            minVec.x,
            minVec.y,
            minVec.z,
            minVec.w)
        return accessor
    }

    override fun addBufferView(gltf: GlTF, buffer: ByteBuffer): BufferView? {
        val bufferView = super.addBufferView(gltf, buffer)
        bufferView!!.target = BaseBuffer.Companion.ARRAY_BUFFER_TYPE
        bufferView.byteStride = 16
        return bufferView
    }
}