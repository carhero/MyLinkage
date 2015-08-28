package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.libre.client.AppPreference;
import com.libre.client.LUCIControl;
import com.libre.client.LUCIPacket;
import com.libre.constants.LSSDPCONST;
import com.libre.constants.MIDCONST;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by libre on 24-01-2015.
 */
public class DialogGroupListActivity {
    int HostPresent=0;

    static final String KEY_TYPE = "type";
    static final String KEY_ID = "id";
    static final String KEY_STATE = "state";
    static final String KEY_NAME = "name";
    static final String KEY_IP = "IP";
    static final String KEY_THUMB_URL = "thumb_url";
    AlertDialog levelDialog ;
    int check_selected = -2;
    static  Handler handler;
    int position;


    static final String KEY_ZONEID = "Zone_ID";
    static final String MASTER    = "Zone Master";
    static final String STATION    = "Zone Station";

    private static final String TAG = "GroupListActvity";
    Context context;
    LibreApplication libreApplication;
    ArrayList<HashMap<String, String>> ddmsnodeList ;


    public DialogGroupListActivity(final Activity context,final int position,final ArrayList<HashMap<String, String>> ddmsnodeList, final int screenstate) {
      libreApplication=   (LibreApplication)context.getApplication();
        this.ddmsnodeList=ddmsnodeList;
        this.context=context;
        this.position=position;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_ddms, (ViewGroup)context.findViewById(R.id.ddms_volume));
        // Strings to Show In Dialog with Radio Buttons
        final SeekBar seekBar=(SeekBar)layout.findViewById(R.id.ddms_volume);
        final TextView tv=(TextView)layout. findViewById(R.id.seekbarValue);
      handler = new Handler() {
            @SuppressLint("InflateParams")
            @Override
            public void handleMessage(Message msg) {

                LUCIPacket pkt = (LUCIPacket) msg.obj;
                byte[] data = new byte[pkt.getDataLen() ];
                pkt.getpayload(data);
                String str=new String(data,0,pkt.getDataLen());

                Log.v(TAG, "---LUCI_RESP_RECIEVED for MID volume =" + str);

                if (msg.what == LSSDPCONST.LUCI_RESP_RECIEVED)
                {

                    //LUCIPacket pkt = (LUCIPacket) msg.obj;
                    if(pkt.getCommand()== MIDCONST.MID_VOL)
                    {

                        /*if(pkt.getCommandType()== LSSDPCONST.LUCI_SET || pkt.getDataLen()==0)
                        {
                            return;
                        }
*/


                        /*byte[] data = new byte[pkt.getDataLen() ];
                        pkt.getpayload(data);
                        String str=new String(data,0,pkt.getDataLen());*/
                        Log.v(TAG, "---LUCI_RESP_RECIEVED for MID volume =" + str);
                        tv.setText(str);
                        int vol;
                       /* if(HostPresent==1)
                        {
                            Log.v(TAG, "HostPresent");
                            vol=scale(Integer.parseInt(str),0,32,0,100);
                        }
                        else*/
                        if (str!=null&&!str.equals("")) {
                            Log.v(TAG, "---LUCI_RESP_RECIEVED Progressbar volume =" + str);
                            vol = Integer.parseInt(str);
                            seekBar.setProgress(vol);
                        }
                    }
                    if(pkt.getCommand()== MIDCONST.MID_HOST_PRESENT)
                    {
                        if(pkt.getCommandType()== LSSDPCONST.LUCI_SET || pkt.getDataLen()==0)
                        {
                            return;
                        }

                        /*byte[] data = new byte[pkt.getDataLen() ];
                        pkt.getpayload(data);
                        String str=new String(data,0,pkt.getDataLen());*/

                        HostPresent= Integer.parseInt(str);
                        //seekBar.setProgress(vol);
                    }
                }


            }
        };
        final LUCIControl luci_=new LUCIControl(ddmsnodeList.get(position).get(KEY_IP));
        luci_.addhandler(handler);
      // luci_.SendCommand(MIDCONST.MID_HOST_PRESENT,null, LSSDPCONST.LUCI_GET);
        luci_.SendCommand(MIDCONST.MID_VOL,null, LSSDPCONST.LUCI_GET);
        HashMap<String, String> node = new HashMap<String, String>();
        node = ddmsnodeList.get(position);
        List<String> strings = new ArrayList<String>();

        if(node.get(KEY_STATE).equals("Free") || node.get(KEY_STATE).equals(MASTER))
        {
            strings.add("Remote Play");

        }

        strings.add("Left Speaker");
        strings.add("Right Speaker");
        strings.add("Stereo Speaker");
        strings.add("Edit Device Name");
        strings.add("Start Aux Input");
        strings.add("Stop Aux Input");
        strings.add("Bluetooth-On");
        strings.add("Bluetooth-Off");

        strings.add("yhcha test text1");
        strings.add("yhcha test text2");
        strings.add("yhcha test text3");
        strings.add("yhcha test text4");



        final CharSequence[] it = strings.toArray(new String[strings.size()]);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setSingleChoiceItems(it, -2, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                check_selected=item;
            }

        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                HashMap<String, String> node = new HashMap<String, String>();
                node = ddmsnodeList.get(position);
                if( screenstate==0&&
                        ddmsnodeList.get(position).get(GroupListActivity.KEY_STATE).equals(STATION)
                        && ! AppPreference.HideGroupMembers() )
                {
                    Log.v(TAG, "Skip Slave in Screen 0");
                    return;
                }
                if( screenstate==1&&
                        ddmsnodeList.get(position).get(GroupListActivity.KEY_STATE).equals(STATION)
                        )
                {
                    check_selected++;

                }

                switch(check_selected)
                {

                    case 0://remote command
                        //Log.v(TAG,"Menu item string"+nodelist.get(pos).getname().toString());
                        Intent intent = new Intent(context, LmpListViewActivity1.class);
                        intent.putExtra(Constant.DEVICENAME, node.get(KEY_NAME));
                        intent.putExtra(Constant.PORT, Integer.parseInt("7777"));
                        intent.putExtra(Constant.IPADRESS, node.get(KEY_IP));
                        context.startActivity(intent);
                        break;


                    case 1:
                        Log.v(TAG, "Set Left");
                        ddmsnodeList.get(position).put(KEY_TYPE, "LEFT");
                        luci_.SendCommand(MIDCONST.MID_DDMS,"SETLEFT", LSSDPCONST.LUCI_SET);
                        if (screenstate==0)
                            ZoneMasterFragment.adapter.notifyDataSetChanged();
                        else  if (screenstate==1)
                            ZoneStationFragment.adapter.notifyDataSetChanged();
                        break;
                    case 2:
                        Log.v(TAG, "Set Right");
                        ddmsnodeList.get(position).put(KEY_TYPE, "RIGHT");
                        luci_.SendCommand(MIDCONST.MID_DDMS,"SETRIGHT", LSSDPCONST.LUCI_SET);
                        if (screenstate==0)
                            ZoneMasterFragment.adapter.notifyDataSetChanged();
                        else  if (screenstate==1)
                            ZoneStationFragment.adapter.notifyDataSetChanged();
                        break;

                    case 3:

                        Log.v(TAG, "Set Stereo");
                        ddmsnodeList.get(position).put(KEY_TYPE, "STEREO");
                        luci_.SendCommand(MIDCONST.MID_DDMS,"SETSTEREO", LSSDPCONST.LUCI_SET);
                        if (screenstate==0)
                            ZoneMasterFragment.adapter.notifyDataSetChanged();
                        else  if (screenstate==1)
                            ZoneStationFragment.adapter.notifyDataSetChanged();
                        break;
                    case 4:
                        Log.v(TAG, "Edit Device Name");
                        DisplayEditDialog(node);
                        luci_.close();

                        dialog.dismiss();
                        //luci_.SendCommand(MIDCONST.MID_DDMS,"SETSTEREO",LSSDPCONST.LUCI_SET);
                        break;

                    case 5://remote command
                        luci_.SendCommand(MIDCONST.MID_AUX_START,null, LSSDPCONST.LUCI_SET);

                        break;
                    case 6://remote command
                        luci_.SendCommand(MIDCONST.MID_AUX_STOP,null, LSSDPCONST.LUCI_SET);

                        break;
                    case 7:
                        luci_.SendCommand(MIDCONST.MID_BLUETOOTH,"ON", LSSDPCONST.LUCI_SET);
                        break;

                    case 8:
                        luci_.SendCommand(MIDCONST.MID_BLUETOOTH,"OFF", LSSDPCONST.LUCI_SET);
                        break;

                    case 9:
                        Intent intent1= new Intent(context,LuciMessenger.class);
                        intent1.putExtra(Constant.IPADRESS,ddmsnodeList.get(position).get(KEY_IP));
                        intent1.putExtra(Constant.DEVICENAME,ddmsnodeList.get(position).get(KEY_NAME));
                        context.startActivity(intent1);

                        break;




                }

                luci_.close();
                luci_.shutdown();

                dialog.dismiss();


            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                luci_.close();
                luci_.shutdown();
                dialog.cancel();
            }
        });


        seekBar.setMax(100);

        seekBar.setProgress(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //int volume=0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                tv.setText(String.valueOf(i));
                //volume=i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //String volume=Integer.toString(seekBar.getProgress());
                int vol=seekBar.getProgress();
                String volume= String.valueOf(vol);
                tv.setText(volume);

               // String volume= Integer.toString(vol);
                Log.v(TAG, "Volume----" + volume);
                luci_.SendCommand(MIDCONST.MID_VOL,volume, LSSDPCONST.LUCI_SET);
            }
        });
        levelDialog = builder.create();
        levelDialog.setView(layout);
        levelDialog.show();




    }



    public int scale(final int valueIn,
                     final int baseMin, final int baseMax,
                     final int limitMin, final int limitMax) {
        //return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
        return valueIn;
    }








    protected void DisplayEditDialog(final HashMap<String, String> node)
    {

        final Dialog  custom = new Dialog(context);
        custom.setContentView(R.layout.editdialog);
        final EditText Fname = (EditText)custom.findViewById(R.id.devname);
        // Fname.setHint("Name:"+node.getFriendlyname());
        Button savebtn = (Button)custom.findViewById(R.id.savebtn);
        Button canbtn = (Button)custom.findViewById(R.id.canbtn);

        custom.setTitle("Custom Dialog");
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub

                String    devicename = Fname.getText().toString();

                int count=devicename.getBytes().length;

                Log.e(TAG,"the editext count is"+count);

                Log.v(TAG, " Name is " + devicename);


                if (!devicename.matches("")&&count<=50) {

                    //  luci_.SendLUCICommand(MIDCONST.MID_DEVNAME,null,node.get(KEY_IP));

                    HashMap<String, String> node = ddmsnodeList.get(position);
                    DMRDev dev=GetSourceUDN(node);
                    if (dev!=null) {
                        if (dev.getIp().equals( ddmsnodeList.get(position).get(KEY_IP)) ){
                            dev.getUuid();
                            if(LibreApplication.PLAYBACK_HELPER_MAP.containsKey(dev.getUuid())) {
                                LibreApplication.PLAYBACK_HELPER_MAP.get(dev.getUuid()).getDmrHelper().getDmrProcessor().dispose();
                                LibreApplication.PLAYBACK_HELPER_MAP.remove(dev.getUuid());
                            }
                        }
                    }


                    SendLUCICommand(MIDCONST.MID_DEVNAME, devicename, node.get(KEY_IP));
                    custom.dismiss();
                    if (levelDialog.isShowing())
                        levelDialog.dismiss();
                }
                else if (devicename.equals("")||devicename==null){
                    Toast.makeText(context, "Text cant be blank", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context,"You have reached maximum level",Toast.LENGTH_SHORT).show();
                }




            }
        });
        canbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub

                custom.dismiss();
                if (levelDialog.isShowing())
                    levelDialog.dismiss();


                //wifihandler.sendEmptyMessage(FINISH_NETCONFIG);
            }
        });
        custom.show();

    }

    void SendLUCICommand(final int MID,final String Data, final String IP)
    {
        new Thread() {
            public void run() {
                String messageData = null;


                LUCIPacket packet=new LUCIPacket(Data.getBytes(), (short) Data.getBytes().length,(short) MID);

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

    private DMRDev GetSourceUDN(final HashMap<String, String> node)
    {

        for (DMRDev dmr  : GroupListActivity.devList)
        {
            if(dmr.getIp().equals(node.get(KEY_IP)))
            {
                Log.v(TAG, "Found a MActch in DMR list ");
                return dmr;
            }

        }
        return null;

    }






}
