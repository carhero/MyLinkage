package com.libre.client.util;

import android.util.Log;

import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor.UpnpProcessorListener;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

import java.util.HashMap;
import java.util.HashSet;

public class UpnpDeviceManager implements UpnpProcessorListener {
	private final static String TAG = UpnpDeviceManager.class.getSimpleName();
	private HashSet<RemoteDevice> remoteDms = new HashSet<RemoteDevice>();
	private HashSet<RemoteDevice> remoteDmr = new HashSet<RemoteDevice>();
	private HashMap<String, RemoteDevice> remoteDmsMap = new HashMap<String, RemoteDevice>();
	private HashMap<String, RemoteDevice> remoteDmrMap = new HashMap<String, RemoteDevice>();
	private HashMap<String, LocalDevice> localDmsMap = new HashMap<String, LocalDevice>();
	private static UpnpDeviceManager manager = new UpnpDeviceManager();
	public static UpnpDeviceManager getInstance() {
		return manager;
	}

	@Override
	public void onRemoteDeviceAdded(RemoteDevice device) {
		// TODO Auto-generated method stub
		Log.v(TAG, "remote dev added:" + device.getDetails().getFriendlyName() +
				", addr:" + device.getIdentity().getDescriptorURL().getHost());
		Log.v(TAG, device.toString());

		String udn = device.getIdentity().getUdn().toString();
		if (device.getType().getNamespace().equals(UpnpProcessorImpl.DMS_NAMESPACE) && 
				device.getType().getType().equals(UpnpProcessorImpl.DMS_TYPE)) {
			if (remoteDmsMap.containsKey(udn)) {
				remoteDms.remove(device);
				remoteDmsMap.remove(udn);
			}
			remoteDms.add(device);
			remoteDmsMap.put(udn, device);
		} else if (device.getType().getNamespace().equals(UpnpProcessorImpl.DMR_NAMESPACE) && 
				device.getType().getType().equals(UpnpProcessorImpl.DMR_TYPE)) {
			if (remoteDmrMap.containsKey(udn)) {
				remoteDmr.remove(device);
				remoteDmrMap.remove(udn);
			}
			remoteDmr.add(device);
			remoteDmrMap.put(udn, device);
		}
	}

	@Override
	public void onRemoteDeviceRemoved(RemoteDevice device) {
		// TODO Auto-generated method stub
		Log.v(TAG, "remote dev removed:" + device.getDetails().getFriendlyName());
		String udn = device.getIdentity().getUdn().toString();
		if (remoteDmsMap.containsKey(udn)) {
			remoteDms.remove(device);
			remoteDmsMap.remove(udn);
		}
		
		if (remoteDmrMap.containsKey(udn)) {
			remoteDmr.remove(device);
			remoteDmrMap.remove(udn);
		}
	}

	@Override
	public void onLocalDeviceAdded(LocalDevice device) {
		// TODO Auto-generated method stub
		Log.i(TAG, "local dev added:" + device.getDetails().getFriendlyName());
		String udn = device.getIdentity().getUdn().toString();
		if (device.getType().getNamespace().equals(UpnpProcessorImpl.DMS_NAMESPACE) && 
				device.getType().getType().equals(UpnpProcessorImpl.DMS_TYPE)) {
			if (localDmsMap.containsKey(udn)) {
				localDmsMap.remove(udn);
			}
			localDmsMap.put(udn, device);
		}
	}

	@Override
	public void onLocalDeviceRemoved(LocalDevice device) {
		// TODO Auto-generated method stub
		Log.i(TAG, "local dev removed:" + device.getDetails().getFriendlyName());
		String udn = device.getIdentity().getUdn().toString();
		if (localDmsMap.containsKey(udn)) {
			localDmsMap.remove(udn);
		}
	}

	@Override
	public void onStartComplete() {
		// TODO Auto-generated method stub
	}

	public HashSet<RemoteDevice> getRemoteDms() {
		return remoteDms;
	}
	
	public HashSet<RemoteDevice> getRemoteDmr() {
		return remoteDmr;
	}

	public HashMap<String, RemoteDevice> getRemoteDmsMap() {
		return remoteDmsMap;
	}

	public HashMap<String, RemoteDevice> getRemoteDmrMap() {
		return remoteDmrMap;
	}

	public HashMap<String, LocalDevice> getLocalDmsMap() {
		return localDmsMap;
	}

}
