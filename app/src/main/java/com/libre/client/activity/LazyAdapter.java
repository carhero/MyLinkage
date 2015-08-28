package com.libre.client.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.libre.client.AppPreference;
import com.libre.client.LUCIControl;
import com.libre.client.LUCIPacket;
import com.libre.client.util.PlaybackHelper;
import com.libre.constants.LSSDPCONST;
import com.libre.constants.MIDCONST;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LazyAdapter extends BaseAdapter {
    
    protected static final String TAG = "LAZYADAPTER";
	private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private List<DMRDev> devList;
    private static LayoutInflater inflater=null;
    private PlaybackHelper m_playbackHelper;

    
	private LibreApplication m_myApp;
	//Handler dhandler;
    LUCIControl luci;

    Handler sendingHandler;

    
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d, LibreApplication m_myApp2,List<DMRDev> devs) {
        activity = a;
        data=d;
        m_myApp = m_myApp2;
        devList=devs;
        sendingHandler= new Handler();
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       
        Log.v(TAG,"---"+AppPreference.getMaxGroups());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    
    
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView artist = (TextView)vi.findViewById(R.id.artist); // artist name
        TextView duration = (TextView)vi.findViewById(R.id.duration);
        TextView dmr1=(TextView)vi.findViewById(R.id.dmr);
        if (AppPreference.ShowDMRintheLIST()) {
            for (DMRDev dmr : devList) {
                if (dmr.getIp().equals(data.get(position).get(GroupListActivity.KEY_IP))) {
                    dmr1.setText("DMR");
                    dmr1.setTextColor(Color.rgb(0, 150, 136));

                }

            }
        }


        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image
        thumb_image.setTag(position);
        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);
        final int pos=position;
        if(song==null)
        	return vi;

        title.setText(song.get(GroupListActivity.KEY_STATE));
        artist.setText(song.get(GroupListActivity.KEY_NAME));
        duration.setText(song.get(GroupListActivity.KEY_TYPE));
        thumb_image.setImageResource(R.drawable.speaker_default);
        
        
        if(song.get(GroupListActivity.KEY_STATE).equals("Free"))
        {
        	thumb_image.setImageResource(R.drawable.free);	
        }
        else if(song.get(GroupListActivity.KEY_STATE).equals(GroupListActivity.STATION))
        {
        		
        	thumb_image.setImageResource(R.drawable.group);
        }
        else
        {
        	if(m_myApp.m_screenstate==1)
        		thumb_image.setImageResource(R.drawable.master);
        	else
        		thumb_image.setImageResource(R.drawable.ic_launcher);
        }
        
        thumb_image.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			final	LUCIControl luci_=new LUCIControl(data.get(pos).get(GroupListActivity.KEY_IP));

				luci_.addhandler(GroupListActivity.ssidHandler);
                List<LUCIPacket > luciPacket = new ArrayList<LUCIPacket>();
				
				if(data.get(pos).get(GroupListActivity.KEY_STATE).equals("Free"))
				{
					if(m_myApp.m_screenstate==0)
					{
						Log.v(TAG,"Attempting Creating a ZOne "+getNumberofOwners()+" "+AppPreference.getMaxGroups());
						if(getNumberofOwners()<AppPreference.getMaxGroups())
						{
							final String Zoneid=getUniqueZoneID();
							Log.e("Master","Zone id for "+Zoneid);

							Log.v(TAG,"ZoneID for setting master is "+Zoneid);
                          LUCIPacket   packet1=new LUCIPacket(Zoneid.getBytes(), (short) Zoneid.length(), (short)MIDCONST.MID_DDMS_ZONE_ID
                                 ,(byte)LSSDPCONST.LUCI_SET);


                            LUCIPacket   packet2=new LUCIPacket(Constant.SETMASTER.getBytes(), (short) Constant.SETMASTER.length(), (short)MIDCONST.MID_DDMS
                                    ,(byte)LSSDPCONST.LUCI_SET);

                            luciPacket.add(packet1);
                            luciPacket.add(packet2);
                            luci_.SendCommand(luciPacket);



							data.get(pos).put(GroupListActivity.KEY_ZONEID, Zoneid);
							data.get(pos).put(GroupListActivity.KEY_STATE, GroupListActivity.MASTER);
				
						}
						else
						{
							 Toast.makeText(m_myApp.getApplicationContext(),"Reached Max Zones",Toast.LENGTH_SHORT).show();
						}
						
					}
					else if(m_myApp.m_screenstate==1)
					{
						if(isGroupOwnerAvailable())
						{
						final	String masterZoneid=getGroupZoneID();
                        final  String cSSID=getcSSID();
							
							if(masterZoneid==null)
								return;


                             if (cSSID!=null) {
                                 LUCIPacket packet1 = new LUCIPacket(cSSID.getBytes(), (short) cSSID.length(), (short) MIDCONST.MID_SSID
                                         , (byte) LSSDPCONST.LUCI_SET);
                                 luciPacket.add(packet1);
                             }

                            LUCIPacket   packet2=new LUCIPacket(masterZoneid.getBytes(), (short) masterZoneid.length(), (short)MIDCONST.MID_DDMS_ZONE_ID
                                    ,(byte)LSSDPCONST.LUCI_SET);
                            LUCIPacket packet3= new LUCIPacket(Constant.SETSLAVE.getBytes(),(short) Constant.SETSLAVE.length(),(short)MIDCONST.MID_DDMS,
                                    (byte)LSSDPCONST.LUCI_SET);

                            luciPacket.add(packet2);
                            luciPacket.add(packet3);
                            luci_.SendCommand(luciPacket);
							data.get(pos).put(GroupListActivity.KEY_STATE, GroupListActivity.STATION);



						}
						else
						{
						
							Toast.makeText(m_myApp.getApplicationContext(),"Failed :No Zone Master",Toast.LENGTH_SHORT).show();
							
						}
					}
					
						
				}
				else if (data.get(pos).get(GroupListActivity.KEY_STATE).equals(GroupListActivity.STATION))
				{
					// if(m_myApp.m_screenstate==1)
					 //{
						 luci_.SendCommand(MIDCONST.MID_DDMS,"SETFREE", LSSDPCONST.LUCI_SET);
						 data.get(pos).put(GroupListActivity.KEY_STATE, "Free");
					 //}
				}
				else if (data.get(pos).get(GroupListActivity.KEY_STATE).equals(GroupListActivity.MASTER))
				{
					if(m_myApp.m_screenstate==1)
					{
					
					m_playbackHelper=m_myApp.getCurrentPlaybackHelper();

					luci_.SendCommand(MIDCONST.MID_DDMS,"SETFREE", LSSDPCONST.LUCI_SET);

					data.get(pos).put(GroupListActivity.KEY_STATE, "Free");
                        if (AppPreference.ForceRemoveThePlaYList()) {

                            for (DMRDev dmr : devList) {
                                if (dmr.getIp().equals(data.get(position).get(GroupListActivity.KEY_IP))) {
                                    m_myApp.setCurrentDmrDeviceUdn(dmr.getUuid());
                                    if (m_myApp.getCurrentPlaybackHelper() != null)
                                        m_myApp.getCurrentPlaybackHelper().setDmsHelper(null);
                                    else
                                        Log.e(TAG, "Player is null");
                                }

                            }
                        }


                     // m_myApp.getCurrentPlaybackHelper().setDmsHelper(null);
                     //m_myApp.setDmsBrowseHelperSaved(null);//Clearing the list

			    	for (HashMap<String, String> node  : data)
			    	{
			    		if(node.get(GroupListActivity.KEY_STATE).equals(GroupListActivity.STATION))
			    		{
			    			node.put(GroupListActivity.KEY_STATE, "Free");
			    			SendLUCICommand(MIDCONST.MID_DDMS,"SETFREE",node.get(GroupListActivity.KEY_IP));
			    		}
			    		
			    	}

					}
					
				}
					notifyDataSetChanged();
                sendingHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        luci_.close();
                       // Log.d("Sathi","Thread is closed");

                    }
                });
				

				Log.v(TAG,"OnClick Image"+data.get(pos).get(GroupListActivity.KEY_NAME));
			}

			
		});
        //imageLoader.DisplayImage(song.get(CustomizedListView.KEY_THUMB_URL), thumb_image);
        return vi;
    }
    private String getGroupZoneID()
    {
    	for (HashMap node : data)
    	{
    		if(node.get(GroupListActivity.KEY_STATE).equals(GroupListActivity.MASTER))
    		{
    			
    			return  node.get(GroupListActivity.KEY_ZONEID).toString();
    		}
    		
    	}
		return null;
		
    	
    }




    private String getcSSID()
    {
        for (HashMap node : data)
        {
            if(node.get(GroupListActivity.KEY_STATE).equals(GroupListActivity.MASTER))
            {

                return  node.get(GroupListActivity.KEY_cSSID).toString();
            }

        }
        return null;


    }

    private String getGroupZoneIPADDRESS()
    {
        for (HashMap node : data)
        {
            if(node.get(GroupListActivity.KEY_STATE).equals(GroupListActivity.MASTER))
            {

                return  node.get(GroupListActivity.KEY_IP).toString();
            }

        }
        return null;


    }




    private String getUniqueZoneID()
    {
    	int port=3000;
    	for (HashMap node : data)
    	{
    		if(node.get(GroupListActivity.KEY_STATE).equals(GroupListActivity.MASTER))
    		{
    			

    			int temp=getPortfromURL("http://"+node.get(GroupListActivity.KEY_ZONEID).toString());
    			/*portScanner.useDelimiter(":");
    			int temp=portScanner.nextInt();*/
    			Log.v(TAG,"Master found with port= " +" "+temp);
    			if(port<=temp)
    				port=temp+100;

    			
    		}
    		
    	}
		return "239.255.255.251:"+port;
		
    	
    }
    public  int getPortfromURL(String url) {
       

            URL aURL = null;
			try {
				aURL = new URL(url);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(aURL==null)
				Log.v(TAG,"Failed to create URL");

			Log.v(TAG,"getPortfromURL:" + aURL.getPort());
            return aURL != null ? aURL.getPort() : 0;
            
    }
    private boolean isGroupOwnerAvailable()
    {
    	for (HashMap node : data)
    	{
    		if(node.get(GroupListActivity.KEY_STATE).equals(GroupListActivity.MASTER))
    		{
    			Log.v(TAG," Found Master ");
    			return true;
    		}
    		
    	}
		return false;
    	
    }
    private int getNumberofOwners()
    {
    	int count=0;
    	for (HashMap node : data)
    	{
    		if(node.get(GroupListActivity.KEY_STATE).equals(GroupListActivity.MASTER))
    		{
    			count++;
    		}
    		
    	}
		return count;
    	
    }
    void SendLUCICommand(final int MID,final String Data, final String IP)
	{
		new Thread() {
	        public void run() {
		String messageData = null;


		LUCIPacket packet=new LUCIPacket(Data.getBytes(), (short) Data.length(),(short) MID);

		int server_port = Integer.parseInt("7777");
		DatagramSocket s = null;


		try {
			s = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch blo
			// ck
			e.printStackTrace();
		}
		InetAddress local = null;
		try {
			local = InetAddress.getByName(IP);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		int msg_length=packet.getlength();

		byte[] message = new byte[msg_length] ;
		packet.getPacket(message);


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
}