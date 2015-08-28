package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.libre.client.AppPreference;
import com.libre.client.LSSDPNodeDB;
import com.libre.client.LSSDPNodes;
import com.libre.client.LUCIControl;
import com.libre.constants.LSSDPCONST;

import java.util.ArrayList;
import java.util.HashMap;

public class ZoneMasterFragment extends Fragment {
    static final String KEY_SONG = "song"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_STATE = "state";
    static final String KEY_NAME = "name";
    HashMap<String, String> groupListOwner = new HashMap<String, String>();
    LibreApplication m_myApp;

    static final String KEY_TYPE = "type";
    static final String KEY_IP = "IP";
    static final String KEY_THUMB_URL = "thumb_url";

    private static final String TAG = "ZoneMasterFragment" ;
    static final String KEY_ZONEID = "Zone_ID";
    static final String MASTER    = "Zone Master";
    static final String STATION    = "Zone Station";
    static final String KEY_cSSID= "DDMSConcurrentSSID";
    SwipeRefreshLayout swipeLayout;
    static LazyAdapter adapter;
    TextView devtext;
    ListView list;
    LUCIControl luci_;
    ArrayList<HashMap<String, String>> ddmsnodeList;
    Context c;
    private Handler mHandler;
    Activity actionBarActivity;
    int devcount;
    Handler ssidHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");

        m_myApp = (LibreApplication) getActivity().getApplication();
        m_myApp.m_screenstate = 0;
       // LibreApplication.SSID=null;
        ddmsnodeList = new ArrayList<HashMap<String, String>>();
        devtext = new TextView(getActivity());


        setHasOptionsMenu(true);


    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(TAG, "OnAttach");

        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBarActivity=((ActionBarActivity)getActivity());
        devcount=0;
        if (actionBar!=null)
        {
            actionBar.setTitle("Zone MasterFregment");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static ZoneMasterFragment newInstance() {
        ZoneMasterFragment myFragment = new ZoneMasterFragment();

        return myFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart");
    }

    public ZoneMasterFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "OnCreateView");

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);


            swipeLayout = (SwipeRefreshLayout)rootView. findViewById(R.id.swipe);
            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //Log.d(TAG, "swipeLayout refresh");
                    m_myApp.getScanThread().clearNodes();
                    ddmsnodeList.clear();
                    StartLSSDPScan();
                    swipeLayout.setRefreshing(false);
                    LSSDPNodeDB LSSDPDB_ = LSSDPNodeDB.getInstance();

                    for (LSSDPNodes node  : LSSDPDB_.GetDB())
                    {
                        updateListMainList(node);

                    }

                }
            });



            list=(ListView)rootView.findViewById(R.id.list);
            adapter=new LazyAdapter(actionBarActivity, ddmsnodeList,m_myApp, GroupListActivity.devList);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Log.v(TAG, "Circle item OnitemClick" + position);

                    groupListOwner = ddmsnodeList.get(position);

                    if (groupListOwner.get(KEY_STATE).equals("Free")) {
                        Log.v(TAG, "Free node in First screen selected");
                        new DialogGroupListActivity(getActivity(),position,ddmsnodeList,0);


                    } else if (groupListOwner.get(KEY_STATE).equals(MASTER)) {
                        Log.e(TAG, "Creating Zone List for Zone ID" + groupListOwner.get(KEY_IP));
                       /* LUCIControl  luci_=new LUCIControl(groupListOwner.get(KEY_IP));
                       // LUCIControl  luci_=LUCIControl.getInstance(groupListOwner.get(KEY_IP));
                        luci_.addhandler(GroupListActivity.ssidHandler);
                        luci_.SendCommand(MIDCONST.MID_SSID,null,LSSDPCONST.LUCI_GET);


                        Log.e("Sathi","Sending 105 Command to "+groupListOwner.get(KEY_NAME));*/
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.container, ZoneStationFragment.newInstance(groupListOwner));
                        fragmentTransaction.commit();


                    }
                }
            });

            list.setOnItemLongClickListener (new AdapterView.OnItemLongClickListener() {
                @SuppressWarnings("rawtypes")
                public boolean onItemLongClick(AdapterView parent, View view, final int position, long id) {
                    HashMap<String, String> node = ddmsnodeList.get(position);
                    DMRDev dev=GetSourceUDN(node);
                    if(dev==null) {
                        Toast.makeText(getActivity(), "Renderer not found", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getActivity(), "메인 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                        m_myApp.setCurrentDmrDeviceUdn(dev.getUuid());
                        m_myApp.setSpeakerName(dev.getDevName());
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }
                    return false;

                }
            });


            return rootView;
        }

    @Override
   public void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
        StartLSSDPScan();





    }




    public void updateListMainList(final LSSDPNodes resp)
    {
        if(resp==null)
            return;
        Log.v(TAG, "updateListMainList Response IP= " + resp.getIP() + ", ZoneID= " + resp.getZoneID()
                + " Name= " + resp.getFriendlyname() + ", State= " + resp.getDeviceState());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> map = new HashMap<String, String>();

                if (resp != null) {

                    // adding each child node to HashMap key => value
                    if (resp.getDeviceState().equals("F")) {
                        map.put(KEY_ID, "XYZ");
                        map.put(KEY_STATE, "Free");
                        map.put(KEY_NAME, resp.getFriendlyname());
                        map.put(KEY_IP, resp.getIP());

                        if (resp.getSpeakerType() == null) {
                            map.put(KEY_TYPE, "STEREO");
                        } else if (resp.getSpeakerType().equals("0"))
                            map.put(KEY_TYPE, "STEREO");
                        else if (resp.getSpeakerType().equals("1"))
                            map.put(KEY_TYPE, "LEFT");
                        else if (resp.getSpeakerType().equals("2"))
                            map.put(KEY_TYPE, "RIGHT");

                        map.put(KEY_THUMB_URL, "EEE");
                        ddmsnodeList.add(map);
                        adapter.notifyDataSetChanged();
                    } else if (resp.getDeviceState().equals("M")) {
                        map.put(KEY_ID, "ABC");
                        map.put(KEY_STATE, MASTER);
                        map.put(KEY_IP, resp.getIP());
                        map.put(KEY_NAME, resp.getFriendlyname());
                        map.put(KEY_ZONEID, resp.getZoneID());
                        map.put(KEY_cSSID,resp.getcSSID());
                        if (resp.getSpeakerType() == null)
                            map.put(KEY_TYPE, "STEREO");

                        if (resp.getSpeakerType().equals("0"))
                            map.put(KEY_TYPE, "STEREO");
                        else if (resp.getSpeakerType().equals("1"))
                            map.put(KEY_TYPE, "LEFT");
                        else if (resp.getSpeakerType().equals("2"))
                            map.put(KEY_TYPE, "RIGHT");
                        ddmsnodeList.add(map);
                        adapter.notifyDataSetChanged();
                    } else if (resp.getDeviceState().equals("S")) {
                        if (!AppPreference.HideGroupMembers()) {
                            Log.d(TAG, "prefernce is working");
                            map.put(KEY_ID, "ABC");
                            map.put(KEY_STATE, STATION);
                            map.put(KEY_IP, resp.getIP());
                            map.put(KEY_NAME, resp.getFriendlyname());
                            map.put(KEY_ZONEID, resp.getZoneID());
                            map.put(KEY_cSSID,resp.getcSSID());
                            if (resp.getSpeakerType().equals("0"))
                                map.put(KEY_TYPE, "STEREO");
                            else if (resp.getSpeakerType().equals("1"))
                                map.put(KEY_TYPE, "LEFT");
                            else if (resp.getSpeakerType().equals("2"))
                                map.put(KEY_TYPE, "RIGHT");
                            ddmsnodeList.add(map);
                            adapter.notifyDataSetChanged();
                        }

                    }
                    devcount++;
                    devtext.setText(Integer.toString(devcount));


                }
            }
        });

    }


    private void StartLSSDPScan() {
        ddmsnodeList.clear();
        m_myApp.getScanThread().clearNodes();
        devcount=0;
        devtext.setText(Integer.toString(devcount));
        m_myApp.getScanThread().addhandler(handler);
        m_myApp.getScanThread().UpdateNodes();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                m_myApp.getScanThread().UpdateNodes();
                adapter.notifyDataSetChanged();

            }
        },100);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                m_myApp.getScanThread().UpdateNodes();
                adapter.notifyDataSetChanged();






            }
        },200);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                m_myApp.getScanThread().UpdateNodes();
                adapter.notifyDataSetChanged();

            }
        },300);

        adapter.notifyDataSetChanged();


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




    Handler handler = new Handler() {
        @SuppressLint("InflateParams")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LSSDPCONST.LSSDP_NEW_NODE_FOUND)
            {
                LSSDPNodes node = (LSSDPNodes)msg.obj;
                Log.v(TAG, " LSSDP Message-----");
                    updateListMainList(node);
            }
            else if (msg.what == LSSDPCONST.LUCI_RESP_RECIEVED)
            {
                Log.e(TAG, "---LUCI_RESP_RECIEVED");
            }
            if (msg.what == LSSDPCONST.LUCI_SOCKET_NOT_CREATED)
            {   Log.e(TAG, "Opening the alert Dialog");
                new AlertDialog.Builder(getActivity())
                        .setTitle("Socket Creation Failed")
                        .setIcon(R.drawable.ic_launcher)
                        .setMessage("Let me restart app for you")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent mStartActivity = new Intent(getActivity(), GroupListActivity.class);
                                int mPendingIntentId = 123456;
                                PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                                AlarmManager mgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                                System.exit(0);
                                // continue with delete
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }


        }
    };


@Override
    public  void onPause()
    {
        Log.d(TAG, "On Pause ");
        ddmsnodeList.clear();
        m_myApp.getScanThread().clearNodes();
        m_myApp.getScanThread().removehandler();
        adapter.notifyDataSetChanged();
        super.onPause();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {

            case  android.R.id.home:
                    getActivity().finish();
                return false;





            default:
                return false;
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        devcount=0;
        devtext.setText(Integer.toString(devcount));
        devtext.setTextColor(Color.YELLOW);
        devtext.setPadding(5, 0, 5, 0);
        devtext.setTypeface(null, Typeface.BOLD);
        devtext.setTextSize(14);
        if (AppPreference.ShowZoneCount())
        menu.add(0, 1, 2, "Count").setActionView(devtext).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "OnDestroy View");
        m_myApp.getScanThread().clearNodes();
        m_myApp.getScanThread().UpdateNodes();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "OnDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
    }


}