package glNext.tut08

import com.jogamp.newt.event.KeyEvent
import com.jogamp.opengl.GL3
import glNext.*
import glm.*
import glm.mat.Mat4
import glm.vec._3.Vec3
import glm.vec._4.Vec4
import main.framework.Framework
import main.framework.component.Mesh
import uno.glm.MatrixStack
import uno.glsl.programOf
import glm.quat.Quat

/**
 * Created by GBarbieri on 10.03.2017.
 */

fun main(args: Array<String>) {
    CameraRelative_Next().setup("Tutorial 08 - Camera Relative")
}

class CameraRelative_Next : Framework() {

    object OffsetRelative {
        val MODEL = 0
        val WORLD = 1
        val CAMERA = 2
        val MAX = 3
    }

    var theProgram = 0
    var modelToCameraMatrixUnif = 0
    var cameraToClipMatrixUnif = 0
    var baseColorUnif = 0

    lateinit var ship: Mesh
    lateinit var plane: Mesh

    val frustumScale = calcFrustumScale(20.0f)

    fun calcFrustumScale(fovDeg: Float) = 1.0f / glm.tan(fovDeg.rad / 2.0f)

    val cameraToClipMatrix = Mat4(0.0f)

    val camTarget = Vec3(0.0f, 10.0f, 0.0f)
    var orientation = Quat(1.0f, 0.0f, 0.0f, 0.0f)

    //In spherical coordinates.
    val sphereCamRelPos = Vec3(90.0f, 0.0f, 66.0f)

    var offset = OffsetRelative.MODEL

    public override fun init(gl: GL3) = with(gl) {

        initializeProgram(gl)

        ship = Mesh(gl, javaClass, "tut08/Ship.xml")
        plane = Mesh(gl, javaClass, "tut08/UnitPlane.xml")

        cullFace {
            enable()
            cullFace = back
            frontFace = cw
        }

        depth {
            test = true
            mask = true
            func = lEqual
            range = 0.0 .. 1.0
        }
    }

    fun initializeProgram(gl: GL3) = with(gl) {

        theProgram = programOf(gl, javaClass, "tut08", "pos-color-local-transform.vert", "color-mult-uniform.frag")

        withProgram(theProgram) {

            modelToCameraMatrixUnif = "modelToCameraMatrix".location
            cameraToClipMatrixUnif = "cameraToClipMatrix".location
            baseColorUnif = "baseColor".location

            val zNear = 1.0f
            val zFar = 600.0f

            cameraToClipMatrix[0].x = frustumScale
            cameraToClipMatrix[1].y = frustumScale
            cameraToClipMatrix[2].z = (zFar + zNear) / (zNear - zFar)
            cameraToClipMatrix[2].w = -1.0f
            cameraToClipMatrix[3].z = 2f * zFar * zNear / (zNear - zFar)

            use { cameraToClipMatrixUnif.mat4 = cameraToClipMatrix }
        }
    }

    public override fun display(gl: GL3) = with(gl) {

        clear {
            color(0)
            depth()
        }

        val currMatrix = MatrixStack()

        val camPos = resolveCamPosition()

        currMatrix setMatrix calcLookAtMatrix(camPos, camTarget, Vec3(0.0f, 1.0f, 0.0f))

        usingProgram(theProgram) {

            currMatrix.apply {

                scale(100.0f, 1.0f, 100.0f)

                glUniform4f(baseColorUnif, 0.2f, 0.5f, 0.2f, 1.0f)
                modelToCameraMatrixUnif.mat4 = top()

                plane.render(gl)

            } run {

                translate(camTarget)
                applyMatrix(orientation.toMat4())
                rotateX(-90.0f)

                glUniform4f(baseColorUnif, 1.0f)
                modelToCameraMatrixUnif.mat4 = top()

                ship.render(gl, "tint")
            }
        }
    }

    fun resolveCamPosition(): Vec3 {

        val phi = sphereCamRelPos.x.rad
        val theta = (sphereCamRelPos.y + 90.0f).rad

        val dirToCamera = Vec3(theta.sin * phi.cos, theta.cos, theta.sin * phi.sin)

        return dirToCamera * sphereCamRelPos.z + camTarget
    }

    fun calcLookAtMatrix(cameraPt: Vec3, lookPt: Vec3, upPt: Vec3): Mat4 {

        val lookDir = (lookPt - cameraPt).normalize()
        val upDir = upPt.normalize()

        val rightDir = (lookDir cross upDir).normalize()
        val perpUpDir = rightDir cross lookDir

        val rotationMat = Mat4(1.0f)
        rotationMat[0] = Vec4(rightDir, 0.0f)
        rotationMat[1] = Vec4(perpUpDir, 0.0f)
        rotationMat[2] = Vec4(-lookDir, 0.0f)

        rotationMat.transpose_()

        val translMat = Mat4(1.0f)
        translMat[3] = Vec4(-cameraPt, 1.0f)

        return rotationMat * translMat
    }

    public override fun reshape(gl: GL3, w: Int, h: Int) = with(gl) {

        cameraToClipMatrix[0].x = frustumScale * (h / w.f)
        cameraToClipMatrix[1].y = frustumScale

        usingProgram(theProgram) {cameraToClipMatrixUnif.mat4 = cameraToClipMatrix}

        glViewport(w, h)
    }

    public override fun end(gl: GL3) = with(gl) {

        glDeleteProgram(theProgram)

        plane.dispose(gl)
        ship.dispose(gl)
    }

    override fun keyPressed(e: KeyEvent) {

        val smallAngleIncrement = 9.0f

        when (e.keyCode) {

            KeyEvent.VK_ESCAPE -> quit()

            KeyEvent.VK_W -> offsetOrientation(Vec3(1.0f, 0.0f, 0.0f), +smallAngleIncrement)
            KeyEvent.VK_S -> offsetOrientation(Vec3(1.0f, 0.0f, 0.0f), -smallAngleIncrement)

            KeyEvent.VK_A -> offsetOrientation(Vec3(0.0f, 0.0f, 1.0f), +smallAngleIncrement)
            KeyEvent.VK_D -> offsetOrientation(Vec3(0.0f, 0.0f, 1.0f), -smallAngleIncrement)

            KeyEvent.VK_Q -> offsetOrientation(Vec3(0.0f, 1.0f, 0.0f), +smallAngleIncrement)
            KeyEvent.VK_E -> offsetOrientation(Vec3(0.0f, 1.0f, 0.0f), -smallAngleIncrement)

            KeyEvent.VK_SPACE -> {

                offset = (++offset) % OffsetRelative.MAX

                when (offset) {

                    OffsetRelative.MODEL -> println("MODEL_RELATIVE")

                    OffsetRelative.WORLD -> println("WORLD_RELATIVE")

                    OffsetRelative.CAMERA -> println("CAMERA_RELATIVE")
                }
            }

            KeyEvent.VK_I -> sphereCamRelPos.y -= if (e.isShiftDown) 1.125f else 11.25f

            KeyEvent.VK_K -> sphereCamRelPos.y += if (e.isShiftDown) 1.125f else 11.25f

            KeyEvent.VK_J -> sphereCamRelPos.x -= if (e.isShiftDown) 1.125f else 11.25f

            KeyEvent.VK_L -> sphereCamRelPos.x += if (e.isShiftDown) 1.125f else 11.25f
        }

        sphereCamRelPos.y = glm.clamp(sphereCamRelPos.y, -78.75f, 10.0f)
    }

    fun offsetOrientation(axis: Vec3, angDeg: Float) {

        axis.normalize()

        axis times_ glm.sin(angDeg.rad / 2.0f)
        val scalar = glm.cos(angDeg.rad / 2.0f)

        val offsetQuat = Quat(scalar, axis)

        when (offset) {

            OffsetRelative.MODEL -> orientation times_ offsetQuat

            OffsetRelative.WORLD -> orientation = offsetQuat * orientation

            OffsetRelative.CAMERA -> {

                val camPos = resolveCamPosition()
                val camMat = calcLookAtMatrix(camPos, camTarget, Vec3(0.0f, 1.0f, 0.0f))

                val viewQuat = camMat.toQuat()
                val invViewQuat = viewQuat.conjugate()

                val worldQuat = invViewQuat * offsetQuat * viewQuat
                orientation = worldQuat * orientation
            }
        }

        orientation.normalize_()
    }
}