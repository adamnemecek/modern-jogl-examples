///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package main.tut06.hierarchy;
//
//import mat.Mat4x4;
//import vec._3.Vec3;
//import java.util.ArrayList;
//
///**
// *
// * @author GBarbieri
// */
//class MatrixStack {
//
//    private Mat4x4 currMat = new Mat4x4(1.0f);
//    private ArrayList<Mat4x4> matrices = new ArrayList<>();
//
//    public Mat4x4 top() {
//        return currMat;
//    }
//
//    public void rotateX (float angDeb) {
//        currMat.rotateX(Math.toRadians(angDeb));
//    }
//
//    public void rotateY (float angDeb) {
//        currMat.rotateY(Math.toRadians(angDeb));
//    }
//
//    public void rotateZ (float angDeb) {
//        currMat.rotateZ(Math.toRadians(angDeb));
//    }
//
//    public void scale (Vec3 scale) {
//        currMat.scale(scale);
//    }
//
//    public void translate (Vec3 offset) {
//        currMat.translate(offset);
//    }
//
//    public void push () {
//        matrices.add(new Mat4().set(currMat));
//    }
//
//    public void pop () {
//        currMat.set(matrices.get(matrices.size() - 1));
//        matrices.remove(matrices.size() - 1);
//    }
//}
