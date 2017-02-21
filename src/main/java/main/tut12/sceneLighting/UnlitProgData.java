///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package main.tut12.sceneLighting;
//
//import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
//import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
//import com.jogamp.opengl.GL3;
//import com.jogamp.opengl.util.glsl.ShaderCode;
//import com.jogamp.opengl.util.glsl.ShaderProgram;
//import main.framework.Framework;
//import main.framework.Semantic;
//import glm.mat._4.Mat4;
//
///**
// *
// * @author elect
// */
//class UnlitProgData {
//
//    public int theProgram;
//
//    public int objectColorUnif;
//
//    public int cameraToClipMatrixUnif;
//    public int modelToCameraMatrixUnif;
//
//    public UnlitProgData(GL3 gl3, String shaderRoot, String vertSrc, String fragSrc) {
//
//        ShaderProgram shaderProgram = new ShaderProgram();
//
//        ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), shaderRoot, null,
//                vertSrc, "vert", null, true);
//        ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), shaderRoot, null,
//                fragSrc, "frag", null, true);
//
//        shaderProgram.add(vertShaderCode);
//        shaderProgram.add(fragShaderCode);
//
//        shaderProgram.link(gl3, System.out);
//
//        theProgram = shaderProgram.program();
//
//        vertShaderCode.destroy(gl3);
//        fragShaderCode.destroy(gl3);
//
//        objectColorUnif = gl3.glGetUniformLocation(theProgram, "objectColor");
//
//        modelToCameraMatrixUnif = gl3.glGetUniformLocation(theProgram, "modelToCameraMatrix");
//        cameraToClipMatrixUnif = gl3.glGetUniformLocation(theProgram, "cameraToClipMatrix");
//
//        gl3.glUniformBlockBinding(theProgram,
//                gl3.glGetUniformBlockIndex(theProgram, "Projection"),
//                Semantic.Uniform.PROJECTION);
//        gl3.glUniformBlockBinding(theProgram,
//                gl3.glGetUniformBlockIndex(theProgram, "Material"),
//                Semantic.Uniform.MATERIAL);
//        gl3.glUniformBlockBinding(theProgram,
//                gl3.glGetUniformBlockIndex(theProgram, "Light"),
//                Semantic.Uniform.LIGHT);
//    }
//
//    public void setWindowData(GL3 gl3, Mat4 cameraToClip) {
//        gl3.glUseProgram(theProgram);
//        gl3.glUniformMatrix4fv(cameraToClipMatrixUnif, 1, false, cameraToClip.toDfb(Framework.matBuffer));
//        gl3.glUseProgram(0);
//    }
//}
