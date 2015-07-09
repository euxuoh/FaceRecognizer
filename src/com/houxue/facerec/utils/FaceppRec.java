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

/**
 * @Description：人脸检测类
 * @author houxue
 * @date 2015.4.21
 */
public class FaceppRec {
	
	DetectCallback callback = null;
	
	public void setDetectCallback(DetectCallback detectCallback) {
		callback = detectCallback;
	}

	public void recognize(final Bitmap image, final String name, final Context context) {
		
		new Thread(new Runnable() {
			
			public void run() {
				// 调用Face++API
				HttpRequests httpRequests = new HttpRequests("13307dabf988e178af502b1e3851af1d", "T-8pBfIQI8XJTMi3BHSruL7iIrO49Ket", true, false);
	    		
	    		ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    		float scale = Math.min(1, Math.min(600f / image.getWidth(), 600f / image.getHeight()));
	    		Matrix matrix = new Matrix();
	    		matrix.postScale(scale, scale);

	    		Bitmap imgSmall = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
	    		imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	    		byte[] array = stream.toByteArray();
	    		
	    		try {
	    			String face_id = null;
	    			// 人脸检测结果
					JSONObject result = httpRequests.detectionDetect(new PostParameters().setImg(array));
					
					if(result.getJSONArray("face").length() > 0) {
						// 从result中获取face_id
						face_id = result.getJSONArray("face").getJSONObject(0).getString("face_id");
						
						// 获取人脸识别验证结果
						JSONObject recResult = httpRequests.recognitionVerify(new PostParameters().setPersonName(name).setFaceId(face_id));
						
						// 调用回调函数
						if (callback != null) {
							callback.detectResult(recResult);
						}
						
					} else {
						// 未检测到人脸
						((Activity) context).runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(context, "未检测到人脸！！！", Toast.LENGTH_SHORT).show();
							}
						});
					}
					
				} catch (FaceppParseException e) {
					Log.e("FaceppRec.recognize()", e.toString());
					((Activity) context).runOnUiThread(new Runnable() {
						public void run() {
							Toast toast = Toast.makeText(context, "验证失败！！！", Toast.LENGTH_SHORT);
							toast.show();
						}
					});
				} catch (JSONException e) {
					Log.e("FaceppRec.recognize()", e.toString());
				}
			}
		}).start();
	}

}
