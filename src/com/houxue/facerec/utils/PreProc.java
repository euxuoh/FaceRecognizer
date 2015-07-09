package com.houxue.facerec.utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.util.Log;

/**
 * 图片预处理，包括：转化为灰度图，直方图均衡化，对齐（归一化剪切）
 * 
 * @author houxue
 * @version 1.1
 * @date 2015.5.11
 */
public class PreProc {

	private static final String TAG = "PreProc.class";

	private String haar_face = null;

	public PreProc(String classifier) {
		this.haar_face = classifier;
	}

	/**
	 * 图像灰度化，直方图均衡化
	 * 
	 * @param srcMat
	 *            原始格式的图像
	 * 
	 * @return dstMat 处理后的图像
	 */
	public Mat cvtColHist(Mat srcMat) {
		Mat tmpMat = new Mat();
		Mat dstMat = new Mat();

		Imgproc.cvtColor(srcMat, tmpMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(tmpMat, dstMat);

		return dstMat;
	}

	/**
	 * 特征位置检测
	 * 
	 * @param srcMat
	 *            灰度化和均衡化后的图像
	 * 
	 * @return feature 人脸特征
	 */
	public FeaturePosi FeatureDetect(Mat srcMat) {
		FeaturePosi feature = new FeaturePosi();

		CascadeClassifier faceClassifier = new CascadeClassifier(haar_face);

		if (faceClassifier.empty()) {
			Log.e(TAG, "Failed to load cascade classifier");
		} else {
			Log.i(TAG, "Loaded cascade classifier from " + haar_face);
		}
		MatOfRect faceRect = new MatOfRect();
		faceClassifier.detectMultiScale(srcMat, faceRect);
		if (faceRect.toArray().length != 0) {
			feature.setFace(faceRect.toArray()[0]);
		} else {
			Log.e(TAG, "Did not detect face.");
		}

		return feature;
	}

	/**
	 * 图片剪切
	 * 
	 * @param srcMat
	 *            灰度化和均衡化后的人脸图像
	 * @param fPosi
	 *            人脸特征，包括人脸，眼睛，鼻子，嘴巴的位置
	 * 
	 * @return dstMat 剪切后的人脸图像
	 */
	public Mat CropImg(Mat srcMat, FeaturePosi fPosi) {
		Mat dstMat = new Mat();

		dstMat = srcMat.submat(fPosi.getFace());

		return dstMat;
	}

	/**
	 * 图像标准化
	 * 
	 * @param srcMat
	 *            灰度化和均衡化后的人脸图像
	 * 
	 * @return dstMat 标准化后的图像
	 */
	public Mat NormalImg(Mat srcMat) {
		Mat dstMat = new Mat();
		Mat result = new Mat();

		Mat tmpMat = cvtColHist(srcMat);
		FeaturePosi fp = FeatureDetect(tmpMat);

		if (fp.getFace() != null) {
			dstMat = CropImg(tmpMat, fp);
			Imgproc.resize(dstMat, result, new Size(200, 200));
		} else {
			// 如果未检测到人脸，则返回null
			return null;
		}

		return result;
	}

}
