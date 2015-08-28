package com.libre.client;
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
import android.util.Log;

import java.net.InetAddress;


public class LSSDPNodes {

    private static final String TAG = "LSSDPNodes";
    private InetAddress nodeaddress;
    private String friendlyname;
    private String nodeUDPport;
    private String nodeTCPport;
    private String DeviceState; //this can be M/F/S M:Audio master S:Audio CLient and Free: Not in DDMS mode
    private String SpeakerType; // L/R/S
    private String P2PState;
    private String USN;
    private String ZoneID;

    private String cSSID;

    public LSSDPNodes(InetAddress addr, String name, String cport, String tcpport,
                      String state, String type,
                      String p2p, String aUSN, String aZoneID, String cSSID) {
        super();
        this.nodeaddress = addr;
        this.friendlyname = name;
        this.nodeUDPport = cport;
        this.DeviceState = state;
        this.SpeakerType = type;
        this.P2PState = p2p;
        this.USN = aUSN;
        this.nodeTCPport = tcpport;
        this.ZoneID = aZoneID;
        this.cSSID = cSSID;

        Log.d(TAG, "addr:" + addr + ",name:" + name +",cport:" + cport + ",state:" + state + ",type:" + type +",p2p:" + p2p + ",aUSN:" + aUSN +",tcpport:" + tcpport + ",aZoneID:" + aZoneID +",cSSID:" + cSSID);
    }

    public String getDeviceState() {
        return DeviceState;
    }

    public String getSpeakerType() {
        return SpeakerType;
    }

    public String getFriendlyname() {
        return friendlyname;
    }

    public InetAddress getNodeAddress() {
        return nodeaddress;
    }

    public String getPort() {
        return nodeUDPport;
    }

    public String getIP() {
        return nodeaddress.getHostAddress();
    }

    public String getUSN() {
        return USN;
    }

    public String getP2PState() {
        return P2PState;
    }

    public String getZoneID() {
        return ZoneID;
    }

    public String getcSSID() {
        return cSSID;
    }

    public void setcSSID(String cSSID) {
        this.cSSID = cSSID;
    }
}