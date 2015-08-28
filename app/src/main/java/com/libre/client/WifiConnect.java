
package com.libre.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.libre.constants.WIFICONST;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

/*********************************************************************************************
 * Copyright (C) 2014 Libre Wireless Technology
 * <p/>
 * "Junk Yard Lab" Project
 * <p/>
 * Libre Sync Android App
 * Author: Subhajeet Roy
 ***********************************************************************************************/

public class WifiConnect   {
	
	protected static final String TAG = "SAC";
	protected static final int KCONNECT_TIMEOUT = 10;	// 20->10	yhcha
	private  BroadcastReceiver Wifibroadcast;
	boolean reciever_registered=false;
	WifiManager wifiManager;
	static ConnectivityManager connManager ;
	Handler m_handler;
	Context m_ctx;
	String mSSID=null;
	WifiConfiguration  HomeAPconf;
	private List<WifiConfiguration> mWifiConfiguration;
    NetworkInterface mNetIf;
		
	public WifiConnect(Context ctx,Handler handler){
        mNetIf = Utils.getActiveNetworkInterface();
		wifiManager = (WifiManager)ctx.getSystemService( Context.WIFI_SERVICE );
		connManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		m_handler=handler;
		m_ctx=ctx;
		if(!wifiManager.isWifiEnabled())
	    {
			Log.e(TAG,"Wifi is tuened off now turning it ON");
			wifiManager.setWifiEnabled(true);	
		
		}
	}
	public void saveHomeAPConf(String HomeAP)
	{
		wifiManager.disconnect();
		mWifiConfiguration = wifiManager.getConfiguredNetworks();
	
		for (int i = 0; i < mWifiConfiguration.size(); i++) {
			String configSSID = mWifiConfiguration.get(i).SSID;
			if(configSSID.equals(HomeAP))
			{
				HomeAPconf=mWifiConfiguration.get(i);
			}
			
		}
	}
	
	public String getconnectedSSIDname()
	{
		
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String ssid = wifiInfo.getSSID();
		Log.d(TAG,"getconnectedSSIDname wifiInfo = "+wifiInfo.toString());
		if (ssid.startsWith("\"") && ssid.endsWith("\"")){
            ssid = ssid.substring(1, ssid.length()-1);
		}
		Log.d(TAG, "Connected SSID" + ssid);
		return ssid;
	}
	/*public int getSecurityofHomeAP() {
		
	    if (HomeAPconf.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
	        return 1;
	    }
	    if (HomeAPconf.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
	    		HomeAPconf.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
	        return 2;
	    }
	    if (HomeAPconf.wepKeys[0] != null)
			return 3;
		else
			return 4;
	}*/
	/*public void ConnectHomeAP(String HomeAP)
	{
		
		
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for( WifiConfiguration i : list ) {
		    if(i.SSID != null && i.SSID.equals("\"" + HomeAP + "\"")) {
		         wifiManager.disconnect();
		         wifiManager.enableNetwork(i.networkId, true);
		         wifiManager.reconnect();               

		         break;
		    }           
		 }
	}*/
/*	public void stopSearch ()
	{
		m_ctx.unregisterReceiver(Wifibroadcast);
	}*/
	public void SearchMore ()
	{
		wifiManager.startScan();
	}
	public boolean SearchForWAC()
	{
		Log.d(TAG,"SearchForWAC");
		IntentFilter i = new IntentFilter(); 
		i.addAction (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        wifiManager.startScan();
		
		reciever_registered=true;
		m_ctx.registerReceiver(Wifibroadcast=new BroadcastReceiver(){ 
		
		public void onReceive(Context c, Intent i){    		
		      
		List<ScanResult> wifiScanList = wifiManager.getScanResults();
		Log.d(TAG,"SearchForWAC onReceive");
		int count=0;
		for (ScanResult data : wifiScanList)
		{
			Log.v(TAG," SCAN SSID:"+data.SSID);
			//if(data.SSID.matches("(.*)LSConfigure(.*)"))
			//{
            //if(data.SSID.contains("LSConfigure_"))
			if(data.SSID.contains("Linkage_SA200_"))
            {
		    	//data.SSID;
				count++;
		    	Message msg = new Message();
		    	msg.what=WIFICONST.SSID_FOUND_NEW;
				msg.obj=data.SSID;
				m_handler.sendMessage(msg);//found
		    	 
		    	//return ;
		    		 
		    }
		    	 
		  }
			if(count==0)
				m_handler.sendEmptyMessage(WIFICONST.SSID_NOT_FOUND ); //not found
			else
				m_handler.sendEmptyMessage(WIFICONST.SSID_SCAN_DONE ); //not found
		       
		  } 
		  }, i );
		// wifiManager.startScan();
		 return true;
		    
	}
	public void close()
	{
		if(reciever_registered)
		{    try {
            if (Wifibroadcast!=null)
            m_ctx.unregisterReceiver(Wifibroadcast);
        } catch(IllegalArgumentException e) {
            Log.d(TAG,"Trying to unregister the services which is not register");
        }

		}
	}
	/*public boolean SearchForOOH()
	{
		IntentFilter i = new IntentFilter(); 
		i.addAction (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION); 
		
		reciever_registered=true;
		m_ctx.registerReceiver(Wifibroadcast=new BroadcastReceiver(){ 
		
		public void onReceive(Context c, Intent i){    		
		      
		List<ScanResult> wifiScanList = wifiManager.getScanResults();
		for (ScanResult data : wifiScanList)
		{
			Log.v(TAG," SCAN SSID"+data.SSID);
			if(data.SSID.matches("(.*)DDMSLab(.*)"))
			{
				Message msg = new Message();
		    	msg.what=0x02;
				msg.obj=data.SSID;
				m_handler.sendMessage(msg);//found
		    	return ;
		    		 
		    }
		    	 
		  }
		  m_handler.sendEmptyMessage(0x01);
		       
		  } 
		  }, i ); 

		
		     
		 wifiManager.startScan();
		 return true;
		    
	}*/
	
	// yhcha, App이 Wifi에 연결되도록 강제로 설정하는 Method이다.
	public void Connect(final String inSSID,final String Password)
	{
		new Thread() {
			public void run() {
				WifiConfiguration conf = new WifiConfiguration();
				Log.v(TAG," Connect ConnectDDMSOOH SSID= "+inSSID);

				String inPassword = Password;//"hello123"; //keep it safe in secured file
				conf.SSID = String.format("\"%s\"", inSSID);
				conf.allowedAuthAlgorithms.clear();
				conf.allowedGroupCiphers.clear();
				conf.allowedPairwiseCiphers.clear();
				conf.allowedProtocols.clear();
				conf.allowedKeyManagement.clear();
				//conf.preSharedKey = String.format("\"%s\"", inPassword);
				conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
				conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);


				if(Password==null||Password.equals(""))
				{
					Log.d(TAG," Connect ConnectDDMSOOH if(Password.equals)");
					conf.allowedKeyManagement.set(KeyMgmt.NONE);
				}
				else
				{
					Log.d(TAG," Connect ConnectDDMSOOH inPassword = "+inPassword);
					conf.preSharedKey = String.format("\"%s\"", inPassword);
					conf.allowedKeyManagement.set(KeyMgmt.WPA_PSK);

				}
				conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
				conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
				conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
				conf.priority = 1;
				
				//conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			//add to wifi manager settings
               // wifiManager.disconnect();
               // wifiManager.disableNetwork(conf.networkId);
               // wifiManager.removeNetwork(conf.networkId);

                wifiManager.saveConfiguration();
				int netId =wifiManager.addNetwork(conf);	
				if(netId==-1) {
                    Log.e(TAG, "Failed to set the settings for  " + inSSID);

                    mWifiConfiguration = wifiManager.getConfiguredNetworks();

                    for (int i = 0; i < mWifiConfiguration.size(); i++) {
                        String configSSID = mWifiConfiguration.get(i).SSID;
                        Log.d(TAG,"Config SSID"+configSSID+"Active SSID"+conf.SSID);
                        if(configSSID.equals(conf.SSID))
                        {
                            netId =mWifiConfiguration.get(i).networkId;
                            Log.d(TAG,"network id"+netId);
                            break;
                        }
                        else
                            Log.e(TAG,"network is not there in wifi Manger"+netId);

                    }



                }

              //  wifiManager.removeNetwork(netId);
				wifiManager.saveConfiguration();
				wifiManager.disconnect();
				wifiManager.enableNetwork(netId, true);
				wifiManager.reconnect();
				boolean connected=false;
				int i=0;
				//check if connected!
				while (true) 
				{
				     //Wait to connect
				     try {
				    	if(isWifiConnected())
				    	{
							if (mNetIf==null)
							{
								return;
							}

							String name = mNetIf.getName();
                            try
							{
                                Log.e(TAG,"wifi is connected"+"Network interface is "+ NetworkInterface.getByName(name));
                            }
							catch (SocketException e) {
                                e.printStackTrace();
                            }
                            connected=true;
				    		break;
				    	}
				    	else
				    	{
				    		Thread.sleep(1000);
                            i++;
				    		if(i==KCONNECT_TIMEOUT)
							{
								break;
							}
				    	}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                     
				 }
				if(connected)
				{
					String activeSSID=getconnectedSSIDname();
                    Log.v(TAG,"ActiveSSID= "+activeSSID+"Device SSID ="+inSSID);
                    if (activeSSID.equals(inSSID))
					{
						//getDevicenameFunc();
						m_handler.sendEmptyMessage(WIFICONST.MAIN_SSID_CONNECTED_CONFIGURING);
						Log.e(TAG,"Connected to wifi successfully "+"ActiveSSID= "+activeSSID+"Device SSID ="+inSSID);
					}
                    else
					{
                        m_handler.sendEmptyMessage(WIFICONST.MAIN_SSID_FAILED_CONFIGURING);
                        Log.e(TAG,"Connected to wrong wifi"+"ActiveSSID= "+activeSSID+"Device SSID ="+inSSID);
                    }

                }
                else {
                    m_handler.sendEmptyMessage(WIFICONST.NOT_ABLE_TO_CONNECT);
                    Log.e(TAG,"Not able to Connect to Device SSID ="+inSSID);
                }

			}
		}.start();
	}
			public static boolean isConnected() {
			   
			    NetworkInfo networkInfo = null;
			    if (connManager != null) {
			        networkInfo = connManager.getActiveNetworkInfo();
                    Log.e(TAG,"networkInfo"+networkInfo.getSubtypeName());


			    }

			    
			    return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
			}


			public void ConnectWAC(final String inSSID)
			{
				
				new Thread() {
					public void run() {
						WifiConfiguration conf = new WifiConfiguration();
                        conf.status=WifiConfiguration.Status.DISABLED;


						Log.v(TAG," ConnectWAC ConnectDDMSOOH SSID= "+inSSID);


						
						conf.SSID = String.format("\"%s\"", inSSID);
						conf.allowedAuthAlgorithms.clear();
						conf.allowedGroupCiphers.clear();
						conf.allowedPairwiseCiphers.clear();
						conf.allowedProtocols.clear();
						conf.allowedKeyManagement.clear();
						conf.priority = 1;
						conf.allowedKeyManagement.set(KeyMgmt.NONE);

					//add to wifi manager settings
                        wifiManager.disconnect();
                        wifiManager.saveConfiguration();
					 	int netId =wifiManager.addNetwork(conf);

						if(netId==-1){
                            Log.e(TAG,"Failed to set the settings for wac "+inSSID);

                            mWifiConfiguration = wifiManager.getConfiguredNetworks();

                            for (int i = 0; i < mWifiConfiguration.size(); i++) {
                                String configSSID = mWifiConfiguration.get(i).SSID;
                                Log.d(TAG,"Config SSID"+configSSID+"Active SSID"+conf.SSID);
                                if(configSSID.equals(conf.SSID))
                                {
                                    netId =mWifiConfiguration.get(i).networkId;
                                    Log.d(TAG,"network id"+netId);
                                    break;
                                }
                                else
                                    Log.e(TAG,"network is not there in wifi Manger"+netId);

                            }

                        }

						wifiManager.enableNetwork(netId, true);
						wifiManager.reconnect();
						boolean connected=false;
						int i=0;
						//check if connected!
						while (true) 
						{
                            Log.d(TAG,"Current the looping value"+i);
						     //Wait to connect
						     try {
						    	if(isWifiConnected())
						    	{
						    		connected=true;
						    		break;
						    	}
						    	else
						    	{
						    		Thread.sleep(1000);
                                    i++;
						    		if(i==KCONNECT_TIMEOUT)
						    			break;
						    	}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}                     
						 }
						if(connected)
						{

                            boolean value = false;
                            int count =0;
                            while(true)
                            {
                                value = ping("192.168.43.1");
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(!value)
                                {
                                    count++;
                                    if (count == 10) {
                                        break;
                                    }
                                }
                                if(value)
                                    break;
                            }

                            String activeSSID=getconnectedSSIDname();
                            Log.i(TAG,"ActiveSSID= "+activeSSID+"Device SSID ="+inSSID);
                            if (activeSSID.equals(inSSID) && value)												 //	getDevicenameFunc();
							 m_handler.sendEmptyMessage(WIFICONST.SSID_CONNECTED);
                             else
                             m_handler.sendEmptyMessage(WIFICONST.NOT_ABLE_TO_CONNECT);
                           // else
						}
						else
							 m_handler.sendEmptyMessage(WIFICONST.SSID_CONNECT_FAILED);
							
					}
				}.start();
			}

    /*public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }*/




    public boolean ping(String url) {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(url);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            Log.d("InetAddress", "printStackTrace");
            e.printStackTrace();
            return false;

        }
        try {
            if(addr.isReachable(5000)) {
                Log.d("InetAddress","\n" + url + " - Respond OK");
                return true;
            } else {
                Log.d("InetAddress","\n" + url + " - Respond NOT OK");
                return false;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d("InetAddress", "\n" + e.toString());
            return false;
        }
    }

    public  boolean isWifiConnected(){
        ConnectivityManager connManager = (ConnectivityManager) m_ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }



}
