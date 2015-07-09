package com.houxue.facerec.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.hoxue.facerec.activity.R;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class FaceDetectService extends Service {

	//private String classifier = null;
	private HttpRequests httpRequests = null;
	private JSONObject result = null;
	private boolean isFaceEsit = false;
	private final IBinder mBinder = new LocalBinder();
	
	private final String TAG = "FaceDetectService"; 
	
	class LocalBinder extends Binder {
		FaceDetectService getService() {
			return FaceDetectService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public boolean onUnblind(Intent intent) {
		return false;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public void onStart(Intent intent, int startId) {
		getClassifier();
	}
	
	/**
	 * 人脸检测
	 * 
	 * @param bm
	 *            人脸图像
	 * 
	 * @return void
	 */
	public void localDetectFace(final Bitmap bm) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				// 重新构造BitMap
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] array = stream.toByteArray();

				try {
					httpRequests = new HttpRequests(
							"13307dabf988e178af502b1e3851af1d",
							"T-8pBfIQI8XJTMi3BHSruL7iIrO49Ket", true, false);
					// 检测
					result = httpRequests.detectionDetect(new PostParameters()
							.setImg(array));

					if (result.getJSONArray("face").length() > 0) {
						isFaceEsit = true;
					}
				} catch (Exception e) {
					Log.e(TAG + ".localDetectFace", e.toString());
				}

			}
		});
	}
	
	public boolean isEsit() {
		return isFaceEsit;
	}
	
	public void getClassifier() {
		try {
			InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt_tree);
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

			//classifier = mCascadeFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}

}
