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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.libre.client.*;
import com.libre.constants.LSSDPCONST;
import com.libre.constants.MIDCONST;
import com.libre.constants.WIFICONST;

@SuppressLint("ResourceAsColor")
public class NetconfigActivity extends Activity{

	public static final int FINISH_NETCONFIG        =0x30;
	public static final int NETCONFIG_WRITE_CREDENTIALS=0x31;
	
	public static final int NETCONFIG_TIMEOUT=0x32;
	protected static final String TAG = "SAC";
	WifiConnect connect;
	int attempts=0;
	private ImageView img2, img3;
	private TextView m_version;
	private boolean animcontinue;
	Dialog custom;
	EditText Fname;
	EditText Lname;
	TextView txt;
	Button savebtn;
	Button canbtn;
    private  static final int LSSDPDEVICENAMESUCESS = 345;
	
	private String homeAP;
	String devicename;
    String password;
    LSSDPNodes confnode;
    List<String> SSIDLIST = new ArrayList<String>();
    String sDeviceName;
	 WifiManager mainWifi;
	    WifiReceiver receiverWifi;
	    static boolean isRescanNeeded = false;
	    static boolean isDropDownNeeded = false;
	    static ArrayList<String> Allconnections=new ArrayList<String>();
	   // Handler handler = new Handler();
	    static ArrayList<String> Security=new ArrayList<String>();
	    static String Main_SSID = null;
	    static String Main_Sec = null;
	    Spinner listView ;
	    ArrayAdapter<String> arrayAdapter;
        NetworkInterface mNetIf;
  
    public enum ConfigurationStates {INIT,SEARCHING,FOUND,LSSDP_FETCHING_INFO,USER_INPUT,SENDING_CONF,WAITING_IN_AP,COMPLETE,FAILED};
    ConfigurationStates State;
    private LibreApplication m_myApp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.confloading);
        mNetIf = com.libre.client.Utils.getActiveNetworkInterface();
		
		Log.d(TAG,"onCreate NetconfigActivity check");
				
		State=ConfigurationStates.INIT;
		img2 = (ImageView) findViewById(R.id.img2);
		img3 = (ImageView) findViewById(R.id.img3);
		m_version = (TextView) findViewById(R.id.textView1);
		listView = (Spinner) findViewById(R.id.list);
		connect=new WifiConnect(this,wifihandler);
		
		isRescanNeeded = true;
       // receiverWifi = new WifiReceiver();
        
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		 mainWifi.startScan();
	     if(!mainWifi.isWifiEnabled())
	     {
	         mainWifi.setWifiEnabled(true);	// yhcha, Wifi 강제 활성화
			 Log.d(TAG, "mainWifi.setWifiEnabled(true);");
	     }
	        
		connect.SearchForWAC();
		//homeAP=connect.getconnectedSSIDname();
		//connect.saveHomeAPConf(homeAP);
	        //connect.SearchForWAC();	
		SSIDLIST.clear();
		m_myApp = (LibreApplication)getApplication();
		m_myApp.getScanThread().addhandler(wifihandler);

		//connect.Connect("ANAM_SOFT_YHCHA", "dudgns2866");	// yhcha, test
	}

	class WifiReceiver extends BroadcastReceiver
    {
    	@Override
        public void onReceive(Context c, Intent intent)
        {
        	if(isRescanNeeded){
        		isRescanNeeded = false;
        		Security.clear();
        		Allconnections.clear();
                boolean isSecurityNone = false;
                List<ScanResult> wifiList;
                wifiList = mainWifi.getScanResults();            
                if (wifiList != null) {
                    for (ScanResult network : wifiList)
                    {
                        if(!network.SSID.contains("LSConfigure_"))
                        {
                            String Capabilities =  network.capabilities;
                            isSecurityNone = false;
                            Log.d (TAG, "AP List[]:" + network.SSID + " capabilities : " + Capabilities);

                            if (Capabilities.contains("WEP")) {
                                Security.add("WEP");
                            }
                            else if (Capabilities.contains("WPA")) {
                                Security.add("WPA-PSK");
                            }
                            else {
                                Security.add("NONE");
                                isSecurityNone = true;
                            }

                            Allconnections.add(network.SSID);
                            //  Security.add(Capabilities);
                            if(isDropDownNeeded)
                            {
                                listView.setAdapter(arrayAdapter);
                            }
                        }
                    }
                    isDropDownNeeded = false;
                	WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                 	// Log.d(TAG, "WifiReceiver wifiManager.getConnectionInfo() SSID= "+wifiManager.getConnectionInfo().toString());
                 	int indexofssid = 0;
                 	
          	 		Main_SSID = wifiManager.getConnectionInfo().getSSID();
          	 		Log.d(TAG, "WifiReceiver Main_SSID initial : " + Main_SSID);
          	 		Log.d(TAG, "WifiReceiver Main_SSID length : " + Main_SSID.length());
          	 		if(Main_SSID.length() != 0)
          	 			Main_SSID = Main_SSID.substring(1 , Main_SSID.length()-1);
          	 		
          	 		if(Main_SSID.equals("") || Main_SSID.contains("LSConfigure_"))
          	 		{
          	 			Main_SSID = "LSConfigure_XXYYZZ";
          	 			Main_Sec = "NONE";
          	 		}
          	 		else
          	 		{
              	 		for (int i=0; i<Allconnections.size(); i++) 
              	 		{
              	 			if(Main_SSID.equals(Allconnections.get(i)))
              	 			{
              	 				indexofssid = i;
              	 				break;
              	 			}
              	 				
              	 		}
              	 		Main_Sec = Security.get(indexofssid);
          	 		}
          	 		Log.d(TAG, "WifiReceiver Main_SSID: " + Main_SSID);
          	 		Log.d(TAG, "WifiReceiver Main_Sec: " + Main_Sec);
          	 		
                }
        	}
        }
    }
    
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
 
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
 
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
 
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else {
                result = "Did not work!";
                Log.e(TAG,"Input stream is null in GET method");
            }
 
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
 
        return result;
    }
 
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }
	
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
 
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	Log.d(TAG, "HttpAsyncTask onPostExecute result = "+result);
        	sDeviceName = result;
            wifihandler.sendEmptyMessage(LSSDPDEVICENAMESUCESS);
       }
    }
    
    public boolean ping(String url) {
      /*  String str = "";
        try {
            Process process = Runtime.getRuntime().exec(
                    "/system/bin/ping -c 8 " + url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            int i;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((i = reader.read(buffer)) > 0)
                output.append(buffer, 0, i);
            reader.close();

            // body.append(output.toString()+"\n");
            str = output.toString();
            // Log.d(TAG, str);
        } catch (IOException e) {
            // body.append("Error\n");
            e.printStackTrace();
        }
        return str;*/
    	
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
    
	public void getDeviceName()
	{
		new Thread()
    	{
    		 public void run()
    		 {
    			 Log.d(TAG, "getDeviceName");
    			 new HttpAsyncTask().execute("http://192.168.43.1:8080/devicename.asp");
    			/* try{
    				// Thread.sleep(3000);
    		           HttpClient httpclient = new DefaultHttpClient();
    		           BufferedReader in = null;
    		           HttpGet request = new HttpGet();
    		           URI website = new URI("http://192.168.43.1:8080/devicename.asp");
    		           request.setURI(website);
    		           
    		           ResponseHandler<String> responseHandler = new BasicResponseHandler();
    		           String val = httpclient.execute(request, responseHandler);


    		           // NEW CODE
    		        //   String line = in.readLine();
    		           
    		           sDeviceName = val.substring(0 , val.length()-1);
    		           Log.d("TAG", "getDeviceName sDeviceName = "+sDeviceName);

    		           // END OF NEW CODE

    		       }catch(Exception e){
    		           Log.e("mytag", "Error in http connection "+e.toString());
    		       }*/
    		 }
    	}.start();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startActivity(new Intent(NetconfigActivity.this,
					MainActivity.class));
			State=ConfigurationStates.INIT;
			wifihandler.sendEmptyMessageDelayed(FINISH_NETCONFIG, 100);
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }
    
	@Override
	protected void onResume() {
		super.onResume();
       registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		m_version.setText("Searching for Speakers...");
		animcontinue=true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true)
				{
					try {
						if(animcontinue)
						{
						
					
							Thread.sleep(1000);
							animhandler.sendEmptyMessage(0x01);
							Thread.sleep(700);
							animhandler.sendEmptyMessage(0x02);
							Thread.sleep(400);
							animhandler.sendEmptyMessage(0x03);
						}
						else
						{
							Thread.sleep(400);
						}
						} catch (InterruptedException e) {
					// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}

			}
		}).start();
		
		//StartScan();
		

	}
	
	private void StartLSSDPScan() {
		m_myApp.getScanThread().clearNodes();
		m_myApp.getScanThread().UpdateNodes();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                m_myApp.getScanThread().UpdateNodes();

            }
        },2000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                m_myApp.getScanThread().UpdateNodes();

            }
        },4000);

		
	}

	
	 Handler wifihandler = new Handler() {
		@SuppressLint("InflateParams")
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == WIFICONST.SSID_FOUND_NEW)
			{
				String data = (String)msg.obj;
				Log.d(TAG,"Found a Speaker Message----- data = "+data);
				SSIDLIST.add(data);

			}
			else if(msg.what == WIFICONST.SSID_SCAN_DONE)
			{
				connect.close();

				if(SSIDLIST.size()==1)
				{
					MultipleSACDeviceAlert();
					/*for (String data : SSIDLIST)
					{
						Message msg2 = new Message();
						msg2.what=WIFICONST.SSID_FOUND;
						msg2.obj=data;
						wifihandler.sendMessage(msg2);//found
					}*/

				}
				else
				{
					MultipleSACDeviceAlert();
				}
			}
			else if (msg.what == WIFICONST.SSID_NOT_FOUND)
			{
				if(attempts<5)
				{
					connect.SearchMore();
						/*connect.SearchForWAC();
						SSIDLIST.clear();
						try {
							Thread.sleep(7000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/

					attempts++;
				}
				else
				{
					animcontinue=false;
					m_version.setTextColor(R.color.darkred);
					m_version.setText("No Speaker Found to connect...");

					connect.close();

					wifihandler.sendEmptyMessageDelayed(FINISH_NETCONFIG, 5000);

				}
			}
			else if (msg.what == WIFICONST.SSID_FOUND)
			{
				String data = (String)msg.obj;
				Log.v(TAG,"Found a Speaker Message-----");
				connect.ConnectWAC(data);

				State=ConfigurationStates.FOUND;
				m_version.setText("Connecting to Speaker...");

			}
			else if(msg.what==WIFICONST.SELECT_SSID_LIST)
			{
				Log.d(TAG, "if(msg.what==WIFICONST.SELECT_SSID_LIST");
				//getDeviceName();

				DisplayDialod(sDeviceName);
			}
            else  if (msg.what==WIFICONST.NOT_ABLE_TO_CONNECT)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        State=ConfigurationStates.FAILED;
                        m_version.setText("not able to connect to the device as WIFI AP");
                        wifihandler.sendEmptyMessageDelayed(FINISH_NETCONFIG,1000);
                    }
                });
            }
			else if (msg.what ==WIFICONST.SSID_CONNECTED)
			{
				if(State==ConfigurationStates.FOUND)
				{

					State=ConfigurationStates.LSSDP_FETCHING_INFO;
					m_version.setText("Fetching Speaker Information...");
					connect.close();
					//m_myApp.Restart();
					//m_myApp.getScanThread().addhandler(wifihandler);
					//StartLSSDPScan();

					Log.d(TAG, "if(State==ConfigurationStates.FOUND)");





				//	homeAP=connect.getconnectedSSIDname();

					 //getDeviceName();

				new Thread()
				{
					public void run()

				    {
                        int count = 0;
						while(true)
						{
							ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
							NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
							boolean value = ping("192.168.43.1");
                            Log.d(TAG,"Wifi is connected== "+mWifi.isConnected()+"Ping value is"+value);

							if (mWifi.isConnected()) {

                                if (value) {
                                    // Do whatever
                                    getDeviceName();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SelectSSIDFromList();
                                        }
                                    });

                                    Log.d(TAG, "if(State==ConfigurationStates.FOUND) DeviceName = "+sDeviceName);
                                    Log.d(TAG, "if(State==ConfigurationStates.FOUND)  Main_SSID: " + Main_SSID);
                                    Log.d(TAG, "if(State==ConfigurationStates.FOUND)  Main_Sec: " + Main_Sec);
                                    break;
                                }
                                else
                                {
                                    count++;
                                    if (count==5) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                m_version.setText("Connection lost,retry again ");
                                            }
                                        });

                                        Log.e(TAG,"Ping is stopped");
                                        State=ConfigurationStates.FAILED;
                                        wifihandler.sendEmptyMessageDelayed(FINISH_NETCONFIG, 1000);
                                        break;
                                    }

                                    Log.e(TAG,"Ping is failed");
                                }
							}
                            else {
                                Log.e(TAG, "wifi  is failed");
                            }
						 }
				    }
				}.start();


						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						//getDeviceName();

	          	 		//if(Main_SSID.equals("LSConfigure_XXYYZZ"))

	          	 		//else
	          	 		//	DisplayDialod(sDeviceName);
					//wifihandler.sendEmptyMessageDelayed(NETCONFIG_TIMEOUT, 300000);
				}

				else if(State==ConfigurationStates.SENDING_CONF)
				{
					if (devicename.matches("")) {
						//m_version.setText("Waiting for"+confnode.getFriendlyname());
						m_version.setText("Waiting for "+sDeviceName);
						//devicename=confnode.getFriendlyname();
						devicename = sDeviceName;
					}
					else
					{
						m_version.setText("Waiting for"+devicename);
					}

					Log.d(TAG, "if(State==ConfigurationStates.SENDING_CONF)  " );
					State=ConfigurationStates.WAITING_IN_AP;



					Log.d(TAG, "if(State==ConfigurationStates.SENDING_CONF) StartLSSDPScan " );



				}


			}
			else if (msg.what == WIFICONST.SSID_CONNECT_FAILED)
			{
				m_version.setText("Failed to connect...");
				animcontinue=false;

				connect.close();
				wifihandler.sendEmptyMessageDelayed(NETCONFIG_TIMEOUT, 5000);
				//DisplayAlert("");

			}

			else if (msg.what == LSSDPCONST.LSSDP_NEW_NODE_FOUND)
			{
				LSSDPNodes data = (LSSDPNodes)msg.obj;
				Log.v(TAG,"LSSDP NEW_NODE_FOUND "+data.getFriendlyname());

				if(State==ConfigurationStates.LSSDP_FETCHING_INFO)
				{
					Log.v(TAG,"LSSDP NEW_NODE_FOUND-----LSSDP_FETCHING_INFO ");
					confnode=data;
					if(confnode==null)
						return;
					//look for CONF header
					m_version.setText("Configuring Speaker...");
					Log.v(TAG,"State is Three and we got a resp");
					State=ConfigurationStates.USER_INPUT;
					//DisplayDialod();
				}
				else if(State==ConfigurationStates.WAITING_IN_AP)
				{
                    if(sDeviceName.equals(data.getFriendlyname())){
					//if (!sDeviceName.matches("(.*)"+data.getFriendlyname()+"(.*)")) {
						Log.v(TAG,"Configured Succesfully ");
						m_version.setText("Configured Successfully...");
						animcontinue=false;
						State=ConfigurationStates.COMPLETE;
						connect.close();
                  						//Log.v(TAG,"LSSDP NEW_NODE_FOUND-----WAITING_IN_AP before FINISH_NETCONFIG ");
					   wifihandler.sendEmptyMessageDelayed(FINISH_NETCONFIG, 5000);
					}
                    else
                    {
                        m_version.setText("Searching the device ....");
                        wifihandler.sendEmptyMessageDelayed(NETCONFIG_TIMEOUT, 20000);
                    }

				}
                else if(msg.what==LSSDPCONST.LUCI_SOCKET_NOT_CREATED) {
                    Log.e(TAG,"Scan Thread is getting failed");
                }


			}

			/*else if (msg.what == LSSDPCONST.LSSDP_HTTP_OK_IN_MULTICAST)
			{
				LSSDPNodes data = (LSSDPNodes)msg.obj;
				Log.v(TAG,"LSSDP LSSDP_HTTP_OK_IN_MULTICAST-----");

				if(State==ConfigurationStates.LSSDP_FETCHING_INFO)
				{
					confnode=data;
					//look for CONF header
					m_version.setText("Configuring Speaker...");
					Log.v(TAG,"State is Three and we got a resp");
					State=ConfigurationStates.USER_INPUT;
					DisplayDialod(data);
				}


			}*/
			else if(msg.what==FINISH_NETCONFIG)
			{
				//System.exit(1);
				//connect.ConnectHomeAP(homeAP);

                if(State==ConfigurationStates.COMPLETE){
                    finish();
                    return;
                }
				//connect.ConnectHomeAP(Main_SSID);
				finish();
			}

			else if(msg.what==NETCONFIG_WRITE_CREDENTIALS)
			{
				m_version.setText("Configuring Speaker...");
				State=ConfigurationStates.SENDING_CONF;
				m_version.setText("Configuring...");
				wifihandler.sendEmptyMessageDelayed(NETCONFIG_TIMEOUT, 45000);




				/*if (!devicename.matches("")) {
					SendLUCICommand(90,devicename,confnode);

				}

				SendLUCICommand(125,homeAP+","+password,confnode);*/
				sendWifiData();

				//connect.Connect(homeAP, password);

				connect.Connect(Main_SSID, password);

			}
			else if(msg.what==NETCONFIG_TIMEOUT)

                			{
				if(State==ConfigurationStates.LSSDP_FETCHING_INFO ||
						State==ConfigurationStates.WAITING_IN_AP ||
						State==ConfigurationStates.SENDING_CONF)
				{
				  m_version.setText("Timeout while Configuring..STOP.");
				//  connect.Connect(homeAP, password);
				   //connect.Connect(Main_SSID, password);
				  //connect.close();
				  wifihandler.sendEmptyMessageDelayed(FINISH_NETCONFIG, 5000);
				}


			}
            else if(msg.what==WIFICONST.MAIN_SSID_CONNECTED_CONFIGURING){
                try {
                    String name = mNetIf.getName();
                    if (isWifiConnected()){

                    Log.d(TAG,"Before restarting"+NetworkInterface.getByName(name));
                    m_myApp.restart();
                    m_myApp.getScanThread().removehandler();
                    m_myApp.getScanThread().addhandler(wifihandler);
                    StartLSSDPScan();
                    State=ConfigurationStates.WAITING_IN_AP;}
                    else
                      Log.d(TAG,"Wifi is failed");
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            else if (msg.what==WIFICONST.MAIN_SSID_FAILED_CONFIGURING){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_version.setText("Password May be Wrong Or connected to some other AP due to network Connection-" +
                                        "Not possible to get the success state"
                                );
                        wifihandler.sendEmptyMessageDelayed(2000,FINISH_NETCONFIG);
                    }
                });

            }

		}
	};
	
	private  Handler animhandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0x01) {
				img2.setImageResource(R.drawable.toloading);
			}
			if (msg.what == 0x02) {
				img3.setImageResource(R.drawable.toloading);
			}
			if (msg.what == 0x03) {
				img2.setImageResource(R.drawable.unloading);
				img3.setImageResource(R.drawable.unloading);

			}
		}
	};

	private void sendWifiData()    
	{

    	new Thread()
    	{
    		 public void run()
    		 {
    			 Log.d(TAG, "sendWifiData thread");
	    		 HttpClient myClient = new DefaultHttpClient();
	    		 HttpPost post = new HttpPost("http://192.168.43.1:8080/goform/HandleSACConfiguration");
	    		 try 
	    		 {
		    		 List<NameValuePair> myArgs = new ArrayList<NameValuePair>();
		    		 myArgs.add(new BasicNameValuePair("SSID", Main_SSID));
		    		 myArgs.add(new BasicNameValuePair("Passphrase", password));
		    		 myArgs.add(new BasicNameValuePair("Security", Main_Sec));
		    		 if(devicename.equals("") || devicename.equals(sDeviceName))
		    		 {
		    			 Log.d(TAG, "sendWifiData thread devicename "+devicename);
		    			 myArgs.add(new BasicNameValuePair("Devicename", ""));
		    		 }
		    		 else
		    			 myArgs.add(new BasicNameValuePair("Devicename", devicename));
                      Log.d(TAG,"trying to set entity");
		    		 post.setEntity(new UrlEncodedFormEntity(myArgs));
		    		 myClient.execute(post);
		    		/* BufferedReader br = new BufferedReader( new InputStreamReader(myResponse.getEntity().getContent()));
		    		 String line = "";
		    		 while ((line = br.readLine()) != null)
		    		 {
		    			 Log.d(TAG, line);
		    		 
		    		 } */
	    		 }
	    		 catch (IOException e)
	    		 {
		    		 e.printStackTrace();
		    		 Log.e(TAG, "sendWifiData thread end exception"+e.toString());
	    		 }

    		 }
    	}.start();
	}
	private void DisplayDialod(String DeviceName/*LSSDPNodes node*/)    
	{
		Log.d(TAG," DisplayDialod");
		custom = new Dialog(NetconfigActivity.this);
        custom.setContentView(R.layout.form1);
        Fname = (EditText)custom.findViewById(R.id.fname);
       // Fname.setHint("Name:"+node.getFriendlyname());
        Fname.setHint("Name: "+DeviceName);
        Lname = (EditText)custom.findViewById(R.id.lname);
        
        if(Main_Sec.equals("NONE")){
        	Lname.setHint("No Password Required for "+Main_SSID);
        	Lname.setEnabled(false);
        }
        else{
        	Lname.setHint("Enter Password of "+Main_SSID);
        	Lname.setEnabled(true);
        }
        
        savebtn = (Button)custom.findViewById(R.id.savebtn);
        canbtn = (Button)custom.findViewById(R.id.canbtn);
        custom.setTitle("Custom Dialog");
        savebtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            // TODO Auto-generated method stub
           devicename = Fname.getText().toString();
           password = Lname.getText().toString();
           if (password.equals("")&&!Main_Sec.equals("NONE")) {
               Toast.makeText(NetconfigActivity.this, "Please enter the password", Toast.LENGTH_SHORT).show();
               return;
           }
           Log.v(TAG," DisplayDialog Name is "+devicename+" and password is "+password);
           animcontinue=true;
           wifihandler.sendEmptyMessageDelayed(NETCONFIG_WRITE_CREDENTIALS, 1000);
           //wifihandler.sendEmptyMessage(0x12);
          custom.dismiss();
          }
        });
        canbtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            // TODO Auto-generated method stub
        	  
            custom.dismiss();
            m_version.setText("Failed to Configure...");
			
			connect.close();
			
			wifihandler.sendEmptyMessageDelayed(FINISH_NETCONFIG, 5000);
            //wifihandler.sendEmptyMessage(FINISH_NETCONFIG);
          }
        });

        custom.show();
      
	}
	void SendLUCICommand(final int MID,final String Data,final LSSDPNodes node)
	{
		new Thread() {
	        public void run() {
		String messageData = null;
		

		LUCIPacket packet=new LUCIPacket(Data.getBytes(), (short) Data.length(),(short) MID);

		int server_port = Integer.parseInt(node.getPort());
		DatagramSocket s = null;


		try {
			s = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch blo
			// ck
			e.printStackTrace();
		}
		InetAddress local = null;
		try {
			local = InetAddress.getByName(node.getIP());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		int msg_length=packet.getlength();
		
		byte[] message = new byte[msg_length] ;
		packet.getPacket(message);
		

		DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
		  
		        	try {
		        		 Log.e(getClass().getName(), "sendto IP "+ local +server_port);
						s.send(p);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    }.start();
     
		
	}
	int check_selected=0;
	void MultipleSACDeviceAlert(){

	AlertDialog levelDialog ;
    LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
    final View layout = inflater.inflate(R.layout.dialog_config, (ViewGroup)findViewById(R.id.ddms_volume));
	// Strings to Show In Dialog with Radio Buttons
   
   
    List<String> strings = new ArrayList<String>();
    for (String data : SSIDLIST)
    {
    	strings.add(data);
    }
    
   
    final CharSequence[] it = strings.toArray(new String[strings.size()]);
    
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setSingleChoiceItems(it, 0, new DialogInterface.OnClickListener() {
	public void onClick(DialogInterface dialog, int item) {
		check_selected=item;
	}
	
});
	
	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int id) {

			Log.v(TAG,"check-selected="+check_selected);
			for (int i = 0; i < SSIDLIST.size(); i++) {
				if(i==check_selected)
				{
					Message msg2 = new Message();
					msg2.what=WIFICONST.SSID_FOUND;
					msg2.obj=SSIDLIST.get(i);
					wifihandler.sendMessage(msg2);//found
				}
			}
		 	dialog.dismiss();
    	}
   });
    builder.setNegativeButton("REFRESH", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
        	connect.SearchForWAC();
        	SSIDLIST.clear();
    	/*	new Thread() {
    	        public void run() {
    	        	Close_or_Rescan();
        }
    }.start();*/
             dialog.cancel();
        }
    });
    
  
   


    levelDialog = builder.create();
    levelDialog.setView(layout);
	levelDialog.show();

	
}


	/*void Close_or_Rescan()
	{

		new AlertDialog.Builder(this)
	    .setTitle("Select")
	    .setMessage("RESCAN or CLOSE")
	    .setPositiveButton("RESCAN", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	connect.SearchForWAC();
	        	SSIDLIST.clear();
	        	 dialog.dismiss();
	        }
	     })
	    .setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
			 	Message msg2 = new Message();
				msg2.what=FINISH_NETCONFIG;
				msg2.obj="";
				wifihandler.sendMessage(msg2);//found
				dialog.cancel();
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();

	}*/

	static int check_selected_SSID = -1;
	static int indexofssid= -1;
	void SelectSSIDFromList()
	{
		Log.d(TAG,"SelectSSIDFromList");
		AlertDialog levelDialog ;
	    LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
	    final View layout = inflater.inflate(R.layout.dialog_config, (ViewGroup)findViewById(R.id.ddms_volume));
		// Strings to Show In Dialog with Radio Buttons
	    
	 		for (int i=0; i<Allconnections.size(); i++) 
	 		{
	 			//Log.d(TAG,"SelectSSIDFromList SSID = "+Allconnections.get(i)+" index = "+i);	
  	 			if(Main_SSID.equals(Allconnections.get(i)))
  	 			{
  	 				indexofssid = i;
  	 				break;
  	 			}
	 		}
	    
	 		Log.d(TAG,"SelectSSIDFromList indexofssid = "+indexofssid);
	    final CharSequence[] it = Allconnections.toArray(new String[Allconnections.size()]);
	    
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setSingleChoiceItems(it, indexofssid, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int item) {
			check_selected_SSID=item;
			Log.d(TAG,"SelectSSIDFromList onClick check_selected_SSID="+check_selected_SSID);
		}
		
	});
	
		builder.setTitle("Select SSID");
	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	public void onClick(DialogInterface dialog, int id) {
		
			if(check_selected_SSID == -1)
				check_selected_SSID = indexofssid;
			Log.d(TAG,"SelectSSIDFromList check_selected_SSID="+check_selected_SSID);
			Log.d(TAG,"SelectSSIDFromList Security="+Security.get(check_selected_SSID));
			Main_Sec = Security.get(check_selected_SSID);
			Log.d(TAG,"SelectSSIDFromList Allconnections="+Allconnections.get(check_selected_SSID));
			Main_SSID = Allconnections.get(check_selected_SSID);
			
		 	Message msg2 = new Message();
			msg2.what=WIFICONST.SELECT_SSID_LIST;
			msg2.obj="";
			wifihandler.sendMessage(msg2);//found
			check_selected_SSID = -1;
			indexofssid= -1;
	     dialog.dismiss();
	       	
	    }
	   });
	    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
	        	connect.SearchForWAC();
	        	SSIDLIST.clear();
	             dialog.cancel();
	        }
	    });
	    
	    levelDialog = builder.create();
	    levelDialog.setView(layout);
		levelDialog.show();
	}


    public  boolean isWifiConnected(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         return mWifi.isConnected();
    }

    @Override
    protected void onDestroy() {
        if (connect!=null)
        connect.close();
        super.onDestroy();
    }
}
