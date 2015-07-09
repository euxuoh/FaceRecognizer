package com.houxue.facerec.utils;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class FaceppDetect {

	DetectCallback callback = null;
	
	public void setDetectCallback(DetectCallback detectCallback) {
		callback = detectCallback;
	}

	public void detect(final Bitmap image, final String name, final Context context) {
		
		new Thread(new Runnable() {
			
			public void run() {
				// 调用Face++API
				HttpRequests httpRequests = new HttpRequests("13307dabf988e178af502b1e3851af1d", "T-8pBfIQI8XJTMi3BHSruL7iIrO49Ket", true, false);
	    		
				// 重新构造BitMap
	    		ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    		float scale = Math.min(1, Math.min(600f / image.getWidth(), 600f / image.getHeight()));
	    		Matrix matrix = new Matrix();
	    		matrix.postScale(scale, scale);

	    		Bitmap imgSmall = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
	    		imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	    		byte[] array = stream.toByteArray();
	    		
	    		try {
	    			// 检测
					JSONObject result = httpRequests.detectionDetect(new PostParameters().setImg(array));
					
					// 创建person
					httpRequests.personCreate(new PostParameters().setPersonName(name));
					
					// 为person添加face
					httpRequests.personAddFace(new PostParameters().setPersonName(name)
																	.setFaceId(result.getJSONArray("face")
																			.getJSONObject(0).getString("face_id")));
					// 训练person
					JSONObject sync = httpRequests.trainVerify(new PostParameters().setPersonName(name));
					System.out.println(sync);
					
					// 调用回调函数
					if (callback != null) {
						callback.detectResult(result);
					}
				} catch (FaceppParseException e) {
					Log.e("FaceppDetect.detect()", e.toString());
					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							Toast toast = Toast.makeText(context, "用户已存在！！！", Toast.LENGTH_SHORT);
							toast.show();
						}
					});
				} catch (JSONException e) {
					Log.e("FaceppDetect.detect()", e.toString());
				}
			}
		}).start();
	}
}
