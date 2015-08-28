package com.libre.client.activity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libre on 16-12-2014.
 */
public class Version1 extends ActionBarActivity {
    private static final String TAG = "Version";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version);
        List<String> listItems= new ArrayList<String>();
        listItems.add("v21.1-Adding play and home button In DMP");
        listItems.add("V21.2- Adding bluetooth on and off-SAC Changes");
        listItems.add("v21.3-Support concurrent mode and dmr");
        listItems.add("V22.0-alpha-Lollipop support,Material icons,improved discovery,mi3 crash fixes");
        listItems.add("V22.0.0-bug fixes,settings changes");
        listItems.add("V22.0.1-Added tcp support for discovery");
        listItems.add("V22.0.2-Bug Fixes and added remove dmr from playlist in settings");
        listItems.add("V22.0.3-timing issue fixes  while adding slave in zone and lucimessanger added");
        listItems.add("V22.0.4-Sac changes ,shuffle and repeat added,playview changes for Remote play,bugs fixes-enabling aux and bluetooth for slaves");
        listItems.add("V22.0.5-Sac  and DMR fix and some bug fixes");
        listItems.add("V22.1.0-"+"Updated to cling 2.0 and discovery changes");
        listItems.add("V22.1.1-"+"Improving discovery performance");
        listItems.add("V22.1.2-"+"Bug fixes ");
        listItems.add("V22.1.5-"+"App name modified to SA200");

        //listItems.add("V22.0-efbnbeta-improved discovery-changed udp to tcp");
        ListView listView= (ListView) findViewById(android.R.id.list);
        listView.setAdapter(new ArrayAdapter(this,  android.R.layout.simple_list_item_1, listItems));

        TextView textView = (TextView) findViewById(R.id.version);
        TextView textView2 = (TextView) findViewById(R.id.releasedate);
        textView.setText(  "Version Name"+getVersionName(this) + System.getProperty("line.separator")+" Reference Code =  " +getVersion(this) );
        textView2.setText("Release date -18/08/15");

    }






    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"Önstart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"ÖnResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"ÖnPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"ÖnStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"ÖnDestroy");
    }

    public int getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
     }

    public String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }





}
