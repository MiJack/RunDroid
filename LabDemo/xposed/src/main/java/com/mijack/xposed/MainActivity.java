package com.mijack.xposed;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends Activity /*implements View.OnClickListener*/ {

	private static final int REQUEST_CODE = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		findViewById(R.id.btn).setOnClickListener(this);
	}

//	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
//			case R.id.btn:
//				if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//					copyToSD();
//				} else {
//					ActivityCompat.requestPermissions(this,
//									new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
//													Manifest.permission.READ_EXTERNAL_STORAGE},
//									REQUEST_CODE);
//				}
//				break;
//		}
//	}
//
//	@Override
//	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//		switch (requestCode) {
//			case REQUEST_CODE:
//				if (isAllGrant(grantResults)) {
//					copyToSD();
//				}
//				break;
//		}
//	}
//
//	private boolean isAllGrant(int[] grantResults) {
//		for (int i = 0; i < grantResults.length; i++) {
//			if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//	private void copyToSD() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				InputStream is = null;
//				FileWriter fw = null;
//				BufferedReader br = null;
//				try {
//					String fileName = XposedLoadPackageHook.FRAMEWORK + XposedLoadPackageHook.MLF_FILE_SUFFIX;
//					File mlf = new File(Environment.getExternalStorageDirectory(), fileName);
//					is = getAssets().open(fileName);
//					br = new BufferedReader(new InputStreamReader(is));
//					fw = new FileWriter(mlf);
//					String line = null;
//					while ((line = br.readLine()) != null) {
//						fw.write(line);
//						fw.write("\n");
//					}
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							Toast.makeText(MainActivity.this, "拷贝成功", Toast.LENGTH_SHORT).show();
//						}
//					});
//				} catch (IOException e) {
//					e.printStackTrace();
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							Toast.makeText(MainActivity.this, "拷贝失败", Toast.LENGTH_SHORT).show();
//						}
//					});
//				} finally {
//					close(is, br, fw);
//				}
//			}
//		}).start();
//
//	}
//
//	private void close(@NonNull Closeable... closeable) {
//		for (int i = 0; i < closeable.length; i++) {
//			try {
//				closeable[i].close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
}
