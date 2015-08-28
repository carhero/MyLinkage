package com.app.dlna.dmc.processor.impl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.upnp.CoreUpnpService;
import com.libre.client.activity.LibreApplication;
import com.libre.client.music.MusicServer;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UpnpProcessorImpl implements UpnpProcessor, RegistryListener {
	private final static String TAG = UpnpProcessorImpl.class.getSimpleName();
	public final static String DMS_NAMESPACE = "schemas-upnp-org";
	public final static String DMS_TYPE = "MediaServer";
	public final static String DMR_NAMESPACE = "schemas-upnp-org";
	public final static String DMR_TYPE = "MediaRenderer";
	

	private Activity m_activity;

	private CoreUpnpService.Binder m_upnpService;

	private ServiceConnection m_serviceConnection;

	private boolean m_isServiceReady;

	private final List<UpnpProcessorListener> m_listeners;

	private MusicServer ms;
	public UpnpProcessorImpl(UpnpListenerActivity activity) {
		m_activity = activity;
		m_isServiceReady = false;
		m_listeners = new ArrayList<UpnpProcessorListener>();
		m_listeners.add(activity);
	}

	public void bindUpnpService() {

		if (!m_isServiceReady) {
			m_serviceConnection = new ServiceConnection() {

				public void onServiceDisconnected(ComponentName name) {
					m_upnpService = null;
					m_isServiceReady = false;
				}

				public void onServiceConnected(ComponentName name, IBinder service) {
					Log.i(TAG, "Upnp Service Ready");
					m_isServiceReady = true;
					m_upnpService = (CoreUpnpService.Binder) service;
					m_upnpService.getRegistry().addListener(UpnpProcessorImpl.this);
					ms = ((LibreApplication)m_activity.getApplication()).getMusicServer();
					ms.prepareMediaServer(m_activity, m_upnpService);
					fireOnStartCompleteEvent();
				}
			};

			Intent intent = new Intent(m_activity, CoreUpnpService.class);
			m_activity.bindService(intent, m_serviceConnection, Context.BIND_AUTO_CREATE);

		}
	}
	public void stopMusicServer(){
		if (ms != null) {
			ms.stop();
		}
	}

	public void unbindUpnpService() {
		Log.d(TAG, "Unbind to service");
		m_isServiceReady = false;
		if (m_upnpService != null) {
			try {
				m_activity.unbindService(m_serviceConnection);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void searchAll() {
		if (m_isServiceReady) {
			Log.d(TAG, "Search invoke");
			m_upnpService.getRegistry().removeAllRemoteDevices();
			m_upnpService.getControlPoint().search();
		}
	}

	public boolean isUpnpServiceReady() {
		return m_isServiceReady;
	}

	public void addListener(UpnpProcessorListener listener) {
		synchronized (m_listeners) {
			if (!m_listeners.contains(listener)) {
				Log.d("UpnpPrcosesorImpl","addListener....");
				m_listeners.add(listener);
			}
		}
	}

	public void removeListener(UpnpProcessorListener listener) {
		synchronized (m_listeners) {
			if (m_listeners.contains(listener)) {
				m_listeners.remove(listener);
			}
		}
	}

	public ControlPoint getControlPoint() {
		return m_upnpService.getControlPoint();
	}

	@Override
	public RemoteDevice getRemoteDevice(String UDN) {
		for (RemoteDevice device : m_upnpService.getRegistry().getRemoteDevices()) {
			if (device.getIdentity().getUdn().toString().equals(UDN))
				return device;
		}
		
		return null;
	}
	
	public Collection<LocalDevice> getLocalDevices() {
		if (m_upnpService != null)
			return m_upnpService.getRegistry().getLocalDevices();
		
		return null;
	}
	
	public Collection<RemoteDevice> getRemoteDevices() {
		if (m_upnpService != null)
			return m_upnpService.getRegistry().getRemoteDevices();
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public Collection<RemoteDevice> getRemoteDMS() {
		DeviceType dmstype = new DeviceType(DMS_NAMESPACE, DMS_TYPE, 1);
		if (m_upnpService == null) return null;
		
		Collection<Device> devices = m_upnpService.getRegistry().getDevices(dmstype);
		if (devices == null) return null;
		
		ArrayList<RemoteDevice> remoteDev = new ArrayList<RemoteDevice>();
		for (Device dev : devices) {
			if (dev instanceof RemoteDevice)
				remoteDev.add((RemoteDevice)dev);
		}
		
		return remoteDev;
	}
	
	@SuppressWarnings("rawtypes")
	public Collection<RemoteDevice> getRemoteDMR() {
		DeviceType dmrtype = new DeviceType(DMR_NAMESPACE, DMR_TYPE, 1);
		if (m_upnpService == null) return null;
		
		Collection<Device> devices = m_upnpService.getRegistry().getDevices(dmrtype);
		if (devices == null) return null;
		
		ArrayList<RemoteDevice> remoteDev = new ArrayList<RemoteDevice>();
		for (Device dev : devices) {
			if (dev instanceof RemoteDevice)
				remoteDev.add((RemoteDevice)dev);
		}
		
		return remoteDev;
	}

	public LocalDevice getLocalDevice(String UDN) {
		
		for (LocalDevice device : m_upnpService.getRegistry().getLocalDevices()) {
			Log.i(TAG, "Local device:" + device.getDetails().getFriendlyName() + "," + device.getIdentity().getUdn().toString());
			if (device.getIdentity().getUdn().toString().compareTo(UDN) == 0)
				return device;
		}
		return null;
	}


	private void fireOnStartCompleteEvent() {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onStartComplete();
			}
		}
	}

	@Override
	public void searchDMS() {
		if (m_isServiceReady) {
			Log.d(TAG, "SearchDMS invoke");
			DeviceType type = new DeviceType(DMS_NAMESPACE, DMS_TYPE, 1);
			if (m_upnpService != null) {
//				m_upnpService.getRegistry().removeAllRemoteDevices();
				m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
			}
			else {
				Log.w(TAG, "UPnP Service is null");
			}
		}

	}

	@Override
	public void searchDMR() {
		if (m_isServiceReady) {
			Log.d(TAG, "SearchDMR invoke");
			DeviceType type = new DeviceType(DMR_NAMESPACE, DMR_TYPE, 1);
			if (m_upnpService != null) {
				//m_upnpService.getRegistry().removeAllRemoteDevices();
				m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
			}
			else {
				Log.w(TAG, "UPnP Service is null");
			}
		}
	}
	public void ClearAll() {
		if (m_isServiceReady) {
			Log.d(TAG, "SearchDMR invoke");
			if (m_upnpService != null) {
				m_upnpService.getRegistry().removeAllRemoteDevices();
//				m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
			}
			else {
				Log.w(TAG, "UPnP Service is null");
			}
		}
	}


	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry,
			RemoteDevice device) {
		// TODO Auto-generated method stub
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry,
			RemoteDevice device, Exception ex) {
		// TODO Auto-generated method stub
	}

	@Override
	public synchronized void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		fireRemoteDeviceAddedEvent(device);
	}

	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
		// TODO Auto-generated method stub
	}

	@Override
	public synchronized void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		fireRemoteDeviceRemovedEvent(device);
	}

	@Override
	public void localDeviceAdded(Registry registry, LocalDevice device) {
		fireLocalDeviceAddedEvent(device);
	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice device) {

		fireLocalDeviceRemovedEvent(device);
	}

	@Override
	public void beforeShutdown(Registry registry) {


	}

	@Override
	public void afterShutdown() {


	}

	private void fireRemoteDeviceAddedEvent(RemoteDevice remoteDevice) {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				
				listener.onRemoteDeviceAdded(remoteDevice);
			}
		}
	}

	private void fireRemoteDeviceRemovedEvent(RemoteDevice remoteDevice) {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onRemoteDeviceRemoved(remoteDevice);
			}
		}
	}

	private void fireLocalDeviceAddedEvent(LocalDevice localDevice) {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onLocalDeviceAdded(localDevice);
			}
		}
	}

	private void fireLocalDeviceRemovedEvent(LocalDevice localDevice) {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onLocalDeviceRemoved(localDevice);
			}
		}
	}
}
