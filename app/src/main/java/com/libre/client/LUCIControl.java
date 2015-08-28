package com.libre.client;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.libre.constants.LSSDPCONST;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

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


public class LUCIControl  {
	private static final String TAG = "LUCI CONTROL";
	private Handler m_handler;

    private Handler sending_m_handler;
	private Socket socket;
  //  private  static LUCIControl luciControl;

	private static final int LUCI_CONTROL_PORT = 7777;
	private NetworkInterface mNetIf;
	Thread serverThread = null;
    ServerThread SERVER;
	private String SERVER_IP;
	volatile boolean shutdown = false;
	public static final int LUCI_RESP_PORT = 3333;

	
	public void addhandler(Handler handler)
	{

        m_handler=handler;
	}
	public void close()


	{
		shutdown=true;

        Log.d("ZoneMasterFragment","Shutdown");
	}


    public  void shutdown(){
        SERVER.shutdown();
    }



	public LUCIControl()

	{

		shutdown=true;


	}
	public LUCIControl(String serverIP)
	
	{


        shutdown=false;
		SERVER_IP=serverIP;
		 mNetIf = Utils.getActiveNetworkInterface();

		SendCommand(3,Utils.getLocalV4Address(mNetIf).getHostAddress()+","+"3333",LSSDPCONST.LUCI_SET);
		Log.v(TAG,"INIT LUCICONTROL with= "+serverIP+" OurIP="+Utils.getLocalV4Address(mNetIf).getHostAddress());
        try {
            SERVER= new ServerThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.serverThread = new Thread(SERVER);

        this.serverThread.start();
	}



    public void SendLUCICommand(final int MID,final String Data, final String IP)
    {
        new Thread() {
            public void run() {
                String messageData = null;

                Log.e(TAG,"SendLUCICommand"+MID);
                //Todo:
                LUCIPacket packet=new LUCIPacket(Data.getBytes(), (short) Data.length(),(short) MID);

                int server_port = Integer.parseInt("7777");
                DatagramSocket s = null;


                try {
                    s = new DatagramSocket();
                } catch (SocketException e) {
                    // TODO Auto-generated catch blo
                    e.printStackTrace();
                }
                InetAddress local = null;
                try {
                    local = InetAddress.getByName(IP);
                } catch (UnknownHostException e) {

                    e.printStackTrace();
                }



                int msg_length=packet.getlength();

                byte[] message = new byte[msg_length] ;
                packet.getPacket(message);

                //Log.e(TAG,"Mid = "+packet.getCommand()+"Mdata = "+new String(message));
                Log.e("LMP","Mid = "+packet.getCommand()+"Mdata = "+new String(message));


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


    public void GetLUCICommand(final int MID,final String Data, final String IP)
    {
        new Thread() {
            public void run() {
                String messageData = null;
              final  LUCIPacket packet;

                Log.e(TAG, "Get LUCICommand" + MID);
                //Todo:
                if (Data != null) {
                   packet  = new LUCIPacket(Data.getBytes(), (short) Data.length(), (short) MID, (byte) LSSDPCONST.LUCI_GET);
                } else {
                    packet=new LUCIPacket(null, (short) 0, (short)MID,(byte)LSSDPCONST.LUCI_GET);
                }


                int server_port = LUCI_CONTROL_PORT;
                DatagramSocket s = null;


                try {
                    s = new DatagramSocket();
                } catch (SocketException e) {
                    // TODO Auto-generated catch blo
                    e.printStackTrace();
                }
                InetAddress local = null;
                try {
                    local = InetAddress.getByName(IP);
                } catch (UnknownHostException e) {

                    e.printStackTrace();
                }



                int msg_length=packet.getlength();

                byte[] message = new byte[msg_length] ;
                packet.getPacket(message);

                //Log.e(TAG,"Mid = "+packet.getCommand()+"Mdata = "+new String(message));
                Log.e("Sathi","Mid = "+packet.getCommand()+"ServerIP"+IP+"LocalIP"+SERVER_IP);



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
    public void SendCommand(final int cmd,final String data,final int cmd_type)
    {
        new Thread() {
            public void run() {
                String messageStr = null;
                LUCIPacket packet=null;
                messageStr= data;
                if(data!=null)
                    packet=new LUCIPacket(messageStr.getBytes(), (short) messageStr.length(), (short)cmd,(byte)cmd_type);

                else
                    packet=new LUCIPacket(null, (short) 0, (short)cmd,(byte)cmd_type);

                int server_port = LUCI_CONTROL_PORT;
                DatagramSocket s = null;
                try {
                    s = new DatagramSocket();
                } catch (SocketException e) {

                    e.printStackTrace();
                }
                InetAddress local = null;
                try {
                    local = InetAddress.getByName(SERVER_IP);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                int msg_length=packet.getlength();

                byte[] message = new byte[msg_length] ;
                packet.getPacket(message);

                DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);


                try {
                    s.send(p);
                    s.close();
                } catch (IOException e) {

                    e.printStackTrace();
                    s.close();
                }
            }

        }.start();

    }






    
	public void SendCommand(final List<LUCIPacket > luciPackets)
	{
		new Thread() {
	        public void run() {
	        String messageStr = null;
	        LUCIPacket packet=null;

        for (LUCIPacket luciPacket:luciPackets){
            int server_port = LUCI_CONTROL_PORT;
            DatagramSocket s = null;
            try {
                s = new DatagramSocket();
            } catch (SocketException e) {

                e.printStackTrace();
            }
            InetAddress local = null;
            try {
                local = InetAddress.getByName(SERVER_IP);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            int msg_length=luciPacket.getlength();

            byte[] message = new byte[msg_length] ;
            luciPacket.getPacket(message);

            Log.e("Sathi"," before sending as a packet Messagebox id"+luciPacket.getCommand());
            Log.v("Sathi",new String(message, 0));
            DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);

            try {
                s.send(p);
                s.close();
               // Log.e(TAG," After sending as a packet Messagebox id"+cmd+"Data"+data);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                s.close();
            }
        }
        }
	    	   
	    	   
	   /* if(data!=null)
		packet=new LUCIPacket(messageStr.getBytes(), (short) messageStr.length(), (short)cmd,(byte)cmd_type);
	    else
	    packet=new LUCIPacket(null, (short) 0, (short)cmd,(byte)cmd_type);*
	    
		int server_port = LUCI_CONTROL_PORT;
		DatagramSocket s = null;
		try {
			s = new DatagramSocket();
		} catch (SocketException e) {

			e.printStackTrace();
		}
		InetAddress local = null;
		try {
			local = InetAddress.getByName(SERVER_IP);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	int msg_length=packet.getlength();
		
		byte[] message = new byte[msg_length] ;
		packet.getPacket(message);

        Log.e(TAG," before sending as a packet Messagebox id"+cmd+"Data"+data);
		
		Log.v(TAG,new String(message, 0));
		DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
		  
		        	try {
                            s.send(p);
                            s.close();
             Log.e(TAG," After sending as a packet Messagebox id"+cmd+"Data"+data);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						s.close();
					}
		        }*/
	        
		    }. start();
		
	}
		
	
	class ServerThread implements Runnable {
		
		private DatagramSocket mUnicastSocket;
		private NetworkInterface mNetIf;
	
		public ServerThread() throws IOException {
		    mNetIf = Utils.getActiveNetworkInterface();

		}
		public void run() {
            Log.e(TAG,"Receiver is created");
			try {
					mUnicastSocket = new DatagramSocket(null);
					mUnicastSocket.setReuseAddress(true);
					mUnicastSocket.bind(new InetSocketAddress(Utils.getLocalV4Address(mNetIf),LUCI_RESP_PORT));
		        
		    	} catch (IOException e) {
		    		Log.e(TAG, "Failed to setup socket ", e);
		    	}

		    while(!shutdown) {
		        	DatagramPacket dp = null;
		        	try {
		        			dp=receive();
		        			InetAddress addr=dp.getAddress();

		        			byte[] buffer;
		        			buffer = dp.getData();
		        			String aString=new String(buffer);
		        			String cutString = aString.substring(0, 100);
		        			Log.v(TAG,"Response Data: "+cutString);
		        			//for(int i=0;i<dp.getLength();i++)
		        				//Log.v(TAG,"buffer="+buffer[i]);
		        			
		        			LUCIPacket packet=new LUCIPacket(buffer);


		        			if(m_handler!=null)
		        			{	Message msg = new Message();// = ((Message) m_handler).obtain(m_handler, 0x10, node);
		        				msg.what=LSSDPCONST.LUCI_RESP_RECIEVED;


		        				msg.obj=packet;
		        				m_handler.sendMessage(msg);

		        			}



		                }


		        	catch (IOException e) {
		        		Log.e(TAG, " fail.", e);
		        	}
		    	}
		    
		}
		public synchronized void shutdown() {
			if (mUnicastSocket.isBound()||!mUnicastSocket.isClosed()) {
                mUnicastSocket.disconnect();
                mUnicastSocket.close();
            }

		   
		} 

		DatagramPacket receive() throws IOException {
		    byte[] buf = new byte[2048];
		    DatagramPacket dp = new DatagramPacket(buf, buf.length);
		    mUnicastSocket.receive(dp);
		    return dp;
		}

	}


}
