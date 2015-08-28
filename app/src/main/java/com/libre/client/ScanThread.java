
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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.libre.constants.LSSDPCONST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;



public class ScanThread  implements Runnable {

    private static final String TAG = "LSSDP";

    public static final String LSSDP_MULTICAST_ADDRESS = "239.255.255.250";
    public static final int LSSDP_PORT = 1800;
    public static final String ST = "ST";
    public static final String LOCATION = "LOCATION";
    public static final String NT = "NT";
    public static final String NTS = "NTS";
    public static final String DEFAULT_ZONEID = "239.255.255.251:3000";
    /* Definitions of start line */
    public static final String SL_NOTIFY = "NOTIFY * HTTP/1.1";
    public static final String SL_MSEARCH = "M-SEARCH * HTTP/1.1";
    public static final String SL_OK = "HTTP/1.1 200 OK";

    /* Definitions of notification sub type */
    public static final String NTS_ALIVE = "ssdp:alive";
    public static final String NTS_BYEBYE = "ssdp:byebye";
    public static final String NTS_UPDATE = "ssdp:update";


    private MulticastSocket mMulticastSocket;
    private DatagramSocket mUnicastSocket;
    private NetworkInterface mNetIf;
    private Context mContext;
    private boolean isSocketCreated = false;

    private boolean mRunning = true;
    Thread MulticastRx;
    Thread UnicastRx;

    private boolean __DEBUG__ = false;
    LSSDPNodeDB DB;
    boolean shutdown = false;
    private Handler m_handler;
    NotifyThread notifyThread;

    Socket tcpUnicastsocket;
    ServerSocket serverSocket;
    Thread lookforMSearch;

    BufferedReader in;

    public ScanThread() throws IOException {
        DB = LSSDPNodeDB.getInstance();
        mNetIf = Utils.getActiveNetworkInterface();


        notifyThread = new NotifyThread();
        lookforMSearch = new Thread(new LookforMSearch());
    }

    boolean findDuplicate(LSSDPNodes data) {
        boolean found = false;
        int size = DB.GetDB().size();
       // Log.d("SAC","Found the size of list"+size);
        for (int i = 0; i < size; i++) {
            if (DB.GetDB().get(i).getIP().equals(data.getIP())) {
                // Log.e(TAG, "We already have this device "+DB.GetDB().get(i).getIP());
                found = true;
            }

        }
        return found;
    }

    public boolean CreateSockets() throws SocketException {

        if (mNetIf == null) {
            Log.d(TAG,"Network interface is false");
            return false;
        }

        mRunning=true;

        //for sending LSSDP M-Search
        try {
            Socket clientSocket;


            String name = mNetIf.getName();
            mMulticastSocket = new MulticastSocket(LSSDP_PORT);

            mMulticastSocket.setReuseAddress(true);
         //   Log.v(TAG, "CREATING LSSDP SOCKET AT Interface  " + name);
            mMulticastSocket.joinGroup(new InetSocketAddress(LSSDP_MULTICAST_ADDRESS, LSSDP_PORT), NetworkInterface.getByName(name));
            Log.i(TAG,"MultiSocket address  is "+LSSDP_MULTICAST_ADDRESS+ " and Network interface is name is= "+NetworkInterface.getByName(name)+
                    " Local v4 adderess is "+Utils.getLocalV4Address(mNetIf));
          //  Log.d(TAG, "Multicast done");
            serverSocket = null;
            try {
                serverSocket = new ServerSocket(LSSDP_PORT, 0, Utils.getLocalV4Address(mNetIf));
            } catch (IOException e) {
               // Log.d(TAG, "Unicast failed");
                e.printStackTrace();
            }

            Log.i(TAG,"Server socket created sucessfully");


        } catch (IOException e) {
            Log.d("SOCKET", "IO EXception");

            Log.e(TAG, "CREATING LSSDP SOCKETS FAILED");


            e.printStackTrace();

            return false;
        }
        return true;

    }

    public   synchronized void close() {

        try {
            mRunning=false;
            if(mMulticastSocket!=null)
            {
                if (!mMulticastSocket.isClosed())

                    mMulticastSocket.close();

            }
            if(serverSocket!=null)
            {
                if (!serverSocket.isClosed())

                    serverSocket.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }





    }



    private   void LookForNotify() {

        DatagramPacket dp = null;
        while (mRunning) {
           // Log.d(TAG,"Notify thread is runninng");
            //Log.d("ZoneMasterFragment", "NotifyThread-Running");

            try {

                dp = receiveMulticast();

               // Log.d("ZoneMasterFragment", "NotifyThread-Receiving");
                InetAddress addr = dp.getAddress();
                String startLine = parseStartLine(dp);

               /*if (true){
                    byte[]  buffer = new byte[1024];
                    buffer = dp.getData();

                    Log.e("Notif",""+new String(buffer));
                }*/



               /* if (startLine.equals(SL_MSEARCH)) {
                    Log.d("ZoneMasterFragment", "NotifyThread-Receiving in SL_MSEARCH");
                    String st = parseHeaderValue(dp, ST);
                    System.out.println("Recieved MSearch from" + addr.getHostAddress() + st);

                } else */if (startLine.equals(SL_NOTIFY)) {


                    String st1 = parseHeaderValue(dp, "PORT");
                    String st2 = parseHeaderValue(dp, "DeviceName");
                    String st3 = parseHeaderValue(dp, "State");
                    String st4 = parseHeaderValue(dp, "USN");
                    String type = parseHeaderValue(dp, "SPEAKERTYPE");
                    String SSID= parseHeaderValue(dp, "DDMSConcurrentSSID");

                    Log.d("received value", "PORT= " + st1 + ", DeviceName:" + st2 + ", State:" + st3 + ", USN:" + st4 + ",SPEAKERTYPE:"+type+ ",DDMSConcurrentSSID:"+SSID);

                  //  Log.d("ZoneMasterFragment", "NotifyThread-Receiving in Notify-State" + st3);
                    if (type == null)
                        type = "0";
                    String zoneid = parseHeaderValue(dp, "ZoneID");
                  //  Log.d("Zoneid","Zone id"+zoneid);

                    if (zoneid == null || zoneid.equals(""))
                        zoneid = DEFAULT_ZONEID;
                    LSSDPNodes node = new LSSDPNodes(addr, st2, st1, "0", st3, type, "0", st4, zoneid,SSID);
                    if (!findDuplicate(node)) {
                        DB.AddtoDB(node);
                        Log.v(TAG, "Received M-Search Resp: DeviceName=" + st2 + "Port=" + st1 + "IP=" + addr.getHostAddress());
                        if (m_handler != null) {
                            Message msg = new Message();// = ((Message) m_handler).obtain(m_handler, 0x10, node);
                            msg.what = LSSDPCONST.LSSDP_NEW_NODE_FOUND;
                            Log.d("ZoneMasterFragment", "New node found in ScanTthread-notify");
                            msg.obj = node;
                            m_handler.sendMessage(msg);
                            //.sendEmptyMessage(0x10);
                        } else {
                            Log.w(TAG, "node founded in without handler- state" + node.getDeviceState());
                        }
                    } else {
                        Log.d("ZoneMasterFragment", "Notifying-Duplicatenodes" + node.getDeviceState());
                    }

                } /*else if (startLine.equals(SL_OK)) {
                    if (m_handler != null)

                        m_handler.sendEmptyMessage(LSSDPCONST.LSSDP_HTTP_OK_IN_MULTICAST);
                    Log.v(TAG, "Recieved HTTP OK from" + addr.getHostAddress());
                    Log.d("ZoneMasterFragment", "NotifyThread-Receiving in SL_OK");
                }*/

            } catch (IOException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        }

    }





    public synchronized void removehandler() {

        m_handler = null;
       // Log.v("ZoneMasterFragment", "Removing Handler");
    }

    public synchronized void addhandler(Handler handler) {

        if (m_handler == null) {
          //  Log.v("ZoneMasterFragment", "Adding handler to scan thread");
            m_handler = handler;
        }
    }

    public void run() {
        try {
            if (!CreateSockets()) {
                if (CreateSockets())
                {
                   UpdateNodes();
                    lookforMSearch.start();
                    notifyThread.start();
                    Log.d(TAG, "Scanning thread started");
                }
                return;
            }
        } catch (SocketException e) {
            Log.d("SOCKET", "SOCKETEXCEPTION EXception");


            e.printStackTrace();
            return;
        }



       UpdateNodes();
        lookforMSearch.start();
        notifyThread.start();
        Log.d(TAG, "Scanning thread started");
    }


    public synchronized void shutdown() {
        Log.v(TAG, "Scan Thread Shutdown");
        mRunning = false;
    }



    DatagramPacket receiveMulticast() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        Log.d(TAG,"Tring to recive");
        mMulticastSocket.receive(dp);
        Log.d(TAG,"received");
        return dp;
    }

    private String parseHeaderValue(String content, String headerName) {
        Scanner s = new Scanner(content);
        s.nextLine(); // Skip the start line

        while (s.hasNextLine()) {
            String line = s.nextLine();
            if (line.equals(""))
                return null;
            int index = line.indexOf(':');
            if (index == -1)
                return null;
            String header = line.substring(0, index);
            if (headerName.equalsIgnoreCase(header.trim())) {
                return line.substring(index + 1).trim();
            }
        }

        return null;
    }



    private String parseHeaderValue(DatagramPacket dp, String headerName) {
        return parseHeaderValue(new String(dp.getData()), headerName);
    }

    private String parseStartLine(String content) {
        Scanner s = new Scanner(content);
        return s.nextLine();
    }

    private String parseStartLine(DatagramPacket dp) {
        return parseStartLine(new String(dp.getData()));
    }

    public boolean clearNodes() {
        DB.GetDB().clear();
        return __DEBUG__;

    }

    public  synchronized boolean  UpdateNodes() {
//DB.GetDB().clear();
        if (mNetIf == null)
            return false;
        new Thread() {
            public void run() {
                String MSearchPayload = "M-SEARCH * HTTP/1.1\r\n" +
                        "MX: 10\r\n" +
                        "ST: urn:schemas-upnp-org:device:DDMSServer:1\r\n" +
                        "HOST: 239.255.255.250:1800\r\n" +
                        "MAN: \"ssdp:discover\"\r\n" +
                        "\r\n";


                Log.v(TAG, "Sending M-search");
                DatagramPacket MSearch = null;
                try {
                    MSearch = new DatagramPacket(MSearchPayload.getBytes(),
                            MSearchPayload.length(), InetAddress.getByName(LSSDP_MULTICAST_ADDRESS), LSSDP_PORT);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block

                    e.printStackTrace();
                }

                try {
                    if (mMulticastSocket == null) {
                        if (m_handler != null) {
                            m_handler.sendEmptyMessage(LSSDPCONST.LUCI_SOCKET_NOT_CREATED);
                           Log.e(TAG, "SOcket is not created,handler is created");
                        } else
                            Log.e(TAG, "Socket is not Created");
                    } else
                        mMulticastSocket.send(MSearch);
                    Log.i(TAG, "Sending M search");


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
        }.start();


        return true;
    }

    public class NotifyThread extends Thread {

        @Override
        public void run() {

            LookForNotify();
        }
    }

    public class LookforMSearch implements Runnable {

        @Override
        public void run() {


            LookforTcpMsearchResp();
        }
    }

    public  void LookforTcpMsearchResp() {
        Socket socClient = null;

        //Infinite loop will listen for client requests to connect
        while (mRunning) {

            //Accept the client connection and hand over communication to server side client socket
            try {
                if( serverSocket != null) {

                    socClient = serverSocket.accept();

                    ServerAsyncTask serverAsyncTask = new ServerAsyncTask();
                    serverAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Socket[] {socClient});
                    Log.d(TAG,"Server socket is  created");

                }

                else {
                    Log.e(TAG,"Server socket is null");
                    Thread.sleep(2000);
                }


            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //  Log.d(TAG, "The value has been accepted");


            //For each client new instance of AsyncTask will be created


        }
    }

    class ServerAsyncTask extends AsyncTask<Socket, Void, String> {

        InetAddress inetAddress;

        @Override
        protected String doInBackground(Socket... params) {
            String data= null;

            Socket mySocket = params[0];
            try {
                //Get the d-ata input stream comming from the client
                InputStream is = mySocket.getInputStream();
                inetAddress = mySocket.getInetAddress();

               BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder(is.available());
                String line;
                PrintWriter out = new PrintWriter(
                        mySocket.getOutputStream(), true);
                out.println("close");

                while ((line = r.readLine())!=null) {
                    total.append(line+"\n");
                }

               data=total.toString();


                mySocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }


        @Override
        protected void onPostExecute(String data) {

            Log.d("Scan Thread", "onPostExecute : " + data);

            String s1 = parseStartLine(data);
            Log.d("Scan Thread", "Parse Data : " + s1);

            if (s1.equals(SL_OK)) {
                String usn = parseHeaderValue(data, "USN");
                String port = parseHeaderValue(data, "PORT");
                String devicename = parseHeaderValue(data, "DeviceName");
                String state = parseHeaderValue(data, "State");
                String netmode = parseHeaderValue(data, "NetMODE");
                String speakertype = parseHeaderValue(data, "SPEAKERTYPE");
                String tcpport = parseHeaderValue(data, "TCPPORT");
                String  zone_id= parseHeaderValue(data, "ZoneID");
                String  SSID=parseHeaderValue(data,"DDMSConcurrentSSID");

                if(speakertype==null)
                {

                    speakertype="0";
                }
                if(zone_id==null||zone_id.equals(""))
                    zone_id=DEFAULT_ZONEID;


                LSSDPNodes node = new LSSDPNodes(inetAddress, devicename, port, "0", state, speakertype, "0", usn, zone_id,SSID);

                if (!findDuplicate(node)) {
                    DB.AddtoDB(node);

                    if (m_handler != null) {
                        Message msg = new Message();// = ((Message) m_handler).obtain(m_handler, 0x10, node);
                        msg.what = LSSDPCONST.LSSDP_NEW_NODE_FOUND;
                        // Log.d("ZoneMasterFragment", "New node found in ScanTthread-notify");
                        msg.obj = node;
                        m_handler.sendMessage(msg);
                        //.sendEmptyMessage(0x10);
                    } else {
                        Log.w(TAG, "node founded in without handler- state" + node.getDeviceState());
                    }
                } else {
                    Log.d("ZoneMasterFragment", "Notifying-Duplicatenodes" + node.getDeviceState());
                }

            }

        }
    }







}


