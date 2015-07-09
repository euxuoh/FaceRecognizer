package com.hoxue.facerec.activity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @Title: MainActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: 系统欢迎界面
 * @author Hou Xue
 * @Date 2015.4.17
 * @version 1.0
 */
public class MainActivity extends Activity {

	private static final String TAG = "MainActivity.";

	// 人脸登录按钮
	private Button login = null;
	// 注册按钮
	private Button register = null;
	// 密码登录按钮
	private Button loginByPsw = null;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case BaseLoaderCallback.SUCCESS:
				Log.i(TAG, "Load success.");
				break;

			default:
				super.onManagerConnected(status);
				Log.i(TAG, "Load failed");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 人脸登录事件监听
		login = (Button) this.findViewById(R.id.btn_login);
		login.setOnClickListener(new OnClickListener() {

			// 点击后跳转至人脸识别界面
			public void onClick(View arg0) {
				Intent loginIntent = new Intent(MainActivity.this,
						LoginActivity.class);
				startActivityForResult(loginIntent, 1);
			}
		});

		// 注册事件监听
		register = (Button) this.findViewById(R.id.btn_register);
		register.setOnClickListener(new OnClickListener() {

			// 点击后跳转至注册界面
			public void onClick(View arg0) {
				Intent registerIntent = new Intent(MainActivity.this,
						Register2Activity.class);
				startActivityForResult(registerIntent, 2);
			}
		});

		// 密码登录事件监听
		loginByPsw = (Button) this.findViewById(R.id.btn_loginByPsw);
		loginByPsw.setOnClickListener(new OnClickListener() {

			// 点击后跳转至密码登录界面
			public void onClick(View arg0) {
				Intent lgPswIntent = new Intent(MainActivity.this,
						LoginByPswActivity.class);
				startActivityForResult(lgPswIntent, 3);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10,
				getApplicationContext(), mLoaderCallback);
		Log.i(TAG, "onResume success load OpenCV...");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
