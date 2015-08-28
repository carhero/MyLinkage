package com.libre.client.activity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.libre.client.AppPreference;
import com.libre.client.LSSDPNodeDB;
import com.libre.client.LSSDPNodes;
import com.libre.client.util.NetUiUtils;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DMRActivity extends BaseActivity {
	private final static String TAG = "DMR ACTIVITY";
	private ListView m_listView;
	private Button m_refresh;
	private TextView m_mydevice;
	private Animation m_operatingAnim = null;
	private UpnpProcessor m_upnpProcessor = null;
	private LibreApplication m_myApp;
	ImageButton back;
	DMRAdapter m_dmradapter;
	private static List<DMRDev> devList = new ArrayList<DMRDev>();
		@Override
	protected void loadViewLayout() {
		setContentView(R.layout.mastersetting);        
		NetUiUtils.canNetWorkOperateInMainThread();
	}
		private void StartLSSDPScan() {
		//m_myApp.getScanThread().addhandler(handler); 
	     m_myApp.getScanThread().UpdateNodes();

		}
	@Override
	protected void findViewById() {
		m_operatingAnim = AnimationUtils.loadAnimation(this, R.anim.btnani);
		LinearInterpolator lin = new LinearInterpolator();
		m_operatingAnim.setInterpolator(lin);
		m_refresh = (Button) findViewById(R.id.masterRefresh);
		m_listView = (ListView) findViewById(R.id.speakers);
		m_mydevice=(TextView) findViewById(R.id.mydevice);
		m_mydevice.setText(LibreApplication.MY_MODEL);
		
		 back=(ImageButton) findViewById(R.id.advance);
		
		
	}

	@Override
	protected void setListener() {
		m_refresh.setOnClickListener(this);
		m_mydevice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG,"m_mydevice onClick");
				m_myApp.setCurrentDmrDeviceUdn(LibreApplication.LOCAL_UDN);
				startActivity(new Intent(DMRActivity.this, MainActivity.class));
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
		back.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				
				
				startActivity(new Intent(DMRActivity.this,
						MainActivity.class));
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				finish();
			}
		});
	}

	@Override
	protected void processLogic() {
		m_myApp = (LibreApplication)getApplication();
		m_upnpProcessor = new UpnpProcessorImpl(DMRActivity.this);
		m_upnpProcessor.bindUpnpService();
		
		m_upnpProcessor.addListener(this);
		m_listView.setAdapter(new DMRAdapter(DMRActivity.this, null));

		//if (m_upnpProcessor.getRemoteDMR() == null) {
			//Log.v(TAG,"m_mydevice onClick");
			m_upnpProcessor.searchAll();
	
			m_refresh.performClick();
		//}
			AppPreference.PREF = PreferenceManager.getDefaultSharedPreferences(DMRActivity.this);
			Log.v(TAG,"-------------"+AppPreference.HideGroupMembers());
	}
	
	@Override
	protected void onCreate(Bundle paramBundle) {
		// TODO Auto-generated method stub
		super.onCreate(paramBundle);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		m_upnpProcessor.searchAll();
		//m_refresh.performClick();
		StartLSSDPScan();
		super.onResume();
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.masterRefresh:
			m_refresh.startAnimation(m_operatingAnim);
			devList.clear();
			if (m_upnpProcessor != null) {
				
				m_upnpProcessor.searchDMR();
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
				  @Override
				  public void run() {
				    //Do something after 100ms
					  findDevs(m_upnpProcessor.getRemoteDMR());
					  m_dmradapter=new DMRAdapter(DMRActivity.this, devList);
						
						m_listView.setAdapter(m_dmradapter);
				  }
				}, 500);
			}
			break;
		default:
			break;
		}
	}



	class DMRAdapter extends BaseAdapter {

		private Context context;
		private List<DMRDev> devList = new ArrayList<DMRDev>();
		private LayoutInflater inflater;

		public DMRAdapter(Context context, List<DMRDev> devs) {
			this.context = context;
			if (devs != null)
				this.devList = devs;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return devList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		public boolean UpdateData(Context context,List<DMRDev> models) {

			
			this.context = context;
			this.devList = models;
			notifyDataSetChanged();
			return true;
		}
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		public int findinLSSDP_DB(int position)
		{
			LSSDPNodeDB LSSDPDB_ = LSSDPNodeDB.getInstance( );
			
			for (LSSDPNodes node  : LSSDPDB_.GetDB())
		 	{
		 		if(devList.get(position).getIp().equals(node.getIP()))
		 		{
		 			Log.v(TAG,"Found a match --");
		 			if(node.getDeviceState().equals("M"))
		 			{
		 				return 1;
		 			}
		 			else if(node.getDeviceState().equals("S"))
		 				return 2;
		 			else
		 				return 3;
		 			
		 		}
		 	}
			return -1;
		}
		@SuppressLint("InflateParams")
		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			ViewHolder viewHolder;
			if (view == null) {
				view = inflater.inflate(R.layout.item_master, null);
				viewHolder = new ViewHolder();
				viewHolder.speakername = (TextView) view.findViewById(R.id.speakername);
				viewHolder.music = (ImageButton) view.findViewById(R.id.music);
				viewHolder.master = (ImageButton) view.findViewById(R.id.masterset);
				viewHolder.pBar = (ProgressBar) view.findViewById(R.id.progress);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			final DMRDev devView = devList.get(position);
			if (devView == null) return view;
				viewHolder.speakername.setText(devView.getDevName());
				int state=findinLSSDP_DB(position);
				if(state==-1)
				{
					viewHolder.master.setBackgroundResource(R.drawable.speaker_default);
				}
				else if (state==1)
					viewHolder.master.setBackgroundResource(R.drawable.master);
				else if (state==2)
					viewHolder.master.setBackgroundResource(R.drawable.group);
				else
					viewHolder.master.setBackgroundResource(R.drawable.free);
				
				viewHolder.speakername.setTextColor(getResources().getColor(R.color.black));
			
			
			//	viewHolder.pBar.setVisibility(View.VISIBLE);
			
				viewHolder.pBar.setVisibility(View.INVISIBLE);
			
			
			viewHolder.speakername.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					DMRActivity masterActivtiy = (DMRActivity)context;
					DMRDev dev = devView;
					m_myApp.setCurrentDmrDeviceUdn(dev.getUuid());
					m_myApp.setSpeakerName(dev.getDevName());
					masterActivtiy.startActivity(new Intent(masterActivtiy, MainActivity.class));
//					masterActivtiy.finish();
				}
			});
			


			viewHolder.master.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					}
			});

			viewHolder.music.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
				}
			});
			
			return view;
			
		}
		
		class ViewHolder {
			ImageButton master;
			ImageButton music;
			TextView speakername;
			ProgressBar pBar;
		}
		
		
		
	}

	
	@Override
	public void onBackPressed() {
		
		startActivity(new Intent(DMRActivity.this, MainActivity.class));
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	
	@Override
	protected void onDestroy() {
		if (m_upnpProcessor != null) {
			m_upnpProcessor.unbindUpnpService();
		}
		
	
		super.onDestroy();
	}

	
	public static void findDevs(Collection<RemoteDevice> devices) {
        devList.clear();
        if(devices==null)
			return;

		for (RemoteDevice device : devices) {
			String ip = device.getIdentity().getDescriptorURL().getHost();
			String uuid = device.getIdentity().getUdn().toString();
			String name = device.getDetails().getFriendlyName();
			boolean same = false;
			boolean itsaslave = false;
			
			if (ip.equals(LibreApplication.LOCAL_IP)) {
				Log.i(TAG, "skip local ip:" + ip);
				continue;
			}
			Log.v(TAG,"UPNP Dev found="+name+"IP="+ip);
			LSSDPNodeDB LSSDPDB_ = LSSDPNodeDB.getInstance();
			for (LSSDPNodes node  : LSSDPDB_.GetDB())
		 	{
				Log.v(TAG,"findDevs Response IP= "+node.getIP()+" ZoneID= "+node.getZoneID()
						  +" Name= "+node.getFriendlyname()+"State= "+node.getDeviceState());
		 		if(ip.equals(node.getIP()))
		 		{
		 			Log.v(TAG,"Found a match of IP--");
		 			if(node.getDeviceState().equals("S") && AppPreference.HideGroupMembers())
		 			{
		 				Log.v(TAG,"And its a Slave too ");
		 				itsaslave=true;
		 			}
		 		}
		 	}
			if(itsaslave)
			{
				itsaslave=false;
				continue;
			}
			Log.w(TAG,"----------------------------------->Device found "+name);
			devList.add(new DMRDev(ip,uuid,name));
			//devsFound.add(new Dev(ip, uuid, name));
		}
	}

	@Override
	public void onStartComplete() {
		// TODO Auto-generated method stub
		//Collection<RemoteDevice> 
		Log.v(TAG,"onStartComplete");
		findDevs(m_upnpProcessor.getRemoteDMR());
		
		m_upnpProcessor.searchDMR();
	}
	
	@Override
	public void onLocalDeviceAdded(LocalDevice device) {
		
		Log.v(TAG,"onLocalDeviceAdded");
		
	}

	@Override
	public void onLocalDeviceRemoved(LocalDevice device) {
		
		Log.d(TAG,"onLocalDeviceRemoved");
		
	}
	
	public void onRemoteDeviceAdded(RemoteDevice device)
	{
		Log.d(TAG,"onRemoteDeviceAdded.....");

		
	}
	public void onRemoteDeviceRemoved(RemoteDevice device)
	{
		Log.d(TAG,"onRemoteDeviceRemoved.....");
	}
}