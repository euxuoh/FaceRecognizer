package com.hoxue.facerec.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import com.houxue.facerec.utils.PreProc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Title: InfoSet2Activity.java
 * @Package: com.houxue.facerec.activity
 * @Description: 注册信息设置界面
 * @author Hou Xue
 * @Date 2015.4.19
 * @version 1.0
 */
public class InfoSet2Activity extends Activity {

	private final String TAG = "InfoSet2Activity";
	private String classifier = null;

	// 保存图片
	private Bitmap bitmap = null;
	private Bitmap normalBitmap = null;

	// 各种控件
	private TextView info_tips = null;
	private ImageView info_img = null;
	private EditText et_name = null;
	private EditText et_psw = null;
	private EditText et_psw2 = null;
	private Button bt_enter = null;

	private boolean isEsitFace = false;

	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/frec/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infosetting);

		getClassifier();

		initView();

		Intent intent = getIntent();
		if (intent != null) {
			// 读取照片并显示
			String imgPath = intent.getStringExtra("imgPath");
			bitmap = BitmapFactory.decodeFile(imgPath);

			if (bitmap != null) {
				info_img.setImageBitmap(bitmap);
			} else {
				info_tips.setText("Picture Not Found!!!");
			}
		}

		// 人脸检测
		localDetectFace(bitmap);

		if (isEsitFace) {
			bt_enter.setEnabled(true);
		} else {
			// 未检测到人脸
			info_tips.setText("未检测到人脸，请重新拍照！！！");
		}

		// 点击确定后，注册个人信息
		bt_enter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				// 创建用户信息
				String userName = et_name.getText().toString();
				String psw = et_psw.getText().toString();
				String psw2 = et_psw2.getText().toString();

				if (!psw.equals("") && psw.equals(psw2)) {
					// -----------------------调用face++
					// API---------------------------------
					/*
					 * // 调用人脸检测，获取人脸信息 FaceppDetect faceppDetect = new
					 * FaceppDetect(); faceppDetect.setDetectCallback(new
					 * DetectCallback() { public void detectResult(JSONObject
					 * rst) { try { final int count = rst.getJSONArray("face")
					 * .length(); if (count != 0) { //
					 * info_tips.setText("注册成功^_^"); } } catch (JSONException e)
					 * { e.printStackTrace(); Toast toast = Toast.makeText(
					 * InfoSetActivity.this, "网络错误！！！", Toast.LENGTH_SHORT);
					 * toast.show(); } } }); faceppDetect.detect(bitmap,
					 * userName, InfoSetActivity.this);
					 */
					// ---------------------------调用face++
					// API-------------------------------------

					store(normalBitmap);

					// 保存用户数据
					SharedPreferences sp = getSharedPreferences("userinfo",
							MODE_PRIVATE);
					Editor editor = sp.edit();
					editor.putString("name", userName);
					editor.putString("password", psw);
					editor.commit();

					// 注册成功，Activity跳转
					Intent intent = new Intent(InfoSet2Activity.this,
							IndexActivity.class);
					intent.putExtra("status", "注册成功^_^");
					startActivity(intent);
					finish();
				} else {
					Toast toast = Toast.makeText(InfoSet2Activity.this,
							"密码有误！！！", Toast.LENGTH_SHORT);
					toast.show();
				}
			}// onClick()
		});
	}

	public void initView() {
		info_tips = (TextView) findViewById(R.id.info_tips);
		info_img = (ImageView) findViewById(R.id.info_img);
		et_name = (EditText) findViewById(R.id.info_name);
		et_psw = (EditText) findViewById(R.id.info_psw);
		et_psw2 = (EditText) findViewById(R.id.info_psw2);
		bt_enter = (Button) findViewById(R.id.info_enter);
		bt_enter.setEnabled(false);
	}

	/**
	 * 获取分类器文件的路径
	 * 
	 *  @param void
	 *  
	 *  @return void
	 */
	public void getClassifier() {
		try {
			InputStream is = getResources().openRawResource(
					R.raw.haarcascade_frontalface_alt_tree);
			File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
			File mCascadeFile = new File(cascadeDir, "haar_face.xml");
			FileOutputStream fos = new FileOutputStream(mCascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
			is.close();
			fos.close();

			classifier = mCascadeFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * 人脸检测
	 * 
	 * @param bm 人脸图像
	 * 
	 * @return void
	 */
	public void localDetectFace(Bitmap bm) {
		Mat srcMat = new Mat();
		Mat normalMat = new Mat();
		PreProc pp = new PreProc(classifier);

		Utils.bitmapToMat(bm, srcMat);
		normalMat = pp.NormalImg(srcMat);

		if (normalMat == null) {
			normalMat = srcMat;
			Log.i(TAG, "normalMat is empty");
		} else {
			isEsitFace = true;
		}
		normalBitmap = Bitmap.createBitmap(normalMat.cols(), normalMat.rows(),
				Config.RGB_565);
		try {
			Utils.matToBitmap(normalMat, normalBitmap);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * 存储规则化后的人脸图像
	 * 
	 * @param bm 规则化后的人脸图像
	 * 
	 * @return void
	 */
	public void store(Bitmap bm) {
		File myCaptureFile = new File(strCaptureFilePath);
		if (!myCaptureFile.exists()) {
			myCaptureFile.mkdirs();
		}
		// 创建文件
		File imgPath = new File(strCaptureFilePath, "normal.jpg");

		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(imgPath));

			// 采用压缩转档方法
			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);

			// 调用flush()方法，更新BufferStream
			bos.flush();

			// 结束OutputStream
			bos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found.");
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}

	}
}
