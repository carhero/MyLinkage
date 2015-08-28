package com.libre.client.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class NetworkStateReceiver extends BroadcastReceiver {


	private static final String TAG = "NetworkStateReceiver";
	private NetworkInterface m_interfaceCache = null;

	private static List<Handler> handlerList = new ArrayList<Handler>();

	public NetworkStateReceiver() {
		//m_nwStateListener = nwStateListener;
	}

	public static void registerforNetchange(Handler handle) {
		handlerList.add(handle);

	}

	public static void unregisterforNetchange(Handler handle) {

		Iterator<Handler> i = handlerList.iterator();
		while (i.hasNext()) {
			Handler o = i.next();
			if (o == handle)
				i.remove();
		}

	}


	@Override
	public void onReceive(final Context context, Intent intent) {
		if (!intent.getAction().equals("android.net.conn.TETHER_STATE_CHANGED")
				&& !intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
			return;



		WifiManager wifiManager =
				(WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		String ssid=getActiveSSID(context);
		if (ssid==null||ssid.equals(""))
			return;

		Log.d(TAG,"Libreid"+ LibreApplication.activeSSID+"Connected id"+ssid);

		if (!ssid.equals(LibreApplication.activeSSID))

		{
			Log.e(TAG, "ssid" + ssid + "Libressid" + LibreApplication.activeSSID);

		NetworkInterface netIf = com.libre.client.Utils.getActiveNetworkInterface();
			if (netIf==null)
				return;

			LibreApplication.activeSSID=ssid;
			LibreApplication application=(LibreApplication) context.getApplicationContext();
			try {
				application.getScanThread().clearNodes();
				application.restart();
			} catch (SocketException e) {
				e.printStackTrace();
			}
			Log.e(TAG,"ssid"+ssid+"Libressid"+ LibreApplication.activeSSID);
		}
		Log.d(TAG,"Receiver is changing");

	}

	public String getActiveSSID(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.getConnectionInfo().getSSID();


	}
}
