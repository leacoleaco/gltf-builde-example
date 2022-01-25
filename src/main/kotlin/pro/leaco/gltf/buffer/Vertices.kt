package pro.leaco.gltf.buffer

import de.javagl.jgltf.impl.v2.Accessor
import de.javagl.jgltf.impl.v2.BufferView
import de.javagl.jgltf.impl.v2.GlTF
import de.javagl.jgltf.impl.v2.MeshPrimitive
import pro.leaco.gltf.GltfBuilder
import java.nio.ByteBuffer
import javax.vecmath.Point3f
import kotlin.math.max
import kotlin.math.min

/**
 * Serializer vertex primitives.
 * @author Leaco
 */
class Vertices(name: String) : BaseBuffer(name) {
    private val pointList = ArrayList<Point3f>()
    val minBounds = Point3f()
    val maxBounds = Point3f()

    init {
        clear()
    }

    fun add(vertex: Point3f) {
        minBounds.x = min(minBounds.x, vertex.x)
        minBounds.y = min(minBounds.y, vertex.y)
        minBounds.z = min(minBounds.z, vertex.z)
        maxBounds.x = max(maxBounds.x, vertex.x)
        maxBounds.y = max(maxBounds.y, vertex.y)
        maxBounds.z = max(maxBounds.z, vertex.z)
        pointList.add(vertex)
    }

    override fun clear() {
        pointList.clear()
        minBounds.x = Float.POSITIVE_INFINITY
        minBounds.y = Float.POSITIVE_INFINITY
        minBounds.z = Float.POSITIVE_INFINITY
        maxBounds.x = Float.NEGATIVE_INFINITY
        maxBounds.y = Float.NEGATIVE_INFINITY
        maxBounds.z = Float.NEGATIVE_INFINITY
    }

    override fun size(): Int {
        return pointList.size
    }

    override fun build(geoWriter: GltfBuilder, meshPirimitive: MeshPrimitive): Accessor? {
        return buildAttrib(geoWriter, meshPirimitive, "POSITION")
    }

    override fun writeBuf(buffer: ByteBuffer) {
        for (i in pointList.indices) {
            val vec = pointList[i]
            buffer.putFloat(vec.x)
            buffer.putFloat(vec.y)
            buffer.putFloat(vec.z)
        }
    }

    override fun addAccessor(gltf: GlTF, bufferView: BufferView?): Accessor {
        val accessor = super.addAccessor(gltf, bufferView)
        accessor!!.componentType = FLOAT_TYPE
        accessor.type = "VEC3"
        accessor.max = arrayOf(
            maxBounds.x,
            maxBounds.y,
            maxBounds.z)
        accessor.min = arrayOf(
            minBounds.x,
            minBounds.y,
            minBounds.z)
        return accessor
    }

    override fun addBufferView(gltf: GlTF, buffer: ByteBuffer): BufferView {
        val bufferView = super.addBufferView(gltf, buffer)
        bufferView!!.target = ARRAY_BUFFER_TYPE
        bufferView.byteStride = 12
        return bufferView
    }
}