package com.hoxue.facerec.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONObject;

import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

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
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @Title: LoginActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: 注册信息设置界面
 * @author Hou Xue
 * @Date 2015.4.19
 * @version 1.0
 */
public class LoginActivity extends Activity implements Callback,
		PreviewCallback {

	private static final String TAG = "LoginActivity.";

	// 预览框
	SurfaceView camerasurface = null;
	Camera camera = null;

	// 前置摄像头layout角度
	private int orientionOfCamera;

	// 各种控件
	private TextView tips_edtx = null;
	private Button enter_btn = null;

	// 人脸图像
	private Bitmap newBitmap = null;

	// 用户姓名
	private SharedPreferences sp = null;
	private String name = null;

	// Handler控制码
	private final int SUCCESS = 1;
	private final int FAILED = 0;

	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SUCCESS:
				// 回传数据
				setResult(RESULT_OK);

				// 跳转至下一个Activity
				Intent intent = new Intent(LoginActivity.this,
						IndexActivity.class);
				intent.putExtra("status", "验证通过^-^");
				startActivity(intent);

				finish();
				break;

			case FAILED:
				Toast.makeText(LoginActivity.this, "验证失败！！！",
						Toast.LENGTH_SHORT).show();
				// 验证失败，重新拍照
				Intent intent2 = new Intent(LoginActivity.this,
						LoginActivity.class);
				startActivity(intent2);
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		initView();
	}

	public void initView() {
		camerasurface = (SurfaceView) findViewById(R.id.surfaceview_login);
		LayoutParams para = new LayoutParams(800, 1000);
		para.addRule(RelativeLayout.CENTER_IN_PARENT);
		camerasurface.setLayoutParams(para);
		camerasurface.getHolder().addCallback(this);
		camerasurface.setKeepScreenOn(true);

		tips_edtx = (TextView) findViewById(R.id.login_tips);
		tips_edtx.setText("请正对摄像头...");

		enter_btn = (Button) findViewById(R.id.login_ent);
		enter_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				camera.takePicture(null, null, takePictureCallback);
				enter_btn.setEnabled(false);
			}
		});

		sp = getSharedPreferences("userinfo", MODE_PRIVATE);
		name = sp.getString("name", "default");

		if (name.equals("default")) {
			Toast.makeText(LoginActivity.this, "首次使用，请先注册！", Toast.LENGTH_SHORT)
					.show();
			enter_btn.setEnabled(false);
		} else {
			enter_btn.setEnabled(true);
		}
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
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

				// 身份验证
				verify(newBitmap);

				tips_edtx.setText("正在验证......");
			} catch (Exception e) {
				Log.e(TAG + "takepicture()", e.getMessage());
			}
		}
	};

	/**
	 * 人脸识别，身份验证
	 * 
	 * @param bm
	 *            人脸图像
	 * 
	 * @return void
	 */
	public void verify(final Bitmap bm) {

		// 在新线程中调用在线API
		new Thread(new Runnable() {

			@Override
			public void run() {
				// 调用Face++API
				HttpRequests httpRequests = new HttpRequests(
						"13307dabf988e178af502b1e3851af1d",
						"T-8pBfIQI8XJTMi3BHSruL7iIrO49Ket", true, false);

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] array = stream.toByteArray();

				try {
					String face_id = null;
					// 人脸检测结果
					JSONObject result = httpRequests
							.detectionDetect(new PostParameters().setImg(array));

					if (result.getJSONArray("face").length() > 0) {
						// 从result中获取face_id
						face_id = result.getJSONArray("face").getJSONObject(0)
								.getString("face_id");

						// 获取人脸识别验证结果
						JSONObject recResult = httpRequests
								.recognitionVerify(new PostParameters()
										.setPersonName(name).setFaceId(face_id));

						// 从返回的JSON结果中，取出result和置信度
						int confidence = recResult.getInt("confidence");
						boolean result2 = recResult
								.getBoolean("is_same_person");

						if (result2 && confidence > 0) {
							Message msg = new Message();
							msg.what = SUCCESS;
							LoginActivity.this.myHandler.sendMessage(msg);
						} else {
							Message msg = new Message();
							msg.what = FAILED;
							LoginActivity.this.myHandler.sendMessage(msg);
						}
					} else {
						Message msg = new Message();
						msg.what = FAILED;
						LoginActivity.this.myHandler.sendMessage(msg);
					}
				} catch (Exception e) {
					Log.e(TAG + "verify()", e.getMessage());
				}
			}
		}).start();
	}

	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
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
