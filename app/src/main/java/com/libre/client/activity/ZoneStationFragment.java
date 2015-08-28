package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import com.libre.client.LUCIPacket;
import com.libre.constants.LSSDPCONST;
import com.libre.constants.MIDCONST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by libre on 26-12-2014.
 */
public class ZoneStationFragment extends Fragment {

    private static final String TAG = "ZoneStationFragment";
    ListView list;
    static LazyAdapter adapter;
    LibreApplication m_myApp;
    static final String KEY_SONG = "song"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_STATE = "state";
    static final String KEY_NAME = "name";
    static final String KEY_TYPE = "type";
    static final String KEY_IP = "IP";
    static final String KEY_THUMB_URL = "thumb_url";
    static final String KEY_ZONEID = "Zone_ID";
    static final String MASTER = "Zone Master";
    static final String STATION = "Zone Station";
    static final String FREE =  "Free";
    static final String KEY_cSSID= "DDMSConcurrentSSID";

    ArrayList<HashMap<String, String>> ddmsnodeList;
    HashMap<String, String> groupListOwner;
    SwipeRefreshLayout swipeLayout;
    Handler mHandler;
    int devcount;
    LUCIControl luci_;
    Handler sendingHandler;
    TextView devtext;
    ActionBarActivity actionBarActivity;


    public static ZoneStationFragment newInstance(HashMap<String, String> groupListOwner) {
        ZoneStationFragment myFragment = new ZoneStationFragment();


        Bundle args = new Bundle();
        args.putSerializable("group", groupListOwner);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ActionBar actionBar = ((ActionBarActivity) activity).getSupportActionBar();
        actionBarActivity = ((ActionBarActivity) activity);
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Station Selection");
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_myApp = (LibreApplication) getActivity().getApplication();
        m_myApp.m_screenstate = 1;
        devtext = new TextView(getActivity());
        sendingHandler= new Handler();
        Bundle bundle = getArguments();
        ddmsnodeList = new ArrayList<HashMap<String, String>>();
        // (ArrayList<HashMap<String, String>>) bundle.getSerializable("ddms");
        groupListOwner = (HashMap<String, String>) bundle.getSerializable("group");


        LSSDPNodeDB LSSDPDB_ = LSSDPNodeDB.getInstance();
        for (LSSDPNodes node : LSSDPDB_.GetDB()) {
            updateGroupList(node, groupListOwner);

        }
        /*mHandler = new Handler() {
            @SuppressLint("InflateParams")
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == LSSDPCONST.LUCI_RESP_RECIEVED)

                {
                    LUCIPacket pkt = (LUCIPacket) msg.obj;
                    Log.e(TAG, "Lazy adapater receving handler- Messagebox" + pkt.getCommand());

                    if (pkt.getCommand() == 105)

                    {
                        Log.e(TAG, "Trying to get at 105-methodin ZoneSlaveFragment" + pkt.getCommandType());
                        byte[] data = new byte[pkt.getDataLen()];
                        pkt.getpayload(data);
                        String str = new String(data, 0, pkt.getDataLen());
                        LazyAdapter.SSID = str;
                        Log.e(TAG, "SSID = " + LazyAdapter.SSID + "Raw String ");

                    }

                }


            }
        };
        */


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        list = (ListView) rootView.findViewById(R.id.list);
        adapter = new LazyAdapter(actionBarActivity, ddmsnodeList, m_myApp, GroupListActivity.devList);


        list.setAdapter(adapter);


        setHasOptionsMenu(true);

        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                m_myApp.getScanThread().clearNodes();
                ddmsnodeList.clear();
                StartLSSDPScan();

            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @SuppressWarnings("rawtypes")
            public boolean onItemLongClick(AdapterView parent, View view, final int position, long id) {
                HashMap<String, String> node = ddmsnodeList.get(position);
                DMRDev dev = GetSourceUDN(node);
                if (dev == null)
                    Toast.makeText(getActivity(), "Renderer not found", Toast.LENGTH_SHORT).show();
                else

                {
                    m_myApp.setCurrentDmrDeviceUdn(dev.getUuid());
                    m_myApp.setSpeakerName(dev.getDevName());
                    startActivity(new Intent(getActivity(), MainActivity.class));
                }
                return false;

            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                new DialogGroupListActivity(getActivity(), position, ddmsnodeList, 1);
            }
        });


        return rootView;
    }


    public void updateGroupList(final LSSDPNodes resp, final HashMap<String, String> grpowner) {


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                if (resp != null) {
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
                        devcount++;
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                    } else if (resp.getDeviceState().equals("M")) {
                        if (resp.getIP().equals(grpowner.get(KEY_IP))) {
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
                            devcount++;
                            if (adapter != null)
                                adapter.notifyDataSetChanged();
                        }
                    } else if (resp.getDeviceState().equals("S")) {
                        if (grpowner.get(KEY_ZONEID).equals(resp.getZoneID())) {
                            map.put(KEY_ID, "ABC");
                            map.put(KEY_STATE, STATION);
                            map.put(KEY_IP, resp.getIP());
                            map.put(KEY_NAME, resp.getFriendlyname());
                            map.put(KEY_ZONEID, resp.getZoneID());
                            if (resp.getSpeakerType().equals("0"))
                                map.put(KEY_TYPE, "STEREO");
                            else if (resp.getSpeakerType().equals("1"))
                                map.put(KEY_TYPE, "LEFT");
                            else if (resp.getSpeakerType().equals("2"))
                                map.put(KEY_TYPE, "RIGHT");
                            ddmsnodeList.add(map);
                            devcount++;
                            if (adapter != null)
                                adapter.notifyDataSetChanged();
                        }
                    }
                    if (devtext != null) {

                        devtext.setText(Integer.toString(devcount));
                    }


                }
                else
                    Log.d(TAG,"Response is null");
            }
        });

    }


    private void StartLSSDPScan() {
        m_myApp.getScanThread().clearNodes();
        devcount = 0;
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

    @Override
    public void onResume() {
        super.onResume();
        StartLSSDPScan();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "On Pause ");
        m_myApp.getScanThread().clearNodes();
        m_myApp.getScanThread().removehandler();
        ddmsnodeList.clear();
        adapter.notifyDataSetChanged();
        super.onPause();
    }


    Handler handler = new Handler() {
        @SuppressLint("InflateParams")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LSSDPCONST.LSSDP_NEW_NODE_FOUND) {
                LSSDPNodes node = (LSSDPNodes) msg.obj;
                updateGroupList(node, groupListOwner);

            } else if (msg.what == LSSDPCONST.LUCI_RESP_RECIEVED) {
                Log.e(TAG, "---LUCI_RESP_RECIEVED");
            }


        }
    };


    private boolean isGroupOwnerAvailable() {
        for (HashMap node : ddmsnodeList) {
            if (node.get(GroupListActivity.KEY_STATE).equals(MASTER)) {

                Log.v(TAG, "Found Master");
                return true;
            }

        }
        return false;

    }

    private  String getCurrentMasterZoneid(){
        for (HashMap node:ddmsnodeList){
            if (node.get(GroupListActivity.KEY_STATE).equals(MASTER)) {

                return  node.get(GroupListActivity.KEY_ZONEID).toString();

            }

        }
        return  null;
    }

    private  String getCurrentMastercSSID(){
        for (HashMap node:ddmsnodeList){
            if (node.get(GroupListActivity.KEY_STATE).equals(MASTER)) {

                return  node.get(KEY_cSSID).toString();

            }

        }
        return  null;
    }


    private  String getCurrentMasterZoneipAdress(){
        for (HashMap node:ddmsnodeList){
            if (node.get(GroupListActivity.KEY_STATE).equals(MASTER)) {

                return  node.get(GroupListActivity.KEY_IP).toString();

            }

        }
        return  null;
    }

    private boolean JoinAll() {
        String masterZoneid=getCurrentMasterZoneid();

        if (m_myApp.m_screenstate == 0)
            return false;

        if (!isGroupOwnerAvailable())
             return false;

        if (masterZoneid==null)
            return false;


        //Toast.makeText(getActivity().getApplicationContext(),"Zoneid"+getCurrentMasterZoneid(),Toast.LENGTH_SHORT).show();
        for (HashMap<String, String> node : ddmsnodeList) {
            if ((node.get(GroupListActivity.KEY_STATE).equals(FREE))) {
                List<LUCIPacket> luciPacket = new ArrayList<LUCIPacket>();
               final LUCIControl luci_ = new LUCIControl(node.get(KEY_IP));
                node.put(GroupListActivity.KEY_STATE, STATION);
                String cSSID=getCurrentMastercSSID();

                if (cSSID!=null) {
                    LUCIPacket packet1 = new LUCIPacket(cSSID.getBytes(), (short) cSSID.length(), (short) MIDCONST.MID_SSID
                            , (byte) LSSDPCONST.LUCI_SET);
                    luciPacket.add(packet1);
                }
                LUCIPacket packet2=new LUCIPacket(masterZoneid.getBytes(), (short) masterZoneid.length(), (short) MIDCONST.MID_DDMS_ZONE_ID
                        ,(byte) LSSDPCONST.LUCI_SET);
                LUCIPacket packet3= new LUCIPacket(Constant.SETSLAVE.getBytes(),(short) Constant.SETSLAVE.length(),(short) MIDCONST.MID_DDMS,
                        (byte) LSSDPCONST.LUCI_SET);

                luciPacket.add(packet2);
                luciPacket.add(packet3);
                luci_.SendCommand(luciPacket);






               // luci_.SendLUCICommand(MIDCONST.MID_DDMS, "SETSLAVE", node.get(KEY_IP));
            }


        }
        adapter.notifyDataSetChanged();
        return false;

    }

    private boolean FreeAll() {

        if (m_myApp.m_screenstate == 0)
            return false;
        LUCIControl luci_ = new LUCIControl();

        for (HashMap<String, String> node : ddmsnodeList) {
            if (!(node.get(GroupListActivity.KEY_STATE).equals(MASTER))) {
                Log.v(TAG, "Found Master");
                node.put(GroupListActivity.KEY_STATE, "Free");
                //m_playbackHelper=m_myApp.getCurrentPlaybackHelper();
                //m_playbackHelper.StopPlayback();
                luci_.SendLUCICommand(MIDCONST.MID_DDMS, "SETFREE", node.get(KEY_IP));

            }

        }
        luci_.close();

        adapter.notifyDataSetChanged();
        return true;


    }


    private DMRDev GetSourceUDN(final HashMap<String, String> node) {

        for (DMRDev dmr : GroupListActivity.devList) {
            if (dmr.getIp().equals(node.get(KEY_IP))) {
                Log.v(TAG, "Found a MActch in DMR list ");
                return dmr;
            }

        }
        return null;

    }


    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.group_actions, menu);

        devtext.setTextColor(Color.YELLOW);

        devtext.setPadding(5, 0, 5, 0);
        devtext.setTypeface(null, Typeface.BOLD);
        devtext.setTextSize(14);
        if (AppPreference.ShowStationCount())
        menu.add(0, 1, 2, "Count").setActionView(devtext).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {

            case  android.R.id.home:
               getActivity(). getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ZoneMasterFragment.newInstance())
                        .commit();
                return true;


            /*case R.id.menu_scan_ddms:
                m_myApp.getScanThread().clearNodes();
                ddmsnodeList.clear();
                StartLSSDPScan();
                return true;*/

            case R.id.action_joinall:
                JoinAll();
                return true;
            case R.id.action_freeall:
                FreeAll();

                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_myApp.getScanThread().clearNodes();

    }
}
