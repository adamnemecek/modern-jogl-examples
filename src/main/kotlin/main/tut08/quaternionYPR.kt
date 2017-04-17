package main.tut08

import com.jogamp.newt.event.KeyEvent
import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL2ES3.GL_COLOR
import com.jogamp.opengl.GL2ES3.GL_DEPTH
import com.jogamp.opengl.GL3
import glNext.*
import glm.*
import glm.mat.Mat4
import glm.quat.Quat
import glm.vec._3.Vec3
import main.framework.Framework
import main.framework.component.Mesh
import uno.glm.MatrixStack
import uno.glsl.programOf

/**
 * Created by GBarbieri on 14.03.2017.
 */

fun main(args: Array<String>) {
    QuaternionYPR_().setup("Tutorial 08 - Quaternion YPR")
}

class QuaternionYPR_ : Framework() {

    class GimbalAngles(
            var angleX: Float = 0f,
            var angleY: Float = 0f,
            var angleZ: Float = 0f)

    lateinit var ship: Mesh

    var theProgram = 0
    var modelToCameraMatrixUnif = 0
    var cameraToClipMatrixUnif = 0
    var baseColorUnif = 0

    val frustumScale = calcFrustumScale(20f)

    fun calcFrustumScale(fovDeg: Float) = 1.0f / glm.tan(fovDeg.rad / 2.0f)

    val cameraToClipMatrix = Mat4(0.0f)

    val angles = GimbalAngles()

    var orientation = Quat(1.0f, 0.0f, 0.0f, 0.0f)

    var rightMultiply = true

    override fun init(gl: GL3) = with(gl) {

        initializeProgram(gl)

        ship = Mesh(gl, javaClass, "tut08/Ship.xml")

        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glFrontFace(GL_CW)

        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDepthFunc(GL_LEQUAL)
        glDepthRangef(0.0f, 1.0f)
    }

    fun initializeProgram(gl: GL3) = with(gl) {

        theProgram = programOf(gl, javaClass, "tut08", "pos-color-local-transform.vert", "color-mult-uniform.frag")

        modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix")
        cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix")
        baseColorUnif = glGetUniformLocation(theProgram, "baseColor")

        val zNear = 1.0f
        val zFar = 600.0f

        cameraToClipMatrix[0].x = frustumScale
        cameraToClipMatrix[1].y = frustumScale
        cameraToClipMatrix[2].z = (zFar + zNear) / (zNear - zFar)
        cameraToClipMatrix[2].w = -1.0f
        cameraToClipMatrix[3].z = 2f * zFar * zNear / (zNear - zFar)

        glUseProgram(theProgram)
        glUniformMatrix4f(cameraToClipMatrixUnif, cameraToClipMatrix)
        glUseProgram()
    }

    override fun display(gl: GL3) = with(gl) {

        glClearBufferf(GL_COLOR, 0)
        glClearBufferf(GL_DEPTH)

        val matrixStack = MatrixStack()
                .translate(0.0f, 0.0f, -200.0f)
                .applyMatrix(orientation.toMat4())

        glUseProgram(theProgram)

        matrixStack
                .scale(3.0f, 3.0f, 3.0f)
                .rotateX(-90.0f)

        glUniform4f(baseColorUnif, 1.0f)
        glUniformMatrix4f(modelToCameraMatrixUnif, matrixStack.top())

        ship.render(gl, "tint")

        glUseProgram()
    }

    override fun reshape(gl: GL3, w: Int, h: Int) = with(gl) {

        cameraToClipMatrix[0].x = frustumScale * (h / w.f)
        cameraToClipMatrix[1].y = frustumScale

        glUseProgram(theProgram)
        glUniformMatrix4f(cameraToClipMatrixUnif, cameraToClipMatrix)
        glUseProgram()

        glViewport(w, h)
    }

    override fun end(gl: GL3) = with(gl){

        glDeleteProgram(theProgram)

        ship.dispose(gl)
    }

    fun offsetOrientation(axis: Vec3, angDeg: Float) {

        axis.normalize_()

        axis times_ (angDeg.rad / 2.0f).sin
        val scalar = (angDeg.rad / 2.0f).cos

        val offset = Quat(scalar, axis)

        if (rightMultiply)
            orientation times_ offset
        else
            orientation = offset * orientation
    }

    override fun keyPressed(e: KeyEvent) {

        val smallAngleIncrement = 9.0f

        when (e.keyCode) {

            KeyEvent.VK_ESCAPE -> quit()

            KeyEvent.VK_W -> offsetOrientation(Vec3(1.0f, 0.0f, 0.0f), smallAngleIncrement)
            KeyEvent.VK_S -> offsetOrientation(Vec3(1.0f, 0.0f, 0.0f), -smallAngleIncrement)

            KeyEvent.VK_A -> offsetOrientation(Vec3(0.0f, 0.0f, 1.0f), smallAngleIncrement)
            KeyEvent.VK_D -> offsetOrientation(Vec3(0.0f, 0.0f, 1.0f), -smallAngleIncrement)

            KeyEvent.VK_Q -> offsetOrientation(Vec3(0.0f, 1.0f, 0.0f), smallAngleIncrement)
            KeyEvent.VK_E -> offsetOrientation(Vec3(0.0f, 1.0f, 0.0f), -smallAngleIncrement)

            KeyEvent.VK_SPACE -> {
                rightMultiply = !rightMultiply
                println("${if (rightMultiply) "Right" else "Left"}-multiply");
            }
        }
    }
}
