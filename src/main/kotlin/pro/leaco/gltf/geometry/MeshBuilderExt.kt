package pro.leaco.gltf

import javax.vecmath.Point3f

/**
 * add a 2D surface with geometry
 * @param geometry 形状
 * @param isTextured 贴图
 */
fun MeshBuilder.addGeometryPlane(isTextured: Boolean): MeshBuilder {


    return this.addGrid(
        arrayOf(
            arrayOf(
                newVertex(Point3f(1f, 1f, 0f)),
                newVertex(Point3f(1f, 0f, 0f)),
                newVertex(Point3f(1f, -2f, 0f))
            ),
            arrayOf(
                newVertex(Point3f(0f, 1f, 0f)),
                newVertex(Point3f(0f, 0f, 0f)),
                newVertex(Point3f(0f, -1f, 0f))
            )
        ),
        isTextured,
        wrapY = false,
        wrapX = false
    )
}