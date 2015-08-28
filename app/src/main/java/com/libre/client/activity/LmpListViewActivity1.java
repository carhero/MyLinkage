package com.libre.client.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.libre.client.LUCIControl;
import com.libre.client.util.PlaybackHelper;

import org.fourthline.cling.model.ModelUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;

public class LmpListViewActivity1 extends ActionBarActivity implements View.OnClickListener{



    protected static final String TAG = "LmpListViewActivity";


    String Browser;
    ArrayList<DataItem> ViewItemArray;

    private int cmd_id;


    private int totalTime;
    private RelativeLayout playview;
    private SeekBar positionbar;
    private boolean SEEK_FLAG = false;
    private TextView volumetetext;


    String name;
    public  static int host_port;
    public  static String host_ip;
    private TextView albumDetails;
    private boolean isPlaying;
    private boolean isShuffle;



    private DatagramSocket mUnicastSocket;
    DatagramPacket dp = null;
    private ImageButton imageBack;
    int intflag = 0;
    private ImageButton imagePause;
    public static final int PORT = 3333;
    private NetworkInterface mNetIf;
    private boolean mRunning = false;
    TextView duration_text;

    TextView totaltime_text;
    ImageView albumart_image;
    boolean iswrongJson = true;
    TextView playing_status;
    SeekBar volumebar;
    int repeat_state;
    boolean gotolastpostion;

    ReceivePacket receivePacket = null;
    Thread receiveUdpThread = null;
    ListView listview;
    private static final String TAG_CMD_ID = "CMD ID";
    private static final String TAG_TITLE = "Title";
    private static final String TAG_WINDOW_CONTENT = "Window CONTENTS";
    private static final String TAG_BROWSER = "Browser";
    private static final String TAG_CUR_INDEX = "Index";
    private static final String TAG_ITEM_COUNT = "Item Count";
    private static final String TAG_ITEM_LIST = "ItemList";
    private static final String TAG_ITEM_ID = "Item ID";
    private static final String TAG_ITEM_TYPE = "ItemType";
    private static final String TAG_ITEM_NAME = "Name";
    private static final int PLAYING = 0;
    private static final int STOPPED = 2;
    private static final int PAUSED = 1;
    private static final int REPEAT_OFF = 0;
    private static final int REPEAT_ONE= 1;
    private static final int REPEAT_ALL = 2;
    private ProgressDialog m_progressDlg;
    private  boolean played;


    Integer Cur_Index;
    ImageButton play_pause;
    ImageButton shuffle;
    ImageButton repeat;
    //LinearLayoutManager mLayoutManager;
    LibreApplication m_myApp;
    PlaybackHelper playbackHelper;
    ViewAdapter mAdapter;


    public class ViewAdapter extends ArrayAdapter<DataItem> {

        private  Context context;
        private  ArrayList<DataItem> ViewItemList;

        public ViewAdapter(Context context, ArrayList<DataItem> ViewArrayList) {

            super(context, R.layout.remotecommand_item, ViewArrayList);

            this.context = context;
            this.ViewItemList = ViewArrayList;
        }

        @Override
        public int getCount() {
            return ViewItemList.size();
        }


        public boolean UpdateData(Context context,ArrayList<DataItem> models) {


            this.context = context;
            this.ViewItemList = models;
            notifyDataSetChanged();
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // 1. Create inflater
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            // 2. Get rowView from inflater



            View rowView = null;
            //if(!ViewItemList.get(position).isGroupHeader()){
            rowView = inflater.inflate(R.layout.remotecommand_item, parent, false);

            // 3. Get icon,title & counter views from the rowView


            ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon);
            TextView titleView = (TextView) rowView.findViewById(R.id.item_title);


            // 4. Set the text for textView
            titleView.setText(ViewItemList.get(position).getItemName()   );
            String folder=" Folder";
            String file=" File ";

            String name=ViewItemList.get(position).getItemName();

            if(ViewItemList.get(position).getItemType().equals(folder))
            {
                imgView.setImageResource(R.drawable.folder1);
            }
            else {
                imgView.setImageResource(R.drawable.note);

            }


            return rowView;
        }
    }








    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        m_myApp=(LibreApplication)getApplication();

        ViewItemArray = new ArrayList<>();
        setContentView(R.layout.lmplayout2);
        Intent intent = getIntent();
        name = intent.getStringExtra(Constant.DEVICENAME);
        host_port = intent.getIntExtra(Constant.PORT, 7777);
        host_ip = intent.getStringExtra(Constant.IPADRESS);
        /*playbackHelper = LibreApplication.PLAYBACK_HELPER_MAP.get(m_myApp.getCurrentDmrDeviceUdn());
        playbackHelper.getDmrHelper().getdmrProcessor(
                LibreApplication.PLAYER_TYPE.REMOTE_PLAY);*/

        Log.e(TAG, "Host Ip Address" + host_ip);
        initializeView();

        receivePacket = new ReceivePacket();
        receiveUdpThread = new Thread(receivePacket);
        receiveUdpThread.start();
        LUCIControl luciControl = new LUCIControl();
        luciControl.SendLUCICommand(Constant.MID_REMOTE_UI, "GETUI", host_ip);
        luciControl.SendLUCICommand(Constant.MID_REMOTE_UI,Constant.BACK,host_ip);
        playing_status=(TextView)findViewById(R.id.playstatus);
        play_pause=(ImageButton) findViewById(R.id.play);
        shuffle=(ImageButton) findViewById(R.id.shuffle);
        repeat=(ImageButton) findViewById(R.id.repeat);
        play_pause.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        repeat.setOnClickListener(this);


        albumart_image =    (ImageView) findViewById(R.id.albumart);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Remote Play");

        listview= (ListView) findViewById(R.id.recyclerview);
        mAdapter = new ViewAdapter(this,ViewItemArray);

        listview.setAdapter(mAdapter);

        listview.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {

                        LUCIControl luciControl = new LUCIControl();
                        luciControl.SendLUCICommand(Constant.MID_REMOTE, Constant.SELECT_ITEM + ":" + position, host_ip);
                        m_progressDlg = ProgressDialog.show(LmpListViewActivity1.this, "Notice", "Loading...", true, true, null);


                    }
                });

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState==0)
                {
                    Log.i(TAG,"SCROLLING");
                 int firstVisibleItem= view.getFirstVisiblePosition();
                    int lastvisibleItem=view.getLastVisiblePosition();

                    if (firstVisibleItem==0)
                    {
                        LUCIControl luciControl = new LUCIControl();
                        luciControl.SendLUCICommand(Constant.MID_REMOTE, Constant.SCROLL_UP, host_ip);
                        Log.i(TAG,"SCROLL_UP");
                    }

                    if (lastvisibleItem==49)
                    {
                        LUCIControl luciControl = new LUCIControl();
                    luciControl.SendLUCICommand(Constant.MID_REMOTE, Constant.SCROLL_DOWN, host_ip);
                      Log.i(TAG,"SCROLL_DOWN");

                    }


                }


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {


                mLastFirstVisibleItem = firstVisibleItem;

            }
        });




        duration_text = (TextView)findViewById(R.id.time);
        totaltime_text = (TextView)findViewById(R.id.totaltime);
        playview   =  (RelativeLayout)findViewById(R.id.playview);
        positionbar=(SeekBar)findViewById(R.id.position_remote_bar);
        volumebar= (SeekBar)findViewById(R.id.remote_seek_volume);
        volumetetext= (TextView)findViewById(R.id.volume);
        albumDetails = (TextView) findViewById(R.id.details);

        positionbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (totalTime!=0){
                    String nowTime = ModelUtil.toTimeString(progress);

                    duration_text.setText(nowTime);


                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SEEK_FLAG= true;

                int pos = seekBar.getProgress();
                String duration = Integer.toString(pos * 1000) ;
                LUCIControl luciControl= new LUCIControl();
                luciControl.SendLUCICommand(40,"SEEK:"+duration,host_ip);
                luciControl.close();


                Log.d("SEEK", "Seekbar Position" + seekBar.getProgress() + "");

            }
        });
        volumebar.setMax(100);


        volumebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int m_progress=0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_progress=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                LUCIControl luciControl= new LUCIControl();
                luciControl.SendLUCICommand(Constant.MID_VOLUME,Integer.toString(m_progress),host_ip);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        volumetetext.setText(Integer.toString(m_progress));
                    }
                });
                luciControl.close();


            }
        });

        // adapter = new ViewAdapter(this, ViewItemArray);




       /* listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter);



        });*/









    }










    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, " Activity Resume");

        //receivepacket();
    }

    protected void onDestroy() {
        mRunning=false;
        super.onDestroy();
        //UPNP_PROCESSOR.unbindUpnpService();
        //unregisterReceiver(mLog.e(TAG,"onRemoteDeviceRemoved.....")_mountedReceiver);
    }

    private void ParseJson(String jsonStr) {
        Log.e(TAG,"PaRSEjSON"+jsonStr);
        if (jsonStr != null) {
            try {

                ViewItemArray.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (m_progressDlg!=null)
                            m_progressDlg.dismiss();

                    }
                });
                JSONObject root = new JSONObject(jsonStr);
                cmd_id = root.getInt(TAG_CMD_ID);
                JSONObject window = root.getJSONObject(TAG_WINDOW_CONTENT);
                if (cmd_id == 1) {
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                            listview.setVisibility(View.VISIBLE);
                            playview.setVisibility(View.INVISIBLE);
                        }
                    });
                    iswrongJson=false;

                    Browser = window.getString(TAG_BROWSER);

                    Cur_Index = window.getInt(TAG_CUR_INDEX);
                    Integer item_count = window.getInt(TAG_ITEM_COUNT);
                    if (item_count==0)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LmpListViewActivity1.this, "No item-Empty", Toast.LENGTH_SHORT).show();
                            }
                        });



                    JSONArray ItemList = window.getJSONArray(TAG_ITEM_LIST);
                    Log.v(TAG, "JSON PARSER item_count =  " + item_count + "  Array SIZE = " + ItemList.length());
                    // looping through All Contacts
                    for (int i = 0; i < ItemList.length(); i++) {
                        JSONObject item = ItemList.getJSONObject(i);
                        DataItem viewItem = new DataItem();
                        viewItem.setItemID(item.getInt(TAG_ITEM_ID));
                        viewItem.setItemType(item.getString(TAG_ITEM_TYPE));
                        viewItem.setItemName(item.getString(TAG_ITEM_NAME));
                        ViewItemArray.add(viewItem);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                           /* if (gotolastpostion)
                                mLayoutManager.scrollToPosition(49);
                            else
                                mLayoutManager.scrollToPosition(0);
                            gotolastpostion=false;*/

                        }
                    });

                } else if (cmd_id == 3)
                {
                    final String   album = window.getString("Album");
                    final String   artist = window.getString("Artist");
                    final String   trackName = window.getString("TrackName");
                    totalTime = window.getInt("TotalTime")/1000;
                    final String covertart=window.getString("CoverArtUrl");
                    final int playstatus= window.getInt("PlayState");
                    final int shuffle_state=window.getInt("Shuffle");
                    repeat_state=window.getInt("Repeat");
                /*    playbackHelper.setSinger(artist);
                    playbackHelper.setSongName(trackName);*/

                   //Intent intent= new Intent(LmpListViewActivity.this,MainActivity.class);
                   /* m_myApp.setM_currentPlayState(LibreApplication.PLAYER_TYPE.REMOTE_PLAY);
                    m_myApp.setPlayNewSong(false);*/
                   // startActivity(intent);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (covertart!=null&&!covertart.equals("null")) {
                                new DownloadImageTask(albumart_image)
                                        .execute("http://" + host_ip + ":8080/" + covertart);
                            }
                            else {
                                albumart_image.setImageResource(R.drawable.defaultcover);
                            }
                            switch (playstatus){
                                case PLAYING:

                                    playing_status.setText("Playing");
                                    play_pause.setImageResource(R.drawable.ic_pause_circle_fill_black_36dp);
                                    isPlaying=true;
                                    break;
                                case STOPPED:
                                    playing_status.setText("Stopped");
                                    play_pause.setImageResource(R.drawable.ic_pause_circle_fill_black_36dp);
                                    isPlaying=false;
                                    break;
                                case PAUSED:
                                    playing_status.setText("Paused");
                                    play_pause.setImageResource(R.drawable.ic_play_orange_36dp);
                                    isPlaying=false;
                                    break;
                            }
                            switch (repeat_state){
                                case REPEAT_OFF:
                                   repeat.setImageResource(R.drawable.ic_repeat_black_24dp);
                                    break;
                                case REPEAT_ONE:
                                    repeat.setImageResource(R.drawable.repeatone);
                                    break;
                                case REPEAT_ALL:
                                    repeat.setImageResource(R.drawable.repeatall);
                                    break;
                            }
                            if (shuffle_state==0) {
                                isShuffle = false;
                                shuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
                            }
                            else if (shuffle_state==1) {
                                isShuffle = true;
                                shuffle.setImageResource(R.drawable.shuffle);
                            }
                           listview.setVisibility(View.GONE);
                            playview.setVisibility(View.VISIBLE);
                            albumDetails.setText( Html.fromHtml("<b>"+ "<big>"+trackName  +"</big>"+ "</b>" + "<br />" +
                                     album   + "-" +
                                      artist ));

                            //albumDetails.setText( "TrackName:" + trackName + "Album:" + album + "\n" + "Artist:" + artist + "\n");

                            if (totalTime!=0)
                                totaltime_text.setText( ModelUtil.toTimeString(totalTime));

                        }


                    });


                }
            } catch (JSONException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iswrongJson=true;
                        if (m_progressDlg!=null)
                            m_progressDlg.dismiss();
                        Toast.makeText(getApplicationContext(), "Json exception", Toast.LENGTH_SHORT).show();
                        mAdapter.notifyDataSetChanged();
                    }
                });
                e.printStackTrace();
            }
        } else {
            iswrongJson=true;
            Log.v(TAG, "JSON PARSER NULL String ");
        }

    }

    public class ReceivePacket implements Runnable {
        //public class receivepacket{
        ReceivePacket() {
            mNetIf = Utils.getActiveNetworkInterface();


            try {
                Log.v(TAG, "mUnicastSocket socket start to receive packet from device at port  " + PORT);
                mUnicastSocket = new DatagramSocket(null);
                mUnicastSocket.setReuseAddress(true);
                mUnicastSocket.bind(new InetSocketAddress(Utils.getLocalV4Address(mNetIf), PORT));



            } catch (IOException e) {
                Log.e(TAG, "Setup UDP reveice  failed.", e);
            }

            //  }start();
        }

        public synchronized void shutdown() {
            Log.v(TAG, "Receive UDP packet shutdown ");
            mRunning = false;
        }

        DatagramPacket receive() throws IOException {

            byte[] buf = new byte[1024*12];
            dp = new DatagramPacket(buf, buf.length);
            mUnicastSocket.receive(dp);
            Log.e(TAG, "DP" + dp.getData().length);
            return dp;
        }

        @Override
        public void run() {

            intflag = 1;
            mRunning = true;

            while (mRunning) {

                try {
                    dp = receive();

                    InetAddress addr = dp.getAddress();


                    if (addr.getHostAddress().equals(host_ip)) {
                        byte[] buffer;
                        byte[] jsonBuf;
                        buffer = dp.getData();
                        int len = dp.getLength() - 10;
                        jsonBuf = new byte[len];
                        for (int i = 0; i < len; i++)
                            jsonBuf[i] = buffer[i + 10];
                        int msgBox = buffer[3] * 16 + buffer[4];
                        Log.i(getClass().getName(), "Messagebox " + msgBox);
                        if (msgBox == 42) {
                            String str = new String(jsonBuf);
                            Log.e(TAG, "Json" + str);
                            ParseJson(str);

                        }

                        else if (msgBox == 64) {

                            final String str = new String(jsonBuf);
                            if (!str.equals("")) {
                                final int vol = Integer.parseInt(str);
                                if (vol != 0)

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            volumebar.setProgress(vol);
                                            volumetetext.setText(str);
                                        }
                                    });


                            }
                        }  else if (msgBox == 49) {
                            String str = new String(jsonBuf);
                           // Log.e(TAG, "String messagebox 49 = " + str);
                            if (!str.equals("")) {
                                int duration = Integer.parseInt(str);
                              //  Log.e(TAG, "Duration- messagebox 49 " + duration + "Totaltime =" + totalTime);
                                positionbar.setMax(totalTime);
                                Log.d("SEEK", "SEEK FLAG = " + SEEK_FLAG);

                                if (SEEK_FLAG)
                                {
                                    SEEK_FLAG = false;
                                    continue;

                                }
                                Log.d("SEEK", "Duration = " + duration / 1000);
                                positionbar.setProgress(duration / 1000);

                            }
                        }
                        else if (msgBox==51) {
                            String str = new String(jsonBuf);
                            if (!str.equals("")) {
                                int playstatus = Integer.parseInt(str);


                            }
                        }

                    }
                } catch (IOException e) {
                    Log.e(TAG, "Remote UDP.", e);
                }
            }
        }


    }









    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.remote, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        LUCIControl luciControl = new LUCIControl();
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.remote_action_home:

                luciControl.SendLUCICommand(Constant.MID_REMOTE_UI, Constant.GET_HOME, host_ip);

                return true;

            case R.id.remote_action_play:

                luciControl.SendLUCICommand(Constant.MID_REMOTE_UI, Constant.GET_PLAY, host_ip);

                return true;

            case android.R.id.home:
            {
                if (iswrongJson || !Browser.equals("HOME"))

                {
                    m_progressDlg = ProgressDialog.show(LmpListViewActivity1.this, "Notice", "Going back....", true, true, null);
                    luciControl.SendLUCICommand(Constant.MID_REMOTE_UI,Constant.BACK,host_ip);


                } else {
                    Intent i = new Intent(LmpListViewActivity1.this, MainActivity.class);
                    i.putExtra("key",played);

                    startActivity(i);


                }
            }
            return  true;

            case R.id.now_playing:
                Intent i = new Intent(LmpListViewActivity1.this, MainActivity.class);

                startActivity(new Intent(this,MainActivity.class));

                finish();
                return  true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                if (mIcon11==null)
                    Log.e(TAG,"Bitmap is null");
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

            if (result!=null){
                bmImage.setVisibility(View.VISIBLE);
         /*   Palette p = Palette.generate(result);

            bmImage.setBackgroundColor(p.getMutedColor(Color.BLUE));*/


                bmImage.setImageBitmap(result);}



        }

    }
    void initializeView() {

        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.previous).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (iswrongJson || !Browser.equals("HOME"))

            {
                m_progressDlg = ProgressDialog.show(LmpListViewActivity1.this, "Notice", "Going back....", true, true, null);
                LUCIControl luciControl = new LUCIControl();
                luciControl.SendLUCICommand(Constant.MID_REMOTE_UI, Constant.BACK, host_ip);
                luciControl.close();

            } else {
                Intent i = new Intent(LmpListViewActivity1.this, MainActivity.class);
                startActivity(i);


            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        LUCIControl luciControl = new LUCIControl();

        switch (v.getId()) {


            case R.id.play:
                if (isPlaying) {
                    luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL, Constant.PAUSE, host_ip);

                    play_pause.setImageResource(R.drawable.ic_pause_circle_fill_black_36dp);
                }
                else {
                    luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL, Constant.RESUME, host_ip);
                    luciControl.close();
                    play_pause.setImageResource(R.drawable.ic_play_orange_36dp);
                }
                break;

            case R.id.next:
                luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL, Constant.PLAY_NEXT, host_ip);

                luciControl.close();
                break;

            case R.id.previous:
                luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL, Constant.PLAY_PREV, host_ip);
                luciControl.close();
                break;

            case R.id.stop:
                luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL, Constant.STOP, host_ip);
                luciControl.close();
                break;


            case R.id.shuffle:
                if (isShuffle) {
                    luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL,"SHUFFLE:OFF", host_ip);
                    luciControl.close();
                }
                else
                    luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL,"SHUFFLE:ON",host_ip);
                luciControl.close();
                break;


            case R.id.repeat:
                if (repeat_state==2){
                    luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL, "REPEAT:OFF", host_ip);
                    luciControl.close();

                    // repeat.setImageResource(R.drawable.ic_repeat_one_black_24dp);
                }
                else  if (repeat_state==0){
                    luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL, "REPEAT:ONE", host_ip);
                    luciControl.close();
                    // repeat.setImageResource(R.drawable.ic_repeat_black_24dp);
                }
                else if (repeat_state==1)
                    luciControl.SendLUCICommand(Constant.MID_PLAYCONTROL, "REPEAT:ALL", host_ip);
                luciControl.close();

                // repeat.setImageResource(R.drawable.ic_repeat_grey600_24dp);

                break;



        }


    }
}