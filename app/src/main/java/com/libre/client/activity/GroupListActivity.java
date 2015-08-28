package com.libre.client.activity;
/*********************************************************************************************
 * 
 * Copyright (C) 2014 Libre Wireless Technology
 *
 * "Junk Yard Lab" Project
 * 
 * Libre Sync Android App
 * Author: Subhajeet Roy
 *  
***********************************************************************************************/

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.libre.client.AppPreference;
import com.libre.client.util.PlaybackHelper;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.libre.client.activity.R.menu.dmr;

public class GroupListActivity extends BaseActivity {
	protected static final String TAG = "GroupListActivity";


	HashMap<String, String> groupListOwner = new HashMap<String, String>();

	ArrayList<HashMap<String, String>> ddmsnodeList;
	protected static List<DMRDev> devList = new ArrayList<DMRDev>();
    private static String SSID;

	// All static variables
	
	static final String KEY_SONG = "song"; // parent node
	static final String KEY_ID = "id";
	static final String KEY_STATE = "state";
	static final String KEY_NAME = "name";
    static final String KEY_TYPE = "type";
	static final String KEY_IP = "IP";
	static final String KEY_THUMB_URL = "thumb_url";
	static final String KEY_ZONEID = "Zone_ID";
	static final String MASTER    = "Zone Master";
	static final String STATION    = "Zone Station";
    static final String KEY_cSSID= "DDMSConcurrentSSID";


	
	///Views
	Dialog custom;
	EditText Fname;
	Button savebtn;
	Button canbtn;
	ListView list;
    LazyAdapter adapter;
    ImageButton back;
    private TextView devcount;


	///

 
    //other variables

	private PlaybackHelper m_playbackHelper;
    private AlertDialog instance;
    static int count;
    private int devcount_=0;
	int check_selected=-1;
	private LibreApplication m_myApp;
	String devicename;
	int HostPresent=0;
	private UpnpProcessor m_upnpProcessor = null;
    Fragment fragment;

    protected static Handler ssidHandler;



    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        m_myApp = (LibreApplication) getApplication();
        m_myApp.getScanThread().removehandler();



        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ZoneMasterFragment.newInstance())
                    .commit();
        }




    }



    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.grouplistmain);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    @Override
    protected void findViewById() {




    }

    @Override
    protected void setListener() {


    }

    @Override
    protected void processLogic() {
        m_upnpProcessor = new UpnpProcessorImpl(GroupListActivity.this);
        m_upnpProcessor.bindUpnpService();

        m_upnpProcessor.addListener(this);
        m_upnpProcessor.searchAll();

    }

    @Override
    protected void onDestroy() {
        if (m_upnpProcessor != null) {
            m_upnpProcessor.unbindUpnpService();
        }


        super.onDestroy();
    }


     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         if (AppPreference.ShowDMRRefreshButtomn())

         getMenuInflater().inflate(dmr, menu);




         return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {


            case R.id.dmr_menu_refresh:
                if (m_upnpProcessor != null) {

                    m_upnpProcessor.searchDMR();

                    final Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            m_upnpProcessor.searchDMR();
                            Log.d(TAG,"First Searh");
                        }
                    });

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            m_upnpProcessor.searchDMR();
                            findDevs(m_upnpProcessor.getRemoteDMR());
                            Log.d(TAG,"Second Searh");
                        }
                    },100);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG,"Thired Searh");
                            m_upnpProcessor.searchDMR();
                        }
                    },300);


                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG,"Receive Search");
                            //Do something after 100ms
                            findDevs(m_upnpProcessor.getRemoteDMR());


                        }
                    }, 600);
                    dmrRefresh();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (m_upnpProcessor != null) {

            m_upnpProcessor.searchDMR();
            m_myApp.getScanThread().clearNodes();
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    m_upnpProcessor.searchDMR();
                    Log.d(TAG,"First Searh");
                }
            });

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    m_upnpProcessor.searchDMR();
                    findDevs(m_upnpProcessor.getRemoteDMR());
                    Log.d(TAG,"Second Searh");
                }
            },100);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"Thired Searh");
                    m_upnpProcessor.searchDMR();
                }
            },300);





            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"Receive Search");
                    //Do something after 100ms
                    findDevs(m_upnpProcessor.getRemoteDMR());


                }
            }, 600);
            dmrRefresh();



        }
    }

    public static void findDevs(Collection<RemoteDevice> devices) {

        devList.clear();

        if(devices==null) {
            Log.e(TAG, "No devices");
            return;
        }


        //Log.e(TAG,"Erasing and adding");
        for (RemoteDevice device : devices) {
            String ip = device.getIdentity().getDescriptorURL().getHost();
            String uuid = device.getIdentity().getUdn().toString();
            String name = device.getDetails().getFriendlyName();


            if (ip.equals(LibreApplication.LOCAL_IP)) {
                Log.i(TAG, "skip local ip:" + ip);
                continue;
            }
            Iterator<DMRDev> i = devList.iterator();
            while (i.hasNext()) {
                DMRDev o = i.next();
                if(o.getIp().equals(ip))
                {
                    Log.v(TAG,"Duplicate..");
                    return;
                }
            }
            Log.v(TAG,"UPNP Dev found Name="+name+"IP="+ip);
            Log.v(TAG,",----------------------------------->Device found "+name);
               devList.add(new DMRDev(ip, uuid, name));





            //devsFound.add(new Dev(ip, uuid, name));
        }

    }


    @Override
    public void onStartComplete() {
        // TODO Auto-generated method stub
        //Collection<RemoteDevice>
     /*   Log.v(TAG,"onStartComplete");
        m_upnpProcessor.searchDMR();
        findDevs(m_upnpProcessor.getRemoteDMR()); */



    }
    @Override
    public void onLocalDeviceAdded(LocalDevice device) {



    }

    @Override
    public void onLocalDeviceRemoved(LocalDevice device) {

        Log.v(TAG,"onLocalDeviceRemoved");

    }

    public synchronized void onRemoteDeviceAdded(RemoteDevice device)
    {
        String ip = device.getIdentity().getDescriptorURL().getHost();
        String uuid = device.getIdentity().getUdn().toString();
        String name = device.getDetails().getFriendlyName();
        Log.e(TAG,"onRemoteDeviceAdded....."+name);


        if (ip.equals(LibreApplication.LOCAL_IP)) {
            Log.i(TAG, "skip local ip:" + ip);
            return;
        }
        Log.v(TAG,"UPNP Dev found Name="+name+"IP="+ip);

        devList.add(new DMRDev(ip,uuid,name));

        dmrRefresh();


    }
    public synchronized void onRemoteDeviceRemoved(RemoteDevice device)
    {

        String ip = device.getIdentity().getDescriptorURL().getHost();


        if (ip.equals(LibreApplication.LOCAL_IP)) {
            Log.i(TAG, "skip local ip:" + ip);
            return;
        }

        Iterator<DMRDev> i = devList.iterator();
          while (i.hasNext()) {

            //Have to fix
            DMRDev o = i.next();
            if(o.getIp().equals(ip))
                try{
                Log.d(TAG,"onRemoteDeviceRemoved....."+o.getDevName());
                i.remove();
                 }
              catch (Exception c){
                  Log.e(TAG,"onRemoteDeviceRemoved with concurrent modificationException...");
              }

        }
        dmrRefresh();



    }

    public void dmrRefresh(){
        if (m_myApp.m_screenstate==0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ZoneMasterFragment.adapter != null)

                        ZoneMasterFragment.adapter.notifyDataSetChanged();

                }
            });

        }
        else if (m_myApp.m_screenstate==1) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ZoneStationFragment.adapter != null)
                          ZoneStationFragment.adapter.notifyDataSetChanged();

                    }
                });

        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (m_myApp.m_screenstate==1)
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ZoneMasterFragment.newInstance())
                        .commit();

            else
            finish();


            return true;
        }

        return super.onKeyDown(keyCode, event);
    }




    @Override
    public void onClick(View v) {

    }
}










