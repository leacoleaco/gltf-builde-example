package pro.leaco.gltf.buffer

import de.javagl.jgltf.impl.v2.Accessor
import de.javagl.jgltf.impl.v2.BufferView
import de.javagl.jgltf.impl.v2.GlTF
import de.javagl.jgltf.impl.v2.MeshPrimitive
import pro.leaco.gltf.GltfBuilder
import java.nio.ByteBuffer
import javax.vecmath.Point2f
import kotlin.math.max
import kotlin.math.min

/**
 * Serializer for texture coordinate primitives.
 * @author Leaco
 */
class TextureCoordinates(name: String) : BaseBuffer(name) {
    private val pointList = ArrayList<Point2f>()
    private val minPoint = Point2f()
    private val maxPoint = Point2f()

    init {
        clear()
    }

    fun add(coord: Point2f) {
        minPoint.x = min(minPoint.x, coord.x)
        minPoint.y = min(minPoint.y, coord.y)
        maxPoint.x = max(maxPoint.x, coord.x)
        maxPoint.y = max(maxPoint.y, coord.y)
        pointList.add(coord)
    }

    override fun clear() {
        pointList.clear()
        minPoint.x = Float.POSITIVE_INFINITY
        minPoint.y = Float.POSITIVE_INFINITY
        maxPoint.x = Float.NEGATIVE_INFINITY
        maxPoint.y = Float.NEGATIVE_INFINITY
    }

    override fun size(): Int {
        return pointList.size
    }

    override fun build(geoWriter: GltfBuilder, meshPirimitive: MeshPrimitive): Accessor? {
        return buildAttrib(geoWriter, meshPirimitive, "TEXCOORD_0")
    }

    override fun writeBuf(buffer: ByteBuffer) {
        for (i in pointList.indices) {
            val vec = pointList[i]
            buffer.putFloat(vec.x)
            buffer.putFloat(vec.y)
        }
    }

    override fun addAccessor(gltf: GlTF, bufferView: BufferView?): Accessor? {
        val accessor = super.addAccessor(gltf, bufferView)
        accessor!!.componentType = BaseBuffer.Companion.FLOAT_TYPE
        accessor.type = "VEC2"
        accessor.max = arrayOf(maxPoint.x, maxPoint.y)
        accessor.min = arrayOf(minPoint.x, minPoint.y)
        return accessor
    }

    override fun addBufferView(gltf: GlTF, buffer: ByteBuffer): BufferView? {
        val bufferView = super.addBufferView(gltf, buffer)
        bufferView!!.target = BaseBuffer.Companion.ARRAY_BUFFER_TYPE
        bufferView.byteStride = 8
        return bufferView
    }
}