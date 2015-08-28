package com.libre.client.activity;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.libre.client.util.ThreadPoolManager;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
//

public abstract class BaseActivity extends UpnpListenerActivity implements View.OnClickListener {
	protected Context context;
	protected ThreadPoolManager threadPoolManager;
	
	public BaseActivity() {
		threadPoolManager = ThreadPoolManager.getInstance();
	}
	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		init();
	}
	private void init() {
		loadViewLayout();
		findViewById();
		setListener();
		processLogic();
	}



	protected abstract void loadViewLayout();
	protected abstract void findViewById();
	protected abstract void setListener();
	protected abstract void processLogic();
}
