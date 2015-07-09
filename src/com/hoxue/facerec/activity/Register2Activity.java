package com.hoxue.facerec.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @Title: Register2Activity.java
 * @Package: com.houxue.facerec.activity
 * @Description: 用户注册时获取人脸信息的界面
 * @author Hou Xue
 * @Date 2015.4.17
 * @version 1.0
 */
public class Register2Activity extends Activity implements Callback,
		PreviewCallback {
	
	private static final String TAG = "Register2Activity.";

	// 预览框
	SurfaceView camerasurface = null;
	Camera camera = null;

	// 前置摄像头layout角度
	private int orientionOfCamera;

	private TextView tips_edtx = null;
	private Button enter_btn = null;

	// 保存图像的路径
	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/frec/";

	// 图像数据
	private Bitmap newBitmap = null;
	
	// 判断拍摄图像是否合格
	private static final int JUDGE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register2);

		initView();
	}

	// 界面初始化
	public void initView() {
		camerasurface = (SurfaceView) findViewById(R.id.surfaceview_register2);
		LayoutParams para = new LayoutParams(800, 1000);
		para.addRule(RelativeLayout.CENTER_IN_PARENT);
		camerasurface.setLayoutParams(para);
		camerasurface.getHolder().addCallback(this);
		camerasurface.setKeepScreenOn(true);

		tips_edtx = (TextView) findViewById(R.id.register2_tips);
		tips_edtx.setText("请正对摄像头...");

		// 确定拍照
		enter_btn = (Button) this.findViewById(R.id.register2_ent);
		enter_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				camera.takePicture(null, null, takePictureCallback);
			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		// 检测摄像头是否被占用
		if (checkCamera(this)) {
			// 获得一个摄像头实例
			camera = getCameraInstance(1);
		}
		CameraInfo info = new CameraInfo();
		orientionOfCamera = info.orientation;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			String result = data.getExtras().getString("result");
			if (result.equals("OK")) {
				// 结束前，回传数据
				setResult(RESULT_OK);
				finish();
			}
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
				matrix.setRotate(orientionOfCamera - 90);
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
				
				// 让相片显示2秒
				tips_edtx.setText("拍照成功...");
				Thread.sleep(1000);

				Intent intent = new Intent(Register2Activity.this,
						InfoSetActivity.class);
				intent.putExtra("imgPath", strCaptureFilePath + "img.jpg");
				startActivityForResult(intent, JUDGE);
			} catch (Exception e) {
				Log.e(TAG + "takePictureCallback()", e.getMessage());
			}
		}
	};

	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		camera.setPreviewCallback(null);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.setDisplayOrientation(90);
		camera.startPreview();
		camera.setPreviewCallback(this);

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}

}
