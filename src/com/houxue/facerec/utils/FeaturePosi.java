package com.houxue.facerec.utils;


import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class FeaturePosi {
	
	Rect face = null;
	MatOfRect eye = null;
	Rect leftEye = null;
	Rect rightEye = null;
	Rect nose = null;
	Rect mouth = null;
	
	public void setFace(Rect face) {
		this.face = face;
	}
	
	public Rect getFace() {
		return this.face;
	}
	
	public void setEye(MatOfRect eye) {
		this.eye = eye;
	}
	
	public MatOfRect getEye() {
		return this.eye;
	}
	
	public void setLeftEye(Rect left) {
		this.leftEye = left;
	}
	
	public Rect getLeftEye() {
		return this.leftEye;
	}
	
	public void setRightEye(Rect right) {
		this.rightEye = right;
	}
	
	public Rect getRightEye() {
		return this.rightEye;
	}
	
	public void setNose(Rect nose) {
		this.nose = nose;
	}
	
	public Rect getNose() {
		return this.nose;
	}
	
	public void setMouth(Rect mouth) {
		this.mouth = mouth;
	}
	
	public Rect getMouth() {
		return this.mouth;
	}
	
	public String toString() {
		return new String("[lefteye : (" + getLeftEye().x + ", " + getLeftEye().y + "), " + 
	                       "rightEye : (" + getRightEye().x + ", " + getRightEye().y + "), " + 
				           "nose : (" + getNose().x + ", "+getNose().y + "), " + 
	                       "mouth : (" + getMouth().x + ", " + getMouth().y + ")]");
	}
	
	public void print() {
		System.out.println(getFace());
		System.out.println(getLeftEye());
		System.out.println(getRightEye());
		System.out.println(getNose());
		System.out.println(getMouth());
	}

}
