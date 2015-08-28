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
import android.app.Application;
import android.util.Log;

import com.libre.client.ScanThread;
import com.libre.client.music.MusicServer;
import com.libre.client.util.DMSBrowseHelper;
import com.libre.client.util.PlaybackHelper;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;


public class LibreApplication extends Application {
	
	public static final String MY_MODEL = android.os.Build.MODEL;
	public int  m_screenstate;
	public static String LOCAL_IP = "";
	public static String LOCAL_UDN = "";
	public static HashMap<String, PlaybackHelper> PLAYBACK_HELPER_MAP = new HashMap<String, PlaybackHelper>();
	private static final String TAG = LibreApplication.class.getSimpleName();
	//private UpnpDeviceManager deviceManager = new UpnpDeviceManager();
	private int imageViewSize = 700;
	private MusicServer musicServer;
	private String currentdmrDeviceUdn = "";
	private String currentdevicename = "";
    public static String SSID;
	
	private DMSBrowseHelper dmsBrowseHelperSaved = null;
	private DMSBrowseHelper dmsBrowseHelperTemp = null;
	private boolean isPlayNewSong = false;
	public  static  String activeSSID;


	
	/******************************************************************/
	ScanThread wt=null;
	Thread scanthread=null;
	
	/*********************************************************************/
	
	public String getCurrentDmrDeviceUdn() {
		return currentdmrDeviceUdn;
	}

	public void setCurrentDmrDeviceUdn(String dmrDeviceUdn) {
		this.currentdmrDeviceUdn = dmrDeviceUdn;
	}
	public void setSpeakerName(String DevName) {
		this.currentdevicename = DevName;
	}
	public String getSpeakerName() {
		return currentdevicename ;
	}
	

	

	public PlaybackHelper getCurrentPlaybackHelper() {
		return PLAYBACK_HELPER_MAP.get(currentdmrDeviceUdn);
	}
	
	public DMSBrowseHelper getDmsBrowseHelperTemp() {
		return dmsBrowseHelperTemp;
	}

	public void setDmsBrowseHelperTemp(DMSBrowseHelper dmsBrowseHelperTemp) {
		this.dmsBrowseHelperTemp = dmsBrowseHelperTemp;
	}

	public DMSBrowseHelper getDmsBrowseHelperSaved() {
		return dmsBrowseHelperSaved;
	}

	public void setDmsBrowseHelperSaved(DMSBrowseHelper dmsBrowseHelper) {
		this.dmsBrowseHelperSaved = dmsBrowseHelper;
	}
	
         //public UpnpDeviceManager getDeviceManager() {
		//return deviceManager;
	//}

	public int getImageViewSize() {
		return imageViewSize;
	}

	public boolean isPlayNewSong() {
		return isPlayNewSong;
	}

	public void setPlayNewSong(boolean isPlayNewSong) {
		this.isPlayNewSong = isPlayNewSong;
	}
	
	public MusicServer getMusicServer() {
		return musicServer;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreated");
		super.onCreate();
		
		setImageViewSize();
		musicServer = new MusicServer();
		isPlayNewSong = false;
		initLUCIServices();
        //luciReceiver= LuciReceiver.getInstance();
	}

	private void setImageViewSize() {
		// TODO Auto-generated method stub
		int width = getResources().getDisplayMetrics().widthPixels;
		imageViewSize = (int) (width * 0.75f); 
	}
	public void initLUCIServices()
	{
        try {
                wt = new ScanThread();

                scanthread = new Thread(wt);

                scanthread.start();


            } catch (IOException e) {

                e.printStackTrace();
            }

	
	}
				
		public ScanThread getScanThread()
	{
		return wt;
		
	}
	public void LSSDPScan()
	{
		wt.UpdateNodes();
		return;
	}
	public synchronized void restart() throws SocketException {

        wt.close();

        try {

            wt = new ScanThread();
            scanthread = new Thread(wt);
            scanthread.start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }





	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onTerminated");
        wt.close();
        for (Iterator<String> i = LibreApplication.PLAYBACK_HELPER_MAP.keySet().iterator(); i.hasNext(); ) {
            String key = i.next();
            LibreApplication.PLAYBACK_HELPER_MAP.get(key).getDmrHelper().getDmrProcessor().dispose();
        }
       // luciReceiver.shutDowntheReceiver();
		super.onTerminate();
	}


}
