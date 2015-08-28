package com.libre.client.activity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.fourthline.cling.model.meta.LocalDevice;

public class LodingActivity extends BaseActivity {
//	private static final String TAG = LodingActivity.class.getSimpleName();
	private ImageView img2, img3,img1,icon;
	private TextView m_version;
    AlertDialog.Builder alertDialog;
    private Button proceed,wifi;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        /*if (!isWifiConnected())
            CreateAlert();*/
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		if (m_upnpProcessor != null) {
//			m_upnpProcessor.unbindUpnpService();
//		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0x01) {
				img2.setImageResource(R.drawable.toloading);
			}
			if (msg.what == 0x02) {
				img3.setImageResource(R.drawable.toloading);
			}
			if (msg.what == 0x03) {
                if (isWifiConnected()) {
                    startActivity(new Intent(LodingActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    m_version.setText("Check your Wi-Fi Connection");
                    img2.setVisibility(View.INVISIBLE);
                    img3.setVisibility(View.INVISIBLE);
                    img1.setVisibility(View.INVISIBLE);
                    proceed.setVisibility(View.VISIBLE);
                    wifi.setVisibility(View.VISIBLE);
                    icon.setImageResource(R.drawable.wifi_launching);
                }

			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
//		if (m_upnpProcessor != null) {
//			m_upnpProcessor.searchDMR();
//		}
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				sf.closeAllSocket();
//				SSDPUtil.isTrue = true;
//				SSDPUtil ssdpUtil = new SSDPUtil();
//				long startTime = System.currentTimeMillis();
//				Log.d(TAG, "SSDP seraching...");
//				ssdpUtil.getDevices();
//				ssdpUtil.close();
//				long endTime = System.currentTimeMillis();
//				Log.d(TAG, "SSDP search time:" + (endTime - startTime) + " count:" + DevUtil.devs.size());
//				devUtil.getDevWithName();
//			}
//		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					Thread.sleep(1000);
					handler.sendEmptyMessage(0x01);
					Thread.sleep(700);
					handler.sendEmptyMessage(0x02);
					Thread.sleep(400);
					handler.sendEmptyMessage(0x03);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		if (m_upnpProcessor != null) {
//			m_upnpProcessor.removeListener(LodingActivity.this);
//		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocalDeviceAdded(LocalDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocalDeviceRemoved(LocalDevice device) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	protected void loadViewLayout() {
		// TODO Auto-generated method stub
		 //Remove title bar
        //Remove title bar
        getSupportActionBar().hide();


        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	   //set content view AFTER ABOVE sequence (to avoid crash)
	    
		setContentView(R.layout.loding);
	}

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
		img2 = (ImageView) findViewById(R.id.img2);
        img1 = (ImageView) findViewById(R.id.img1);
		img3 = (ImageView) findViewById(R.id.img3);
		m_version = (TextView) findViewById(R.id.textView1);
		proceed=(Button)findViewById(R.id.proceedwith);
        wifi=(Button)findViewById(R.id.connect_to_wifi);
        icon = (ImageView)findViewById(R.id.icon);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LodingActivity.this, MainActivity.class));
                finish();
            }
        });

        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                finish();
            }
        });
		
	}
	public int getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

	@Override
	protected void setListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processLogic() {
		// TODO Auto-generated method stub
//		m_upnpProcessor = new UpnpProcessorImpl(LodingActivity.this);
//		m_upnpProcessor.bindUpnpService();
		//String Version =new String ("Version "+getVersion(this));
		String Version = "Libre" ;
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(pInfo!=null)
		 Version = pInfo.versionName;
		
		m_version.setText(Version);
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// do nothing
	}

    public  boolean isWifiConnected(){
        ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }




   /* public void CreateAlert(){
         alertDialog= new AlertDialog.Builder(LodingActivity.this)

       .setTitle("Alert")
       .setMessage("No Wifi Connection")

      .setPositiveButton("Take me to wifi settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                       dialog.dismiss();
                        startActivity(new Intent(
                                Settings.ACTION_WIFI_SETTINGS));
                        finish();

                    }
                })
        .setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();

                        finish();

                    }
                });
        alertDialog.create();

        alertDialog.show();
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {


                finish();

        }
      return  true;
    }
}
