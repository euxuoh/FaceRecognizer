package com.hoxue.facerec.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @Title: LoginByPswActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: 密码登录界面
 * @author Hou Xue
 * @Date 2015.4.17
 * @version 1.0
 */
public class LoginByPswActivity extends Activity {
	
	// private static final String TAG = "LoginByPswActivity.";

	// 密码
	private String password = null;
	// 登录按钮
	private Button login = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginbypsw);
		
		SharedPreferences sp = getSharedPreferences("userinfo", MODE_PRIVATE);
		final String name = sp.getString("name", "default");
		final String password2 = sp.getString("password", "default");
		
		if (name.equals("default")) {
			Toast.makeText(LoginByPswActivity.this, "首次使用，请先注册！", Toast.LENGTH_SHORT).show();
		}
		
		// 登录事件监听
		login = (Button)this.findViewById(R.id.loginbypsw);
		login.setOnClickListener(new OnClickListener() {
			
			// 点击后验证密码是否正确
			public void onClick(View arg) {
				EditText psw = (EditText)findViewById(R.id.input_psw);
				password = psw.getText().toString();
				
				// 密码正确跳转至应用首页
				if (password.equals(password2)) {
					// 结束前，回传数据
					setResult(RESULT_OK);
					
					Intent indexIntent = new Intent(LoginByPswActivity.this, IndexActivity.class);
					indexIntent.putExtra("status", "登录成功^_^");
					startActivity(indexIntent);
					
					finish();
				} else {
					Toast.makeText(LoginByPswActivity.this, "密码错误！！！", Toast.LENGTH_SHORT).show();
				}
			}
		});		
	}
}
