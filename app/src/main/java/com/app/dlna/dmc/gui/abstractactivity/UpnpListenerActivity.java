package com.app.dlna.dmc.gui.abstractactivity;



import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

public abstract class UpnpListenerActivity extends ActionBarActivity implements UpnpProcessor.UpnpProcessorListener {

	@Override
	public void onRemoteDeviceAdded(RemoteDevice device) {

	}

	
	@Override

	public void onRemoteDeviceRemoved(RemoteDevice device) {

	}

	@Override
	public void onLocalDeviceAdded(LocalDevice device) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onLocalDeviceRemoved(LocalDevice device) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStartComplete() {

	}
	
}