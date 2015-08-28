package com.libre.client.music;

import android.util.Log;

import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

public class MediaServer {

    private UDN udn = UDN.valueOf(new UUID(0,10).toString());
	private LocalDevice localDevice;

	private final static String deviceType = UpnpProcessorImpl.DMS_TYPE;
	private final static int version = 1;
	private final static String TAG = MediaServer.class.getSimpleName();
	private static int port = 11891;
	private static InetAddress localAddress;
	public HttpServer hs = null;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MediaServer(InetAddress localAddress) throws ValidationException {
		DeviceType type = new UDADeviceType(deviceType, version);

		DeviceDetails details = new DeviceDetails(android.os.Build.MODEL,
				new ManufacturerDetails(android.os.Build.MANUFACTURER),
				new ModelDetails("GNaP", "GNaP MediaServer for Android", "v1"));

		LocalService service = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);

		service.setManager(new DefaultServiceManager<ContentDirectoryService>(service, ContentDirectoryService.class));

		localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, service);
		MediaServer.localAddress = localAddress;

		Log.v(TAG, "MediaServer device created");
		Log.v(TAG, "friendly name: " + details.getFriendlyName());
		Log.v(TAG, "manufacturer: " + details.getManufacturerDetails().getManufacturer());
		Log.v(TAG, "model: " + details.getModelDetails().getModelName());
		
		//start http server
//		hs=new HttpServer(port);
		try {
//			hs=new HttpServer(port);
			hs = HttpServer.getInstance(port);
		}
		catch (IOException ioe)
		{
			System.err.println( "Couldn't start server:\n" + ioe );	
		}
		
		Log.v(TAG, "Started Http Server on port " + port);
	}
	

	public LocalDevice getDevice() {
		return localDevice;
	}

	public String getAddressAndPort() {
		return localAddress.getHostAddress() + ":" + port;
	}
	public void stop(){
		if(hs!=null)
			hs.stop();
	}
	
}
