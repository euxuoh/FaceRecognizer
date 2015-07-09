package com.hoxue.facerec.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @Title: RegisterActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: 用户注册时获取人脸信息的界面
 * @author Hou Xue
 * @Date 2015.4.17
 * @version 1.0
 */
public class RegisterActivity extends Activity {
	
	private static final String TAG = "RegisterActivity.";

	// 摄像头编号
	private final int FRONT_CAMERA = 1;
	// private final int REAR_CAMERA = 0;

	// 摄像头实例
	private Camera mCamera = null;
	// 摄像头预览实例
	private CameraPreview mPreview = null;
	// 前置摄像头layout角度
	private int orientionOfCamera;

	private TextView tips_edtx = null;
	private Button enter_btn = null;

	// 保存图像的路径
	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/frec/";

	// 图像数据
	private Bitmap newBitmap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		tips_edtx = (TextView) findViewById(R.id.Register_tips);
		tips_edtx.setText("请正对摄像头...");

		// 确定拍照
		enter_btn = (Button) this.findViewById(R.id.Register_ent);
		enter_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				mCamera.takePicture(null, null, takePictureCallback);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 检测摄像头是否被占用
		if (checkCamera(this)) {
			// 获得一个摄像头实例
			mCamera = getCameraInstance(FRONT_CAMERA);
		}

		// 设置摄像头的角度
		setCameraDisplayOrientation(FRONT_CAMERA, mCamera);

		// 获得摄像头预览实例
		mPreview = new CameraPreview(this, mCamera);
		// 将预览放入layout中
		FrameLayout previewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview_reg);
		previewFrameLayout.addView(mPreview);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
		}
	}

	/**
	 * 检测摄像头是否存在
	 * 
	 * @param context
	 *            本Activity上下文
	 * 
	 * @return true 摄像头存在 false 摄像头不存在
	 */
	private boolean checkCamera(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			Log.i(TAG + "checkCamera()", "摄像头存在");
			return true;
		} else {
			Log.w(TAG + "checkCamera()", "摄像头不存在");
			return false;
		}
	}

	/**
	 * 获取一个摄像头实例
	 * 
	 * @param int 摄像头编号
	 * 
	 * @return Camera 一个摄像头实例
	 */
	@SuppressLint("NewApi")
	public Camera getCameraInstance(int parmInt) {
		Camera camera = null;

		try {
			// 0：打开后置摄像头；1：打开前置摄像头
			camera = Camera.open(parmInt);
		} catch (Exception e) {
			Log.w(TAG + "getCameraInstance()", "摄像头被占用");
		}

		return camera;
	}

	/**
	 * 设置摄像头的方向，一般默认是横向的
	 * 
	 * @param int 摄像头编号 Camera 前文获取的摄像头实例
	 * 
	 * @return void
	 */
	@SuppressLint("NewApi")
	public void setCameraDisplayOrientation(int paramInt, Camera paramCamera) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(paramInt, info);
		// 获得显示器件角度
		int rotation = ((WindowManager) getSystemService("window"))
				.getDefaultDisplay().getRotation();
		int degrees = 0;
		Log.i(TAG + "setCameraDisplayOrientation()",
				"getRotation's rotation is " + String.valueOf(rotation));
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		// 获得摄像头的安装旋转角度
		orientionOfCamera = info.orientation;
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			// 补偿镜
			result = (360 - result) % 360;
		} else {
			// 后置摄像头
			result = (info.orientation - degrees + 360) % 360;
		}
		// 注意前后置的处理，前置是映象画面，该段是SDK文档的标准DEMO
		paramCamera.setDisplayOrientation(result);
	}

	// 在takepicture中调用的回调方法之一，接收jpeg格式的图像
	private PictureCallback takePictureCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			try {
				// 取得相片
				Bitmap bm = BitmapFactory.decodeByteArray(_data, 0,
						_data.length);

				// 按照摄像头的旋转角度重置BitMap
				float scale = Math.min(1,
						Math.min(600f / bm.getWidth(), 600f / bm.getHeight()));
				Matrix matrix = new Matrix();
				matrix.setRotate(orientionOfCamera - 360);
				matrix.postScale(scale, scale);
				newBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						bm.getHeight(), matrix, true);

				// 创建文件目录
				File myCaptureFile = new File(strCaptureFilePath);
				if (!myCaptureFile.exists()) {
					myCaptureFile.mkdirs();
				}
				// 创建文件
				File imgPath = new File(strCaptureFilePath, "img.jpg");
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(imgPath));

				// 采用压缩转档方法
				newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

				// 调用flush()方法，更新BufferStream
				bos.flush();

				// 结束OutputStream
				bos.close();

				// 释放相机
				releaseCamera();

				// 让相片显示2秒
				tips_edtx.setText("拍照成功...");
				Thread.sleep(2000);

				Intent intent = new Intent(RegisterActivity.this,
						InfoSetActivity.class);
				intent.putExtra("imgPath", strCaptureFilePath + "img.jpg");
				startActivity(intent);

			} catch (Exception e) {
				Log.e(TAG + "takePictureCallback()", e.getMessage());
			}
		}
	};

	/**
	 * 释放摄像头
	 * 
	 * @param void
	 * 
	 * @return void
	 */
	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

}
