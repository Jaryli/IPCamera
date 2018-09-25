package com.thinkjoy.iplook;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

/**
 * 主线程
 */
public class MonitorActivity extends Activity {

	private SurfaceView sf_VideoMonitor;

	private final StartRenderingReceiver receiver = new StartRenderingReceiver();
	/**
	 * 返回标记
	 */
	private boolean backflag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		// 设置用于发广播的上下文
		HC_DVRManager.getInstance().setContext(getApplicationContext());
		initView();
	}

	private DeviceBean getDeviceBean() {
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"dbinfo", 0);
		String ip = sharedPreferences.getString("ip", "192.168.0.235");
		String port = sharedPreferences.getString("port", "8000");
		String userName = sharedPreferences.getString("userName", "admin");
		String passWord = sharedPreferences.getString("passWord", "Thinkjoy2015");
		String channel = sharedPreferences.getString("channel", "1");
		DeviceBean bean = new DeviceBean();
		// bean.setIP("192.168.10.64");
		// bean.setPort("8000");
		// bean.setUserName("admin");
		// bean.setPassWord("123");
		// bean.setChannel("1");
		bean.setIP(ip);
		bean.setPort(port);
		bean.setUserName(userName);
		bean.setPassWord(passWord);
		bean.setChannel(channel);
		return bean;
	}

	// 向系统中存入devicebean的相关数据
	public void setDBData(String ip, String port, String userName,
			String passWord, String channel) {
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"dbinfo", 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("ip", ip);
		editor.putString("port", port);
		editor.putString("userName", userName);
		editor.putString("passWord", passWord);
		editor.putString("channel", channel);
		editor.commit();
	}

	protected void startPlay() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(HC_DVRManager.ACTION_START_RENDERING);
		filter.addAction(HC_DVRManager.ACTION_DVR_OUTLINE);
		registerReceiver(receiver, filter);

		if (backflag) {
			backflag = false;
			new Thread() {
				@Override
				public void run() {
					HC_DVRManager.getInstance().setSurfaceHolder(
							sf_VideoMonitor.getHolder());
					HC_DVRManager.getInstance().realPlay();
				}
			}.start();
		} else {
			new Thread() {
				@Override
				public void run() {
					HC_DVRManager.getInstance().setDeviceBean(getDeviceBean());
					HC_DVRManager.getInstance().setSurfaceHolder(
							sf_VideoMonitor.getHolder());
					HC_DVRManager.getInstance().initSDK();
					HC_DVRManager.getInstance().loginDevice();
					HC_DVRManager.getInstance().realPlay();
				}
			}.start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		startPlay();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		new Thread() {
			@Override
			public void run() {
				HC_DVRManager.getInstance().logoutDevice();
				HC_DVRManager.getInstance().freeSDK();
			}
		}.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new Thread() {
				@Override
				public void run() {
					HC_DVRManager.getInstance().stopPlay();
				}
			}.start();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 初始化
	 */
	private void initView() {
		// 获取手机分辨率
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		sf_VideoMonitor = (SurfaceView) findViewById(R.id.sf_VideoMonitor);

		LayoutParams lp = sf_VideoMonitor.getLayoutParams();
		lp.width = dm.widthPixels - 30;
		lp.height = lp.width / 16 * 9;
		sf_VideoMonitor.setLayoutParams(lp);
		Log.d("DEBUG", "视频窗口尺寸：" + lp.width + "x" + lp.height);

		sf_VideoMonitor.getHolder().addCallback(new Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.d("DEBUG", getLocalClassName() + " surfaceDestroyed");
				sf_VideoMonitor.destroyDrawingCache();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				Log.d("DEBUG", getLocalClassName() + " surfaceCreated");
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				Log.d("DEBUG", getLocalClassName() + " surfaceChanged");
			}
		});

	}

	// 广播接收器
	private class StartRenderingReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (HC_DVRManager.ACTION_START_RENDERING.equals(intent.getAction())) {
			}
			if (HC_DVRManager.ACTION_DVR_OUTLINE.equals(intent.getAction())) {
			}
		}
	}

}
