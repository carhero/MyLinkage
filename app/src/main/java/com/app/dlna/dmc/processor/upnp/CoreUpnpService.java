package com.app.dlna.dmc.processor.upnp;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.android.AndroidRouter;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;

public class CoreUpnpService extends AndroidUpnpServiceImpl {

	private static final String TAG = "CoreUpnpService";

	private static final int NOTIFICATION = 1500;
//	private static final String TAG = CoreUpnpService.class.getName();
	//private HttpThread m_httpThread;
	private UpnpService upnpService;
	private Binder binder = new Binder();
	private NotificationManager m_notificationManager;
	private WifiLock m_wifiLock;

    @Override
    protected AndroidRouter createRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, Context context) {
        return super.createRouter(configuration, protocolFactory, context);
    }

    @Override
	public void onCreate() {
		super.onCreate();
		WifiManager m_wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		m_wifiLock = m_wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "UpnpWifiLock");
		m_wifiLock.acquire();
		//HTTPServerData.HOST = Utility.intToIp(m_wifiManager.getDhcpInfo().ipAddress);

	/*	m_notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		m_httpThread = new HttpThread();
		m_httpThread.start();*/

		final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		upnpService = new UpnpServiceImpl(createConfiguration());

	}

	protected AndroidUpnpServiceConfiguration createConfiguration(WifiManager wifiManager) {
		return new AndroidUpnpServiceConfiguration();
	}

	/*protected AndroidWifiSwitchableRouter createRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory,
			WifiManager wifiManager, ConnectivityManager connectivityManager) {
		return new AndroidWifiSwitchableRouter(configuration, protocolFactory, wifiManager, connectivityManager);
	}*/

	@Override
	public void onDestroy() {
		super.onDestroy();

			//unregisterReceiver(((AndroidWifiSwitchableRouter) upnpService.getRouter()).getBroadcastReceiver());
		try {
			upnpService.shutdown();
		} catch (Exception ex) {
		}

		try {
			m_wifiLock.release();
		} catch (Exception ex) {
		}

		//m_httpThread.stopHttpThread();

	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	protected boolean isListeningForConnectivityChanges() {
		return true;
	}

	public class Binder extends android.os.Binder implements AndroidUpnpService {

		public UpnpService get() {
			return upnpService;
		}

		public UpnpServiceConfiguration getConfiguration() {
			return upnpService.getConfiguration();
		}

		public Registry getRegistry() {
			return upnpService.getRegistry();
		}

		public ControlPoint getControlPoint() {
			return upnpService.getControlPoint();
		}
	}

	/*private void showNotification() {
		Notification notification = new Notification(R.drawable.ic_launcher, "CoreUpnpService started",
				System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

		notification.setLatestEventInfo(this, "CoreUpnpService", "Service is running", contentIntent);

		m_notificationManager.notify(NOTIFICATION, notification);
	}*/
}