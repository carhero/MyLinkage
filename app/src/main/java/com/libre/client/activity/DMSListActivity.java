package com.libre.client.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.libre.client.util.DMSBrowseHelper;
import com.libre.client.util.NetUiUtils;
import com.libre.client.util.UpnpDeviceManager;

import org.fourthline.cling.model.meta.RemoteDevice;

import java.util.Collection;

public class DMSListActivity extends UpnpListenerActivity {
	private static final String TAG = DMSListActivity.class.getName();
	private UpnpProcessor m_upnpProcessor;
	private RemoteDMSArrayAdapter m_adapter;
	private TextView m_mydevice;
	private ImageButton m_mydeviceIcon;
	private ListView m_lvDevices;
	private ImageButton m_back;
	private Button m_refresh;
	private ImageButton m_nowPlaying;
	private Animation m_operatingAnim = null;
	private LibreApplication m_myApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		NetUiUtils.canNetWorkOperateInMainThread();
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.dmslist_activity);
		m_myApp = (LibreApplication)getApplication();
		m_adapter = new RemoteDMSArrayAdapter(this, 0, 0);
		
		m_back = (ImageButton) findViewById(R.id.back);
		m_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
		});
		
		m_lvDevices = (ListView) findViewById(R.id.lv_ListRenderer);
		m_lvDevices.setAdapter(m_adapter);
		m_lvDevices.setOnItemClickListener(itemClickListener);
		m_upnpProcessor = new UpnpProcessorImpl(DMSListActivity.this);
		m_upnpProcessor.bindUpnpService();
		m_mydevice = (TextView) findViewById(R.id.mydevice);
		m_mydevice.setText(LibreApplication.MY_MODEL);
		m_mydevice.setOnClickListener(m_onMyDeviceClick);
		m_mydeviceIcon = (ImageButton) findViewById(R.id.mydeviceIcon);
		m_mydeviceIcon.setOnClickListener(m_onMyDeviceClick);
		
		m_operatingAnim = AnimationUtils.loadAnimation(this, R.anim.btnani);
		LinearInterpolator lin = new LinearInterpolator();
		m_operatingAnim.setInterpolator(lin);
		m_refresh = (Button) findViewById(R.id.dmslistRefresh);
		m_refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				m_refresh.startAnimation(m_operatingAnim);
				m_adapter.clear();
				m_lvDevices.setAdapter(null);
				Collection<RemoteDevice> devices = UpnpDeviceManager.getInstance().getRemoteDms();
				if (devices == null) return;
				m_adapter.addAll(devices);
				m_adapter.notifyDataSetChanged();
				m_lvDevices.setAdapter(m_adapter);
			}
		});
		
		m_nowPlaying = (ImageButton) findViewById(R.id.nowplaying);
		m_nowPlaying.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(DMSListActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
		});
		
		if (m_myApp.getDmsBrowseHelperSaved() != null) {
			m_myApp.setDmsBrowseHelperTemp(m_myApp.getDmsBrowseHelperSaved().clone());

			Intent intent = new Intent(DMSListActivity.this, DMSBrowserActivity.class);
            Constant.isUpNPbroswer=false;
			DMSListActivity.this.startActivity(intent);
		}
	}

	private OnClickListener m_onMyDeviceClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			m_myApp.setMusicUdn(m_myApp.getLocalDeviceUdn());
			m_myApp.setDmsBrowseHelperTemp(
					new DMSBrowseHelper(true, LibreApplication.LOCAL_UDN));
			
			Intent intent = new Intent(DMSListActivity.this, DMSBrowserActivity.class);
			startActivity(intent);
            Constant.isUpNPbroswer=false;
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		
		m_adapter.clear();
		Collection<RemoteDevice> devices = UpnpDeviceManager.getInstance().getRemoteDms();
		if (devices == null) {
			m_refresh.performClick();
			return;
		}
		m_adapter.addAll(devices);
		m_adapter.notifyDataSetChanged();
		
		if (m_upnpProcessor != null) {
			m_upnpProcessor.addListener(DMSListActivity.this);
		}
	}

	protected void onPause() {
		super.onPause();
		if (m_upnpProcessor != null) {
			m_upnpProcessor.removeListener(DMSListActivity.this);
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (m_upnpProcessor != null)
			m_upnpProcessor.unbindUpnpService();
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	private void refresh() {
		m_adapter.clear();
		m_upnpProcessor.searchDMS();
//		m_upnpProcessor.searchAll();
	}



	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
			RemoteDevice device = m_adapter.getItem(position);
//			((ClintApplication)getApplication()).setMusicUdn(device.getIdentity().getUdn().toString());
			m_myApp.setDmsBrowseHelperTemp(
					new DMSBrowseHelper(false, device.getIdentity().getUdn().toString()));
            Constant.isUpNPbroswer=true;
			
			Intent intent = new Intent(DMSListActivity.this, DMSBrowserActivity.class);
			DMSListActivity.this.startActivity(intent);

			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}
	};

	public void onRemoteDeviceAdded(final RemoteDevice device) {
		runOnUiThread(new Runnable() {

			public void run() {
				if (device.getType().getNamespace().equals(UpnpProcessorImpl.DMS_NAMESPACE) && 
						device.getType().getType().equals(UpnpProcessorImpl.DMS_TYPE)) {
					Log.i(TAG, "Remote dms added:" + device.getDetails().getFriendlyName());
					m_adapter.add(device);
					m_adapter.notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	public void onRemoteDeviceRemoved(final RemoteDevice device) {
		runOnUiThread(new Runnable() {

			public void run() {
				if (device.getType().getNamespace().equals(UpnpProcessorImpl.DMS_NAMESPACE) && 
						device.getType().getType().equals(UpnpProcessorImpl.DMS_TYPE)) {

					Log.i(TAG, "Remote dms removed:" + device.getDetails().getFriendlyName());
					m_adapter.remove(device);
					m_adapter.notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	public void onStartComplete() {
	//	Toast.makeText(DMSListActivity.this, "Start upnp service complete", Toast.LENGTH_SHORT).show();
		refresh();
//		m_adapter.clear();
//		Collection<RemoteDevice> devices = m_upnpProcessor.getRemoteDMS();
		Collection<RemoteDevice> devices = UpnpDeviceManager.getInstance().getRemoteDms();
		if (devices == null) return;
		m_adapter.addAll(devices);
		m_adapter.notifyDataSetChanged();
	}

}