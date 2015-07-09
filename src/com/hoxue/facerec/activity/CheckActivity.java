package com.hoxue.facerec.activity;

import java.io.IOException;

import org.json.JSONObject;

import com.houxue.facerec.utils.DetectCallback;
import com.houxue.facerec.utils.FaceppRec;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Title: CheckActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: 人脸登录界面
 * @author Hou Xue
 * @Date 2015.4.19
 * @version 1.0
 */
public class CheckActivity extends Activity {

	private final String TAG = "CheckActivity.";
	
	// 摄像头编号
	private final int FRONT_CAMERA = 1;
	// private final int REAR_CAMERA = 0;

	// 摄像头实例
	private Camera mCamera = null;
	// 摄像头预览实例
	private CameraPreview mPreview = null;
	// 前置摄像头layout角度
	private int orientionOfCamera;
	// 人脸图像
	private Bitmap newBitmap = null;

	// 各种控件
	private TextView tips_edtx = null;
	private Button enter_btn = null;

	// 用户姓名
	private SharedPreferences sp = null;
	private String name = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check);

		sp = getSharedPreferences("userinfo", MODE_PRIVATE);
		name = sp.getString("name", "default");

		tips_edtx = (TextView) findViewById(R.id.check_tips);
		tips_edtx.setText("请正对摄像头...");

		enter_btn = (Button) findViewById(R.id.check_ent);
		enter_btn.setOnClickListener(new View.OnClickListener() {

			@Override
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
		FrameLayout previewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview_check);
		previewFrameLayout.addView(mPreview);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mCamera != null) {
			mCamera.release();
			finish();
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

				// 人脸识别
				FaceppRec faceppRec = new FaceppRec();
				faceppRec.setDetectCallback(new DetectCallback() {
					@Override
					public void detectResult(JSONObject rst) {
						try {
							// 从返回的JSON结果中，取出result和置信度
							int confidence = rst.getInt("confidence");
							boolean result = rst.getBoolean("is_same_person");

							if (result && confidence > 50) {
								// 验证通过
								Intent intent = new Intent(CheckActivity.this,
										IndexActivity.class);
								intent.putExtra("status", "验证通过^_^");
								startActivity(intent);
							} else {
								// 验证未通过
								CheckActivity.this
										.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(
														CheckActivity.this,
														"验证未通过！！！",
														Toast.LENGTH_SHORT)
														.show();
												Intent intent = new Intent(
														CheckActivity.this,
														MainActivity.class);
												startActivity(intent);
											}
										});
							}
						} catch (Exception e) {
							Log.e(TAG + "takePicture()", e.getMessage());
						}
					}
				});
				faceppRec.recognize(newBitmap, name, CheckActivity.this);

			} catch (Exception e) {
				Log.e(TAG + "takePicture()", e.getMessage());
			} finally {
				releaseCamera();
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

/**
 * @Description：摄像头预览类
 * @author 农民伯伯
 * @Date 2015.4.18
 */
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
	private final String TAG = "CheckActivity.CameraPreview.";

	private SurfaceHolder mHolder;
	private Camera mCamera;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		// 安装一个SurfaceHolder.Callback，这样创建和销毁底层surface时能够获得通知。
		mHolder = getHolder();
		mHolder.addCallback(this);

		// 已过期的设置，但版本低于3.0的Android还需要
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// surface已被创建，现在把预览画面的位置通知摄像头
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(TAG + "surfaceCreated()",
					"Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO::注意在activity中释放摄像头预览对象
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

		// 如果预览无法更改或旋转，注意此处的事件,确保在缩放或重排时停止预览
		if (mHolder.getSurface() == null) {
			// 预览surface不存在
			Log.d(TAG + "surfaceChanged()", "surface不存在");
			return;
		}

		// 更改时停止预览
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// 忽略：试图停止不存在的预览
		}

		// 在此进行缩放、旋转和重新组织格式,以新的设置启动预览
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d(TAG + "surfaceChanged()",
					"Error starting camera preview: " + e.getMessage());
		}
	}
}
