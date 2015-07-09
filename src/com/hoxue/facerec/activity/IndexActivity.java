package com.hoxue.facerec.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * @Title: IndexActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: 登录/注册成功界面
 * @author Hou Xue
 * @Date 2015.4.17
 * @version 1.0
 */
public class IndexActivity extends Activity {
	
	// private static final String TAG = "IndexActivity."; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
		
		// 获取从上一个Activity中传来的status
		Intent intent = getIntent();
		String status = intent.getStringExtra("status");
		
		Toast.makeText(IndexActivity.this, status, Toast.LENGTH_LONG).show();
	}
}
